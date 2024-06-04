package com.example.dolt.bottomnav.tasks;

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

import java.util.HashMap;
import java.util.Map;

public class TasksFragment extends Fragment {
    private FragmentTasksBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);

        getParentFragmentManager().beginTransaction().replace(binding.fragmentContainer1.getId(), new MyTasksFragment()).commit();
        binding.tasksNav.setSelectedItemId(R.id.myTasks);

        Map<Integer, Fragment> fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.myTasks, new MyTasksFragment());
        fragmentMap.put(R.id.incomingTasks, new IncomingTasksFragment());
        fragmentMap.put(R.id.sentTasks, new SentTasksFragment());


        binding.tasksNav.setOnItemSelectedListener(item -> {
            Fragment fragment = fragmentMap.get(item.getItemId());

            assert fragment != null;
            getParentFragmentManager().beginTransaction().replace(binding.fragmentContainer1.getId(), fragment).commit();

            return true;
        });


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(TasksFragment.this.getContext(), AddNewTask.class);
                i.putExtra("isUpdate", false);

                startActivity(i);
            }
        });

        return binding.getRoot();
    }


}
