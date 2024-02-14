package com.example.todo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.Calendar;
import java.util.Locale;



    public class AddNewTask extends DialogFragment {

        private EditText addTaskTitle, addSubject, addDueTime, addDueday;
        Button btnAdd;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.activity_add_new_task, container, false);

            addTaskTitle = view.findViewById(R.id.addTask);
            addSubject = view.findViewById(R.id.addSubject);
            addDueday = view.findViewById(R.id.addDueday);
            addDueTime = view.findViewById(R.id.addDueTime);

            addDueday.setOnClickListener(view1 -> showDatePicker(addDueday));

            // Set onClickListener for end time EditText
            addDueTime.setOnClickListener(view12 -> showTimePicker(addDueTime));

            btnAdd = view.findViewById(R.id.btnAdd);
            btnAdd.setOnClickListener(v-> saveTask());

            return view;
        }


        void saveTask(){
            String taskTitle = addTaskTitle.getText().toString();
            String taskSubject= addSubject.getText().toString();
            String taskDueday = addDueday.getText().toString();
            String taskDuetime = addDueTime.getText().toString();

            if (taskTitle.isEmpty() || taskTitle == null){
                Toast.makeText(requireContext(), "Please enter Title", Toast.LENGTH_SHORT).show();
                addTaskTitle.setError("Task Title is required");
                return;
            }
            if (taskDueday.isEmpty() || taskDueday == null){
                Toast.makeText(requireContext(), "Please enter Due Day", Toast.LENGTH_SHORT).show();
                addDueday.setError("Due Day is required");
                return;
            }
            if (taskDuetime.isEmpty() || taskDuetime == null){
                Toast.makeText(requireContext(), "Please enter Due Time", Toast.LENGTH_SHORT).show();
                addDueTime.setError("Due Time is required");
                return;
            }

            TaskModel taskmodel = new TaskModel();
            taskmodel.setTitle(taskTitle);
            taskmodel.setSubject(taskSubject);
            taskmodel.setDueDay(taskDueday);
            taskmodel.setDueTime(taskDuetime);

            saveTasktoFirebase(taskmodel);

        }

        void saveTasktoFirebase(TaskModel taskmodel){

            DocumentReference documentReference;
            documentReference = TaskUtility.getCollectionReferenceForToDo().document();

            documentReference.set(taskmodel).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(requireContext(),"Task Added",Toast.LENGTH_SHORT).show();

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
                        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                        Intent alarmIntent = new Intent(requireContext(), NotificationReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month - 1); // Month is 0-based
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);

                        long alarmTime = calendar.getTimeInMillis();
                        Log.d("AlarmTime", "Calculated alarm time: " + alarmTime);

                        // Set a one-time alarm
                        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);

                        dismiss();
                    }else{
                        Toast.makeText(requireContext(),"Failed Adding Task",Toast.LENGTH_SHORT).show();
                        Log.e("FirebaseError", "Error adding task to Firestore", task.getException());
                    }
                }
            });
        }





        private void showTimePicker(final EditText editText) {
            // Inflate the custom layout for the time picker dialog
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_time_picker, null);

            // Create a dialog and set its content view
            final Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(dialogView);

            // Get references to TimePicker and Button in the dialog
            final TimePicker dialogTimePicker = dialogView.findViewById(R.id.dialogTimePicker);
            Button btnSetTime = dialogView.findViewById(R.id.btnSetTime);

            // Set up the TimePicker
            dialogTimePicker.setIs24HourView(false);

            // Set a listener to handle the "Set Time" button click
            btnSetTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int hour = dialogTimePicker.getHour();
                    int minute = dialogTimePicker.getMinute();

                    // Format the selected time in 12-hour format with AM/PM
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d %s",
                            (hour == 0 || hour == 12) ? 12 : hour % 12, minute, (hour < 12) ? "AM" : "PM");

                    // Set the selected time to the EditText
                    editText.setText(selectedTime);

                    // Dismiss the dialog
                    dialog.dismiss();
                }
            });

            // Show the dialog
            dialog.show();
        }

        private void showDatePicker(final EditText editText) {
            // Inflate the custom layout for the date picker dialog
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_date_picker, null);

            // Create a dialog and set its content view
            final Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(dialogView);

            // Get references to DatePicker and Button in the dialog
            final DatePicker dialogDatePicker = dialogView.findViewById(R.id.dialogDatePicker);
            Button btnSetDate = dialogView.findViewById(R.id.btnSetDate);

            // Set a listener to handle the "Set Date" button click
            btnSetDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int year = dialogDatePicker.getYear();
                    int month = dialogDatePicker.getMonth() + 1; // Month is 0-based, so add 1
                    int day = dialogDatePicker.getDayOfMonth();

                    // Format the selected date
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", month, day, year);

                    // Set the selected date to the EditText
                    editText.setText(selectedDate);

                    // Dismiss the dialog
                    dialog.dismiss();
                }
            });

            // Show the dialog
            dialog.show();
        }
    }
