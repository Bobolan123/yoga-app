package com.example.yogaapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditClassActivity extends AppCompatActivity {

    Spinner spinnerDay;
    EditText etTime, etCapacity, etDuration, etPrice, etType, etDescription;
    Button btnUpdate;

    DatabaseHelper dbHelper;
    int classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class);

        dbHelper = new DatabaseHelper(this);
        classId = getIntent().getIntExtra("classId", -1);

        spinnerDay = findViewById(R.id.spinnerDay);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(adapter);

        etTime = findViewById(R.id.etTime);
        etCapacity = findViewById(R.id.etCapacity);
        etDuration = findViewById(R.id.etDuration);
        etPrice = findViewById(R.id.etPrice);
        etType = findViewById(R.id.etType);
        etDescription = findViewById(R.id.etDescription);
        btnUpdate = findViewById(R.id.btnUpdate);


        loadClassDetails();

        btnUpdate.setOnClickListener(v -> {
            if (validateInputs()) {
                updateClass();
            } else {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadClassDetails() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME + " WHERE id=?",
                new String[]{String.valueOf(classId)});

        if (cursor.moveToFirst()) {
            int colDay = cursor.getColumnIndex("day");
            int colTime = cursor.getColumnIndex("time");
            int colCapacity = cursor.getColumnIndex("capacity");
            int colDuration = cursor.getColumnIndex("duration");
            int colPrice = cursor.getColumnIndex("price");
            int colType = cursor.getColumnIndex("type");
            int colDescription = cursor.getColumnIndex("description");

            if (colDay >= 0) {
                String dayFromDb = cursor.getString(colDay);
                String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                for (int i = 0; i < days.length; i++) {
                    if (days[i].equalsIgnoreCase(dayFromDb)) {
                        spinnerDay.setSelection(i);
                        break;
                    }
                }
            }
            if (colTime >= 0) etTime.setText(cursor.getString(colTime));
            if (colCapacity >= 0) etCapacity.setText(String.valueOf(cursor.getInt(colCapacity)));
            if (colDuration >= 0) etDuration.setText(cursor.getString(colDuration));
            if (colPrice >= 0) etPrice.setText(String.valueOf(cursor.getFloat(colPrice)));
            if (colType >= 0) etType.setText(cursor.getString(colType));
            if (colDescription >= 0) etDescription.setText(cursor.getString(colDescription));
        }

        cursor.close();
    }

    private boolean validateInputs() {
        return spinnerDay.getSelectedItemPosition() != Spinner.INVALID_POSITION &&
                !etTime.getText().toString().isEmpty() &&
                !etCapacity.getText().toString().isEmpty() &&
                !etDuration.getText().toString().isEmpty() &&
                !etPrice.getText().toString().isEmpty() &&
                !etType.getText().toString().isEmpty();
    }

    private void updateClass() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("day", spinnerDay.getSelectedItem().toString());
        values.put("time", etTime.getText().toString());
        values.put("capacity", Integer.parseInt(etCapacity.getText().toString()));
        values.put("duration", etDuration.getText().toString());
        values.put("price", Float.parseFloat(etPrice.getText().toString()));
        values.put("type", etType.getText().toString());
        values.put("description", etDescription.getText().toString());

        int result = db.update(DatabaseHelper.TABLE_NAME, values, "id=?", new String[]{String.valueOf(classId)});
        if (result > 0) {
            Toast.makeText(this, "Class updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update class", Toast.LENGTH_SHORT).show();
        }
    }
}
