package com.example.taskmanager;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taskmanager.model.TaskController;
import com.example.taskmanager.model.Tasks;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TaskController controller;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> taskDescriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controller = new TaskController();
        taskDescriptions = new ArrayList<>();

        // 🔁 Load saved tasks from SharedPreferences
        List<Tasks> savedTasks = loadTasksFromPreferences();
        for (Tasks task : savedTasks) {
            controller.addTask(task.getTitle()); // or task.getTitle() + "\n" + task.getSubtitle()

        }

        EditText taskInput = findViewById(R.id.taskInput);
        Button addButton = findViewById(R.id.addButton);
        ListView taskList = findViewById(R.id.taskList);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskDescriptions);
        taskList.setAdapter(adapter);

        refreshTaskList(); // 🔄 Display the loaded tasks on launch

        // ➕ Add Task
        addButton.setOnClickListener(v -> {
            String text = taskInput.getText().toString();
            if (!text.isEmpty()) {
                controller.addTask(text);
                saveTasksToPreferences(controller.getAllTasks()); // ✅ Save after add
                refreshTaskList();
                taskInput.setText("");
            }
        });

        // ❌ Delete Task on Tap
        taskList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTask = taskDescriptions.get(position);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete Task")
                    .setMessage("Do you want to delete this task?\n\n" + selectedTask)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        controller.removeTask(position);
                        saveTasksToPreferences(controller.getAllTasks()); // ✅ Save after delete
                        refreshTaskList();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void refreshTaskList() {
        taskDescriptions.clear();
        for (Tasks task : controller.getAllTasks()) {
            taskDescriptions.add(task.getTitle()); // or task.getTitle() + "\n" + task.getSubtitle()

        }
        adapter.notifyDataSetChanged();
    }

    private void saveTasksToPreferences(List<Tasks> taskList) {
        SharedPreferences prefs = getSharedPreferences("task_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        editor.putString("task_list", json);
        editor.apply();
    }

    private List<Tasks> loadTasksFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("task_data", MODE_PRIVATE);
        String json = prefs.getString("task_list", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Tasks>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }
}
