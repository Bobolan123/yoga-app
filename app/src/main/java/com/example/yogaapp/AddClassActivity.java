package com.example.yogaapp;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddClassActivity extends AppCompatActivity {
    
    private AutoCompleteTextView scheduleDay;
    private TextInputEditText sessionTime, maxParticipants, sessionDuration, 
                             sessionFee, sessionCategory, sessionDescription;
    private MaterialButton createSessionButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        initializeComponents();
        setupScheduleDayDropdown();
        setupCreateSessionButton();
    }

    private void initializeComponents() {
        scheduleDay = findViewById(R.id.actvScheduleDay);
        sessionTime = findViewById(R.id.etSessionTime);
        maxParticipants = findViewById(R.id.etMaxParticipants);
        sessionDuration = findViewById(R.id.etSessionDuration);
        sessionFee = findViewById(R.id.etSessionFee);
        sessionCategory = findViewById(R.id.etSessionCategory);
        sessionDescription = findViewById(R.id.etSessionDescription);
        createSessionButton = findViewById(R.id.btnCreateSession);
        databaseHelper = new DatabaseHelper(this);
    }

    private void setupScheduleDayDropdown() {
        String[] weekDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, weekDays);
        scheduleDay.setAdapter(dayAdapter);
    }

    private void setupCreateSessionButton() {
        createSessionButton.setOnClickListener(v -> {
            if (validateSessionInputs()) {
                showConfirmationDialog();
            }
        });
    }

    private boolean validateSessionInputs() {
        if (getTextFromField(scheduleDay).isEmpty()) {
            displayError("Please select a schedule day");
            return false;
        }

        if (getTextFromField(sessionTime).isEmpty()) {
            sessionTime.setError("Session time is required");
            sessionTime.requestFocus();
            return false;
        }

        String participantsText = getTextFromField(maxParticipants);
        if (participantsText.isEmpty()) {
            maxParticipants.setError("Maximum participants is required");
            maxParticipants.requestFocus();
            return false;
        }

        try {
            int participants = Integer.parseInt(participantsText);
            if (participants <= 0) {
                maxParticipants.setError("Participants must be greater than 0");
                maxParticipants.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            maxParticipants.setError("Please enter a valid number");
            maxParticipants.requestFocus();
            return false;
        }

        if (getTextFromField(sessionDuration).isEmpty()) {
            sessionDuration.setError("Session duration is required");
            sessionDuration.requestFocus();
            return false;
        }

        String feeText = getTextFromField(sessionFee);
        if (feeText.isEmpty()) {
            sessionFee.setError("Session fee is required");
            sessionFee.requestFocus();
            return false;
        }

        try {
            float fee = Float.parseFloat(feeText);
            if (fee < 0) {
                sessionFee.setError("Fee cannot be negative");
                sessionFee.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            sessionFee.setError("Please enter a valid amount");
            sessionFee.requestFocus();
            return false;
        }

        if (getTextFromField(sessionCategory).isEmpty()) {
            sessionCategory.setError("Session category is required");
            sessionCategory.requestFocus();
            return false;
        }

        return true;
    }

    private void showConfirmationDialog() {
        String confirmationMessage = buildConfirmationMessage();
        
        new AlertDialog.Builder(this)
                .setTitle("Confirm Session Details")
                .setMessage(confirmationMessage)
                .setPositiveButton("Create Session", (dialog, which) -> saveSessionData())
                .setNegativeButton("Review", null)
                .show();
    }

    private String buildConfirmationMessage() {
        return "Schedule Day: " + getTextFromField(scheduleDay) + "\n" +
               "Time: " + getTextFromField(sessionTime) + "\n" +
               "Max Participants: " + getTextFromField(maxParticipants) + "\n" +
               "Duration: " + getTextFromField(sessionDuration) + "\n" +
               "Fee: £" + getTextFromField(sessionFee) + "\n" +
               "Category: " + getTextFromField(sessionCategory) + "\n" +
               "Description: " + getTextFromField(sessionDescription);
    }

    private void saveSessionData() {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        ContentValues sessionValues = new ContentValues();
        
        sessionValues.put("day", getTextFromField(scheduleDay));
        sessionValues.put("time", getTextFromField(sessionTime));
        sessionValues.put("capacity", Integer.parseInt(getTextFromField(maxParticipants)));
        sessionValues.put("duration", getTextFromField(sessionDuration));
        sessionValues.put("price", Float.parseFloat(getTextFromField(sessionFee)));
        sessionValues.put("type", getTextFromField(sessionCategory));
        sessionValues.put("description", getTextFromField(sessionDescription));

        long insertResult = database.insert(DatabaseHelper.TABLE_NAME, null, sessionValues);
        
        if (insertResult != -1) {
            displaySuccess("Wellness session created successfully!");
            finish();
        } else {
            displayError("Failed to create session. Please try again.");
        }
    }

    private String getTextFromField(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private String getTextFromField(AutoCompleteTextView field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void displayError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void displaySuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
