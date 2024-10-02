package com.example.dolt;

import static com.example.dolt.DifferentMethods.getCurrentUser;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.dolt.databinding.ActivityTaskInfoBinding;
import com.example.dolt.tasks.Task;
import com.example.dolt.utils.DatabaseFriends;
import com.example.dolt.utils.DatabaseTasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class TaskInfo extends AppCompatActivity {

    ActivityTaskInfoBinding binding;
    private DatabaseFriends databaseFriends;
    private DatabaseTasks databaseTasks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseTasks = new DatabaseTasks(this);
        databaseTasks.openDatabase();
        databaseFriends = new DatabaseFriends(this);
        databaseFriends.openDatabase();

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        String taskId = extras.getString("taskId");
        Task task = databaseTasks.getTaskByTaskId(taskId);
        loadTaskInfo(task);
        String userFromId = task.getUserFromId();
        String userToId = task.getUserToId();

        binding.backButton.setOnClickListener(v -> intentToTaskFragment(userFromId, userToId));
        binding.editOrRejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(getCurrentUser(getApplicationContext()).getUserId(), userFromId)) {
                    intentToAddNewTask(taskId);
                } else if (Objects.equals(getCurrentUser(getApplicationContext()).getUserId(), userToId)) {
                    AlertDialog.Builder builder = getBuilder();
                    builder.setNegativeButton("Нет", (dialog, which) -> {
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
            @NonNull
            private AlertDialog.Builder getBuilder() {
                AlertDialog.Builder builder = new AlertDialog.Builder(TaskInfo.this);
                builder.setTitle("Отказаться от задачи");
                builder.setMessage("Отказаться от этой задачи?");
                builder.setPositiveButton("Да",
                        (dialog, which) -> {
                            FirebaseDatabase.getInstance().getReference().child("Tasks").child(taskId).child("taskStatus").setValue(-1);
                            databaseTasks = new DatabaseTasks(this.getBuilder().getContext());
                            databaseTasks.openDatabase();
                            databaseTasks.deleteTask(databaseTasks.getTaskByTaskId(taskId).getId());
                            databaseTasks.close();
                            intentToTaskFragment(userFromId, userToId);
                        });
                return builder;
            }
        });
    }

    static String returnStringDegreeOfImportance(int degreeOfImportance) {
        String degreeOfImportanceText = "Неизвестная важность задачи";
        String[] degreeOfImportanceTextes = new String[]{"Неважно", "Не очень важно", "Важно", "Очень важно"};
        if (degreeOfImportance > -1 && degreeOfImportance < 4) {
            degreeOfImportanceText = degreeOfImportanceTextes[degreeOfImportance];
        }
        return degreeOfImportanceText;
    }

    private String returnStringStatus(int taskStatus) {
        String[] taskStatuses = new String[]{"Отклонена", "В ожидании принятия", "Принята к выполнению", "Ждёт проверки",
                "Выполненна неверно", "Выполненна"};
        return taskStatuses[taskStatus+1];
    }

    private String returnStringTaskCheck(int taskCheck) {
        String taskCheckText = "Нет";
        String[] taskCheckTextes = new String[]{"Нет", "Текст", "Фото", "Текст и фото"};
        if (taskCheck > -1 && taskCheck < 4) {
            taskCheckText = taskCheckTextes[taskCheck];
        }
        return taskCheckText;
    }

    private void loadTaskInfo(Task task){
        String taskId = task.getTaskId();
        String userFromId = task.getUserFromId();
        String userToId = task.getUserToId();
        String userFrom = databaseFriends.getFriendByUserId(userFromId).getUsername();
        String userTo = databaseFriends.getFriendByUserId(userToId).getUsername();
        String taskText = task.getTaskText();
        String termDateTime = task.getTermDateTime();
        int degreeOfImportance = task.getDegreeOfImportance();
        int taskCheck = task.getIsTaskCheck();
        int taskStatus = task.getTaskStatus();
        String degreeOfImportanceText = returnStringDegreeOfImportance(degreeOfImportance);
        String taskStatusText = returnStringStatus(taskStatus);
        String taskCheckString = returnStringTaskCheck(taskCheck);

        binding.infoTaskText.setText(taskText);
        binding.infoFromUser.setText(userFrom);
        binding.infoToUser.setText(userTo);
        binding.infoDegreeOfImportance.setText(degreeOfImportanceText);
        binding.infoTermDateTime.setText(termDateTime);
        binding.infoTaskCheck.setText(taskCheckString);
        binding.infoTaskStatus.setText(taskStatusText);

        if (taskStatus==2 || taskStatus==4) {
            assert taskId != null;
            FirebaseDatabase.getInstance().getReference().child("Tasks").child(taskId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (taskCheck == 1) {
                                binding.infoImageChecking.setVisibility(View.GONE);
                                binding.infoTextChecking.setVisibility(View.VISIBLE);
                                binding.infoTextChecking.setText(Objects.requireNonNull(snapshot.child("textForChecking").getValue()).toString());
                            } else if (taskCheck == 2) {
                                binding.infoImageChecking.setVisibility(View.VISIBLE);
                                binding.infoTextChecking.setVisibility(View.GONE);
                                String profileImage = Objects.requireNonNull(snapshot.child("taskImage").getValue()).toString();

                                if (!profileImage.isEmpty()) {
                                    Glide.with(getApplicationContext()).load(profileImage).into(binding.infoImageChecking);
                                }
                            } else if (taskCheck == 3) {
                                binding.infoImageChecking.setVisibility(View.VISIBLE);
                                binding.infoTextChecking.setVisibility(View.VISIBLE);
                                binding.infoTextChecking.setText(Objects.requireNonNull(snapshot.child("textForChecking").getValue()).toString());
                                String profileImage = Objects.requireNonNull(snapshot.child("taskImage").getValue()).toString();

                                if (!profileImage.isEmpty()) {
                                    Glide.with(getApplicationContext()).load(profileImage).into(binding.infoImageChecking);
                                }
                            } else {
                                binding.infoImageChecking.setVisibility(View.GONE);
                                binding.infoTextChecking.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else {
            binding.infoTextCheckingLayout.setVisibility(View.GONE);
            binding.infoImageChecking.setVisibility(View.GONE);
        }

        if (!Objects.equals(getCurrentUser(getApplicationContext()).getUsername(), userFrom)) {
            binding.editOrRejectBtn.setText("Отказаться от задачи");
            binding.editOrRejectBtn.setBackgroundColor(Color.argb(100, 179, 38, 30));
        }
    }

    private void intentToAddNewTask(String taskId) {
        Intent intent = new Intent(TaskInfo.this, AddNewTask.class);
        intent.putExtra("isUpdate", true);
        intent.putExtra("taskId", taskId);
        startActivity(intent);
    }

    private void intentToTaskFragment(String userFromId, String userToId) {
        Intent intent = new Intent(TaskInfo.this, MainActivity.class);
        intent.putExtra("fragment", "tasksFragment");
        if (!Objects.equals(userFromId, getCurrentUser(getApplicationContext()).getUserId())){
            intent.putExtra("fragmentTasks", "incomingTasksFragment");
        } else {
            if (!Objects.equals(userToId, getCurrentUser(getApplicationContext()).getUserId()))
                intent.putExtra("fragmentTasks", "sentTasksFragment");
            else intent.putExtra("fragmentTasks", "myTasksFragment");
        }
        startActivity(intent);
    }

}