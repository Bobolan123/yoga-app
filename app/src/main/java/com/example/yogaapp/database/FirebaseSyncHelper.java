package com.example.yogaapp.database;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.*;

import com.example.yogaapp.models.YogaClass;
import com.example.yogaapp.models.ClassInstance;

public class FirebaseSyncHelper {
    private final FirebaseFirestore firestore;
    private final DatabaseHelper dbHelper;

    public FirebaseSyncHelper(Context context) {
        firestore = FirebaseFirestore.getInstance();
        dbHelper = new DatabaseHelper(context);
    }

    public interface SyncCallback {
        void onComplete();
        void onError(String errorMessage);
    }

    public void uploadAllClasses(SyncCallback callback) {
        List<YogaClass> classes = dbHelper.getAllClasses();
        Log.d("FirebaseSyncHelper", "Found " + classes.size() + " classes to sync");
        if (classes.isEmpty()) {
            Log.d("FirebaseSyncHelper", "No classes to sync, calling onComplete");
            callback.onComplete();
            return;
        }

        int[] completedOps = {0};
        int[] errorCount = {0};
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
                        syncInstancesForClass(classId, yogaClass.id, new SyncCallback() {
                            @Override
                            public void onComplete() {
                                completedOps[0]++;
                                if (completedOps[0] >= totalOps) {
                                    if (errorCount[0] > 0) {
                                        callback.onError("Sync completed with " + errorCount[0] + " errors");
                                    } else {
                                        callback.onComplete();
                                    }
                                }
                            }

                            @Override
                            public void onError(String errorMessage) {
                                errorCount[0]++;
                                completedOps[0]++;
                                if (completedOps[0] >= totalOps) {
                                    callback.onError("Sync completed with " + errorCount[0] + " errors");
                                }
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseSync", "Failed to upload class: " + classId, e);
                        errorCount[0]++;
                        completedOps[0]++;
                        if (completedOps[0] >= totalOps) {
                            callback.onError("Sync completed with " + errorCount[0] + " errors");
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
                    callback.onError("Failed to sync instances for class " + classId);
                });
    }
}
