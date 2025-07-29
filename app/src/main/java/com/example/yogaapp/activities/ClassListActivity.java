package com.example.yogaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.yogaapp.R;
import java.util.ArrayList;
import java.util.List;

import com.example.yogaapp.models.YogaClass;
import com.example.yogaapp.database.DatabaseHelper;
import com.example.yogaapp.adapters.YogaClassAdapter;

public class ClassListActivity extends AppCompatActivity {
    private RecyclerView sessionRecyclerView;
    private List<YogaClass> sessionList;
    private List<YogaClass> filteredSessionList;
    private DatabaseHelper databaseHelper;
    private YogaClassAdapter sessionAdapter;
    private EditText searchSessionsField;
    private TextView emptyStateView;
    private FloatingActionButton addSessionFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);

        initializeComponents();
        setupRecyclerView();
        setupSearchFunctionality();
        setupFloatingActionButton();
        loadSessionData();
    }

    private void initializeComponents() {
        sessionRecyclerView = findViewById(R.id.sessionRecyclerView);
        searchSessionsField = findViewById(R.id.etSearchSessions);
        emptyStateView = findViewById(R.id.tvEmptyState);
        addSessionFab = findViewById(R.id.fabAddSession);
        databaseHelper = new DatabaseHelper(this);
        sessionList = new ArrayList<>();
        filteredSessionList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        sessionAdapter = new YogaClassAdapter(this, filteredSessionList, databaseHelper);
        sessionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessionRecyclerView.setAdapter(sessionAdapter);
    }

    private void setupSearchFunctionality() {
        searchSessionsField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSessionList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFloatingActionButton() {
        addSessionFab.setOnClickListener(v -> {
            Intent createSessionIntent = new Intent(ClassListActivity.this, AddClassActivity.class);
            startActivity(createSessionIntent);
        });
    }

    private void loadSessionData() {
        sessionList.clear();
        sessionList.addAll(databaseHelper.getAllClasses());
        filterSessionList("");
    }

    private void filterSessionList(String searchQuery) {
        filteredSessionList.clear();
        
        if (searchQuery.isEmpty()) {
            filteredSessionList.addAll(sessionList);
        } else {
            String lowerCaseQuery = searchQuery.toLowerCase();
            for (YogaClass session : sessionList) {
                if (session.type.toLowerCase().contains(lowerCaseQuery) ||
                    session.day.toLowerCase().contains(lowerCaseQuery) ||
                    session.time.toLowerCase().contains(lowerCaseQuery)) {
                    filteredSessionList.add(session);
                }
            }
        }
        
        sessionAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredSessionList.isEmpty()) {
            emptyStateView.setVisibility(TextView.VISIBLE);
            sessionRecyclerView.setVisibility(RecyclerView.GONE);
        } else {
            emptyStateView.setVisibility(TextView.GONE);
            sessionRecyclerView.setVisibility(RecyclerView.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSessionData();
    }
}
