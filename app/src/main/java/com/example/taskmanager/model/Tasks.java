package com.example.taskmanager.model;

public class Tasks {
    private String title;
    private String subtitle;

    public Tasks() {} // Required by Gson

    public Tasks(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}

