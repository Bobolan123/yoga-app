package com.example.yogaapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EditInstanceActivity extends AppCompatActivity {

    EditText etDate, etTeacher, etComment;
    Button btnSaveInstance;

    DatabaseHelper dbHelper;
    int instanceId;
    int classId;
    String classScheduledDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class_instance); // reuse same layout

        etDate = findViewById(R.id.etDate);
        etTeacher = findViewById(R.id.etTeacher);
        etComment = findViewById(R.id.etComment);
        btnSaveInstance = findViewById(R.id.btnSaveInstance);
        btnSaveInstance.setText("Update Instance");

        dbHelper = new DatabaseHelper(this);

        instanceId = getIntent().getIntExtra("instanceId", -1);
        if (instanceId == -1) {
            Toast.makeText(this, "Invalid instance ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load existing data
        ClassInstance instance = dbHelper.getInstanceById(instanceId);
        if (instance == null) {
            Toast.makeText(this, "Instance not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        classId = instance.classId;
        classScheduledDay = getScheduledDayOfClass(classId);

        etDate.setText(instance.date);
        etTeacher.setText(instance.teacher);
        etComment.setText(instance.comment);

        btnSaveInstance.setOnClickListener(v -> {
            if (!validateInputs()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String inputDate = etDate.getText().toString();
            String weekday = getWeekdayFromDate(inputDate);
            if (weekday == null) {
                Toast.makeText(this, "Invalid date format (yyyy-MM-dd)", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Calendar inputCal = Calendar.getInstance();
                inputCal.setTime(sdf.parse(inputDate));

                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                if (inputCal.before(today)) {
                    Toast.makeText(this, "Date must be after today", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!weekday.equalsIgnoreCase(classScheduledDay)) {
                    Toast.makeText(this, "Date does not match class's scheduled day (" + classScheduledDay + ")", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateInstance(instanceId, inputDate, etTeacher.getText().toString(), etComment.getText().toString());

            } catch (ParseException e) {
                Toast.makeText(this, "Invalid date format (yyyy-MM-dd)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        return !etDate.getText().toString().isEmpty()
                && !etTeacher.getText().toString().isEmpty();
    }

    private void updateInstance(int id, String date, String teacher, String comment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("teacher", teacher);
        values.put("comment", comment);

        int result = db.update("instances", values, "id = ?", new String[]{String.valueOf(id)});
        if (result > 0) {
            Toast.makeText(this, "Instance updated!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
        }
    }

    private ClassInstance getInstanceById(int id) {
        return dbHelper.getInstanceById(id);
    }

    private String getScheduledDayOfClass(int classId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT day FROM classes WHERE id = ?", new String[]{String.valueOf(classId)});
        String result = "";
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndexOrThrow("day"));
        }
        cursor.close();
        return result;
    }

    private String getWeekdayFromDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(dateString));
            return cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        } catch (ParseException e) {
            return null;
        }
    }
}
