package com.example.dolt.bottomnav.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dolt.databinding.FragmentMyTasksBinding;
import com.example.dolt.tasks.Task;
import com.example.dolt.tasks.TasksAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MyTasksFragment extends Fragment {

    private FragmentMyTasksBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyTasksBinding.inflate(inflater, container, false);

        loadMyTasks();

        return binding.getRoot();
    }
    public void loadMyTasks(){
        ArrayList<Task> myTasks = new ArrayList<>();

        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String allTasksStr;
                if (snapshot.child("Tasks").child("tasks").getValue() != null) {
                    allTasksStr = Objects.requireNonNull(snapshot.child("Tasks").child("tasks").getValue()).toString();
                } else {
                    binding.textView8.setVisibility(View.VISIBLE);
                    return;
                }
                String[] allTasksIds = allTasksStr.split(",");
                ArrayList<String> mytasks = new ArrayList<>();
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
                    if (snapshot.child("Tasks").child(str).child("userToId").getValue()!=null) {
                        if (Objects.requireNonNull(snapshot.child("Tasks").child(str).child("userToId").getValue()).toString().equals(uid)) {
                            mytasks.add(finalStr);
                        }
                    }
                }

                for (String taskId : mytasks){
                    DataSnapshot taskSnapshot = snapshot.child("Tasks").child(taskId);

                    String taskText = Objects.requireNonNull(taskSnapshot.child("taskText").getValue()).toString();
                    String userFromId = Objects.requireNonNull(taskSnapshot.child("userFromId").getValue()).toString();
                    String userFrom = Objects.requireNonNull(snapshot.child("Users").child(userFromId).child("username").getValue()).toString();
                    String userToId = Objects.requireNonNull(taskSnapshot.child("userToId").getValue()).toString();
                    String userTo = Objects.requireNonNull(snapshot.child("Users").child(userToId).child("username").getValue()).toString();
                    String toOrFrom = "От:";
                    int degreeOfImportance  = ((Long) taskSnapshot.child("degreeOfImportance").getValue()).intValue();
                    String termDateTime = Objects.requireNonNull(taskSnapshot.child("termDateTime").getValue()).toString();
                    int taskStatus = Integer.parseInt(taskSnapshot.child("taskStatus").getValue().toString());
                    int isTaskCheck = Integer.parseInt(taskSnapshot.child("isTaskCheck").getValue().toString());

                    if (taskStatus!=-1) {
                        if (userFromId.equals(userToId)) {
                            Task task = new Task(taskText, taskId, userFrom, userFromId, userTo, userToId, toOrFrom, taskStatus, degreeOfImportance, termDateTime, isTaskCheck);
                            myTasks.add(task);
                        }
                    }
                }
                myTasks.sort(Task::compareTo);

                if (!myTasks.isEmpty())
                    binding.textView8.setVisibility(View.GONE);

                binding.myTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.myTasksRecyclerView.setAdapter(new TasksAdapter(myTasks));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}