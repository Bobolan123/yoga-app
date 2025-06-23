package com.example.yogaapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.ListView;

import java.util.List;
public class ClassListActivity extends AppCompatActivity {
    ListView listView;
    List<YogaClass> classList;
    DatabaseHelper dbHelper;
    YogaClassAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);

        listView = findViewById(R.id.classListView);
        dbHelper = new DatabaseHelper(this);
        classList = dbHelper.getAllClasses();

        adapter = new YogaClassAdapter(this, classList, dbHelper);
        listView.setAdapter(adapter);

        Button btnCreateNew = findViewById(R.id.btnCreateNew);
        btnCreateNew.setOnClickListener(v -> {
            Intent intent = new Intent(ClassListActivity.this, AddClassActivity.class);
            startActivity(intent);
        });

//        listView.setOnItemClickListener((parent, view, position, id) -> {
//            YogaClass selectedClass = classList.get(position);
//            Intent intent = new Intent(ClassListActivity.this, InstanceListActivity.class);
//            intent.putExtra("classId", selectedClass.id);
//            intent.putExtra("className", selectedClass.type);
//            startActivity(intent);
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        classList.clear();
        classList.addAll(dbHelper.getAllClasses());
        adapter.notifyDataSetChanged();
    }

}
