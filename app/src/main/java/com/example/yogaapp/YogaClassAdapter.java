package com.example.yogaapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;

public class YogaClassAdapter extends BaseAdapter {
    private Context context;
    private List<YogaClass> classList;
    private DatabaseHelper dbHelper;

    public YogaClassAdapter(Context context, List<YogaClass> classList, DatabaseHelper dbHelper) {
        this.context = context;
        this.classList = classList;
        this.dbHelper = dbHelper;
    }

    @Override
    public int getCount() {
        return classList.size();
    }

    @Override
    public Object getItem(int position) {
        return classList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return classList.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        YogaClass yogaClass = classList.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_yoga_class, parent, false);
        }

        TextView tvClassInfo = convertView.findViewById(R.id.tvClassInfo);
        ImageView btnEdit = convertView.findViewById(R.id.btnEdit);
        ImageView btnDelete = convertView.findViewById(R.id.btnDelete);
        ImageView btnViewInstances = convertView.findViewById(R.id.btnViewInstances);

        // Display info (customize as needed)
        tvClassInfo.setText(yogaClass.type);

        // View Instances
        btnViewInstances.setOnClickListener(v -> {
            Intent intent = new Intent(context, InstanceListActivity.class);
            intent.putExtra("classId", yogaClass.id);
            intent.putExtra("className", yogaClass.type);

            Toast.makeText(context, "Starting InstanceListActivity for ID " + yogaClass.id, Toast.LENGTH_SHORT).show();

            context.startActivity(intent);
        });


        // Edit Class
        btnEdit.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, EditClassActivity.class);
                intent.putExtra("classId", yogaClass.id);
                context.startActivity(intent);
                Toast.makeText(context, "Opening class to edit", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "Error: Unable to open class", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete Class
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Class")
                    .setMessage("Are you sure you want to delete this class and all its instances?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        try {
                            int deleted = dbHelper.deleteClassById(yogaClass.id);
                            if (deleted > 0) {
                                classList.remove(position);
                                notifyDataSetChanged();
                                Toast.makeText(context, "Class deleted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to delete class", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, "Error: Unable to delete class", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


        return convertView;
    }
}
