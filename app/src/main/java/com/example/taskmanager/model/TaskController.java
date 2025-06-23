package com.example.taskmanager.model;

import java.util.ArrayList;
import java.util.List;

public class TaskController {
    private List<Tasks> taskList;

    public TaskController() {
        taskList = new ArrayList<>();
    }

    public void addTask(String title) {
        taskList.add(new Tasks(title, "")); // empty subtitle
    }



    public void removeTask(int index) {
        if (index >= 0 && index < taskList.size()) {
            taskList.remove(index);
        }
    }

    public List<Tasks> getAllTasks() {
        return taskList;
    }
}
