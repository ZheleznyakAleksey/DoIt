package com.example.dolt.bottomnav.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dolt.databinding.FragmentSentTasksBinding;
import com.example.dolt.tasks.Task;
import com.example.dolt.tasks.TasksAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class SentTasksFragment extends Fragment {

    private FragmentSentTasksBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSentTasksBinding.inflate(inflater, container, false);

        loadMyTasks();

        return binding.getRoot();
    }
    public void loadMyTasks(){
        ArrayList<Task> outTasks = new ArrayList<>();

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String allTasksStr;
                if (snapshot.child("Tasks").child("tasks").getValue() != null) {
                    allTasksStr = Objects.requireNonNull(snapshot.child("Tasks").child("tasks").getValue()).toString();

                } else return;
                String[] allTasksIds = allTasksStr.split(",");
                ArrayList<String> outgoingTasks = new ArrayList<>();
                if (allTasksIds[0].length()<4) return;
                for (int i = 0; i < allTasksIds.length; i++) {
                    String str = allTasksIds[i];
                    str = str.substring(1, allTasksIds[i].length()-1);
                    if(i==0)
                        str = str.substring(0, allTasksIds[i].length()-2);
                    if (i==allTasksIds.length-1)
                        str = str.substring(0, allTasksIds[i].length()-3);
                    allTasksIds[i] = str;
                    String finalStr = str;
                    if (snapshot.child("Tasks").child(str).child("userFromId").getValue()!=null) {
                        if (Objects.requireNonNull(snapshot.child("Tasks").child(str).child("userFromId").getValue()).toString().equals(uid)) {
                            outgoingTasks.add(finalStr);
                        }
                    }
                }

                for (String taskId : outgoingTasks){
                    DataSnapshot taskSnapshot = snapshot.child("Tasks").child(taskId);

                    String taskText = Objects.requireNonNull(taskSnapshot.child("taskText").getValue()).toString();
                    String userFromId = Objects.requireNonNull(taskSnapshot.child("userFromId").getValue()).toString();
                    String userFrom = Objects.requireNonNull(snapshot.child("Users").child(userFromId).child("username").getValue()).toString();
                    String userToId = Objects.requireNonNull(taskSnapshot.child("userToId").getValue()).toString();
                    String userTo = Objects.requireNonNull(snapshot.child("Users").child(userToId).child("username").getValue()).toString();
                    String toOrFrom = "Кому:";
                    int degreeOfImportance  = ((Long) taskSnapshot.child("degreeOfImportance").getValue()).intValue();
                    String termDateTime = Objects.requireNonNull(taskSnapshot.child("termDateTime").getValue()).toString();
                    int taskStatus = Integer.parseInt(taskSnapshot.child("taskStatus").getValue().toString());
                    int isTaskCheck = Integer.parseInt(taskSnapshot.child("isTaskCheck").getValue().toString());

                    if (!userToId.equals(uid)) {
                        Task task = new Task(taskText, taskId, userFrom, userFromId, userTo, userToId, toOrFrom, taskStatus, degreeOfImportance, termDateTime, isTaskCheck);
                        outTasks.add(task);
                    }
                }
                outTasks.sort(Task::compareTo);

                binding.sentTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.sentTasksRecyclerView.setAdapter(new TasksAdapter(outTasks));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}