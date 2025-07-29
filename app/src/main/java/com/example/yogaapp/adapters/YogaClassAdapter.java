package com.example.yogaapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yogaapp.R;
import java.util.List;

import com.example.yogaapp.models.YogaClass;
import com.example.yogaapp.database.DatabaseHelper;
import com.example.yogaapp.activities.InstanceListActivity;
import com.example.yogaapp.activities.EditClassActivity;

public class YogaClassAdapter extends RecyclerView.Adapter<YogaClassAdapter.SessionViewHolder> {
    private Context appContext;
    private List<YogaClass> sessionsList;
    private DatabaseHelper dataHelper;

    public YogaClassAdapter(Context context, List<YogaClass> sessionsList, DatabaseHelper dataHelper) {
        this.appContext = context;
        this.sessionsList = sessionsList;
        this.dataHelper = dataHelper;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(appContext).inflate(R.layout.item_yoga_class, parent, false);
        return new SessionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        YogaClass currentSession = sessionsList.get(position);
        holder.bindSessionData(currentSession, position);
    }

    @Override
    public int getItemCount() {
        return sessionsList.size();
    }

    public class SessionViewHolder extends RecyclerView.ViewHolder {
        private TextView sessionTitle, sessionSchedule, sessionFee, sessionCapacity, sessionDuration, sessionTypeIcon;
        private ImageView viewInstancesButton, editSessionButton, deleteSessionButton;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            initializeViewElements();
        }

        private void initializeViewElements() {
            sessionTitle = itemView.findViewById(R.id.tvSessionTitle);
            sessionSchedule = itemView.findViewById(R.id.tvSessionSchedule);
            sessionFee = itemView.findViewById(R.id.tvSessionFee);
            sessionCapacity = itemView.findViewById(R.id.tvSessionCapacity);
            sessionDuration = itemView.findViewById(R.id.tvSessionDuration);
            sessionTypeIcon = itemView.findViewById(R.id.tvSessionTypeIcon);
            viewInstancesButton = itemView.findViewById(R.id.btnViewSessionInstances);
            editSessionButton = itemView.findViewById(R.id.btnEditSession);
            deleteSessionButton = itemView.findViewById(R.id.btnDeleteSession);
        }

        public void bindSessionData(YogaClass session, int position) {
            populateSessionInfo(session);
            setupActionButtons(session, position);
        }

        private void populateSessionInfo(YogaClass session) {
            sessionTitle.setText(session.type);
            sessionSchedule.setText(String.format("%s at %s", session.day, session.time));
            sessionFee.setText(String.format("£%.2f", session.price));
            sessionCapacity.setText(String.valueOf(session.capacity));
            sessionDuration.setText(session.duration);
            
            String firstLetter = session.type.length() > 0 ? 
                session.type.substring(0, 1).toUpperCase() : "W";
            sessionTypeIcon.setText(firstLetter);
        }

        private void setupActionButtons(YogaClass session, int position) {
            viewInstancesButton.setOnClickListener(v -> navigateToInstancesList(session));
            editSessionButton.setOnClickListener(v -> navigateToEditSession(session));
            deleteSessionButton.setOnClickListener(v -> confirmSessionDeletion(session, position));
        }

        private void navigateToInstancesList(YogaClass session) {
            Intent instancesIntent = new Intent(appContext, InstanceListActivity.class);
            instancesIntent.putExtra("classId", session.id);
            instancesIntent.putExtra("className", session.type);
            
            showUserFeedback("Opening session instances");
            appContext.startActivity(instancesIntent);
        }

        private void navigateToEditSession(YogaClass session) {
            try {
                Intent editIntent = new Intent(appContext, EditClassActivity.class);
                editIntent.putExtra("classId", session.id);
                appContext.startActivity(editIntent);
                showUserFeedback("Opening session editor");
            } catch (Exception e) {
                showUserFeedback("Unable to open session editor");
            }
        }

        private void confirmSessionDeletion(YogaClass session, int position) {
            new AlertDialog.Builder(appContext)
                    .setTitle("Remove Session")
                    .setMessage("This will permanently delete the session and all related instances. Continue?")
                    .setPositiveButton("Delete", (dialog, which) -> performSessionDeletion(session, position))
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void performSessionDeletion(YogaClass session, int position) {
            try {
                int deletionResult = dataHelper.deleteClassById(session.id);
                if (deletionResult > 0) {
                    sessionsList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, sessionsList.size());
                    showUserFeedback("Session removed successfully");
                } else {
                    showUserFeedback("Session removal failed");
                }
            } catch (Exception e) {
                showUserFeedback("Error during session removal");
            }
        }

        private void showUserFeedback(String message) {
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
        }
    }
}
