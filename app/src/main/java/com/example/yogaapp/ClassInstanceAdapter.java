package com.example.yogaapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClassInstanceAdapter extends RecyclerView.Adapter<ClassInstanceAdapter.InstanceViewHolder> {
    private Context appContext;
    private List<ClassInstance> instancesList;
    private DatabaseHelper dataHelper;

    public ClassInstanceAdapter(Context context, List<ClassInstance> instancesList, DatabaseHelper dataHelper) {
        this.appContext = context;
        this.instancesList = instancesList;
        this.dataHelper = dataHelper;
    }

    @NonNull
    @Override
    public InstanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(appContext).inflate(R.layout.item_class_instance, parent, false);
        return new InstanceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InstanceViewHolder holder, int position) {
        ClassInstance currentInstance = instancesList.get(position);
        holder.bindInstanceData(currentInstance, position);
    }

    @Override
    public int getItemCount() {
        return instancesList.size();
    }

    public class InstanceViewHolder extends RecyclerView.ViewHolder {
        private TextView instanceDate, instanceStatus, instanceTime, instanceParticipants, 
                        instanceNotes, instanceStatusIcon;
        private ImageView editInstanceButton, deleteInstanceButton;

        public InstanceViewHolder(@NonNull View itemView) {
            super(itemView);
            initializeViewElements();
        }

        private void initializeViewElements() {
            instanceDate = itemView.findViewById(R.id.tvInstanceDate);
            instanceStatus = itemView.findViewById(R.id.tvInstanceStatus);
            instanceTime = itemView.findViewById(R.id.tvInstanceTime);
            instanceParticipants = itemView.findViewById(R.id.tvInstanceParticipants);
            instanceNotes = itemView.findViewById(R.id.tvInstanceNotes);
            instanceStatusIcon = itemView.findViewById(R.id.tvInstanceStatusIcon);
            editInstanceButton = itemView.findViewById(R.id.btnEditInstance);
            deleteInstanceButton = itemView.findViewById(R.id.btnDeleteInstance);
        }

        public void bindInstanceData(ClassInstance instance, int position) {
            populateInstanceInfo(instance);
            setupActionButtons(instance, position);
            setupDetailView(instance);
        }

        private void populateInstanceInfo(ClassInstance instance) {
            instanceDate.setText(formatInstanceDate(instance.date));
            instanceStatus.setText(determineInstanceStatus(instance));
            instanceTime.setText(instance.teacher != null ? instance.teacher : "No instructor assigned");
            instanceParticipants.setText("Enrolled participants");
            
            if (instance.comment != null && !instance.comment.trim().isEmpty()) {
                instanceNotes.setText(instance.comment);
                instanceNotes.setVisibility(View.VISIBLE);
            } else {
                instanceNotes.setVisibility(View.GONE);
            }
            
            instanceStatusIcon.setText("●");
        }

        private String formatInstanceDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception e) {
                return dateString;
            }
        }

        private String determineInstanceStatus(ClassInstance instance) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date instanceDate = dateFormat.parse(instance.date);
                Date currentDate = new Date();
                
                if (instanceDate.before(currentDate)) {
                    return "Completed";
                } else if (instanceDate.equals(currentDate)) {
                    return "Today";
                } else {
                    return "Scheduled";
                }
            } catch (Exception e) {
                return "Scheduled";
            }
        }

        private void setupActionButtons(ClassInstance instance, int position) {
            editInstanceButton.setOnClickListener(v -> navigateToEditInstance(instance));
            deleteInstanceButton.setOnClickListener(v -> confirmInstanceDeletion(instance, position));
        }

        private void setupDetailView(ClassInstance instance) {
            itemView.setOnClickListener(v -> showInstanceDetails(instance));
        }

        private void navigateToEditInstance(ClassInstance instance) {
            Intent editIntent = new Intent(appContext, EditInstanceActivity.class);
            editIntent.putExtra("instanceId", instance.id);
            appContext.startActivity(editIntent);
        }

        private void confirmInstanceDeletion(ClassInstance instance, int position) {
            new AlertDialog.Builder(appContext)
                    .setTitle("Remove Instance")
                    .setMessage("This will permanently delete this session instance. Continue?")
                    .setPositiveButton("Delete", (dialog, which) -> performInstanceDeletion(instance, position))
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void performInstanceDeletion(ClassInstance instance, int position) {
            int deletionResult = dataHelper.deleteInstanceById(instance.id);
            if (deletionResult > 0) {
                instancesList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, instancesList.size());
                showUserFeedback("Instance removed successfully");
            } else {
                showUserFeedback("Instance removal failed");
            }
        }

        private void showInstanceDetails(ClassInstance instance) {
            String detailsMessage = "Date: " + instance.date + "\n" +
                                   "Instructor: " + (instance.teacher != null ? instance.teacher : "Not assigned") + "\n" +
                                   "Notes: " + (instance.comment != null && !instance.comment.trim().isEmpty() ? 
                                              instance.comment : "No additional notes");
            
            new AlertDialog.Builder(appContext)
                    .setTitle("Session Instance Details")
                    .setMessage(detailsMessage)
                    .setPositiveButton("Close", null)
                    .show();
        }

        private void showUserFeedback(String message) {
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
        }
    }
}
