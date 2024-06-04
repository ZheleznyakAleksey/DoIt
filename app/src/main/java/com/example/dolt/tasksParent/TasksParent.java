package com.example.dolt.tasksParent;

import com.example.dolt.tasks.Task;

import java.util.ArrayList;

public class TasksParent {
    private ArrayList<Task> tasks;

    public TasksParent(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }


    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

}
