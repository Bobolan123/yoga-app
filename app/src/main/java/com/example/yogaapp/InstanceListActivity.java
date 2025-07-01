package com.example.yogaapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.*;

public class InstanceListActivity extends AppCompatActivity {

    private int classId;
    private ListView listView;
    private Button btnAddInstance;
    private EditText etSearch;
    private List<ClassInstance> instanceList;
    private List<ClassInstance> allInstances;
    private DatabaseHelper dbHelper;
    private ClassInstanceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instance_list);

        classId = getIntent().getIntExtra("classId", -1);
        String className = getIntent().getStringExtra("className");

        if (classId == -1) {
            Toast.makeText(this, "Invalid class ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvHeader = findViewById(R.id.tvClassHeader);
        tvHeader.setText("Instances for: " + className);

        listView = findViewById(R.id.listInstances);
        btnAddInstance = findViewById(R.id.btnAddInstance);
        etSearch = findViewById(R.id.etSearchInstance);

        dbHelper = new DatabaseHelper(this);
        allInstances = new ArrayList<>(dbHelper.getInstancesForClass(classId));

        instanceList = new ArrayList<>(allInstances);
        adapter = new ClassInstanceAdapter(this, instanceList, dbHelper);
        listView.setAdapter(adapter);

        btnAddInstance.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddClassInstanceActivity.class);
            intent.putExtra("classId", classId);
            startActivity(intent);
        });

        // 🔍 Live search listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterInstances(s.toString().trim());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        allInstances = dbHelper.getInstancesForClass(classId);
        filterInstances(etSearch.getText().toString());
    }

    private void filterInstances(String query) {
        List<ClassInstance> filtered = dbHelper.searchInstances(classId, query);

        // Extra filter for day of week if the query matches a weekday
        if (query.matches("(?i)Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday")) {
            filtered = filterByWeekday(filtered, query);
        }

        instanceList.clear();
        instanceList.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    private List<ClassInstance> filterByWeekday(List<ClassInstance> input, String weekday) {
        List<ClassInstance> result = new ArrayList<>();
        for (ClassInstance instance : input) {
            if (getDayOfWeek(instance.date).equalsIgnoreCase(weekday)) {
                result.add(instance);
            }
        }
        return result;
    }

    private String getDayOfWeek(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(dateStr));
            return cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        } catch (Exception e) {
            return "";
        }
    }
}
