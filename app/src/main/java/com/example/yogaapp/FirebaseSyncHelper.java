package com.example.yogaapp;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class FirebaseSyncHelper {
    private final FirebaseFirestore firestore;
    private final DatabaseHelper dbHelper;

    public FirebaseSyncHelper(Context context) {
        firestore = FirebaseFirestore.getInstance();
        dbHelper = new DatabaseHelper(context);
    }

    public interface SyncCallback {
        void onComplete();
    }

    public void uploadAllClasses(SyncCallback callback) {
        List<YogaClass> classes = dbHelper.getAllClasses();
        if (classes.isEmpty()) {
            callback.onComplete();
            return;
        }

        List<ClassInstance> pendingInstances = new ArrayList<>();
        for (YogaClass c : classes) {
            pendingInstances.addAll(dbHelper.getInstancesForClass(c.id));
        }

        int totalOps = classes.size() + pendingInstances.size();
        int[] completedOps = {0};

        for (YogaClass yogaClass : classes) {
            Map<String, Object> classData = new HashMap<>();
            classData.put("type", yogaClass.type);
            classData.put("day", yogaClass.day);

            firestore.collection("classes")
                    .document(String.valueOf(yogaClass.id))
                    .set(classData)
                    .addOnSuccessListener(aVoid -> checkCompletion(callback, completedOps, totalOps))
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseSync", "Class upload failed", e);
                        checkCompletion(callback, completedOps, totalOps);
                    });

            List<ClassInstance> instances = dbHelper.getInstancesForClass(yogaClass.id);
            for (ClassInstance instance : instances) {
                Map<String, Object> instData = new HashMap<>();
                instData.put("date", instance.date);
                instData.put("teacher", instance.teacher);
                instData.put("comment", instance.comment);

                firestore.collection("classes")
                        .document(String.valueOf(yogaClass.id))
                        .collection("instances")
                        .add(instData)
                        .addOnSuccessListener(docRef -> checkCompletion(callback, completedOps, totalOps))
                        .addOnFailureListener(e -> {
                            Log.e("FirebaseSync", "Instance upload failed", e);
                            checkCompletion(callback, completedOps, totalOps);
                        });
            }
        }
    }

    private void checkCompletion(SyncCallback callback, int[] completedOps, int totalOps) {
        completedOps[0]++;
        if (completedOps[0] >= totalOps) {
            callback.onComplete();
        }
    }
}
