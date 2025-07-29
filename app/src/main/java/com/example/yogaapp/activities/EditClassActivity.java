package com.example.yogaapp.activities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.yogaapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.yogaapp.database.DatabaseHelper;

public class EditClassActivity extends AppCompatActivity {

    private AutoCompleteTextView editScheduleDayDropdown;
    private TextInputEditText editClassTime, editClassCapacity, editClassDuration, 
                             editClassFee, editClassType, editClassDescription;
    private MaterialButton updateClassButton;
    private DatabaseHelper databaseHelper;
    private int yogaClassIdentifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class);

        initializeActivityComponents();
        retrieveClassIdentifier();
        setupWeekdayDropdownOptions();
        populateExistingClassData();
        configureUpdateButtonBehavior();
    }

    private void initializeActivityComponents() {
        editScheduleDayDropdown = findViewById(R.id.actvEditScheduleDay);
        editClassTime = findViewById(R.id.etEditClassTime);
        editClassCapacity = findViewById(R.id.etEditClassCapacity);
        editClassDuration = findViewById(R.id.etEditClassDuration);
        editClassFee = findViewById(R.id.etEditClassFee);
        editClassType = findViewById(R.id.etEditClassType);
        editClassDescription = findViewById(R.id.etEditClassDescription);
        updateClassButton = findViewById(R.id.btnUpdateClass);
        databaseHelper = new DatabaseHelper(this);
    }

    private void retrieveClassIdentifier() {
        yogaClassIdentifier = getIntent().getIntExtra("classId", -1);
        
        if (yogaClassIdentifier == -1) {
            displayUserMessage("Invalid class identifier provided");
            finish();
        }
    }

    private void setupWeekdayDropdownOptions() {
        String[] weekdayOptions = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayAdapter<String> weekdayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, weekdayOptions);
        editScheduleDayDropdown.setAdapter(weekdayAdapter);
    }

    private void populateExistingClassData() {
        SQLiteDatabase readableDatabase = databaseHelper.getReadableDatabase();
        Cursor classDataCursor = readableDatabase.rawQuery(
            "SELECT * FROM " + DatabaseHelper.TABLE_NAME + " WHERE id=?",
            new String[]{String.valueOf(yogaClassIdentifier)}
        );

        if (classDataCursor.moveToFirst()) {
            extractAndPopulateClassFields(classDataCursor);
        } else {
            displayUserMessage("Class data not found");
            finish();
        }

        classDataCursor.close();
    }

    private void extractAndPopulateClassFields(Cursor cursor) {
        int dayColumnIndex = cursor.getColumnIndex("day");
        int timeColumnIndex = cursor.getColumnIndex("time");
        int capacityColumnIndex = cursor.getColumnIndex("capacity");
        int durationColumnIndex = cursor.getColumnIndex("duration");
        int priceColumnIndex = cursor.getColumnIndex("price");
        int typeColumnIndex = cursor.getColumnIndex("type");
        int descriptionColumnIndex = cursor.getColumnIndex("description");

        if (dayColumnIndex >= 0) {
            String selectedDay = cursor.getString(dayColumnIndex);
            editScheduleDayDropdown.setText(selectedDay, false);
        }
        if (timeColumnIndex >= 0) {
            editClassTime.setText(cursor.getString(timeColumnIndex));
        }
        if (capacityColumnIndex >= 0) {
            editClassCapacity.setText(String.valueOf(cursor.getInt(capacityColumnIndex)));
        }
        if (durationColumnIndex >= 0) {
            editClassDuration.setText(cursor.getString(durationColumnIndex));
        }
        if (priceColumnIndex >= 0) {
            editClassFee.setText(String.valueOf(cursor.getFloat(priceColumnIndex)));
        }
        if (typeColumnIndex >= 0) {
            editClassType.setText(cursor.getString(typeColumnIndex));
        }
        if (descriptionColumnIndex >= 0) {
            editClassDescription.setText(cursor.getString(descriptionColumnIndex));
        }
    }

    private void configureUpdateButtonBehavior() {
        updateClassButton.setOnClickListener(v -> {
            if (performInputValidation()) {
                presentUpdateConfirmationDialog();
            }
        });
    }

    private boolean performInputValidation() {
        if (extractFieldText(editScheduleDayDropdown).isEmpty()) {
            displayValidationError("Please select a schedule day");
            return false;
        }

        if (extractFieldText(editClassTime).isEmpty()) {
            editClassTime.setError("Class time is required");
            editClassTime.requestFocus();
            return false;
        }

        String capacityText = extractFieldText(editClassCapacity);
        if (capacityText.isEmpty()) {
            editClassCapacity.setError("Class capacity is required");
            editClassCapacity.requestFocus();
            return false;
        }

        try {
            int capacityValue = Integer.parseInt(capacityText);
            if (capacityValue <= 0) {
                editClassCapacity.setError("Capacity must be greater than 0");
                editClassCapacity.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            editClassCapacity.setError("Please enter a valid number");
            editClassCapacity.requestFocus();
            return false;
        }

        if (extractFieldText(editClassDuration).isEmpty()) {
            editClassDuration.setError("Class duration is required");
            editClassDuration.requestFocus();
            return false;
        }

        String feeText = extractFieldText(editClassFee);
        if (feeText.isEmpty()) {
            editClassFee.setError("Class fee is required");
            editClassFee.requestFocus();
            return false;
        }

        try {
            float feeValue = Float.parseFloat(feeText);
            if (feeValue < 0) {
                editClassFee.setError("Fee cannot be negative");
                editClassFee.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            editClassFee.setError("Please enter a valid amount");
            editClassFee.requestFocus();
            return false;
        }

        if (extractFieldText(editClassType).isEmpty()) {
            editClassType.setError("Class type is required");
            editClassType.requestFocus();
            return false;
        }

        return true;
    }

    private void presentUpdateConfirmationDialog() {
        String confirmationSummary = constructUpdateSummary();
        
        new AlertDialog.Builder(this)
                .setTitle("Confirm Class Updates")
                .setMessage(confirmationSummary)
                .setPositiveButton("Update Class", (dialog, which) -> executeClassUpdate())
                .setNegativeButton("Review Changes", null)
                .show();
    }

    private String constructUpdateSummary() {
        return "Schedule Day: " + extractFieldText(editScheduleDayDropdown) + "\n" +
               "Time: " + extractFieldText(editClassTime) + "\n" +
               "Capacity: " + extractFieldText(editClassCapacity) + "\n" +
               "Duration: " + extractFieldText(editClassDuration) + "\n" +
               "Fee: £" + extractFieldText(editClassFee) + "\n" +
               "Type: " + extractFieldText(editClassType) + "\n" +
               "Description: " + extractFieldText(editClassDescription);
    }

    private void executeClassUpdate() {
        SQLiteDatabase writableDatabase = databaseHelper.getWritableDatabase();
        ContentValues updatedValues = new ContentValues();
        
        updatedValues.put("day", extractFieldText(editScheduleDayDropdown));
        updatedValues.put("time", extractFieldText(editClassTime));
        updatedValues.put("capacity", Integer.parseInt(extractFieldText(editClassCapacity)));
        updatedValues.put("duration", extractFieldText(editClassDuration));
        updatedValues.put("price", Float.parseFloat(extractFieldText(editClassFee)));
        updatedValues.put("type", extractFieldText(editClassType));
        updatedValues.put("description", extractFieldText(editClassDescription));

        int updateResult = writableDatabase.update(
            DatabaseHelper.TABLE_NAME, 
            updatedValues, 
            "id=?", 
            new String[]{String.valueOf(yogaClassIdentifier)}
        );

        if (updateResult > 0) {
            displayUserMessage("Yoga class updated successfully!");
            finish();
        } else {
            displayUserMessage("Failed to update class. Please try again.");
        }
    }

    private String extractFieldText(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private String extractFieldText(AutoCompleteTextView field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void displayUserMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void displayValidationError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
