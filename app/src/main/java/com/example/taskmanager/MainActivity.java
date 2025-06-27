package com.example.taskmanager;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.taskmanager.model.TaskController;
import com.example.taskmanager.model.Tasks;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TaskController controller;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> taskDescriptions;
    private long selectedTimeInMillis = -1; // store selected time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Exact alarms are disabled for this app. Please enable them in battery settings.", Toast.LENGTH_LONG).show();

                // Optionally, open the settings page for the user:
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }


        controller = new TaskController();
        taskDescriptions = new ArrayList<>();

        // load saved tasks
        List<Tasks> savedTasks = loadTasksFromPreferences();
        for (Tasks task : savedTasks) {
            controller.addTask(task.getTitle(), task.getSubtitle(), task.getTimeInMillis());
        }

        EditText taskInput = findViewById(R.id.taskInput);
        Button addButton = findViewById(R.id.addButton);
        Button setTimeButton = findViewById(R.id.setTimeButton);
        TextView timeDisplay = findViewById(R.id.timeDisplay);
        ListView taskList = findViewById(R.id.taskList);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskDescriptions);
        taskList.setAdapter(adapter);

        refreshTaskList();

        // set time button
        setTimeButton.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            new TimePickerDialog(MainActivity.this, (view, h, m) -> {
                cal.set(Calendar.HOUR_OF_DAY, h);
                cal.set(Calendar.MINUTE, m);
                cal.set(Calendar.SECOND, 0);

                selectedTimeInMillis = cal.getTimeInMillis();
                timeDisplay.setText("Reminder at: " + h + ":" + String.format("%02d", m));

            }, hour, minute, true).show();
        });

        // add task button
        addButton.setOnClickListener(v -> {
            String text = taskInput.getText().toString();
            if (!text.isEmpty()) {
                long timeToUse = selectedTimeInMillis != -1 ? selectedTimeInMillis : System.currentTimeMillis();

                controller.addTask(text, "", timeToUse);
                saveTasksToPreferences(controller.getAllTasks());
                scheduleReminder(text, timeToUse);

                refreshTaskList();
                taskInput.setText("");
                timeDisplay.setText("No time selected");
                selectedTimeInMillis = -1;
            }
        });

        // delete on tap
        taskList.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTask = taskDescriptions.get(position);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete Task")
                    .setMessage("Delete this task?\n\n" + selectedTask)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        controller.removeTask(position);
                        saveTasksToPreferences(controller.getAllTasks());
                        refreshTaskList();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void refreshTaskList() {
        taskDescriptions.clear();
        for (Tasks task : controller.getAllTasks()) {
            String display = task.getTitle();
            if (task.getTimeInMillis() > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(task.getTimeInMillis());
                String timeStr = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
                display += " (Reminder at " + timeStr + ")";
            }
            taskDescriptions.add(display);
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

    private void scheduleReminder(String taskTitle, long triggerTime) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("task_title", taskTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) triggerTime, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }
}
