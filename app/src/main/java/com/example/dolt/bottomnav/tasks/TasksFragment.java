package com.example.dolt.bottomnav.tasks;

import static com.example.dolt.DifferentMethods.makeToast;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dolt.AddNewTask;
import com.example.dolt.R;
import com.example.dolt.databinding.FragmentTasksBinding;
import com.example.dolt.utils.DatabaseTasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class TasksFragment extends Fragment {
    private FragmentTasksBinding binding;

    int startFragmentId;
    Fragment startFragment;
    DatabaseTasks databaseTasks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        databaseTasks = new DatabaseTasks(TasksFragment.this.getContext());

        AtomicReference<String> currentFragment = new AtomicReference<>("myTasksFragment");
        if (savedInstanceState == null) {
            startFragmentId = R.id.myTasks;
            startFragment = new MyTasksFragment();
        } else {
            startFragmentId = savedInstanceState.getInt("startFragmentId", 0);
            if (startFragmentId==R.id.myTasks) {
                startFragment = new MyTasksFragment();
            } else if (startFragmentId==R.id.sentTasks) {
                startFragment = new SentTasksFragment();
            } else if (startFragmentId==R.id.incomingTasks) {
                startFragment = new IncomingTasksFragment();
            }
            //makeToast(this.getContext(), String.valueOf(startFragmentId));
        }
        if (requireActivity().getIntent()!=null) {
            if (Objects.requireNonNull(requireActivity().getIntent()).getExtras() != null) {
                if (Objects.requireNonNull(Objects.requireNonNull(requireActivity().getIntent()).getExtras()).getString("fragmentTasks") != null) {
                    String extras = Objects.requireNonNull(Objects.requireNonNull(requireActivity().getIntent()).getExtras()).getString("fragmentTasks");
                    if (Objects.equals(extras, "myTasksFragment")) {
                        startFragment = new MyTasksFragment();
                        startFragmentId = R.id.myTasks;
                        currentFragment.set("myTasksFragment");
                    } else if (Objects.equals(extras, "sentTasksFragment")) {
                        startFragment = new SentTasksFragment();
                        startFragmentId = R.id.sentTasks;
                        currentFragment.set("sentTasksFragment");
                    } else if (Objects.equals(extras, "incomingTasksFragment")) {
                        startFragment = new IncomingTasksFragment();
                        startFragmentId = R.id.incomingTasks;
                        currentFragment.set("incomingTasksFragment");
                    } else {
                        startFragment = new MyTasksFragment();
                        startFragmentId = R.id.myTasks;
                        currentFragment.set("myTasksFragment");
                    }
                }
            }
        }

        getParentFragmentManager().beginTransaction().replace(binding.fragmentContainer1.getId(), startFragment).commit();
        binding.tasksNav.setSelectedItemId(startFragmentId);

        Map<Integer, Fragment> fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.myTasks, new MyTasksFragment());
        fragmentMap.put(R.id.incomingTasks, new IncomingTasksFragment());
        fragmentMap.put(R.id.sentTasks, new SentTasksFragment());

        binding.tasksNav.setOnItemSelectedListener(item -> {
            Fragment fragment = fragmentMap.get(item.getItemId());

            if (Objects.equals(item.getItemId(), R.id.myTasks)) {
                currentFragment.set("myTasksFragment");
            } else if (Objects.equals(item.getItemId(), R.id.sentTasks)) {
                currentFragment.set("sentTasksFragment");
            } else if (Objects.equals(item.getItemId(), R.id.incomingTasks)) {
                currentFragment.set("incomingTasksFragment");
            }

            assert fragment != null;
            getParentFragmentManager().beginTransaction().replace(binding.fragmentContainer1.getId(), fragment).commit();

            return true;
        });

        binding.fab.setOnClickListener(v -> {
            final Intent i = new Intent(TasksFragment.this.getContext(), AddNewTask.class);
            i.putExtra("isUpdate", false);
            i.putExtra("fromFragment", currentFragment.toString());
            startActivity(i);
        });

        binding.updateTasksButton.setOnClickListener(v -> {
            databaseTasks.openDatabase();
            databaseTasks.deleteAllTasks();
            databaseTasks.insertAllTasks(this.getContext());
            databaseTasks.close();
            makeToast(TasksFragment.this.getContext(), "Задачи обновлены");
        });

        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("startFragmentId", binding.tasksNav.getSelectedItemId());
    }

}
