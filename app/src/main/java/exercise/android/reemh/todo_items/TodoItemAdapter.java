package exercise.android.reemh.todo_items;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TodoItemAdapter extends RecyclerView.Adapter<TodoItemViewHolder> {

    List<TodoItem> todoItemList;
    TodoItemsHolderImpl todoItemsHolder;
    private boolean onBind;
    private Context context;

    TodoItemAdapter(TodoItemsHolderImpl holder){
        todoItemsHolder = holder;
        todoItemList = holder.getCurrentItems();
        onBind = false;
    }

    @NonNull
    @Override
    public TodoItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();  // Access to Android resources
        View view = LayoutInflater.from(context).inflate(R.layout.row_todo_item, parent, false);

        return new TodoItemViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull TodoItemViewHolder holder, int position) {
        onBind = true;
        TodoItem todoItem = todoItemList.get(position);

        if (todoItem.isDone()){
            holder.getDescription().setPaintFlags(holder.getDescription().getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        else{
            holder.getDescription().setPaintFlags(holder.getDescription().getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // prepare description text:
        holder.getDescription().setText(todoItem.getDescription());

        // prepare date text:
        holder.getDateTimeDisplay().setText(todoItem.getCreationTime());

        // init checkBox state:
        holder.getCheckBox().setChecked(todoItem.isDone());

        // listener for checkBox:
        holder.getCheckBox().setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!onBind){
                todoItemsHolder.setItemProgressByPosition(position, isChecked);
                notifyDataSetChanged();
            }
        });

        holder.getDescription().setOnClickListener(v -> {
            if (!onBind) {
                Intent editItemIntent = new Intent(context, EditItemActivity.class);
                editItemIntent.putExtra("itemToEdit", todoItem.getId());
                context.startActivity(editItemIntent);
            }
        });
        onBind = false;
    }

    @Override
    public int getItemCount() {
        return todoItemList.size();
    }

    public void setAdapterFields(List<TodoItem> itemList){
        todoItemList.clear();
        todoItemList.addAll(itemList);
        notifyDataSetChanged();
    }
}
