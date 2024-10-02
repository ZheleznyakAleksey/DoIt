package com.example.dolt;

import static com.example.dolt.DifferentMethods.getCurrentUser;
import static java.lang.String.valueOf;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dolt.databinding.ActivityAddNewTaskBinding;
import com.example.dolt.tasks.Task;
import com.example.dolt.users.User;
import com.example.dolt.utils.DatabaseFriends;
import com.example.dolt.utils.DatabaseTasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class AddNewTask extends AppCompatActivity {

    private ActivityAddNewTaskBinding binding;
    private final String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private DatabaseTasks databaseTasks;
    private DatabaseFriends databaseFriends;
    TextView tomorrowDateTime;
    Calendar dateAndTime=Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddNewTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseTasks = new DatabaseTasks(this);
        databaseTasks.openDatabase();
        databaseFriends = new DatabaseFriends(this);
        databaseFriends.openDatabase();

        tomorrowDateTime = findViewById(R.id.termDateTime);
        fullSpinner();

        setInitialDateTime();
        boolean isUpdate = false;
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        isUpdate = extras.getBoolean("isUpdate");
        if(isUpdate) {
            String taskId = extras.getString("taskId");

            binding.toUser.setVisibility(View.GONE);
            Task task = databaseTasks.getTaskByTaskId(taskId);
            binding.newTaskText.setText(task.getTaskText());
            binding.termDateTime.setText(task.getTermDateTime());
            binding.degreeOfImportance.setSelection(task.getDegreeOfImportance());
            binding.changeTaskCheck.setSelection(task.getIsTaskCheck());
        } else {
            binding.deleteTaskButton.setVisibility(View.GONE);
        }

        binding.userTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (binding.userTo.getItemAtPosition(position).toString().equals(getCurrentUser(getApplicationContext()).getUsername())) {
                    binding.isTaskCheckLinearLayout.setBackgroundColor(Color.GRAY);
                    binding.changeTaskCheck.setEnabled(false);
                    binding.changeTaskCheck.setSelection(0);
                } else {
                    binding.isTaskCheckLinearLayout.setBackgroundColor(Color.WHITE);
                    binding.changeTaskCheck.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        boolean finalIsUpdate = isUpdate;
        binding.newTaskButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                if (binding.newTaskText.getText().length()!=0) {
                    String userFromId = getCurrentUser(getApplicationContext()).getUserId();
                    String userToId = getCurrentUser(getApplicationContext()).getUserId();
                    String userTo = binding.userTo.getSelectedItem().toString();
                    ArrayList<User> friends= databaseFriends.getAllFriends();

                    for (int i = 0; i < friends.size(); i++) {
                        if (Objects.equals(friends.get(i).getUsername(), userTo)) {
                            userToId = friends.get(i).getUserId();
                        }
                    }

                    HashMap<String, Object> taskInfo = new HashMap<>();
                    String taskId;
                    if (finalIsUpdate) {
                        taskId = extras.getString("taskId");

                        taskInfo.put("degreeOfImportance", binding.degreeOfImportance.getSelectedItemId());
                        taskInfo.put("taskStatus", databaseTasks.getTaskByTaskId(taskId).getTaskStatus());

                    } else {
                        taskId = valueOf(UUID.randomUUID());
                        FirebaseDatabase.getInstance().getReference().child("Tasks").child("tasks").child(taskId).setValue("");
                        taskInfo.put("userToId", userToId);
                        taskInfo.put("degreeOfImportance", binding.degreeOfImportance.getSelectedItemId());
                    }

                    taskInfo.put("userFromId", userFromId);
                    taskInfo.put("taskText", binding.newTaskText.getText().toString());
                    taskInfo.put("termDateTime", binding.termDateTime.getText());
                    taskInfo.put("isTaskCheck", binding.changeTaskCheck.getSelectedItemId());
                    if (!finalIsUpdate) {
                        if (Objects.requireNonNull(taskInfo.get("userToId")).toString().equals(Objects.requireNonNull(taskInfo.get("userFromId")).toString())) {
                                taskInfo.put("taskStatus", 1);
                            }
                        else {
                                taskInfo.put("taskStatus", 0);
                            }
                    }

                    Task task = new Task();
                    task.setTaskText(binding.newTaskText.getText().toString());
                    task.setDegreeOfImportance((int) binding.degreeOfImportance.getSelectedItemId());
                    task.setTermDateTime((String) binding.termDateTime.getText());
                    task.setIsTaskCheck((int) binding.changeTaskCheck.getSelectedItemId());
                    task.setToOrFrom("От: ");
                    task.setTaskId(taskId);
                    if (!finalIsUpdate) {
                        task.setUserFromId(userFromId);
                        task.setUserToId(userToId);
                        assert userToId != null;
                        if (userToId.equals(userFromId)) {
                            task.setTaskStatus(1);
                        } else task.setTaskStatus(0);
                        databaseTasks.insertTask(task);
                    } else {
                        Task oldTask = databaseTasks.getTaskByTaskId(taskId);
                        task.setUserFromId(oldTask.getUserFromId());
                        task.setUserToId(oldTask.getUserToId());
                        task.setTaskStatus(oldTask.getTaskStatus());
                        databaseTasks.updateTask(databaseTasks.getTaskByTaskId(taskId).getId(), task);
                    }
                    databaseTasks.close();
                    databaseFriends.close();

                    FirebaseDatabase.getInstance().getReference().child("Tasks").child(taskId).updateChildren(taskInfo);

                    final Intent intent = new Intent(AddNewTask.this, MainActivity.class);
                    intent.putExtra("fragment", "tasksFragment");
                    startActivity(intent);
                } else
                    Toast.makeText(getApplicationContext(), "Задача не может быть пустой!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.deleteTaskButton.setOnClickListener(v -> FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String taskId = extras.getString("taskId");

                snapshot.child("Tasks").child("tasks").child(taskId).getRef().setValue(null);
                snapshot.child("Tasks").child(taskId).getRef().removeValue();
                databaseTasks.deleteTask(databaseTasks.getTaskByTaskId(taskId).getId());

                databaseTasks.close();
                databaseFriends.close();

                Intent intent = new Intent(AddNewTask.this, MainActivity.class);
                intent.putExtra("fragment", "tasksFragment");
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
    }

    public void fullSpinner(){
        ArrayList<User> friends= databaseFriends.getAllFriends();
        ArrayList<String> friendsNames = new ArrayList<>();

        int selPos = 0;
        for (int i = 0; i < friends.size(); i++) {
            friendsNames.add(friends.get(i).getUsername());
            if (friends.get(i).getUserId().equals(getCurrentUser(getApplicationContext()).getUserId())) {
                selPos = i;
            }
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, friendsNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.userTo.setAdapter(spinnerArrayAdapter);
        binding.userTo.setSelection(selPos);
    }

    // отображаем диалоговое окно для выбора даты
    public void setDate(View v) {
        new DatePickerDialog(AddNewTask.this, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // отображаем диалоговое окно для выбора времени
    public void setTime(View v) {
        new TimePickerDialog(AddNewTask.this, t,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true)
                .show();
    }
    // установка начальных даты и времени
    private void setInitialDateTime() {
        tomorrowDateTime.setText(DateUtils.formatDateTime(this,
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
    }

    // установка обработчика выбора времени
    TimePickerDialog.OnTimeSetListener t= (view, hourOfDay, minute) -> {
        dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateAndTime.set(Calendar.MINUTE, minute);
        setInitialDateTime();
    };

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d= (view, year, monthOfYear, dayOfMonth) -> {
        dateAndTime.set(Calendar.YEAR, year);
        dateAndTime.set(Calendar.MONTH, monthOfYear);
        dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        setInitialDateTime();
    };

}