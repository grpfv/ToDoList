package com.example.todo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.Calendar;

public class TaskAdapter extends FirestoreRecyclerAdapter<TaskModel, TaskAdapter.TaskViewHolder> {

    Context context;

    public TaskAdapter(@NonNull FirestoreRecyclerOptions<TaskModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull TaskViewHolder holder, int position, @NonNull TaskModel taskModel) {
        holder.taskTextView.setText(taskModel.title);
        holder.dueDateTextView.setText(taskModel.dueDay);

        setupAlarmForTask(taskModel, position);
    }

    private void setupAlarmForTask(TaskModel taskmodel, int position) {
        // Get due date and time components
        String[] dateComponents = taskmodel.getDueDay().split("/");
        int year = Integer.parseInt(dateComponents[2]);
        int month = Integer.parseInt(dateComponents[0]); // Month is 0-based
        int day = Integer.parseInt(dateComponents[1]);

        String[] timeAndAmPm = taskmodel.getDueTime().split(" ");
        String time = timeAndAmPm[0];

        String[] timeComponents = time.split(":");
        int hour = Integer.parseInt(timeComponents[0]);
        int minute = Integer.parseInt(timeComponents[1]);

        if (timeAndAmPm[1].equalsIgnoreCase("PM")) {
            hour += 12;
        }

        // Set up AlarmManager to trigger the AlarmReceiver at the specified due date and time
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, position, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1); // Month is 0-based
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            // The due date and time have already passed; no need to set the alarm
            Log.d("AlarmSkipped", "Skipped setting alarm for task at position " + position);
            return;
        }

        long alarmTime = calendar.getTimeInMillis();
        Log.d("AlarmTime", "Calculated alarm time: " + alarmTime);

        // Set a one-time alarm
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_todo_tasks, parent,false);
        return new TaskViewHolder(view);
    }



    class TaskViewHolder extends RecyclerView.ViewHolder{
        TextView taskTextView, dueDateTextView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTextView = itemView.findViewById(R.id.taskTextView);
            dueDateTextView = itemView.findViewById(R.id.dueDateTextView);
        }
    }
}
