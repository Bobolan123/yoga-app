package com.example.yogaapp;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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

        int[] completedOps = {0};
        int totalOps = classes.size();

        for (YogaClass yogaClass : classes) {
            String classId = String.valueOf(yogaClass.id);

            Map<String, Object> classData = new HashMap<>();
            classData.put("type", yogaClass.type);
            classData.put("day", yogaClass.day);
            classData.put("time", yogaClass.time);
            classData.put("duration", yogaClass.duration);
            classData.put("description", yogaClass.description);
            classData.put("capacity", yogaClass.capacity);
            classData.put("price", yogaClass.price);

            // Upload or update class
            firestore.collection("classes")
                    .document(classId)
                    .set(classData)
                    .addOnSuccessListener(aVoid -> {
                        // Then sync instances
                        syncInstancesForClass(classId, yogaClass.id, () -> {
                            completedOps[0]++;
                            if (completedOps[0] >= totalOps) {
                                callback.onComplete();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseSync", "Failed to upload class: " + classId, e);
                        completedOps[0]++;
                        if (completedOps[0] >= totalOps) {
                            callback.onComplete();
                        }
                    });
        }
    }

    private void syncInstancesForClass(String classId, int localClassId, SyncCallback callback) {
        List<ClassInstance> localInstances = dbHelper.getInstancesForClass(localClassId);
        Set<String> localIds = new HashSet<>();
        for (ClassInstance instance : localInstances) {
            localIds.add(String.valueOf(instance.id));
        }

        firestore.collection("classes")
                .document(classId)
                .collection("instances")
                .get()
                .addOnSuccessListener(snapshot -> {
                    Set<String> remoteIds = new HashSet<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        remoteIds.add(doc.getId());
                    }

                    // 1. Upload/update local instances
                    for (ClassInstance instance : localInstances) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("date", instance.date);
                        data.put("teacher", instance.teacher);
                        data.put("comment", instance.comment);

                        firestore.collection("classes")
                                .document(classId)
                                .collection("instances")
                                .document(String.valueOf(instance.id))
                                .set(data);
                    }

                    // 2. Delete remote instances not in local DB
                    for (String remoteId : remoteIds) {
                        if (!localIds.contains(remoteId)) {
                            firestore.collection("classes")
                                    .document(classId)
                                    .collection("instances")
                                    .document(remoteId)
                                    .delete();
                        }
                    }

                    callback.onComplete();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseSync", "Failed to fetch remote instances for classId=" + classId, e);
                    callback.onComplete();
                });
    }
}
