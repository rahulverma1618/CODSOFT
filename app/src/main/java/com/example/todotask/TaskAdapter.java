package com.example.todotask;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private static List<JSONObject> taskList;
    private static ArrayList<JSONObject> deletionTasks;
    static int EDIT_TASK_REQUEST_CODE = 2;
    private Context context;

    // Constructor accepts List<JSONObject> instead of JSONArray
    public TaskAdapter(Context context, List<JSONObject> taskList) {
        this.taskList = taskList; // Initialize the list of tasks
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        JSONObject task = taskList.get(position);

        try {
            holder.taskTitle.setText(task.getString("taskName"));
            holder.taskDesc.setText(task.getString("desc"));
            holder.status.setText(task.getString("status").equals("1") ? "Active" : "Done");

            if (task.getString("status").equals("1")) {
                holder.taskStatusBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.active_task_color)));
            } else {
                holder.taskStatusBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.completed_task_color)));
            }

            // Convert timestamps to a readable date and time format
            long startTimeStamp = task.getLong("startTime");
            long endTimeStamp = task.getLong("endTime");

            // Create SimpleDateFormat instance for the desired date format
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy h:mm", Locale.getDefault());

            // Format the timestamps
            String startTime = dateFormat.format(new Date(startTimeStamp));
            String endTime = dateFormat.format(new Date(endTimeStamp));
            // Set the formatted strings to the TextViews
            holder.startTime.setText(startTime);
            holder.endTime.setText(endTime);
        } catch (JSONException e) {
            Log.e("TaskAdapter", "Error parsing JSON", e);
        }

        // Check if the task is marked for deletion (optional state in your Task model)
        try{
            if (deletionTasks.contains(task)) {
                holder.itemView.setAlpha(0.5f);
            } else {
                holder.itemView.setAlpha(1.0f);
            }
        } catch (Exception e) {
            Log.e("error deletion",e.toString());
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        TextView taskDesc;
        TextView status;
        TextView startTime;
        TextView endTime;
        Button taskStatusBtn;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.TaskTitle);
            taskDesc = itemView.findViewById(R.id.TaskDesc);
            status = itemView.findViewById(R.id.TaskStatus);
            startTime = itemView.findViewById(R.id.TaskStartTime);
            endTime = itemView.findViewById(R.id.TaskEndTime);
            taskStatusBtn = itemView.findViewById(R.id.TaskStatusBtn);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    // Check that position is valid and deletionTasks is not empty
                    Log.d("alpha", String.valueOf(v.getAlpha()));
                    if (position != RecyclerView.NO_POSITION && v.getAlpha() == 1.0) {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ViewTaskActivity.class);
                        intent.putExtra("task_data", taskList.get(position).toString());
                        intent.putExtra("task_pos", position);
                        //start the view task activity
                        Log.d("alpha next", String.valueOf(v.getAlpha()));
                        ((Activity) context).startActivityForResult(intent, EDIT_TASK_REQUEST_CODE);
                    }
                }
            });

        }
    }

    public void updateDeletionTasks(ArrayList<JSONObject> deletionTasks){
        this.deletionTasks = deletionTasks;
    }
}
