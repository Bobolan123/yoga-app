package com.example.yogaapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class InstanceListActivity extends AppCompatActivity {

    private int classId;
    private ListView listView;
    private Button btnAddInstance;
    private List<ClassInstance> instanceList;
    private DatabaseHelper dbHelper;
    private ClassInstanceAdapter adapter;
    private EditText etSearch;
    private List<ClassInstance> allInstances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instance_list);

        // Get extras from intent
        classId = getIntent().getIntExtra("classId", -1);
        String className = getIntent().getStringExtra("className");

        if (classId == -1) {
            Toast.makeText(this, "Invalid class ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set header
        TextView tvHeader = findViewById(R.id.tvClassHeader);
        tvHeader.setText("Instances for: " + className);

        listView = findViewById(R.id.listInstances);
        btnAddInstance = findViewById(R.id.btnAddInstance);
        dbHelper = new DatabaseHelper(this);

        // ✅ Safe list and adapter setup
        instanceList = new ArrayList<>();
        adapter = new ClassInstanceAdapter(this, instanceList, dbHelper);
        listView.setAdapter(adapter);

        // Add new instance button
        btnAddInstance.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddClassInstanceActivity.class);
            intent.putExtra("classId", classId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh data safely
        instanceList.clear();
        instanceList.addAll(dbHelper.getInstancesForClass(classId));
        adapter.notifyDataSetChanged();
    }


}
