package com.example.dolt.tasks;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dolt.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    CheckBox todoCheckbox;
    TextView user, toOrFrom, termDateTime, taskText, taskStatus, taskStatusText, textChecking;
    FloatingActionButton editImage;
    ImageView imageChecking;
    Button acceptTheTask, rejectATask, accept, reject;
    LinearLayout acceptRejectLinearLayout, checkingTheTask;
    ConstraintLayout taskCheckConstraintLayout;


    public TaskViewHolder(@NonNull View itemView) {
        super(itemView);

        todoCheckbox = itemView.findViewById(R.id.todoCheckBox);
        taskText = itemView.findViewById(R.id.taskText);
        user = itemView.findViewById(R.id.user);
        editImage = itemView.findViewById(R.id.editImage);
        toOrFrom = itemView.findViewById(R.id.toOrFrom);
        termDateTime = itemView.findViewById(R.id.termDateTime);
        acceptTheTask  = itemView.findViewById(R.id.acceptTheTask);
        rejectATask = itemView.findViewById(R.id.rejectATask);
        taskStatus = itemView.findViewById(R.id.taskStatus);
        taskStatusText = itemView.findViewById(R.id.taskStatusText);
        acceptRejectLinearLayout = itemView.findViewById(R.id.acceptRejectLinearLayout);
        accept = itemView.findViewById(R.id.accept);
        reject = itemView.findViewById(R.id.reject);
        checkingTheTask = itemView.findViewById(R.id.checkingTheTask);
        taskCheckConstraintLayout = itemView.findViewById(R.id.taskCheckConstraintLayout);
        imageChecking = itemView.findViewById(R.id.imageCheking);
        textChecking = itemView.findViewById(R.id.textChecking);
    }
}
