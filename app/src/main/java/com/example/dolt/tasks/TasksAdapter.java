package com.example.dolt.tasks;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

    private final ArrayList<Task> tasks;


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
        setVisibilityComponents(holder, position);

        loadTaskInfo(holder, position);

        if (getItem(position).getTaskStatus()==2 && Objects.equals(FirebaseAuth.getInstance().getUid(), getItem(position).getUserFromId())) {
            holder.taskCheckConstraintLayout.setVisibility(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference().child("Tasks").child(getItem(position).getTaskId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            setTaskCheckInfo(holder, snapshot, position);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        } else {
            holder.taskCheckConstraintLayout.setVisibility(View.GONE);
        }

        holder.accept.setOnClickListener(v -> setTaskStatus(holder, 4, position));
        holder.reject.setOnClickListener(v -> setTaskStatus(holder, 3, position));
        holder.itemView.findViewById(R.id.editImage).setOnClickListener(v -> intentToAddNewTask(holder, position));
        holder.itemView.setOnClickListener(v -> intentToTaskInfo(holder, position));
        holder.acceptTheTask.setOnClickListener(v -> setTaskStatus(holder, 1, position));
        holder.rejectATask.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setTitle("Отказаться от задачи");
            builder.setMessage("Отказаться от этой задачи?");
            builder.setPositiveButton("Да",
                    (dialog, which) -> {
                        getItem(position).setTaskStatus(-1);
                        FirebaseDatabase.getInstance().getReference().child("Tasks").child(tasks.get(position).getTaskId()).child("taskStatus").setValue(-1);
                        removeItem(holder);
                    });
            builder.setNegativeButton("Нет", (dialog, which) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        });

        holder.todoCheckbox.setEnabled(getItem(position).getTaskStatus() != -1 && getItem(position).getTaskStatus() != 0 && getItem(position).getTaskStatus() != 2);

        holder.todoCheckbox.setOnClickListener(v -> {
            if (tasks.get(position).getTaskStatus()==4) {
                holder.todoCheckbox.setChecked(true);
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                builder.setTitle("Отметить задачу как невыполненную");
                builder.setMessage("Отметить эту задачу как невыполненную?");
                builder.setPositiveButton("Да",
                        (dialog, which) -> setTaskStatus(holder, 1, position));
                builder.setNegativeButton("Нет" , (dialog, which) -> {
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            } else if (tasks.get(position).getTaskStatus() == 1 || tasks.get(position).getTaskStatus() == 3) {
                if (getItem(position).getIsTaskCheck()!=0 && !Objects.equals(getItem(position).getUserFromId(), FirebaseAuth.getInstance().getUid())) {
                    intentToSendingToVerification(holder, position);
                } else {
                    setTaskStatus(holder, 4, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    private Task getItem(int position) {
        return tasks.get(position);
    }

    private void removeItem(TaskViewHolder holder) {
        int actualPosition = holder.getBindingAdapterPosition();
        tasks.remove(actualPosition);
        notifyItemRemoved(actualPosition);
        notifyItemRangeChanged(actualPosition, tasks.size());
    }

    private void setTaskCheckInfo(TaskViewHolder holder, DataSnapshot snapshot,  int position) {
        if (getItem(position).getIsTaskCheck()==1) {
            holder.imageChecking.setVisibility(View.GONE);
            holder.textChecking.setText(Objects.requireNonNull(snapshot.child("textForChecking").getValue()).toString());
        } else if (getItem(position).getIsTaskCheck()==2) {
            holder.textChecking.setVisibility(View.GONE);
            String profileImage = Objects.requireNonNull(snapshot.child("taskImage").getValue()).toString();

            if (!profileImage.isEmpty()){
                Glide.with(holder.itemView.getContext()).load(profileImage).into(holder.imageChecking);
            }
        } else if (getItem(position).getIsTaskCheck()==3) {
            holder.textChecking.setText(Objects.requireNonNull(snapshot.child("textForChecking").getValue()).toString());
            String profileImage = Objects.requireNonNull(snapshot.child("taskImage").getValue()).toString();

            if (!profileImage.isEmpty()){
                Glide.with(holder.itemView.getContext()).load(profileImage).into(holder.imageChecking);
            }
        }
    }

    private void setTaskStatus(TaskViewHolder holder, int newTaskStatus, int position) {
        getItem(position).setTaskStatus(newTaskStatus);
        FirebaseDatabase.getInstance().getReference().child("Tasks").child(tasks.get(position).getTaskId()).child("taskStatus").setValue(newTaskStatus);
        holder.taskStatus.setText(getTaskStatusText(newTaskStatus));
        holder.todoCheckbox.setEnabled(newTaskStatus != -1 && newTaskStatus != 0 && newTaskStatus != 2);
        holder.todoCheckbox.setChecked(newTaskStatus==4);

        if (newTaskStatus==0) {
            holder.acceptRejectLinearLayout.setVisibility(View.VISIBLE);
            holder.taskCheckConstraintLayout.setVisibility(View.GONE);
        } else if (newTaskStatus == 2 && Objects.equals(FirebaseAuth.getInstance().getUid(), getItem(position).getUserFromId())) {
            holder.taskCheckConstraintLayout.setVisibility(View.VISIBLE);
            holder.acceptRejectLinearLayout.setVisibility(View.GONE);
            FirebaseDatabase.getInstance().getReference().child("Tasks").child(getItem(position).getTaskId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            setTaskCheckInfo(holder, snapshot, position);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else {
            holder.acceptRejectLinearLayout.setVisibility(View.GONE);
            holder.taskCheckConstraintLayout.setVisibility(View.GONE);
        }
    }

    private void intentToAddNewTask(TaskViewHolder holder, int position) {
        Intent intent = new Intent(holder.itemView.getContext(), AddNewTask.class);
        intent.putExtra("isUpdate", true);
        intent.putExtra("taskId", tasks.get(position).getTaskId());
        intent.putExtra("taskStatus", tasks.get(position).getTaskStatus());
        holder.itemView.getContext().startActivity(intent);
    }

    private void intentToSendingToVerification(TaskViewHolder holder, int position) {
        Intent intent = new Intent(holder.itemView.getContext(), SendingForVerification.class);
        intent.putExtra("taskId", getItem(position).getTaskId());
        intent.putExtra("taskText", getItem(position).getTaskText());
        intent.putExtra("userFrom", getItem(position).getUserFrom());
        intent.putExtra("degreeOfImportance", getItem(position).getDegreeOfImportance());
        intent.putExtra("termDateTime", getItem(position).getTermDateTime());
        intent.putExtra("taskCheck", getItem(position).getIsTaskCheck());
        intent.putExtra("taskStatus", getItem(position).getTaskStatus());
        holder.itemView.getContext().startActivity(intent);
    }

    private void intentToTaskInfo(TaskViewHolder holder, int position) {
        Intent intent = new Intent(holder.itemView.getContext(), TaskInfo.class);
        intent.putExtra("taskId", getItem(position).getTaskId());
        intent.putExtra("taskText", getItem(position).getTaskText());
        intent.putExtra("userFrom", getItem(position).getUserFrom());
        intent.putExtra("degreeOfImportance", getItem(position).getDegreeOfImportance());
        intent.putExtra("termDateTime", getItem(position).getTermDateTime());
        intent.putExtra("taskCheck", getItem(position).getIsTaskCheck());
        intent.putExtra("taskStatus", getItem(position).getTaskStatus());
        holder.itemView.getContext().startActivity(intent);
    }

    private void setVisibilityComponents(TaskViewHolder holder, int position) {
        if (!tasks.get(position).getUserFromId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()))
            holder.editImage.setVisibility(View.GONE);
        else
            holder.editImage.setVisibility(View.VISIBLE);

        if (getItem(position).getTaskStatus()==0 && Objects.equals(FirebaseAuth.getInstance().getUid(), getItem(position).getUserToId())) {
            holder.acceptRejectLinearLayout.setVisibility(View.VISIBLE);
        } else {
            holder.acceptRejectLinearLayout.setVisibility(View.GONE);
        }


    }

    private void loadTaskInfo(TaskViewHolder holder, int position) {
        holder.taskText.setText(tasks.get(position).getTaskText());
        holder.itemView.findViewById(R.id.taskConstraintLayout).setBackgroundColor(getTaskColor(position));
        holder.todoCheckbox.setChecked(getItem(position).isChecked());
        holder.termDateTime.setText(tasks.get(position).getTermDateTime());
        //holder.taskStatus.setText(getTaskStatusText(getItem(position).getTaskStatus()));
        //holder.toOrFrom.setText(tasks.get(position).getToOrFrom());
        //if (holder.toOrFrom.getText()=="От:") {
        //        holder.user.setText(tasks.get(position).getUserFrom());
        //    } else {
        //        holder.user.setText(tasks.get(position).getUserTo());
        //    }
    }

    private String getTaskStatusText(int taskStatus) {
        String taskStatusText;
        if (taskStatus==-1) {
            taskStatusText = "Отклонена";
        } else if (taskStatus==0) {
            taskStatusText = "В ожидании принятия";
        } else if (taskStatus==1) {
            taskStatusText = "Принята к выполнению";
        } else if (taskStatus==2) {
            taskStatusText = "Ждёт проверки";
        } else if (taskStatus==3) {
            taskStatusText = "Выполненна неверно";
        }else if (taskStatus==4) {
            taskStatusText = "Выполненна";
        } else {
            taskStatusText = "Неизвестный статус";
        }
        return taskStatusText;
    }

    private int getTaskColor(int position) {
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
        return taskColor;
    }
}
