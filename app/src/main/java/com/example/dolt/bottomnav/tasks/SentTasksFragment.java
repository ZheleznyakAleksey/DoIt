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
import com.example.dolt.utils.DatabaseTasks;

import java.util.ArrayList;

public class SentTasksFragment extends Fragment {

    private FragmentSentTasksBinding binding;
    private DatabaseTasks databaseTasks;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSentTasksBinding.inflate(inflater, container, false);

        loadSentTasks();

        return binding.getRoot();
    }
    public void loadSentTasks(){
        databaseTasks = new DatabaseTasks(this.getContext());
        databaseTasks.openDatabase();

        ArrayList<Task> dbTasks = databaseTasks.getAllTasks("sent");
        dbTasks.sort(Task::compareTo);

        databaseTasks.close();
        binding.sentTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.sentTasksRecyclerView.setAdapter(new TasksAdapter(dbTasks));
        if(dbTasks.isEmpty()){
            binding.textView8.setVisibility(View.VISIBLE);
        }
    }
}