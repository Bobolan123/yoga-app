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
import java.util.*;

public class EditInstanceActivity extends AppCompatActivity {

    private TextInputEditText editInstanceDateField, editInstructorNameField, editInstanceNotesField;
    private MaterialButton updateInstanceButton;
    private DatabaseHelper databaseHelper;
    private int classInstanceIdentifier;
    private int parentClassIdentifier;
    private String parentClassScheduleDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_instance);

        initializeActivityComponents();
        retrieveInstanceIdentifier();
        loadExistingInstanceData();
        configureDatePickerFunctionality();
        configureUpdateButtonBehavior();
    }

    private void initializeActivityComponents() {
        editInstanceDateField = findViewById(R.id.etEditInstanceDate);
        editInstructorNameField = findViewById(R.id.etEditInstructorName);
        editInstanceNotesField = findViewById(R.id.etEditInstanceNotes);
        updateInstanceButton = findViewById(R.id.btnUpdateInstance);
        databaseHelper = new DatabaseHelper(this);
    }

    private void retrieveInstanceIdentifier() {
        classInstanceIdentifier = getIntent().getIntExtra("instanceId", -1);
        
        if (classInstanceIdentifier == -1) {
            displayUserMessage("Invalid instance identifier provided");
            finish();
        }
    }

    private void loadExistingInstanceData() {
        ClassInstance existingInstance = databaseHelper.getInstanceById(classInstanceIdentifier);
        
        if (existingInstance == null) {
            displayUserMessage("Instance data not found");
            finish();
            return;
        }

        parentClassIdentifier = existingInstance.classId;
        parentClassScheduleDay = retrieveParentClassScheduleDay(parentClassIdentifier);

        populateInstanceFields(existingInstance);
    }

    private void populateInstanceFields(ClassInstance instance) {
        editInstanceDateField.setText(instance.date);
        editInstructorNameField.setText(instance.teacher != null ? instance.teacher : "");
        editInstanceNotesField.setText(instance.comment != null ? instance.comment : "");
    }

    private void configureDatePickerFunctionality() {
        editInstanceDateField.setOnClickListener(v -> presentDatePickerDialog());
    }

    private void presentDatePickerDialog() {
        Calendar currentDate = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                    selectedYear, selectedMonth + 1, selectedDay);
                editInstanceDateField.setText(formattedDate);
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.getDatePicker().setMinDate(currentDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private void configureUpdateButtonBehavior() {
        updateInstanceButton.setOnClickListener(v -> {
            if (performInputValidation()) {
                presentUpdateConfirmationDialog();
            }
        });
    }

    private boolean performInputValidation() {
        if (extractFieldText(editInstanceDateField).isEmpty()) {
            editInstanceDateField.setError("Instance date is required");
            editInstanceDateField.requestFocus();
            return false;
        }

        String selectedDate = extractFieldText(editInstanceDateField);
        String weekdayFromDate = extractWeekdayFromDate(selectedDate);
        
        if (weekdayFromDate == null) {
            editInstanceDateField.setError("Invalid date format");
            editInstanceDateField.requestFocus();
            return false;
        }

        if (!validateDateIsInFuture(selectedDate)) {
            editInstanceDateField.setError("Date must be in the future");
            editInstanceDateField.requestFocus();
            return false;
        }

        if (!weekdayFromDate.equalsIgnoreCase(parentClassScheduleDay)) {
            editInstanceDateField.setError("Date must match class schedule day (" + parentClassScheduleDay + ")");
            editInstanceDateField.requestFocus();
            return false;
        }

        if (extractFieldText(editInstructorNameField).isEmpty()) {
            editInstructorNameField.setError("Instructor name is required");
            editInstructorNameField.requestFocus();
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

    private void presentUpdateConfirmationDialog() {
        String confirmationSummary = constructUpdateSummary();
        
        new AlertDialog.Builder(this)
                .setTitle("Confirm Instance Updates")
                .setMessage(confirmationSummary)
                .setPositiveButton("Update Instance", (dialog, which) -> executeInstanceUpdate())
                .setNegativeButton("Review Changes", null)
                .show();
    }

    private String constructUpdateSummary() {
        return "Date: " + extractFieldText(editInstanceDateField) + "\n" +
               "Instructor: " + extractFieldText(editInstructorNameField) + "\n" +
               "Notes: " + (extractFieldText(editInstanceNotesField).isEmpty() ? 
                           "No additional notes" : extractFieldText(editInstanceNotesField));
    }

    private void executeInstanceUpdate() {
        SQLiteDatabase writableDatabase = databaseHelper.getWritableDatabase();
        ContentValues updatedValues = new ContentValues();
        
        updatedValues.put("date", extractFieldText(editInstanceDateField));
        updatedValues.put("teacher", extractFieldText(editInstructorNameField));
        updatedValues.put("comment", extractFieldText(editInstanceNotesField));

        int updateResult = writableDatabase.update(
            "instances", 
            updatedValues, 
            "id = ?", 
            new String[]{String.valueOf(classInstanceIdentifier)}
        );

        if (updateResult > 0) {
            displayUserMessage("Class instance updated successfully!");
            finish();
        } else {
            displayUserMessage("Failed to update instance. Please try again.");
        }
    }

    private String retrieveParentClassScheduleDay(int classId) {
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
