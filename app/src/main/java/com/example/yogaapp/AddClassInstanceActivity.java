package com.example.yogaapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddClassInstanceActivity extends AppCompatActivity {

    EditText etDate, etTeacher, etComment;
    Button btnSaveInstance;

    DatabaseHelper dbHelper;
    int classId;
    String classScheduledDay; // e.g., "Monday"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class_instance);

        etDate = findViewById(R.id.etDate);
        etTeacher = findViewById(R.id.etTeacher);
        etComment = findViewById(R.id.etComment);
        btnSaveInstance = findViewById(R.id.btnSaveInstance);

        dbHelper = new DatabaseHelper(this);

        // Get classId from intent
        classId = getIntent().getIntExtra("classId", -1);
        if (classId == -1) {
            Toast.makeText(this, "Invalid class ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load class day (e.g., Monday)
        classScheduledDay = getScheduledDayOfClass(classId);

        btnSaveInstance.setOnClickListener(v -> {
            if (validateInputs()) {
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

                    saveInstance(inputDate, etTeacher.getText().toString(), etComment.getText().toString());

                } catch (ParseException e) {
                    Toast.makeText(this, "Invalid date format (yyyy-MM-dd)", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        return !etDate.getText().toString().isEmpty()
                && !etTeacher.getText().toString().isEmpty();
    }

    private void saveInstance(String date, String teacher, String comment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("class_id", classId);
        values.put("date", date);
        values.put("teacher", teacher);
        values.put("comment", comment);

        long result = db.insert("instances", null, values);
        if (result != -1) {
            Toast.makeText(this, "Instance added successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add instance", Toast.LENGTH_SHORT).show();
        }
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
