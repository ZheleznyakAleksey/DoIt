package com.example.dolt.tasksParent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dolt.R;
import com.example.dolt.tasks.TasksAdapter;

import java.util.ArrayList;

public class TasksParentAdapter extends RecyclerView.Adapter<TasksParentViewHolder> {

    private ArrayList<TasksParent> tasksParent = new ArrayList<>();

    public TasksParentAdapter(ArrayList<TasksParent> tasksParent) {
        this.tasksParent = tasksParent;
    }

    @NonNull
    @Override
    public TasksParentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tasks_parent_layout, parent, false);
        return new TasksParentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TasksParentViewHolder holder, int position) {

        TasksAdapter tasksAdapter = new TasksAdapter(tasksParent.get(position).getTasks());
        holder.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.tasksRecyclerView.setAdapter(tasksAdapter);
    }

    @Override
    public int getItemCount() {
        return tasksParent.size();
    }
}
