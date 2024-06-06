package com.example.dolt;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.dolt.databinding.ActivityTaskInfoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class TaskInfo extends AppCompatActivity {

    ActivityTaskInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        loadTaskInfo(extras);
        String taskId = extras.getString("taskId");
        String taskText = extras.getString("taskText");
        String userFrom = extras.getString("userFrom");
        String userTo = extras.getString("userTo");
        String degreeOfImportance = String.valueOf(extras.getInt("degreeOfImportance"));
        String termDateTime = extras.getString("termDateTime");
        int taskCheck = extras.getInt("taskCheck");
        String taskStatusString = String.valueOf(extras.getInt("taskStatus"));
        int taskStatus = Integer.parseInt(taskStatusString);
        String degreeOfImportanceText = returnStringDegreeOfImportance(Integer.parseInt(degreeOfImportance));
        String taskStatusText = returnStringStatus(taskStatus);
        String taskCheckString = returnStringTaskCheck(taskCheck);

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToTaskFragment(userFrom, userTo);
            }
        });

        binding.editOrRejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Users")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (Objects.equals(snapshot.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("username").getValue(), userFrom)) {
                                    intentToAddNewTask(taskId, taskStatus, userTo);
                                } else if (Objects.equals(snapshot.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("username").getValue(), userTo)) {
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
                                            intentToTaskFragment(userFrom, userTo);
                                        });
                                return builder;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

    }

    private String returnStringDegreeOfImportance(int degreeOfImportance) {
        String degreeOfImportanceText;
        if (degreeOfImportance==0) {
            degreeOfImportanceText = "Неважно";
        } else if (degreeOfImportance==1) {
            degreeOfImportanceText = "Не очень важно";
        } else if (degreeOfImportance==2) {
            degreeOfImportanceText = "Важно";
        } else if (degreeOfImportance==3) {
            degreeOfImportanceText = "Очень важно";
        } else {
            degreeOfImportanceText = "Неизвестная важность задачи";
        }
        return degreeOfImportanceText;
    }

    private String returnStringStatus(int taskStatus) {
        String taskStatusText;
        if (taskStatus==-1) {
            taskStatusText = "Задача отклонена";
        } else if (taskStatus==0) {
            taskStatusText = "В ожидании принятия";
        } else if (taskStatus==1) {
            taskStatusText = "Задача принята к выполнению";
        } else if (taskStatus==2) {
            taskStatusText = "В ожидании проверки";
        } else if (taskStatus==3) {
            taskStatusText = "Задача выполненна неверно";
        }else if (taskStatus==4) {
            taskStatusText = "Задача выполненна";
        } else {
            taskStatusText = "Неизвестный статус задачи";
        }
        return taskStatusText;
    }

    private String returnStringTaskCheck(int taskCheck) {
        String taskCheckText;
        if (taskCheck==0) {
            taskCheckText = "Нет";
        } else if (taskCheck==1) {
            taskCheckText = "Текст";
        } else if (taskCheck==2) {
            taskCheckText = "Фото";
        } else if (taskCheck==3) {
            taskCheckText = "Текст и фото";
        } else {
            taskCheckText = "Неизвестная важность задачи";
        }
        return taskCheckText;
    }

    private void loadTaskInfo(Bundle extras){
        String taskId = extras.getString("taskId");
        String taskText = extras.getString("taskText");
        String userFrom = extras.getString("userFrom");
        String userTo = extras.getString("userTo");
        String degreeOfImportance = String.valueOf(extras.getInt("degreeOfImportance"));
        String termDateTime = extras.getString("termDateTime");
        int taskCheck = extras.getInt("taskCheck");
        String taskStatusString = String.valueOf(extras.getInt("taskStatus"));
        int taskStatus = Integer.parseInt(taskStatusString);
        String degreeOfImportanceText = returnStringDegreeOfImportance(Integer.parseInt(degreeOfImportance));
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

        FirebaseDatabase.getInstance().getReference().child("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!Objects.equals(snapshot.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("username").getValue(), userFrom)) {
                            binding.editOrRejectBtn.setText("Отказаться от задачи");
                            binding.editOrRejectBtn.setBackgroundColor(Color.argb(100, 179, 38, 30));
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void intentToAddNewTask(String taskId, int taskStatus, String userTo) {
        FirebaseDatabase.getInstance().getReference().child("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Intent intent = new Intent(TaskInfo.this, AddNewTask.class);
                        intent.putExtra("isUpdate", true);
                        if (!Objects.equals(userTo, snapshot.child(FirebaseAuth.getInstance().getUid()).child("username").getValue().toString()))
                            intent.putExtra("fromFragment", "sentTasksFragment");
                        else intent.putExtra("fromFragment", "myTasksFragment");
                        intent.putExtra("taskId", taskId);
                        intent.putExtra("taskStatus", taskStatus);
                        startActivity(intent);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void intentToTaskFragment(String userFrom, String userTo) {
        FirebaseDatabase.getInstance().getReference().child("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Intent intent = new Intent(TaskInfo.this, MainActivity.class);
                        intent.putExtra("fragment", "tasksFragment");
                        if (!Objects.equals(userFrom, snapshot.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("username").getValue().toString())){
                            intent.putExtra("fragmentTasks", "incomingTasksFragment");
                        } else {
                            if (!Objects.equals(userTo, snapshot.child(FirebaseAuth.getInstance().getUid()).child("username").getValue().toString()))
                                intent.putExtra("fragmentTasks", "sentTasksFragment");
                            else intent.putExtra("fragmentTasks", "myTasksFragment");
                        }
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

}