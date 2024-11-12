package com.example.todotask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todotask.fragments.AllFragment;
import com.example.todotask.utils.FileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import interfaces.TaskDeletionCallback;

public class BaseFragment extends Fragment implements TaskDeletionCallback {

    boolean isTaskSelected = false;
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<JSONObject> taskList;
    private ArrayList<JSONObject> deletionTasks = new ArrayList<>();
    private MainActivity mainActivity;
    private Boolean isPriorityChanged = false;
    File tasksFile;

    private TaskDeletionCallback taskDeletionCallback;

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TaskDeletionCallback) {
            ((MainActivity) context).setTaskDeletionCallback(this);
            Log.d("callbacktodelete","call attached initiated");
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tasksFile = new File(getContext().getExternalFilesDir(null), "tasks.json");

        taskList = loadTasksFromJson(getContext());
        mainActivity = new MainActivity();

        // Check if taskList is not null before setting the adapter
        if (taskList != null) {
            taskAdapter = new TaskAdapter(getContext(), taskList);
            recyclerView.setAdapter(taskAdapter);
        }

        // Set up ItemTouchHelper for drag and drop
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                0) {
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                // Update the list and notify the adapter
                JSONObject movedTask = taskList.remove(fromPosition);
                taskList.add(toPosition, movedTask);
                taskAdapter.notifyItemMoved(fromPosition, toPosition);

                // Save the updated task list to internal storage
                swapPriority(fromPosition,toPosition);

                isPriorityChanged = true;
                return true;
            }

            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.LEFT) {
                    taskList.remove(position);
                    taskAdapter.notifyItemRemoved(position);
                }
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    if (viewHolder instanceof TaskAdapter.TaskViewHolder) {
                        TaskAdapter.TaskViewHolder taskViewHolder = (TaskAdapter.TaskViewHolder) viewHolder;

                        if(taskViewHolder.itemView.getAlpha() == 1.0){
                            taskViewHolder.itemView.setAlpha(0.5f);
                        }else{
                        }
                    }
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                TaskAdapter.TaskViewHolder taskViewHolder = (TaskAdapter.TaskViewHolder) viewHolder;
                int position = taskViewHolder.getAdapterPosition();

                if(!isPriorityChanged){
                    if(deletionTasks.contains(taskList.get(position))){
                        taskViewHolder.itemView.setAlpha(1.0f);
                        deletionTasks.remove(taskList.get(position));

                        taskAdapter.updateDeletionTasks(deletionTasks);
                        mainActivity.updateDeletionTasks(deletionTasks);
                    }else{
                        deletionTasks.add(taskList.get(position));

                        taskAdapter.updateDeletionTasks(deletionTasks);
                        mainActivity.updateDeletionTasks(deletionTasks);

                        // enable the delete button
                        showDeleteButton();
                        isTaskSelected = true;
                    }
                }else {
                    taskViewHolder.itemView.setAlpha(1.0f);
                }
                if(deletionTasks.isEmpty()){
                    hideDeleteButton();
                    isTaskSelected=false;
                }
                isPriorityChanged = false;
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
        return view;
    }

    private void swapPriority(int moveeTask, int movedTask) {
        try {
            // Update the priority of the moved task
            taskList.get(moveeTask).put("taskPriority", moveeTask+1);
            // Update the priority of the original task in the new position
            taskList.get(movedTask).put("taskPriority", movedTask+1);
            updateMainList();
        } catch (JSONException e) {
            Log.e("JSON Error", "Error updating task priority", e);
        }

        //accessing all frag
        AllFragment allFragment = new AllFragment();
    }


    protected List<JSONObject> loadTasksFromJson(Context context) {
        List<JSONObject> tasks = new ArrayList<>();

        // Check if the file exists
        if (!tasksFile.exists()) {
            try {
                // If the file doesn't exist, create it with initial data
                tasksFile.createNewFile();
                writeInitialTasksToJsonFile(tasksFile);
                Log.i("File Creation", "Tasks file created at: " + tasksFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e("File Creation Error", "Error creating tasks file", e);
                return tasks;
            }
        }

        try {
            // Read the tasks from the file
            String jsonStr = FileUtil.readFile(tasksFile);

            // Parse the JSON string
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray jsonArray = jsonObject.getJSONArray("tasks");

            // Convert JSONArray to List<JSONObject>
            for (int i = 0; i < jsonArray.length(); i++) {
                tasks.add(jsonArray.getJSONObject(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // Method to write initial data to tasks.json
    protected void writeInitialTasksToJsonFile(File tasksFile) {
        String initialData = "{\n" +
                "  \"tasks\": [" +
                "    {\n" +
                "        \"taskName\": \"Finish Report Draft\",\n" +
                "        \"desc\": \"Complete the first draft of the monthly report for the team meeting.\",\n" +
                "        \"status\": 0,\n" +
                "        \"taskPriority\": 5,\n" +
                "        \"startTime\": 1698835200000,\n" +
                "        \"endTime\": 1698921600000\n" +
                "    },\n" +
                "    {\n" +
                "        \"taskName\": \"Grocery Shopping\",\n" +
                "        \"desc\": \"Buy groceries for the week, including vegetables, fruits, and snacks.\",\n" +
                "        \"status\": 0,\n" +
                "        \"taskPriority\": 4,\n" +
                "        \"startTime\": 1698838800000,\n" +
                "        \"endTime\": 1698846000000\n" +
                "    },\n" +
                "    {\n" +
                "        \"taskName\": \"Workout Session\",\n" +
                "        \"desc\": \"Attend a yoga class at the gym to improve flexibility and relaxation.\",\n" +
                "        \"status\": 0,\n" +
                "        \"taskPriority\": 3,\n" +
                "        \"startTime\": 1698856800000,\n" +
                "        \"endTime\": 1698864000000\n" +
                "    },\n" +
                "    {\n" +
                "        \"taskName\": \"Read a Book\",\n" +
                "        \"desc\": \"Finish reading 'The Alchemist' by Paulo Coelho.\",\n" +
                "        \"status\": 0,\n" +
                "        \"taskPriority\": 2,\n" +
                "        \"startTime\": 1698871200000,\n" +
                "        \"endTime\": 1698892800000\n" +
                "    },\n" +
                "    {\n" +
                "        \"taskName\": \"Prepare Presentation\",\n" +
                "        \"desc\": \"Create and rehearse the presentation for the upcoming conference.\",\n" +
                "        \"status\": 1,\n" +
                "        \"taskPriority\": 1,\n" +
                "        \"startTime\": 1698900000000,\n" +
                "        \"endTime\": 1698925200000\n" +
                "    }\n" +
                "  ]\n" +
                "}";
                ; // Start with an empty tasks array
        FileUtil.writeFile(tasksFile, initialData);
    }

    public void updateRecyclerView(JSONObject newTask) {
        taskList.add(0, newTask); // Add to the top of the list
        taskAdapter.notifyItemInserted(0);
        taskAdapter.notifyItemRangeChanged(0, taskList.size());
    }

    private void showDeleteButton() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.showDeleteBtn();
        } else {
            Log.e("BaseFragment", "Activity is not an instance of MainActivity");
        }
    }

    private void hideDeleteButton() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.hideDeleteBtn();
        } else {
            Log.e("BaseFragment", "Activity is not an instance of MainActivity");
        }
    }

    @Override
    public void deleteSelectedTasks(ArrayList<JSONObject> deletionTasks) throws JSONException {
        // Remove tasks based on the deletionTasks list
        taskList.removeAll(deletionTasks);
        taskAdapter.notifyDataSetChanged(); // Notify the adapter of the changes

        taskAdapter.updateDeletionTasks(deletionTasks);
        mainActivity.updateDeletionTasks(deletionTasks);
        updateMainList();
        deletionTasks.clear(); // Clear the list after processing
        hideDeleteButton();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateEditedTask(int task_pos, JSONObject task) throws JSONException, IOException {
        taskList.get(task_pos).put("taskName", task.get("title"));
        taskList.get(task_pos).put("desc", task.get("desc"));
        taskList.get(task_pos).put("status", task.get("status").equals("Active") ? 1 : 0);
        taskList.get(task_pos).put("taskPriority", task.get("priority"));
        taskList.get(task_pos).put("startTime", task.get("start_date"));
        taskList.get(task_pos).put("endTime ", task.get("due_date"));

        taskAdapter.notifyDataSetChanged();
        updateMainList();
    }

    public void updateMainList() {
        mainActivity.updateTasklist(taskList);
    }
}
