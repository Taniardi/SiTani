//package edu.bluejack24_2.myapplication.activities;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import androidx.recyclerview.widget.ItemTouchHelper;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.QuerySnapshot;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//
//import edu.bluejack24_2.myapplication.R;
//import edu.bluejack24_2.myapplication.adapters.TodoAdapter;
//import edu.bluejack24_2.myapplication.models.TodoItem;
//import edu.bluejack24_2.myapplication.utils.FirebaseHelper;
//
//public class ToDoActivity extends AppCompatActivity implements TodoAdapter.OnTodoClickListener {
//
//    private SwipeRefreshLayout swipeRefreshLayout;
//    private RecyclerView todoRecyclerView;
//    private FloatingActionButton addTaskFab;
//    private BottomNavigationView bottomNavigationView;
//
//    private TodoAdapter todoAdapter;
//    private List<TodoItem> todoList;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_todo);
//
//        // Mengatur Padding untuk System Bars (Status bar & Nav bar)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            // Kita atur padding bottom 0 saja biar nav bar tidak bolong,
//            // karena kita pakai CoordinatorLayout
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
//            return insets;
//        });
//
//        initViews();
//        setupRecyclerView();
//        setupListeners();
//
//        // Load data awal
//        loadTodos();
//    }
//
//    private void initViews() {
//        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
//        todoRecyclerView = findViewById(R.id.todoRecyclerView);
//        addTaskFab = findViewById(R.id.addTaskFab);
//        bottomNavigationView = findViewById(R.id.bottomNavigationView);
//    }
//
//    private void setupRecyclerView() {
//        todoList = new ArrayList<>();
//        // Pastikan Adapter kamu support constructor (Context, Listener)
//        todoAdapter = new TodoAdapter(this, this);
//        todoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        todoRecyclerView.setAdapter(todoAdapter);
//
//        // --- FITUR SWIPE TO DELETE ---
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                int position = viewHolder.getAdapterPosition();
//                TodoItem item = todoList.get(position);
//
//                // Konfirmasi Hapus
//                new AlertDialog.Builder(ToDoActivity.this)
//                        .setTitle("Delete Task")
//                        .setMessage("Are you sure want to delete this task?")
//                        .setPositiveButton("Delete", (dialog, which) -> {
//                            deleteTaskFromFirebase(item);
//                        })
//                        .setNegativeButton("Cancel", (dialog, which) -> {
//                            todoAdapter.notifyItemChanged(position); // Batal hapus, kembalikan item
//                        })
//                        .show();
//            }
//        });
//        itemTouchHelper.attachToRecyclerView(todoRecyclerView);
//    }
//
//    private void setupListeners() {
//        addTaskFab.setOnClickListener(v -> showAddTaskDialog());
//
//        swipeRefreshLayout.setOnRefreshListener(() -> {
//            loadTodos();
//        });
//
//        // Setup Bottom Nav (Placeholder)
//        bottomNavigationView.setOnItemSelectedListener(item -> {
//            // Handle navigasi di sini jika sudah ada menu
//            return true;
//        });
//    }
//
//    // --- LOGIC FIREBASE ---
//
//    private void loadTodos() {
//        FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
//        if (currentUser == null) return;
//
//        swipeRefreshLayout.setRefreshing(true);
//
//        FirebaseHelper.getUserTodos(currentUser.getUid(), task -> {
//            swipeRefreshLayout.setRefreshing(false);
//            if (task.isSuccessful()) {
//                QuerySnapshot snapshot = task.getResult();
//                if (snapshot != null) {
//                    todoList.clear();
//                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
//                        TodoItem item = doc.toObject(TodoItem.class);
//                        if (item != null) todoList.add(item);
//                    }
//
//                    // --- PENTING: SORTING BERDASARKAN TANGGAL ---
//                    // Agar Header "Today/Tomorrow" di Adapter bekerja, data WAJIB urut.
//                    sortTodoListByDate();
//
//                    todoAdapter.setTodoItems(todoList);
//                }
//            } else {
//                Toast.makeText(this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void sortTodoListByDate() {
//        Collections.sort(todoList, new Comparator<TodoItem>() {
//            @Override
//            public int compare(TodoItem o1, TodoItem o2) {
//                // TODO: Sesuaikan 'getDate()' dengan nama field tanggal di Model kamu
//                // Jika fieldnya long/timestamp:
//                // return Long.compare(o2.getDate(), o1.getDate()); // Descending (Terbaru diatas)
//                return 0; // Hapus baris ini jika sudah ada logic di atas
//            }
//        });
//    }
//
//    private void addTaskToFirebase(String title, String desc) {
//        FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
//        if (currentUser == null) return;
//
//        String id = java.util.UUID.randomUUID().toString();
//        // TODO: Tambahkan parameter tanggal saat ini jika constructor model kamu butuh tanggal
//        TodoItem newItem = new TodoItem(id, title, desc, currentUser.getUid());
//
//        FirebaseHelper.saveTodoItem(newItem, task -> {
//            if (task.isSuccessful()) {
//                loadTodos(); // Reload
//                Toast.makeText(this, "Task Added!", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void updateTaskInFirebase(TodoItem item) {
//        FirebaseHelper.updateTodoItem(item, task -> {
//            if (task.isSuccessful()) {
//                loadTodos();
//                Toast.makeText(this, "Task Updated!", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void deleteTaskFromFirebase(TodoItem item) {
//        FirebaseHelper.deleteTodoItem(item.getId(), task -> {
//            if (task.isSuccessful()) {
//                loadTodos();
//                Toast.makeText(this, "Task Deleted!", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    // --- DIALOGS ---
//
//    private void showAddTaskDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
//
//        TextInputEditText etTitle = view.findViewById(R.id.titleEditText);
//        TextInputEditText etDesc = view.findViewById(R.id.descriptionEditText);
//
//        builder.setView(view)
//                .setTitle("Add New Task")
//                .setPositiveButton("Add", (dialog, which) -> {
//                    String title = etTitle.getText().toString();
//                    String desc = etDesc.getText().toString();
//                    if (!title.isEmpty()) {
//                        addTaskToFirebase(title, desc);
//                    }
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    private void showEditTaskDialog(TodoItem item) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
//
//        TextInputEditText etTitle = view.findViewById(R.id.titleEditText);
//        TextInputEditText etDesc = view.findViewById(R.id.descriptionEditText);
//
//        etTitle.setText(item.getTitle());
//        etDesc.setText(item.getDescription());
//
//        builder.setView(view)
//                .setTitle("Edit Task")
//                .setPositiveButton("Update", (dialog, which) -> {
//                    item.setTitle(etTitle.getText().toString());
//                    item.setDescription(etDesc.getText().toString());
//                    updateTaskInFirebase(item);
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    // --- ADAPTER CALLBACKS ---
//
//    @Override
//    public void onTodoClick(TodoItem todoItem) {
//        // Klik item untuk Edit
//        showEditTaskDialog(todoItem);
//    }
//
//    // Callback lain jika ada di interface adapter (hapus jika tidak ada di interface kamu)
//    @Override
//    public void onTodoEdit(TodoItem todoItem) {
//        showEditTaskDialog(todoItem);
//    }
//
//    @Override
//    public void onTodoDelete(TodoItem todoItem) {
//        // Tidak dipakai karena pakai Swipe, tapi biarkan kosong agar tidak error interface
//    }
//
//    @Override
//    public void onTodoStatusChanged(TodoItem todoItem, boolean isCompleted) {
//        todoItem.setCompleted(isCompleted);
//        updateTaskInFirebase(todoItem);
//    }
//}