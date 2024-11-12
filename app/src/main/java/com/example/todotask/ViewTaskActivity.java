package com.example.todotask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todotask.utils.FileUtil;
import com.example.todotask.utils.TimeUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewTaskActivity extends AppCompatActivity {

    TextView title;
    TextView desc;
    TextView status;
    TextView priority;
    TextView created_date;
    TextView due_date;

    private ImageButton edit_btn;
    private Button edit_done;
    private List<String> text_view_ids = Arrays.asList("task_title", "task_status", "task_priority", "task_desc", "task_created_date", "task_due_date");
    private int updatingTaskIndex;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);

        title = findViewById(R.id.task_title);
        desc = findViewById(R.id.task_desc);
        status = findViewById(R.id.task_status);
        priority = findViewById(R.id.task_priority);
        created_date = findViewById(R.id.task_created_date);
        due_date = findViewById(R.id.task_due_date);

        edit_btn = findViewById(R.id.edit_btn);
        edit_done = findViewById(R.id.edit_done);

        JSONObject task_data_json;
        String task_data_string = getIntent().getStringExtra("task_data");
        updatingTaskIndex = getIntent().getIntExtra("task_pos", 0);

        try {
            task_data_json = new JSONObject(task_data_string);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            // Set text fields from JSON data
            title.setText(task_data_json.getString("taskName"));
            desc.setText(task_data_json.getString("desc"));

            // Conditional logic for status
            String statusValue = task_data_json.getString("status").equals("1") ? "Active" : "Done";
            status.setText(statusValue);

            priority.setText(task_data_json.getString("taskPriority"));

            // Convert timestamp to local date and time string
            long startTimeMillis = task_data_json.getLong("startTime");
            long endTimeMillis = task_data_json.getLong("endTime");

            // Define a date format for the local time string
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

            // Convert timestamps and set text
            String startDateString = dateFormat.format(new Date(startTimeMillis));
            String endDateString = dateFormat.format(new Date(endTimeMillis));

            created_date.setText(startDateString);
            due_date.setText(endDateString);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edit_done.getVisibility() == View.GONE){
                    edit_done.setVisibility(View.VISIBLE);
                    replaceTextViewsWithEditTexts(view.getContext(), text_view_ids);
                    replaceTextViewWithSpinner("task_status");
                }
            }
        });

        edit_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                edit_done.setVisibility(View.GONE);
                replaceEditTextsWithTextViews(view.getContext(), text_view_ids);

                JSONObject edited_task_data = new JSONObject();
                String pattern = "yyyy-MM-dd HH:mm:ss";

                try {
                    edited_task_data.put("task_pos",updatingTaskIndex);
                    edited_task_data.put("title", title.getText());
                    edited_task_data.put("desc", desc.getText());
                    edited_task_data.put("status", status.getText());
                    edited_task_data.put("priority", priority.getText());
                    edited_task_data.put("start_date", TimeUtil.convertToMillis(created_date.getText().toString(), pattern));
                    edited_task_data.put("due_date", TimeUtil.convertToMillis((String) due_date.getText().toString(), pattern));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                Intent resultIntent = new Intent();
                resultIntent.putExtra("edited_task_data", edited_task_data.toString());

                setResult(Activity.RESULT_OK, resultIntent);
                //finish();
            }
        });

    }

    public void replaceTextViewsWithEditTexts(Context context, List<String> textViewIdNames) {
        for (String idName : textViewIdNames) {
            // Get the integer ID from the string name
            int id = context.getResources().getIdentifier(idName, "id", context.getPackageName());

            if (id == 0) {
                continue;
            }
            // Find the TextView by ID
            TextView textView = findViewById(id);
            if (textView == null) {
                continue;
            }
            if (idName.equals("task_status") || idName.equals("task_created_date") || idName.equals("task_priority")){
                textView.setTextColor(Color.parseColor("#FF0000"));
                continue;
            }

            // Get layout parameters and position of the TextView
            ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
            ViewGroup parent = (ViewGroup) textView.getParent();
            int index = parent.indexOfChild(textView);

            // Create a new EditText
            EditText editText = new EditText(context);

            // Apply the same layout parameters and styles
            editText.setId(textView.getId());
            editText.setLayoutParams(layoutParams);
            editText.setText(textView.getText());
            editText.setTextSize(textView.getTextSize()/2);
            editText.setTextColor(textView.getTextColors());
            editText.setTypeface(textView.getTypeface());

            // Set padding and background if needed
            editText.setPadding(
                    textView.getPaddingLeft(),
                    textView.getPaddingTop(),
                    textView.getPaddingRight(),
                    textView.getPaddingBottom()
            );
            editText.setBackground(textView.getBackground());

            // Replace TextView with EditText in the parent layout
            parent.removeViewAt(index);
            parent.addView(editText, index);
        }
    }

    public void replaceTextViewWithSpinner(String textViewId) {
        // Find the TextView by ID
        TextView textView = findViewById(getResources().getIdentifier(textViewId, "id", getPackageName()));

        if (textView != null) {
            // Get the current text from the TextView (either "Active" or "Done")
            String currentStatus = textView.getText().toString().trim();
            // Create a new Spinner programmatically
            Spinner spinner = new Spinner(this);

            // Create an ArrayAdapter with options "Active" and "Done"
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    new String[] {"Active", "Done"}
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setId(textView.getId());

            // Set the Spinner to the current status (if it's "Active" or "Done")
            int position = currentStatus.equalsIgnoreCase("Active") ? 0 : 1;
            spinner.setSelection(position);

            // Get the parent of the TextView
            ViewGroup parent = (ViewGroup) textView.getParent();
            int index = parent.indexOfChild(textView);

            // Remove the TextView and add the Spinner in its place
            parent.removeViewAt(index);
            parent.addView(spinner, index);

            // Optionally, set up an OnItemSelectedListener to capture changes in the Spinner
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                    // Get the selected status
                    String selectedStatus = (String) parentView.getItemAtPosition(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // Handle case where nothing is selected if necessary
                }
            });
        }
    }

    public void replaceEditTextsWithTextViews(Context context, List<String> editTextIdNames) {
        for (String idName : editTextIdNames) {
            // Get the integer ID from the string name
            int id = context.getResources().getIdentifier(idName, "id", context.getPackageName());

            EditText editText;

            if (id == 0) {
                continue;
            }
            if (idName.equals("task_status") || idName.equals("task_created_date") || idName.equals("task_priority")){
                if(idName.equals("task_status")){
                    replaceSpinnerWithTextView("task_status");
                }else{
                    try {
                        TextView textView = findViewById(id);
                        textView.setTextColor(Color.parseColor("#000000"));
                    }catch(Exception e){
                        Log.e("npe",e.toString());
                    }
                }
                continue;
            }else{
                editText = findViewById(id);
            }
            if (editText == null) {
                continue;
            }

            // Get layout parameters and position of the EditText
            ViewGroup.LayoutParams layoutParams = editText.getLayoutParams();
            ViewGroup parent = (ViewGroup) editText.getParent();
            int index = parent.indexOfChild(editText);

            // Create a new TextView
            TextView textView = new TextView(context);

            // Apply the same layout parameters and styles
            textView.setLayoutParams(layoutParams);
            textView.setId(editText.getId());
            textView.setText(editText.getText());
            textView.setTextSize(editText.getTextSize()/2);
            textView.setTextColor(editText.getCurrentTextColor());
            textView.setTypeface(editText.getTypeface());

            // Set padding and background if needed
            textView.setPadding(
                    editText.getPaddingLeft(),
                    editText.getPaddingTop(),
                    editText.getPaddingRight(),
                    editText.getPaddingBottom()
            );
            textView.setBackground(editText.getBackground());

            // Replace EditText with TextView in the parent layout
            parent.removeViewAt(index);
            parent.addView(textView, index);
        }

        title = findViewById(R.id.task_title);
        desc = findViewById(R.id.task_desc);
        status = findViewById(R.id.task_status);
        priority = findViewById(R.id.task_priority);
        created_date = findViewById(R.id.task_created_date);
        due_date = findViewById(R.id.task_due_date);

    }

    public void replaceSpinnerWithTextView(String spinnerId) {
        // Find the Spinner by ID
        Spinner spinner = findViewById(getResources().getIdentifier(spinnerId, "id", getPackageName()));

        if (spinner != null) {
            // Get the selected status from the Spinner
            String selectedStatus = spinner.getSelectedItem().toString().trim();

            // Create a new TextView programmatically
            TextView textView = new TextView(this);
            textView.setId(spinner.getId());  // Set the TextView ID to match the original Spinner ID
            textView.setTextColor(getResources().getColor(R.color.black));
            textView.setText(selectedStatus); // Set the text to the selected status
            textView.setTextSize(18);         // Optional: set the text size or other styling
            textView.setPadding(8, 6, 8, 6);  // Optional: set padding
//            textView.setBackground(R.drawable.edit_text_background_light);

            // Get the parent of the Spinner
            ViewGroup parent = (ViewGroup) spinner.getParent();
            int index = parent.indexOfChild(spinner);

            // Remove the Spinner and add the TextView in its place
            parent.removeViewAt(index);
            parent.addView(textView, index);
        }
    }

}
