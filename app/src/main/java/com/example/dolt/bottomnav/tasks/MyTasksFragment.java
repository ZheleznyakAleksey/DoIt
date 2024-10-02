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
import com.example.dolt.utils.DatabaseTasks;

import java.util.ArrayList;

public class MyTasksFragment extends Fragment {

    private FragmentMyTasksBinding binding;
    private DatabaseTasks databaseTasks;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyTasksBinding.inflate(inflater, container, false);

        loadMyTasks();

        return binding.getRoot();
    }
    public void loadMyTasks(){
        databaseTasks = new DatabaseTasks(this.getContext());
        databaseTasks.openDatabase();

        ArrayList<Task> dbTasks = databaseTasks.getAllTasks("my");
        dbTasks.sort(Task::compareTo);

        databaseTasks.close();
        binding.myTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.myTasksRecyclerView.setAdapter(new TasksAdapter(dbTasks));
        if(dbTasks.isEmpty()){
            binding.textView8.setVisibility(View.VISIBLE);
        }
    }
}