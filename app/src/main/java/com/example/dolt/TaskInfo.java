package com.example.dolt;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.dolt.databinding.ActivityTaskInfoBinding;
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
    }

}