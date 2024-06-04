package com.example.dolt.tasks;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dolt.AddNewTask;
import com.example.dolt.R;
import com.example.dolt.SendingForVerification;
import com.example.dolt.TaskInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class TasksAdapter extends RecyclerView.Adapter<TaskViewHolder> {

    private ArrayList<Task> tasks = new ArrayList<>();


    public TasksAdapter(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        return new TaskViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (!tasks.get(position).getUserFromId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            holder.editImage.setVisibility(View.GONE);
        else
            holder.editImage.setVisibility(View.VISIBLE);
        holder.taskText.setText(tasks.get(position).getTaskText());
        holder.toOrFrom.setText(tasks.get(position).getToOrFrom());
        holder.termDateTime.setText(tasks.get(position).getTermDateTime());
        holder.todoCheckbox.setChecked(getItem(position).isChecked());
        String taskStatusText;
        int taskStatus  = getItem(position).getTaskStatus();
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

        int taskColor;
        if (tasks.get(position).getDegreeOfImportance()==0)
            taskColor = Color.argb(50, 100, 120, 70);
        else if (tasks.get(position).getDegreeOfImportance()==1)
            taskColor = Color.argb(100, 50, 251, 155);
        else if (tasks.get(position).getDegreeOfImportance()==2)
            taskColor = Color.argb(100, 101, 150, 0);
        else if (tasks.get(position).getDegreeOfImportance()==3)
            taskColor = Color.argb(100, 42, 99, 255);
        else taskColor = Color.WHITE;
        holder.itemView.findViewById(R.id.taskConstraintLayout).setBackgroundColor(taskColor);


        if (getItem(position).getTaskStatus()==0 && Objects.equals(FirebaseAuth.getInstance().getUid(), getItem(position).getUserToId())) {
            holder.acceptRejectLinearLayout.setVisibility(View.VISIBLE);
        } else {
            holder.acceptRejectLinearLayout.setVisibility(View.GONE);
        }

        if (getItem(position).getTaskStatus()==2 && Objects.equals(FirebaseAuth.getInstance().getUid(), getItem(position).getUserFromId())) {
            holder.taskCheckConstraintLayout.setVisibility(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference().child("Tasks").child(getItem(position).getTaskId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (getItem(position).getIsTaskCheck()==1) {
                                holder.imageChecking.setVisibility(View.GONE);
                                holder.textChecking.setText(snapshot.child("textForChecking").getValue().toString());
                            } else if (getItem(position).getIsTaskCheck()==2) {
                                holder.textChecking.setVisibility(View.GONE);
                                String profileImage = snapshot.child("taskImage").getValue().toString();

                                if (!profileImage.isEmpty()){
                                    Glide.with(holder.itemView.getContext()).load(profileImage).into(holder.imageChecking);
                                }
                            } else if (getItem(position).getIsTaskCheck()==3) {
                                holder.textChecking.setText(snapshot.child("textForChecking").getValue().toString());
                                String profileImage = snapshot.child("taskImage").getValue().toString();

                                if (!profileImage.isEmpty()){
                                    Glide.with(holder.itemView.getContext()).load(profileImage).into(holder.imageChecking);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        } else {
            holder.taskCheckConstraintLayout.setVisibility(View.GONE);
        }

        holder.itemView.findViewById(R.id.editImage).setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), AddNewTask.class);
            intent.putExtra("isUpdate", true);
            intent.putExtra("taskId", tasks.get(position).getTaskId());
            intent.putExtra("taskStatus", tasks.get(position).getTaskStatus());

            holder.itemView.getContext().startActivity(intent);
        });

        if (getItem(position).getTaskStatus()==-1 || getItem(position).getTaskStatus()==0 ||getItem(position).getTaskStatus()==2) {
            holder.todoCheckbox.setEnabled(false);
        }

        holder.todoCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tasks.get(position).getTaskStatus()==4) {
                    holder.todoCheckbox.setChecked(true);
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setTitle("Пометить задачу как невыполненную");
                    builder.setMessage("Вы уверены, что хотите отметить эту задачу как невыполненную?");
                    builder.setPositiveButton("Да, уверен",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    holder.taskStatus.setText("Задача принята к выполнению");
                                    holder.todoCheckbox.setChecked(false);
                                    getItem(position).setTaskStatus(1);
                                    FirebaseDatabase.getInstance().getReference().child("Tasks").child(tasks.get(position).getTaskId()).child("taskStatus").setValue(1);
                                }
                            });
                    builder.setNegativeButton("Нет" , new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                } else if (tasks.get(position).getTaskStatus() == 1 || tasks.get(position).getTaskStatus() == 3) {
                    if (getItem(position).getIsTaskCheck()!=0 && !Objects.equals(getItem(position).getUserFromId(), FirebaseAuth.getInstance().getUid())) {
                        holder.todoCheckbox.setChecked(false);
                        Intent intent = new Intent(holder.itemView.getContext(), SendingForVerification.class);
                        intent.putExtra("taskId", getItem(position).getTaskId());
                        intent.putExtra("taskText", getItem(position).getTaskText());
                        intent.putExtra("userFrom", getItem(position).getUserFrom());
                        intent.putExtra("degreeOfImportance", getItem(position).getDegreeOfImportance());
                        intent.putExtra("termDateTime", getItem(position).getTermDateTime());
                        intent.putExtra("taskCheck", getItem(position).getIsTaskCheck());
                        intent.putExtra("taskStatus", getItem(position).getTaskStatus());
                        holder.itemView.getContext().startActivity(intent);
                    } else {
                        holder.taskStatus.setText("Задача выполнена");
                        getItem(position).setChecked(true);
                        getItem(position).setTaskStatus(4);
                        FirebaseDatabase.getInstance().getReference().child("Tasks").child(tasks.get(position).getTaskId()).child("taskStatus").setValue(4);
                    }
                }
            }
        });

        holder.acceptTheTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getItem(position).setTaskStatus(1);
                FirebaseDatabase.getInstance().getReference().child("Tasks").child(tasks.get(position).getTaskId()).child("taskStatus").setValue(1);
                holder.acceptRejectLinearLayout.setVisibility(View.GONE);
                holder.todoCheckbox.setVisibility(View.VISIBLE);
                holder.taskStatus.setText("Задача принята к выполнению");
            }
        });
        holder.rejectATask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                builder.setTitle("Отказаться от задачи");
                builder.setMessage("Вы уверены, что хотите отказаться от этой задачи?");
                builder.setPositiveButton("Да, уверен",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getItem(position).setTaskStatus(-1);
                                FirebaseDatabase.getInstance().getReference().child("Tasks").child(tasks.get(position).getTaskId()).child("taskStatus").setValue(-1);
                                removeItem(holder);
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getItem(position).setTaskStatus(4);
                FirebaseDatabase.getInstance().getReference().child("Tasks").child(tasks.get(position).getTaskId()).child("taskStatus").setValue(4);
                holder.taskCheckConstraintLayout.setVisibility(View.GONE);
                holder.todoCheckbox.setVisibility(View.VISIBLE);
                holder.taskStatus.setText("Задача выполнена");
                holder.todoCheckbox.setChecked(true);
            }
        });
        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getItem(position).setTaskStatus(3);
                FirebaseDatabase.getInstance().getReference().child("Tasks").child(tasks.get(position).getTaskId()).child("taskStatus").setValue(3);
                holder.taskCheckConstraintLayout.setVisibility(View.GONE);
                holder.todoCheckbox.setVisibility(View.VISIBLE);
                holder.taskStatus.setText("Задача выполненна неверно");
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), TaskInfo.class);
            intent.putExtra("taskId", getItem(position).getTaskId());
            intent.putExtra("taskText", getItem(position).getTaskText());
            intent.putExtra("userFrom", getItem(position).getUserFrom());
            intent.putExtra("degreeOfImportance", getItem(position).getDegreeOfImportance());
            intent.putExtra("termDateTime", getItem(position).getTermDateTime());
            intent.putExtra("taskCheck", getItem(position).getIsTaskCheck());
            intent.putExtra("taskStatus", getItem(position).getTaskStatus());
            holder.itemView.getContext().startActivity(intent);
        });

        holder.taskStatus.setText(taskStatusText);
        if (holder.toOrFrom.getText()=="От:") {
            if (tasks.get(position).getUserFromId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                holder.user.setVisibility(View.GONE);
                holder.toOrFrom.setVisibility(View.GONE);
                holder.taskStatus.setVisibility(View.GONE);
                holder.taskStatusText.setVisibility(View.GONE);
            }
            else
                holder.user.setText(tasks.get(position).getUserFrom());

        } else {
            if (tasks.get(position).getUserToId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                holder.user.setText(tasks.get(position).getUserTo() + " (Мне)");
            else
                holder.user.setText(tasks.get(position).getUserTo());
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public Task getItem(int position) {
        return tasks.get(position);
    }

    private void removeItem(TaskViewHolder holder) {
        int actualPosition = holder.getAdapterPosition();
        tasks.remove(actualPosition);
        notifyItemRemoved(actualPosition);
        notifyItemRangeChanged(actualPosition, tasks.size());
    }

}
