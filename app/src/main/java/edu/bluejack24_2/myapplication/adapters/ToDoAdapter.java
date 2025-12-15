//package edu.bluejack24_2.myapplication.adapters; // Sesuaikan jika ada folder adapters
//
//import android.content.Context;
//import android.text.format.DateUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//import edu.bluejack24_2.myapplication.R;
//
//public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
//
//    private Context context;
//    private List<TodoItem> todoList;
//    private OnTodoClickListener listener;
//
//    // Interface untuk interaksi (Klik item, dll)
//    public interface OnTodoClickListener {
//        void onTodoClick(TodoItem todoItem);
//        void onTodoEdit(TodoItem todoItem);
//        void onTodoDelete(TodoItem todoItem);
//        void onTodoStatusChanged(TodoItem todoItem, boolean isCompleted);
//    }
//
//    public TodoAdapter(Context context, OnTodoClickListener listener) {
//        this.context = context;
//        this.listener = listener;
//        this.todoList = new ArrayList<>();
//    }
//
//    public void setTodoItems(List<TodoItem> todoItems) {
//        this.todoList = todoItems;
//        notifyDataSetChanged();
//    }
//
//    @NonNull
//    @Override
//    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        // Menggunakan item_todo.xml yang baru kita revisi
//        View view = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false);
//        return new TodoViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
//        TodoItem currentItem = todoList.get(position);
//
//        // 1. Set Data Utama
//        holder.tvTitle.setText(currentItem.getTitle());
//        holder.tvDescription.setText(currentItem.getDescription());
//
//        // 2. Format Tanggal untuk Chip (Pojok Kanan Bawah)
//        // Contoh: Feb 21, 2024
//        SimpleDateFormat chipFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
//        String chipDate = chipFormat.format(new Date(currentItem.getCreatedAt()));
//        holder.tvDateChip.setText(chipDate);
//
//        // 3. LOGIKA HEADER (Today, Tomorrow, Specific Date)
//        long currentDate = currentItem.getCreatedAt();
//
//        // Cek item sebelumnya untuk menentukan apakah Header perlu muncul
//        boolean showHeader = false;
//        if (position == 0) {
//            showHeader = true; // Item pertama selalu punya header
//        } else {
//            long prevDate = todoList.get(position - 1).getCreatedAt();
//            // Jika hari berbeda dengan item sebelumnya, tampilkan header
//            if (!isSameDay(currentDate, prevDate)) {
//                showHeader = true;
//            }
//        }
//
//        if (showHeader) {
//            holder.tvSectionHeader.setVisibility(View.VISIBLE);
//            holder.tvSectionHeader.setText(getFriendlyDateString(currentDate));
//        } else {
//            holder.tvSectionHeader.setVisibility(View.GONE);
//        }
//
//        // 4. Handle Klik Item (Buka detail/edit)
//        holder.itemView.setOnClickListener(v -> {
//            if (listener != null) {
//                listener.onTodoClick(currentItem);
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return todoList.size();
//    }
//
//    // --- HELPER METHODS ---
//
//    // Cek apakah dua timestamp berada di hari yang sama
//    private boolean isSameDay(long time1, long time2) {
//        Calendar cal1 = Calendar.getInstance();
//        Calendar cal2 = Calendar.getInstance();
//        cal1.setTimeInMillis(time1);
//        cal2.setTimeInMillis(time2);
//        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
//                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
//    }
//
//    // Ubah timestamp jadi teks "Today", "Yesterday", atau Tanggal
//    private String getFriendlyDateString(long timeInMillis) {
//        if (DateUtils.isToday(timeInMillis)) {
//            return "Today";
//        } else if (DateUtils.isToday(timeInMillis + DateUtils.DAY_IN_MILLIS)) {
//            return "Yesterday"; // Karena createdAt biasanya masa lalu
//        } else {
//            // Format jika bukan hari ini: "16 December 2025"
//            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
//            return sdf.format(new Date(timeInMillis));
//        }
//    }
//
//    // --- VIEWHOLDER ---
//    public static class TodoViewHolder extends RecyclerView.ViewHolder {
//        TextView tvSectionHeader;
//        TextView tvTitle;
//        TextView tvDescription;
//        TextView tvDateChip;
//
//        public TodoViewHolder(@NonNull View itemView) {
//            super(itemView);
//            // Sesuai ID di item_todo.xml yang baru
//            tvSectionHeader = itemView.findViewById(R.id.tvSectionHeader);
//            tvTitle = itemView.findViewById(R.id.titleTextView);
//            tvDescription = itemView.findViewById(R.id.descriptionTextView);
//            tvDateChip = itemView.findViewById(R.id.tvDateChip);
//        }
//    }
//}