package com.example.taskmanager.model;

public class Tasks {
    private String title;
    private String subtitle;
    private long timeInMillis;

    public Tasks() {} // needed for Gson

    public Tasks(String title, String subtitle, long timeInMillis) {
        this.title = title;
        this.subtitle = subtitle;
        this.timeInMillis = timeInMillis;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }
}
