package com.example.yogaapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import androidx.appcompat.app.AppCompatActivity;

public class AddClassActivity extends AppCompatActivity {
    EditText etTime, etCapacity, etDuration, etPrice, etType, etDescription;
    Button btnSave;
    Spinner spinnerDay;


    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        dbHelper = new DatabaseHelper(this);

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
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveData();
            }
        });
    }

    private boolean validateInputs() {
        return spinnerDay.getSelectedItemPosition() != AdapterView.INVALID_POSITION &&
                !etTime.getText().toString().isEmpty() &&
                !etCapacity.getText().toString().isEmpty() &&
                !etDuration.getText().toString().isEmpty() &&
                !etPrice.getText().toString().isEmpty() &&
                !etType.getText().toString().isEmpty();
    }

    private void saveData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("day", spinnerDay.getSelectedItem().toString());
        values.put("time", etTime.getText().toString());
        values.put("capacity", Integer.parseInt(etCapacity.getText().toString()));
        values.put("duration", etDuration.getText().toString());
        values.put("price", Float.parseFloat(etPrice.getText().toString()));
        values.put("type", etType.getText().toString());
        values.put("description", etDescription.getText().toString());

        long result = db.insert(DatabaseHelper.TABLE_NAME, null, values);
        if (result != -1) {
            Toast.makeText(this, "Class Saved!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save!", Toast.LENGTH_SHORT).show();
        }
    }
}
