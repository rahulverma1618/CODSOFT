package com.example.todotask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CreateTaskActivity extends AppCompatActivity {

    private TextView taskTitle;
    private TextView taskDesc;
    private TextView dueDate;
    private TextView dueTime;
    private Button datePickerButton;
    private Button timePickerButton;
    private Button createTaskBtn;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        taskTitle = findViewById(R.id.task_name_input);
        taskDesc = findViewById(R.id.task_desc_input);

        datePickerButton = findViewById(R.id.date_picker_btn);
        dueDate = findViewById(R.id.task_due_date_input);
        datePickerButton.setOnClickListener(v -> openDatePicker());

        timePickerButton = findViewById(R.id.time_picker_btn);
        dueTime = findViewById(R.id.task_due_time_input);
        timePickerButton.setOnClickListener(v -> openTimePicker());

        createTaskBtn = findViewById(R.id.create_task_button);
        createTaskBtn.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        try {
            // Get task details from EditTexts
            String name = taskTitle.getText().toString();
            String desc = taskDesc.getText().toString();
            long startDate = System.currentTimeMillis();

            // Parse due date from the EditText
            String dueDateString = dueDate.getText().toString();
            String dueTimeString = dueTime.getText().toString();
            String dueDateTimeString = dueDateString + " " + dueTimeString;

            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
                    .appendLiteral('/')
                    .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
                    .appendLiteral('/')
                    .appendValue(ChronoField.YEAR, 4)
                    .appendLiteral(' ')
                    .appendPattern("HH:mm")
                    .toFormatter();
            LocalDateTime localDateTime = LocalDateTime.parse(dueDateTimeString, formatter);
            long dueDateTimestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            // Create a new JSONObject for the task
            JSONObject task = new JSONObject();
            task.put("taskName", name);
            task.put("desc", desc);
            task.put("status", 1);
            task.put("taskPriority", 1);
            task.put("startTime", startDate);
            task.put("endTime", dueDateTimestamp);

            saveTaskToFile(task, getApplicationContext());

            // Return the task data back to the MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("newTask", task.toString());
            setResult(Activity.RESULT_OK, resultIntent);

            Toast.makeText(this, "Task " + name + " added", Toast.LENGTH_SHORT).show();
            // Close the activity
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.schedule(() -> {
                finish();
            }, 2, TimeUnit.SECONDS);

            executor.shutdown();

        } catch (Exception e) {
            Log.d("error",e.toString());
            e.printStackTrace();
        }
    }

    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            dueDate.setText(date);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void openTimePicker() {
        // Get current time
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            // Format and display selected time (HH:mm)
            String time = String.format("%02d:%02d", selectedHour, selectedMinute);
            dueTime.setText(time);
        }, hour, minute, true);

        // Show the TimePickerDialog
        timePickerDialog.show();
    }

    private void saveTaskToFile(JSONObject task, Context context) {
        File tasksFile = new File(context.getExternalFilesDir(null), "tasks.json");

        try {
            // Read the tasks from the file
            try {
                // Read the tasks file
                String jsonStr = FileUtil.readFile(tasksFile);

                JSONObject jsonObject = new JSONObject(jsonStr);
                JSONArray tasksArray = jsonObject.getJSONArray("tasks");

                // Convert JSONArray to List<JSONObject> for sorting
                List<JSONObject> tasksList = new ArrayList<>();
                for (int i = 0; i < tasksArray.length(); i++) {
                    tasksList.add(tasksArray.getJSONObject(i));
                }

                //set the coorect max priority
                int maxPriority = Integer.parseInt(tasksList.get(0).getString("taskPriority"));
                task.put("taskPriority",maxPriority+1);

                //add the new task to list
                tasksList.add(task);
                // Sort tasks in descending order based on taskPriority
                tasksList.sort(new Comparator<JSONObject>() {
                    @Override
                    public int compare(JSONObject task1, JSONObject task2) {
                        try {
                            return Integer.compare(task2.getInt("taskPriority"), task1.getInt("taskPriority"));
                        } catch (JSONException e) {
                            return 0;
                        }
                    }
                });

                // Convert the sorted list back to JSONArray
                JSONArray sortedTasksArray = new JSONArray(tasksList);
                jsonObject.put("tasks", sortedTasksArray);

                // Save the file back
                FileUtil.writeFile(tasksFile, jsonObject.toString(4));

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
