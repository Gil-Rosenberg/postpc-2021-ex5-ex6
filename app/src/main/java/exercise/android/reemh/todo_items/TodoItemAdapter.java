package exercise.android.reemh.todo_items;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.LinkedList;
import java.util.List;

public class TodoItemAdapter extends RecyclerView.Adapter<TodoItemViewHolder> {

    List<TodoItem> todoItemList = new LinkedList<>();
    TodoItemsHolderImpl todoItemsHolder;
    private boolean onBind;

    TodoItemAdapter(TodoItemsHolderImpl holder){
        todoItemsHolder = holder;
        todoItemList = holder.getCurrentItems();
        onBind = false;
    }

    @NonNull
    @Override
    // here we create view holder
    public TodoItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();  // Access to Android resources
        View view = LayoutInflater.from(context).inflate(R.layout.row_todo_item, parent, false);

        return new TodoItemViewHolder(view);
    }

    @Override
    // here we need to configure the view holder
    public void onBindViewHolder(@NonNull TodoItemViewHolder holder, int position) {
        onBind = true;
        TodoItem todoItem = todoItemList.get(position);

        if (todoItem.getCompleted()){
            holder.getText().setPaintFlags(holder.getText().getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        else{
            holder.getText().setPaintFlags(holder.getText().getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // prepare description text:
        holder.getText().setText(todoItem.getTodoText());

        // prepare date text:
        holder.getDateTimeDisplay().setText(todoItem.getCreationTime());

        // init checkBox state:
        holder.getCheckBox().setChecked(todoItem.getCompleted());

        // listener for checkBox:
        holder.getCheckBox().setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!onBind){
                todoItemsHolder.setItemProgress(position, isChecked);
                notifyDataSetChanged();
            }
        });

        // TODO listener for deleting
//        if (!onBind){
//        }

        onBind = false;
    }

    @Override
    // recyclerView asks the adapter how much items it needs to render in total (including titles)
    public int getItemCount() {
        return todoItemList.size();
    }
}
