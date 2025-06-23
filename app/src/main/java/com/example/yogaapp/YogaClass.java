package com.example.yogaapp;

public class YogaClass {
    public int id;
    public String day, time, duration, type, description;
    public int capacity;
    public float price;

    public String toString() {
        return day + " - " + time + " | " + type;
    }
}
