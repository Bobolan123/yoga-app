package com.example.yogaapp;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddClassInstanceActivity extends AppCompatActivity {

    private TextInputEditText instanceDateField, instructorNameField, instanceNotesField;
    private MaterialButton createInstanceButton;
    private DatabaseHelper databaseHelper;
    private int parentClassIdentifier;
    private String parentClassScheduleDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class_instance);

        initializeActivityComponents();
        retrieveParentClassIdentifier();
        loadParentClassScheduleDay();
        configureDatePickerFunctionality();
        configureCreateButtonBehavior();
    }

    private void initializeActivityComponents() {
        instanceDateField = findViewById(R.id.etInstanceDate);
        instructorNameField = findViewById(R.id.etInstructorName);
        instanceNotesField = findViewById(R.id.etInstanceNotes);
        createInstanceButton = findViewById(R.id.btnCreateInstance);
        databaseHelper = new DatabaseHelper(this);
    }

    private void retrieveParentClassIdentifier() {
        parentClassIdentifier = getIntent().getIntExtra("classId", -1);
        
        if (parentClassIdentifier == -1) {
            displayUserMessage("Invalid class identifier provided");
            finish();
        }
    }

    private void loadParentClassScheduleDay() {
        parentClassScheduleDay = retrieveClassScheduleDay(parentClassIdentifier);
        
        if (parentClassScheduleDay.isEmpty()) {
            displayUserMessage("Unable to retrieve class schedule information");
            finish();
        }
    }

    private void configureDatePickerFunctionality() {
        instanceDateField.setOnClickListener(v -> presentDatePickerDialog());
    }

    private void presentDatePickerDialog() {
        Calendar currentDate = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                    selectedYear, selectedMonth + 1, selectedDay);
                instanceDateField.setText(formattedDate);
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.getDatePicker().setMinDate(currentDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void configureCreateButtonBehavior() {
        createInstanceButton.setOnClickListener(v -> {
            if (performInputValidation()) {
                presentCreationConfirmationDialog();
            }
        });
    }

    private boolean performInputValidation() {
        if (extractFieldText(instanceDateField).isEmpty()) {
            instanceDateField.setError("Instance date is required");
            instanceDateField.requestFocus();
            return false;
        }

        String selectedDate = extractFieldText(instanceDateField);
        String weekdayFromDate = extractWeekdayFromDate(selectedDate);
        
        if (weekdayFromDate == null) {
            instanceDateField.setError("Invalid date format");
            instanceDateField.requestFocus();
            return false;
        }

        if (!validateDateIsInFuture(selectedDate)) {
            instanceDateField.setError("Date must be in the future");
            instanceDateField.requestFocus();
            return false;
        }

        if (!weekdayFromDate.equalsIgnoreCase(parentClassScheduleDay)) {
            instanceDateField.setError("Date must match class schedule day (" + parentClassScheduleDay + ")");
            instanceDateField.requestFocus();
            return false;
        }

        if (extractFieldText(instructorNameField).isEmpty()) {
            instructorNameField.setError("Instructor name is required");
            instructorNameField.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateDateIsInFuture(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.setTime(dateFormat.parse(dateString));

            Calendar todayCalendar = Calendar.getInstance();
            todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
            todayCalendar.set(Calendar.MINUTE, 0);
            todayCalendar.set(Calendar.SECOND, 0);
            todayCalendar.set(Calendar.MILLISECOND, 0);

            return !selectedCalendar.before(todayCalendar);
        } catch (ParseException e) {
            return false;
        }
    }

    private void presentCreationConfirmationDialog() {
        String confirmationSummary = constructCreationSummary();
        
        new AlertDialog.Builder(this)
                .setTitle("Confirm Instance Creation")
                .setMessage(confirmationSummary)
                .setPositiveButton("Create Instance", (dialog, which) -> executeInstanceCreation())
                .setNegativeButton("Review Details", null)
                .show();
    }

    private String constructCreationSummary() {
        return "Date: " + extractFieldText(instanceDateField) + "\n" +
               "Instructor: " + extractFieldText(instructorNameField) + "\n" +
               "Notes: " + (extractFieldText(instanceNotesField).isEmpty() ? 
                           "No additional notes" : extractFieldText(instanceNotesField));
    }

    private void executeInstanceCreation() {
        SQLiteDatabase writableDatabase = databaseHelper.getWritableDatabase();
        ContentValues instanceValues = new ContentValues();
        
        instanceValues.put("class_id", parentClassIdentifier);
        instanceValues.put("date", extractFieldText(instanceDateField));
        instanceValues.put("teacher", extractFieldText(instructorNameField));
        instanceValues.put("comment", extractFieldText(instanceNotesField));

        long creationResult = writableDatabase.insert("instances", null, instanceValues);
        
        if (creationResult != -1) {
            displayUserMessage("Class instance created successfully!");
            finish();
        } else {
            displayUserMessage("Failed to create instance. Please try again.");
        }
    }

    private String retrieveClassScheduleDay(int classId) {
        SQLiteDatabase readableDatabase = databaseHelper.getReadableDatabase();
        Cursor classCursor = readableDatabase.rawQuery(
            "SELECT day FROM classes WHERE id = ?", 
            new String[]{String.valueOf(classId)}
        );
        
        String scheduleDay = "";
        if (classCursor.moveToFirst()) {
            scheduleDay = classCursor.getString(classCursor.getColumnIndexOrThrow("day"));
        }
        classCursor.close();
        return scheduleDay;
    }

    private String extractWeekdayFromDate(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateFormat.parse(dateString));
            return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        } catch (ParseException e) {
            return null;
        }
    }

    private String extractFieldText(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void displayUserMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
