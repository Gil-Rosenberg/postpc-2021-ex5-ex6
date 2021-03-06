package exercise.android.reemh.todo_items;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TodoItemsHolderImpl implements TodoItemsHolder {

  private final ArrayList<TodoItem> currentItems = new ArrayList<>();
  private final LinkedList<TodoItem> itemsInProgress = new LinkedList<>();
  private final ArrayList<TodoItem> doneItems = new ArrayList<>();
  private final SharedPreferences sp;
  private final MutableLiveData<List<TodoItem>> itemsLiveDataMutable = new MutableLiveData<>();
  public final LiveData<List<TodoItem>> itemsLiveDataPublic = itemsLiveDataMutable;

  @RequiresApi(api = Build.VERSION_CODES.O)
  public TodoItemsHolderImpl(Context context) {
    this.sp = context.getSharedPreferences("local_db_items", Context.MODE_PRIVATE);
    initializeFromSp();
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void initializeFromSp() {
    itemsInProgress.clear();
    doneItems.clear();

    Set<String> keys = sp.getAll().keySet();
    for (String key : keys) {
      String itemSavedAsString = sp.getString(key, null);
      TodoItem item = stringToItem(itemSavedAsString);
      if (!item.isDone()) {
        itemsInProgress.addFirst(item);
      } else {
        doneItems.add(item);
      }
    }
    itemsLiveDataMutable.setValue(new LinkedList<>(this.getCurrentItems()));
  }

  @Override
  public List<TodoItem> getCurrentItems() {
    currentItems.clear();
    currentItems.addAll(itemsInProgress);
    currentItems.addAll(doneItems);
    return currentItems;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void addNewInProgressItem(String description) {
    String date = java.text.DateFormat.getDateTimeInstance().format(new Date());
    String id = UUID.randomUUID().toString();
    TodoItem todoItem = new TodoItem(id, description, date, false);
    itemsInProgress.addFirst(todoItem);

    SharedPreferences.Editor editor = sp.edit();
    editor.putString(todoItem.getId(), todoItem.ItemToString());
    editor.apply();

    itemsLiveDataMutable.setValue(new ArrayList<>(this.getCurrentItems()));
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void markItemDone(TodoItem item) {
    if (item == null) return;

    TodoItem newItem = new TodoItem(item.getId(), item.getDescription(), item.getCreationTime(), true);
    itemsInProgress.remove(item);
    doneItems.add(newItem);

    SharedPreferences.Editor editor = sp.edit();
    editor.putString(newItem.getId(), newItem.ItemToString());
    editor.apply();

    itemsLiveDataMutable.setValue(new LinkedList<>(this.getCurrentItems()));
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void markItemInProgress(TodoItem item) {
    if (item == null) return;

    TodoItem newItem = new TodoItem(item.getId(), item.getDescription(), item.getCreationTime(), false);
    doneItems.remove(item);
    itemsInProgress.addFirst(newItem);

    SharedPreferences.Editor editor = sp.edit();
    editor.putString(newItem.getId(), newItem.ItemToString());
    editor.apply();

    itemsLiveDataMutable.setValue(new LinkedList<>(this.getCurrentItems()));
  }

  @Override
  public void deleteItem(TodoItem item) {
    if (item == null) return;

    if (itemsInProgress.contains(item)) {
      itemsInProgress.remove(item);
    } else doneItems.remove(item);

    SharedPreferences.Editor editor = sp.edit();
    editor.remove(item.getId());
    editor.apply();

    itemsLiveDataMutable.setValue(new LinkedList<>(this.getCurrentItems()));
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void setItemProgressByPosition(int position, boolean isChecked) {
    TodoItem itemToEdit = this.getCurrentItems().get(position);

    if (isChecked) {
      markItemDone(itemToEdit);
    } else {
      markItemInProgress(itemToEdit);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  public void setItemProgress(TodoItem item, boolean isChecked) {
    if (isChecked) {
      markItemDone(item);
    } else {
      markItemInProgress(item);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void editDescription(String id, String newDescription) {
    if (id.equals("") || newDescription.equals("")) return;

    TodoItem oldItem = getItemById(id);
    if (oldItem == null) return;

    String date = java.text.DateFormat.getDateTimeInstance().format(new Date());
    TodoItem newItem = new TodoItem(id, newDescription, date, oldItem.isDone());

    if (itemsInProgress.contains(oldItem)) {
      itemsInProgress.remove(oldItem);
      itemsInProgress.addFirst(newItem);
    } else {
      doneItems.remove(oldItem);
      doneItems.add(newItem);
    }

    SharedPreferences.Editor editor = sp.edit();
    editor.putString(newItem.getId(), newItem.ItemToString());
    editor.apply();

    itemsLiveDataMutable.setValue(new LinkedList<>(this.getCurrentItems()));
  }

  private @Nullable
  TodoItem getItemById(String id) {
    TodoItem todoItem = null;

    for (TodoItem item : currentItems) {
      if (item.getId().equals(id)) {
        todoItem = item;
      }
    }
    return todoItem;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public TodoItem stringToItem(String string) {
    if (string == null) return null;
    try {
      List<String> split = Arrays.asList(string.split("%#"));
      String description = split.get(0);
      String time = split.get(1);
      boolean isCompleted = Boolean.getBoolean(split.get(2));
      String id = split.get(3);
      return new TodoItem(id, description, time, isCompleted);
    } catch (Exception e) {
      System.err.println("exception. input: " + string + ".\n" + "exception: " + e);
      return null;
    }
  }

  public LinkedList<TodoItem> getCopies() {
    return new LinkedList<>(currentItems);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  public void editDescription(TodoItem itemToEdit, String description) {
    itemToEdit.setDescription(description);
    itemToEdit.setLastModification(LocalDateTime.now().toString());

    itemsLiveDataMutable.setValue(getCurrentItems());

    SharedPreferences.Editor editor = sp.edit();
    editor.putString(itemToEdit.getId(), itemToEdit.ItemToString());
    editor.apply();
  }

  public TodoItem getById(String id) {
    for (TodoItem item : itemsInProgress) {
      if (item.getId().equals(id)) {
        return item;
      }
    }

    for (TodoItem item : doneItems) {
      if (item.getId().equals(id)) {
        return item;
      }
    }
    return null;
  }
}

