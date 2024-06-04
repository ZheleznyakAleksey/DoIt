package com.example.dolt.tasksParent;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dolt.R;

public class TasksParentViewHolder extends RecyclerView.ViewHolder {

    RecyclerView tasksRecyclerView;

    public TasksParentViewHolder(@NonNull View itemView) {
        super(itemView);

        tasksRecyclerView = itemView.findViewById(R.id.tasksRecyclerView);
    }
}
