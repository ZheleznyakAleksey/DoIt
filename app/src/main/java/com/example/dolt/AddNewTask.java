package com.example.dolt;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class AddNewTask extends AppCompatActivity {

    private ActivityAddNewTaskBinding binding;
    private final String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    TextView tomorrowDateTime;
    Calendar dateAndTime=Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddNewTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tomorrowDateTime = findViewById(R.id.termDateTime);
        setInitialDateTime();

        boolean isUpdate = false;
        String fromFragment = "myTasksFragment";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isUpdate = extras.getBoolean("isUpdate");
            fromFragment = extras.getString("fromFragment");
        }
        String finalFromFragment = fromFragment;

        final String[] friendsStr = new String[1];
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference().child("Users");

        fullSpinner(dr, friendsStr);

        if(isUpdate) {
            binding.toUser.setVisibility(View.GONE);
            String taskId = extras.getString("taskId");
            FirebaseDatabase.getInstance().getReference().child("Tasks").child(taskId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    binding.newTaskText.setText(Objects.requireNonNull(snapshot.child("taskText").getValue()).toString());
                                    binding.termDateTime.setText(Objects.requireNonNull(snapshot.child("termDateTime").getValue()).toString());
                                    binding.degreeOfImportance.setSelection(Integer.parseInt(Objects.requireNonNull(snapshot.child("degreeOfImportance").getValue()).toString()));
                                    binding.changeTaskCheck.setSelection(Integer.parseInt(Objects.requireNonNull(snapshot.child("isTaskCheck").getValue()).toString()));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
        } else {
            binding.deleteTaskButton.setVisibility(View.GONE);
        }

        binding.userTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("username")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (binding.userTo.getItemAtPosition(position).toString().equals(Objects.requireNonNull(snapshot.getValue()) + " (Я)")) {
                                    binding.isTaskCheckLinearLayout.setBackgroundColor(Color.GRAY);
                                    binding.changeTaskCheck.setEnabled(false);
                                    binding.changeTaskCheck.setSelection(0);
                                } else {
                                    binding.isTaskCheckLinearLayout.setBackgroundColor(Color.WHITE);
                                    binding.changeTaskCheck.setEnabled(true);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
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
                    int itemPosition = binding.userTo.getSelectedItemPosition();

                    String userToId = null;

                    final String[][] allFriendsList = new String[2][friendsStr[0].split(",").length];
                    allFriendsList[0] = friendsStr[0].split(",");

                    for (int i = 0; i < allFriendsList[0].length; i++) {
                        String str = allFriendsList[0][i];
                        str = str.substring(1, allFriendsList[0][i].length() - 1);
                        if (i == 0)
                            str = str.substring(0, allFriendsList[0][i].length() - 2);
                        if (i == allFriendsList[0].length - 1)
                            str = str.substring(0, allFriendsList[0][i].length() - 3);
                        if (i == itemPosition)
                            userToId = str;
                    }

                    HashMap<String, Object> taskInfo = new HashMap<>();
                    String taskId;
                    if (finalIsUpdate) {
                        taskId = extras.getString("taskId");
                        taskInfo.put("degreeOfImportance", binding.degreeOfImportance.getSelectedItemId());
                        taskInfo.put("taskStatus", extras.getInt("taskStatus"));

                    } else {
                        taskId = valueOf(UUID.randomUUID());
                        FirebaseDatabase.getInstance().getReference().child("Tasks").child("tasks").child(taskId).setValue("");
                        taskInfo.put("userToId", userToId);
                        taskInfo.put("degreeOfImportance", binding.degreeOfImportance.getSelectedItemId());
                    }
                    String userFromId = valueOf(FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())));
                    userFromId = userFromId.substring(userFromId.length() - 28);
                    taskInfo.put("userFromId", userFromId);
                    taskInfo.put("taskText", binding.newTaskText.getText().toString());
                    taskInfo.put("termDateTime", binding.termDateTime.getText());
                    taskInfo.put("isTaskCheck", binding.changeTaskCheck.getSelectedItemId());
                    if (!finalIsUpdate) {
                        if (taskInfo.get("userToId").toString().equals(taskInfo.get("userFromId").toString())) {
                            taskInfo.put("taskStatus", 1);
                        }
                        else {
                            taskInfo.put("taskStatus", 0);
                        }
                    }

                    FirebaseDatabase.getInstance().getReference().child("Tasks").child(taskId).updateChildren(taskInfo);
                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    final Intent intent = new Intent(AddNewTask.this, MainActivity.class);
                                    intent.putExtra("fragment", "tasksFragment");
                                    intent.putExtra("fragmentTasks", finalFromFragment);
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
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

                Intent intent = new Intent(AddNewTask.this, MainActivity.class);
                intent.putExtra("fragment", "tasksFragment");
                intent.putExtra("fragmentTasks", finalFromFragment);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));

    }

    public void fullSpinner(DatabaseReference dr, String[] friendsStr){

        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendsStr[0] = Objects.requireNonNull(snapshot.child(uid).child("friends").getValue()).toString();
                String friendsStr = Objects.requireNonNull(snapshot.child(uid).child("friends").getValue()).toString();
                final String[][] allFriendsList = new String[2][friendsStr.split(",").length];
                allFriendsList[0] = friendsStr.split(",");
                int selPos = 0;

                for (int i = 0; i < allFriendsList[0].length; i++) {
                    String str = allFriendsList[0][i];
                    str = str.substring(1, allFriendsList[0][i].length()-1);
                    if(i==0)
                        str = str.substring(0, allFriendsList[0][i].length()-2);
                    if (i== allFriendsList[0].length-1)
                        str = str.substring(0, allFriendsList[0][i].length()-3);
                    String username = Objects.requireNonNull(snapshot.child(str).child("username").getValue()).toString();
                    allFriendsList[0][i] = username;
                    allFriendsList[1][i] = str;
                    if (str.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                        selPos = i;
                        allFriendsList[0][i] += " (Я)";
                    }
                }

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, allFriendsList[0]);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.userTo.setAdapter(spinnerArrayAdapter);
                binding.userTo.setSelection(selPos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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