package com.example.todotask;

import static com.example.todotask.TaskAdapter.EDIT_TASK_REQUEST_CODE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.todotask.utils.FileUtil;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import interfaces.TaskDeletionCallback;

public class MainActivity extends AppCompatActivity implements TaskDeletionCallback {

    private TaskDeletionCallback taskDeletionCallback;
    Button createTaskBtn;
    Button delete_btn;
    private static final int CREATE_TASK_REQUEST_CODE = 1;
    private static ArrayList<JSONObject> deletionTasks = new ArrayList<>();
    private BaseFragment baseFragment;
    private static List<JSONObject> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        delete_btn = findViewById(R.id.delete_task_btn);

        createTaskBtn = findViewById(R.id.create_task_btn);
        createTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the CreateTaskActivity
                Intent intent = new Intent(MainActivity.this, CreateTaskActivity.class);
                startActivityForResult(intent, CREATE_TASK_REQUEST_CODE);
            }
        });

        // Get the reference to BaseFragment
        BaseFragment BaseFragment = new BaseFragment();

        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        // Setting up the ViewPager with the adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        // Optional: Linking TabLayout with ViewPager
        tabLayout.setupWithViewPager(viewPager);

        // Set up the click listener for delete_btn
        delete_btn.setOnClickListener(v -> {
            // Trigger deletion in the fragment via the callback
            if (taskDeletionCallback != null) {
                Log.d("callbacktodelete","success main");
                try {
                    taskDeletionCallback.deleteSelectedTasks(deletionTasks);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void saveTaskToFile() throws JSONException {
        if (taskList != null) {
            JSONObject taskObj = new JSONObject();
            taskObj.put("tasks", new JSONArray(taskList));

            File tasksFile = new File(getExternalFilesDir(null), "tasks.json");
            FileUtil.writeFile(tasksFile, taskObj.toString(4));
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        try {
            saveTaskToFile();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Toast.makeText(this, "Tasks Saved Locally", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_TASK_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String newTaskString = data.getStringExtra("newTask");
                try {
                    JSONObject newTaskJson = new JSONObject(newTaskString);
                    notifyBaseFragment(newTaskJson);
                } catch (JSONException e) {
                    e.printStackTrace(); // Handle JSON exception
                }
            }
        }

        if (requestCode == EDIT_TASK_REQUEST_CODE && resultCode == RESULT_OK){
            String editTaskString = data.getStringExtra("edited_task_data");
            BaseFragment baseFragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + 0);
            JSONObject editTaskJSon;
            Integer task_pos = 0;

            try {
                editTaskJSon = new JSONObject(editTaskString);
                task_pos = editTaskJSon.getInt("task_pos");

                if (baseFragment != null) {
                    baseFragment.updateEditedTask(task_pos, editTaskJSon);
                } else {
                    Log.e("MainActivity", "BaseFragment not found or not properly initialized");
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void notifyBaseFragment(JSONObject newTask) throws JSONException {
        // Get the ViewPager
        ViewPager viewPager = findViewById(R.id.viewPager);
        // Get the current fragment for AllFragment
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + 0); // AllFragment is at position 0
        if (fragment instanceof BaseFragment) {
            ((BaseFragment) fragment).updateRecyclerView(newTask);
        } else {
            Log.e("notifyBaseFragment", "Fragment is not an instance of AllFragment or not found");
        }
    }

    public void showDeleteBtn(){
        delete_btn.setVisibility(View.VISIBLE);
    }

    public void hideDeleteBtn(){
        delete_btn.setVisibility(View.GONE);
    }

    public void setTaskDeletionCallback(TaskDeletionCallback callback) {
        this.taskDeletionCallback = callback;
        Log.d("callbacktodelete","call attached success");
    }



    @Override
    public void deleteSelectedTasks(ArrayList<JSONObject> deletionTasks) throws JSONException {
    }

    public void updateDeletionTasks(ArrayList<JSONObject> deletionTasks){
        this.deletionTasks = deletionTasks;
    }

    public void updateTasklist(List<JSONObject> tasklist) {
        MainActivity.taskList = tasklist;
    }

}
