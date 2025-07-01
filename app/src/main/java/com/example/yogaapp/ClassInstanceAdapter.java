package com.example.yogaapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;

import java.util.List;

public class ClassInstanceAdapter extends BaseAdapter {
    private Context context;
    private List<ClassInstance> instances;
    private DatabaseHelper dbHelper;

    public ClassInstanceAdapter(Context context, List<ClassInstance> instances, DatabaseHelper dbHelper) {
        this.context = context;
        this.instances = instances;
        this.dbHelper = dbHelper;
    }

    @Override
    public int getCount() {
        return instances.size();
    }

    @Override
    public Object getItem(int position) {
        return instances.get(position);
    }

    @Override
    public long getItemId(int position) {
        return instances.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ClassInstance instance = instances.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_class_instance, parent, false);
        }

        TextView tvInfo = convertView.findViewById(R.id.tvInstanceInfo);
        ImageView btnEdit = convertView.findViewById(R.id.btnEditInstance);
        ImageView btnDelete = convertView.findViewById(R.id.btnDeleteInstance);

        tvInfo.setText(instance.toString());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditInstanceActivity.class);
            intent.putExtra("instanceId", instance.id);
            context.startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Instance")
                    .setMessage("Are you sure you want to delete this instance?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        int result = dbHelper.deleteInstanceById(instance.id);
                        if (result > 0) {
                            instances.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        convertView.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Instance Details")
                    .setMessage(
                            "Date: " + instance.date + "\n" +
                                    "Teacher: " + instance.teacher + "\n" +
                                    "Comment: " + (instance.comment == null ? "None" : instance.comment)
                    )
                    .setPositiveButton("OK", null)
                    .show();
        });

        return convertView;
    }


}
