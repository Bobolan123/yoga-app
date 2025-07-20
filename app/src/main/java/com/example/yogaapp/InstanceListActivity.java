package com.example.yogaapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.*;

public class InstanceListActivity extends AppCompatActivity {

    private int sessionClassId;
    private RecyclerView instanceRecyclerView;
    private FloatingActionButton addInstanceFab;
    private EditText searchInstancesField;
    private TextView emptyStateView;
    private TextView headerTextView;
    private List<ClassInstance> instancesList;
    private List<ClassInstance> filteredInstancesList;
    private DatabaseHelper databaseHelper;
    private ClassInstanceAdapter instanceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instance_list);

        sessionClassId = getIntent().getIntExtra("classId", -1);
        String sessionClassName = getIntent().getStringExtra("className");

        if (sessionClassId == -1) {
            showUserFeedback("Invalid session ID");
            finish();
            return;
        }

        initializeComponents();
        setupHeader(sessionClassName);
        setupRecyclerView();
        setupSearchFunctionality();
        setupFloatingActionButton();
        loadInstanceData();
    }

    private void initializeComponents() {
        headerTextView = findViewById(R.id.tvClassHeader);
        instanceRecyclerView = findViewById(R.id.instanceRecyclerView);
        searchInstancesField = findViewById(R.id.etSearchInstance);
        emptyStateView = findViewById(R.id.tvEmptyState);
        addInstanceFab = findViewById(R.id.fabAddInstance);
        databaseHelper = new DatabaseHelper(this);
        instancesList = new ArrayList<>();
        filteredInstancesList = new ArrayList<>();
    }

    private void setupHeader(String sessionName) {
        headerTextView.setText("Instances: " + (sessionName != null ? sessionName : "Session"));
    }

    private void setupRecyclerView() {
        instanceAdapter = new ClassInstanceAdapter(this, filteredInstancesList, databaseHelper);
        instanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        instanceRecyclerView.setAdapter(instanceAdapter);
    }

    private void setupSearchFunctionality() {
        searchInstancesField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterInstanceData(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFloatingActionButton() {
        addInstanceFab.setOnClickListener(v -> {
            Intent createInstanceIntent = new Intent(this, AddClassInstanceActivity.class);
            createInstanceIntent.putExtra("classId", sessionClassId);
            startActivity(createInstanceIntent);
        });
    }

    private void loadInstanceData() {
        instancesList.clear();
        instancesList.addAll(databaseHelper.getInstancesForClass(sessionClassId));
        filterInstanceData("");
    }

    private void filterInstanceData(String searchQuery) {
        filteredInstancesList.clear();

        if (searchQuery.isEmpty()) {
            filteredInstancesList.addAll(instancesList);
        } else {
            List<ClassInstance> searchResults = databaseHelper.searchInstances(sessionClassId, searchQuery);
            
            if (isWeekdayQuery(searchQuery)) {
                searchResults = filterInstancesByWeekday(searchResults, searchQuery);
            }
            
            filteredInstancesList.addAll(searchResults);
        }

        instanceAdapter.notifyDataSetChanged();
        updateEmptyStateVisibility();
    }

    private boolean isWeekdayQuery(String query) {
        return query.matches("(?i)Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday");
    }

    private List<ClassInstance> filterInstancesByWeekday(List<ClassInstance> instances, String weekdayName) {
        List<ClassInstance> weekdayFiltered = new ArrayList<>();
        
        for (ClassInstance instance : instances) {
            String instanceWeekday = extractWeekdayFromDate(instance.date);
            if (instanceWeekday.equalsIgnoreCase(weekdayName)) {
                weekdayFiltered.add(instance);
            }
        }
        
        return weekdayFiltered;
    }

    private String extractWeekdayFromDate(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateFormat.parse(dateString));
            return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        } catch (Exception e) {
            return "";
        }
    }

    private void updateEmptyStateVisibility() {
        if (filteredInstancesList.isEmpty()) {
            emptyStateView.setVisibility(TextView.VISIBLE);
            instanceRecyclerView.setVisibility(RecyclerView.GONE);
        } else {
            emptyStateView.setVisibility(TextView.GONE);
            instanceRecyclerView.setVisibility(RecyclerView.VISIBLE);
        }
    }

    private void showUserFeedback(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInstanceData();
    }
}
