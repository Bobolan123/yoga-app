package com.example.yogaapp.models;

public class ClassInstance {
    public int id;
    public int classId;
    public String date;
    public String teacher;
    public String comment;

    @Override
    public String toString() {
        return date + " | " + teacher + (comment != null && !comment.isEmpty() ? " - " + comment : "");
    }
}
