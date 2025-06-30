package com.example.yogaapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "YogaClasses.db";
    public static final String TABLE_NAME = "classes";

    public DatabaseHelper(Context context) {
        // Change the version from 1 to 2
        super(context, DB_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE classes (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "day TEXT NOT NULL, time TEXT NOT NULL, capacity INTEGER NOT NULL, " +
                        "duration TEXT NOT NULL, price REAL NOT NULL, " +
                        "type TEXT NOT NULL, description TEXT)");

        db.execSQL(
                "CREATE TABLE instances (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "class_id INTEGER NOT NULL, " +
                        "date TEXT NOT NULL, " +
                        "teacher TEXT NOT NULL, " +
                        "comment TEXT, " +
                        "FOREIGN KEY(class_id) REFERENCES classes(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop both tables
        db.execSQL("DROP TABLE IF EXISTS instances");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Recreate the database
        onCreate(db);
    }
    public List<YogaClass> getAllClasses() {
        List<YogaClass> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                YogaClass yc = new YogaClass();
                yc.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                yc.day = cursor.getString(cursor.getColumnIndexOrThrow("day"));
                yc.time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                yc.capacity = cursor.getInt(cursor.getColumnIndexOrThrow("capacity"));
                yc.duration = cursor.getString(cursor.getColumnIndexOrThrow("duration"));
                yc.price = cursor.getFloat(cursor.getColumnIndexOrThrow("price"));
                yc.type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                yc.description = cursor.getString(cursor.getColumnIndexOrThrow("description"));

                list.add(yc);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int deleteClassById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(id)});
        return id;
    }

    public List<ClassInstance> getInstancesForClass(int classId) {
        List<ClassInstance> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM instances WHERE class_id = ?", new String[]{String.valueOf(classId)});
        if (cursor.moveToFirst()) {
            do {
                ClassInstance ci = new ClassInstance();
                ci.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                ci.classId = cursor.getInt(cursor.getColumnIndexOrThrow("class_id"));
                ci.date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                ci.teacher = cursor.getString(cursor.getColumnIndexOrThrow("teacher"));
                ci.comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"));
                list.add(ci);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int deleteInstanceById(int instanceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("instances", "id = ?", new String[]{String.valueOf(instanceId)});
    }

    public ClassInstance getInstanceById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM instances WHERE id = ?", new String[]{String.valueOf(id)});
        ClassInstance instance = null;
        if (cursor.moveToFirst()) {
            instance = new ClassInstance();
            instance.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            instance.classId = cursor.getInt(cursor.getColumnIndexOrThrow("class_id"));
            instance.date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            instance.teacher = cursor.getString(cursor.getColumnIndexOrThrow("teacher"));
            instance.comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"));
        }
        cursor.close();
        return instance;
    }

}
