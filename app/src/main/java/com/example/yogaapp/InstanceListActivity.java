package com.example.yogaapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class InstanceListActivity extends AppCompatActivity {

    int classId;
    ListView listView;
    Button btnAddInstance;
    List<ClassInstance> instanceList;
    DatabaseHelper dbHelper;
    ArrayAdapter<ClassInstance> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instance_list);

        classId = getIntent().getIntExtra("classId", -1);
        String className = getIntent().getStringExtra("className");

        ((TextView) findViewById(R.id.tvClassHeader)).setText("Instances for: " + className);

        listView = findViewById(R.id.listInstances);
        btnAddInstance = findViewById(R.id.btnAddInstance);
        dbHelper = new DatabaseHelper(this);

        instanceList = dbHelper.getInstancesForClass(classId);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, instanceList);
        listView.setAdapter(adapter);

        btnAddInstance.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddClassInstanceActivity.class);
            intent.putExtra("classId", classId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        instanceList.clear();
        instanceList.addAll(dbHelper.getInstancesForClass(classId));
        adapter.notifyDataSetChanged();
    }
}
