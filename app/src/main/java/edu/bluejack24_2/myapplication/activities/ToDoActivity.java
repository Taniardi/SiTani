package edu.bluejack24_2.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.bluejack24_2.myapplication.R;
import edu.bluejack24_2.myapplication.adapters.TodoAdapter;
import edu.bluejack24_2.myapplication.models.TodoItem;
import edu.bluejack24_2.myapplication.utils.FirebaseHelper;

public class ToDoActivity extends AppCompatActivity implements TodoAdapter.OnTodoClickListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView todoRecyclerView;
    private FloatingActionButton addTaskFab;
    private BottomNavigationView bottomNavigationView;
    private ProgressBar progressBar;

    private TodoAdapter todoAdapter;
    private List<TodoItem> todoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_todo);

        // Padding untuk System Bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        initViews();
        setupRecyclerView();
        setupListeners();

        // Load data awal
        loadTodos();
    }

    private void initViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        todoRecyclerView = findViewById(R.id.todoRecyclerView);
        addTaskFab = findViewById(R.id.addTaskFab);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        todoList = new ArrayList<>();
        todoAdapter = new TodoAdapter(this, this);
        todoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        todoRecyclerView.setAdapter(todoAdapter);

        // --- FITUR SWIPE TO DELETE ---
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                TodoItem item = todoList.get(position);

                new AlertDialog.Builder(ToDoActivity.this)
                        .setTitle("Delete Task")
                        .setMessage("Are you sure want to delete this task?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteTaskFromFirebase(item);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            todoAdapter.notifyItemChanged(position);
                        })
                        .show();
            }
        });
        itemTouchHelper.attachToRecyclerView(todoRecyclerView);
    }
    private void setupListeners() {
        addTaskFab.setOnClickListener(v -> showAddTaskDialog());

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadTodos();
        });

        // Setup Bottom Nav
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                // Pindah ke Home (MainActivity)
                startActivity(new Intent(ToDoActivity.this, MainActivity.class));
                finish(); // Tutup activity ini agar tidak menumpuk
                return true;
            } else if (itemId == R.id.navigation_dashboard) {
                // Sudah di sini (Dashboard/ToDo)
                return true;
            } else if (itemId == R.id.navigation_notifications) {
                Toast.makeText(this, "Notification Feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                Toast.makeText(this, "Profile Feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Pastikan icon Dashboard terpilih saat halaman ini aktif
        bottomNavigationView.setSelectedItemId(R.id.navigation_dashboard);
    }

    // --- LOGIC FIREBASE ---

    private void loadTodos() {
        FirebaseUser currentUser = FirebaseHelper.getCurrentUser();

        // Handling jika user belum login/null
         if (currentUser == null) {
             Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
             return;
         }

        // Tampilkan loading (bisa pakai swipe refresh atau progress bar)
        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        FirebaseHelper.getUserTodos(currentUser.getUid(), task -> {

            swipeRefreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot != null) {
                    todoList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        TodoItem item = doc.toObject(TodoItem.class);
                        if (item != null) todoList.add(item);
                    }

                    sortTodoListByDate();
                    todoAdapter.setTodoItems(todoList);
                }
            } else {
                Toast.makeText(this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortTodoListByDate() {
        Collections.sort(todoList, new Comparator<TodoItem>() {
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return Long.compare(o2.getCreatedAt(), o1.getCreatedAt());
            }
        });
    }

    private void addTaskToFirebase(String title, String desc) {
        FirebaseUser currentUser = FirebaseHelper.getCurrentUser();

        if (currentUser == null) return;

        String id = java.util.UUID.randomUUID().toString();
        TodoItem newItem = new TodoItem(id, title, desc, currentUser.getUid());

        progressBar.setVisibility(View.VISIBLE); // Show loading
        FirebaseHelper.saveTodoItem(newItem, task -> {
            progressBar.setVisibility(View.GONE); // Hide loading
            if (task.isSuccessful()) {
                loadTodos();
                Toast.makeText(this, "Task Added!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTaskInFirebase(TodoItem item) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.updateTodoItem(item, task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                loadTodos();
                Toast.makeText(this, "Task Updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteTaskFromFirebase(TodoItem item) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.deleteTodoItem(item.getId(), task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                loadTodos();
                Toast.makeText(this, "Task Deleted!", Toast.LENGTH_SHORT).show();
            }
        });
    }

// --- DIALOGS ---
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);

        TextInputEditText titleEditText = view.findViewById(R.id.titleEditText);
        TextInputEditText descriptionEditText = view.findViewById(R.id.descriptionEditText);

        builder.setView(view)
                .setTitle("Add New Task")
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = titleEditText.getText().toString().trim();
                    String desc = descriptionEditText.getText().toString().trim();

                    if (!title.isEmpty()) {
                        addTaskToFirebase(title, desc);
                    } else {
                        // Opsional: Beri peringatan jika judul kosong
                        Toast.makeText(ToDoActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditTaskDialog(TodoItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);

        // Inisialisasi View sesuai ID di XML
        TextInputEditText titleEditText = view.findViewById(R.id.titleEditText);
        TextInputEditText descriptionEditText = view.findViewById(R.id.descriptionEditText);

        // Isi data lama ke dalam inputan (Pre-fill)
        titleEditText.setText(item.getTitle());
        descriptionEditText.setText(item.getDescription());

        builder.setView(view)
                .setTitle("Edit Task")
                .setPositiveButton("Update", (dialog, which) -> {
                    String newTitle = titleEditText.getText().toString().trim();
                    String newDesc = descriptionEditText.getText().toString().trim();

                    if (!newTitle.isEmpty()) {
                        // Update object item dengan data baru
                        item.setTitle(newTitle);
                        item.setDescription(newDesc);

                        // Kirim ke Firebase
                        updateTaskInFirebase(item);
                    } else {
                        Toast.makeText(ToDoActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    // --- ADAPTER CALLBACKS ---

    @Override
    public void onTodoClick(TodoItem todoItem) {
        showEditTaskDialog(todoItem);
    }

    @Override
    public void onTodoEdit(TodoItem todoItem) {
        showEditTaskDialog(todoItem);
    }

    @Override
    public void onTodoDelete(TodoItem todoItem) {
        // Handled by Swipe
    }

    @Override
    public void onTodoStatusChanged(TodoItem todoItem, boolean isCompleted) {
        todoItem.setCompleted(isCompleted);
        updateTaskInFirebase(todoItem);
    }
}