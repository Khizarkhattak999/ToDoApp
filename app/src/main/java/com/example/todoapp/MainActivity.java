package com.example.todoapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnClicked {
    RecyclerView recyclerView,rvTasks;
    ExtendedFloatingActionButton floatingActionButton;
    private final Date currentDate = getCurrentDateTime();
    @SuppressLint("SimpleDateFormat")
    private final String dateFormat = new SimpleDateFormat("d-MM-yyyy").format(currentDate);
    TextView textView;
    DBHandler dbHandler;
    EditText etSearch;
    private ArrayList<AddTask> tasks;
    private ArrayList<AddTask> filteredItems;
    TaskAdapter taskAdapter;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    String formattedDate="";
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Proceed with showing notifications
                } else {
                    // Permission is denied. Inform the user about the importance of notification permission
                }
            });
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView = findViewById(R.id.rvCalender);
        rvTasks=findViewById(R.id.rvTasks);
        floatingActionButton=findViewById(R.id.btnAdd);
        dbHandler=new DBHandler(MainActivity.this);
        textView=findViewById(R.id.textView);
        etSearch=findViewById(R.id.etSearch);
        tasks=new ArrayList<>();
        filteredItems=new ArrayList<>();


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd,MMM", Locale.getDefault());
        formattedDate = sdf.format(calendar.getTime());

        tasks = dbHandler.readTODOList(formattedDate);
        taskAdapter=new TaskAdapter(tasks,this);
        rvTasks.setAdapter(taskAdapter);
        checkNotificationPermission();
        textView.setText("Pending Tasks ("+tasks.size()+")");
        String[] currentDateSeparated = dateFormat.split("-");
        int currentDay = Integer.parseInt(currentDateSeparated[0]);
        int currentMonth = Integer.parseInt(currentDateSeparated[1]);
        setUpHorizontalDate(currentDay,currentMonth);

        searchByTitle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestExactAlarmPermission();
        }
        floatingActionButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,AddTodoActivity.class)));
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestExactAlarmPermission() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            }
            startActivity(intent);
        }
    }
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission();
            }
        }
    }

    private void requestNotificationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
            // Show an explanation to the user why the permission is necessary
            new AlertDialog.Builder(this)
                    .setTitle("Notification Permission Needed")
                    .setMessage("This app needs the Notification permission to alert you of important updates.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // User cancelled the dialog
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        } else {
            // No explanation needed; request the permission
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void searchByTitle() {
       etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void filter(String text) {
        filteredItems.clear();
        if (text.isEmpty()) {
            filteredItems.addAll(tasks);
        } else {
            for (AddTask item : tasks) {
                if (item.getTitleName().toLowerCase().contains(text.toLowerCase())) {
                    filteredItems.add(item);
                }
            }
        }
        textView.setText("Pending Tasks ("+filteredItems.size()+")");

        taskAdapter=new TaskAdapter(filteredItems,this);
        rvTasks.setAdapter(taskAdapter);
    }
    public Date getCurrentDateTime() {
        return Calendar.getInstance().getTime();
    }
    private void setUpHorizontalDate(int currentDate, int currentMonth) {
        int position = 0;
        switch (currentMonth) {
            case 1:
                position = (currentDate - 1);
                break;
            case 2:
                position = 31 + (currentDate - 1);
                break;
            case 3:
                position = 31 + 29 + (currentDate - 1);
                break;
            case 4:
                position = 31 + 29 + 31 + (currentDate - 1);
                break;
            case 5:
                position = 31 + 29 + 31 + 30 + (currentDate - 1);
                break;
            case 6:
                position = 31 + 29 + 31 + 30 + 31 + (currentDate - 1);
                break;
            case 7:
                position = 31 + 29 + 31 + 30 + 31 + 30 + (currentDate - 1);
                break;
            case 8:
                position = 31 + 29 + 31 + 30 + 31 + 30 + 31 + (currentDate - 1);
                break;
            case 9:
                position = 31 + 29 + 31 + 30 + 31 + 30 + 31 + 31 + (currentDate - 1);
                break;
            case 10:
                position = 31 + 29 + 31 + 30 + 31 + 30 + 31 + 31 + 29 + (currentDate - 1);
                break;
            case 11:
                position = 31 + 29 + 31 + 30 + 31 + 30 + 31 + 31 + 29 + 31 + (currentDate - 1);
                break;
            case 12:
                position = 31 + 29 + 31 + 30 + 31 + 30 + 31 + 31 + 29 + 31 + 30 + (currentDate - 1);
                break;
        }

        Constants.calenderDate = position;
        ArrayList<HorizontalCalender> data = new ArrayList<>();
        data.add(new HorizontalCalender(1, "Jan", "Mon"));
        data.add(new HorizontalCalender(2, "Jan", "Tue"));
        data.add(new HorizontalCalender(3, "Jan", "Wed"));
        data.add(new HorizontalCalender(4, "Jan", "Thu"));
        data.add(new HorizontalCalender(5, "Jan", "Fri"));
        data.add(new HorizontalCalender(6, "Jan", "Sat"));
        data.add(new HorizontalCalender(7, "Jan", "Sun"));
        data.add(new HorizontalCalender(8, "Jan", "Mon"));
        data.add(new HorizontalCalender(9, "Jan", "Tue"));
        data.add(new HorizontalCalender(10, "Jan", "Wed"));
        data.add(new HorizontalCalender(11, "Jan", "Thu"));
        data.add(new HorizontalCalender(12, "Jan", "Fri"));
        data.add(new HorizontalCalender(13, "Jan", "Sat"));
        data.add(new HorizontalCalender(14, "Jan", "Sun"));
        data.add(new HorizontalCalender(15, "Jan", "Mon"));
        data.add(new HorizontalCalender(16, "Jan", "Tue"));
        data.add(new HorizontalCalender(17, "Jan", "Wed"));
        data.add(new HorizontalCalender(18, "Jan", "Thu"));
        data.add(new HorizontalCalender(19, "Jan", "Fri"));
        data.add(new HorizontalCalender(20, "Jan", "Sat"));
        data.add(new HorizontalCalender(21, "Jan", "Sun"));
        data.add(new HorizontalCalender(22, "Jan", "Mon"));
        data.add(new HorizontalCalender(23, "Jan", "Tue"));
        data.add(new HorizontalCalender(24, "Jan", "Wed"));
        data.add(new HorizontalCalender(25, "Jan", "Thu"));
        data.add(new HorizontalCalender(26, "Jan", "Fri"));
        data.add(new HorizontalCalender(27, "Jan", "Sat"));
        data.add(new HorizontalCalender(28, "Jan", "Sun"));
        data.add(new HorizontalCalender(29, "Jan", "Mon"));
        data.add(new HorizontalCalender(30, "Jan", "Tue"));
        data.add(new HorizontalCalender(31, "Jan", "Wed"));


        data.add(new HorizontalCalender(1, "Feb", "Thu"));
        data.add(new HorizontalCalender(2, "Feb", "Fri"));
        data.add(new HorizontalCalender(3, "Feb", "Sat"));
        data.add(new HorizontalCalender(4, "Feb", "Sun"));
        data.add(new HorizontalCalender(5, "Feb", "Mon"));
        data.add(new HorizontalCalender(6, "Feb", "Tue"));
        data.add(new HorizontalCalender(7, "Feb", "Wed"));
        data.add(new HorizontalCalender(8, "Feb", "Thu"));
        data.add(new HorizontalCalender(9, "Feb", "Fri"));
        data.add(new HorizontalCalender(10, "Feb", "Sat"));
        data.add(new HorizontalCalender(11, "Feb", "Sun"));
        data.add(new HorizontalCalender(12, "Feb", "Mon"));
        data.add(new HorizontalCalender(13, "Feb", "Tue"));
        data.add(new HorizontalCalender(14, "Feb", "Wed"));
        data.add(new HorizontalCalender(15, "Feb", "Thu"));
        data.add(new HorizontalCalender(16, "Feb", "Fri"));
        data.add(new HorizontalCalender(17, "Feb", "Sat"));
        data.add(new HorizontalCalender(18, "Feb", "Sun"));
        data.add(new HorizontalCalender(19, "Feb", "Mon"));
        data.add(new HorizontalCalender(20, "Feb", "Tue"));
        data.add(new HorizontalCalender(21, "Feb", "Wed"));
        data.add(new HorizontalCalender(22, "Feb", "Thu"));
        data.add(new HorizontalCalender(23, "Feb", "Fri"));
        data.add(new HorizontalCalender(24, "Feb", "Sat"));
        data.add(new HorizontalCalender(25, "Feb", "Sun"));
        data.add(new HorizontalCalender(26, "Feb", "Mon"));
        data.add(new HorizontalCalender(27, "Feb", "Tue"));
        data.add(new HorizontalCalender(28, "Feb", "Wed"));
        data.add(new HorizontalCalender(29, "Feb", "Thu"));

        data.add(new HorizontalCalender(1, "Mar", "Fri"));
        data.add(new HorizontalCalender(2, "Mar", "Sat"));
        data.add(new HorizontalCalender(3, "Mar", "Sun"));
        data.add(new HorizontalCalender(4, "Mar", "Mon"));
        data.add(new HorizontalCalender(5, "Mar", "Tue"));
        data.add(new HorizontalCalender(6, "Mar", "Wed"));
        data.add(new HorizontalCalender(7, "Mar", "Thu"));
        data.add(new HorizontalCalender(8, "Mar", "Fri"));
        data.add(new HorizontalCalender(9, "Mar", "Sat"));
        data.add(new HorizontalCalender(10, "Mar", "Sun"));
        data.add(new HorizontalCalender(11, "Mar", "Mon"));
        data.add(new HorizontalCalender(12, "Mar", "Tue"));
        data.add(new HorizontalCalender(13, "Mar", "Wed"));
        data.add(new HorizontalCalender(14, "Mar", "Thu"));
        data.add(new HorizontalCalender(15, "Mar", "Fri"));
        data.add(new HorizontalCalender(16, "Mar", "Sat"));
        data.add(new HorizontalCalender(17, "Mar", "Sun"));
        data.add(new HorizontalCalender(18, "Mar", "Mon"));
        data.add(new HorizontalCalender(19, "Mar", "Tue"));
        data.add(new HorizontalCalender(20, "Mar", "Wed"));
        data.add(new HorizontalCalender(21, "Mar", "Thu"));
        data.add(new HorizontalCalender(22, "Mar", "Fri"));
        data.add(new HorizontalCalender(23, "Mar", "Sat"));
        data.add(new HorizontalCalender(24, "Mar", "Sun"));
        data.add(new HorizontalCalender(25, "Mar", "Mon"));
        data.add(new HorizontalCalender(26, "Mar", "Tue"));
        data.add(new HorizontalCalender(27, "Mar", "Wed"));
        data.add(new HorizontalCalender(28, "Mar", "Thu"));
        data.add(new HorizontalCalender(29, "Mar", "Fri"));
        data.add(new HorizontalCalender(30, "Mar", "Sat"));
        data.add(new HorizontalCalender(31, "Mar", "Sun"));


        data.add(new HorizontalCalender(1, "Apr", "Mon"));
        data.add(new HorizontalCalender(2, "Apr", "Tue"));
        data.add(new HorizontalCalender(3, "Apr", "Wed"));
        data.add(new HorizontalCalender(4, "Apr", "Thu"));
        data.add(new HorizontalCalender(5, "Apr", "Fri"));
        data.add(new HorizontalCalender(6, "Apr", "Sat"));
        data.add(new HorizontalCalender(7, "Apr", "Sun"));
        data.add(new HorizontalCalender(8, "Apr", "Mon"));
        data.add(new HorizontalCalender(9, "Apr", "Tue"));
        data.add(new HorizontalCalender(10, "Apr", "Wed"));
        data.add(new HorizontalCalender(11, "Apr", "Thu"));
        data.add(new HorizontalCalender(12, "Apr", "Fri"));
        data.add(new HorizontalCalender(13, "Apr", "Sat"));
        data.add(new HorizontalCalender(14, "Apr", "Sun"));
        data.add(new HorizontalCalender(15, "Apr", "Mon"));
        data.add(new HorizontalCalender(16, "Apr", "Tue"));
        data.add(new HorizontalCalender(17, "Apr", "Wed"));
        data.add(new HorizontalCalender(18, "Apr", "Thu"));
        data.add(new HorizontalCalender(19, "Apr", "Fri"));
        data.add(new HorizontalCalender(20, "Apr", "Sat"));
        data.add(new HorizontalCalender(21, "Apr", "Sun"));
        data.add(new HorizontalCalender(22, "Apr", "Mon"));
        data.add(new HorizontalCalender(23, "Apr", "Tue"));
        data.add(new HorizontalCalender(24, "Apr", "Wed"));
        data.add(new HorizontalCalender(25, "Apr", "Thu"));
        data.add(new HorizontalCalender(26, "Apr", "Fri"));
        data.add(new HorizontalCalender(27, "Apr", "Sat"));
        data.add(new HorizontalCalender(28, "Apr", "Sun"));
        data.add(new HorizontalCalender(29, "Apr", "Mon"));
        data.add(new HorizontalCalender(30, "Apr", "Tue"));

        data.add(new HorizontalCalender(1, "May", "Wed"));
        data.add(new HorizontalCalender(2, "May", "Thu"));
        data.add(new HorizontalCalender(3, "May", "Fri"));
        data.add(new HorizontalCalender(4, "May", "Sat"));
        data.add(new HorizontalCalender(5, "May", "Sun"));
        data.add(new HorizontalCalender(6, "May", "Mon"));
        data.add(new HorizontalCalender(7, "May", "Tue"));
        data.add(new HorizontalCalender(8, "May", "Wed"));
        data.add(new HorizontalCalender(9, "May", "Thu"));
        data.add(new HorizontalCalender(10, "May", "Fri"));
        data.add(new HorizontalCalender(11, "May", "Sat"));
        data.add(new HorizontalCalender(12, "May", "Sun"));
        data.add(new HorizontalCalender(13, "May", "Mon"));
        data.add(new HorizontalCalender(14, "May", "Tue"));
        data.add(new HorizontalCalender(15, "May", "Wed"));
        data.add(new HorizontalCalender(16, "May", "Thu"));
        data.add(new HorizontalCalender(17, "May", "Fri"));
        data.add(new HorizontalCalender(18, "May", "Sat"));
        data.add(new HorizontalCalender(19, "May", "Sun"));
        data.add(new HorizontalCalender(20, "May", "Mon"));
        data.add(new HorizontalCalender(21, "May", "Tue"));
        data.add(new HorizontalCalender(22, "May", "Wed"));
        data.add(new HorizontalCalender(23, "May", "Thu"));
        data.add(new HorizontalCalender(24, "May", "Fri"));
        data.add(new HorizontalCalender(25, "May", "Sat"));
        data.add(new HorizontalCalender(26, "May", "Sun"));
        data.add(new HorizontalCalender(27, "May", "Mon"));
        data.add(new HorizontalCalender(28, "May", "Tue"));
        data.add(new HorizontalCalender(29, "May", "Wed"));
        data.add(new HorizontalCalender(30, "May", "Thu"));
        data.add(new HorizontalCalender(31, "May", "Fri"));

        data.add(new HorizontalCalender(1, "June", "Sat"));
        data.add(new HorizontalCalender(2, "June", "Sun"));
        data.add(new HorizontalCalender(3, "June", "Mon"));
        data.add(new HorizontalCalender(4, "June", "Tue"));
        data.add(new HorizontalCalender(5, "June", "Wed"));
        data.add(new HorizontalCalender(6, "June", "Thu"));
        data.add(new HorizontalCalender(7, "June", "Fri"));
        data.add(new HorizontalCalender(8, "June", "Sat"));
        data.add(new HorizontalCalender(9, "June", "Sun"));
        data.add(new HorizontalCalender(10, "June", "Mon"));
        data.add(new HorizontalCalender(11, "June", "Tue"));
        data.add(new HorizontalCalender(12, "June", "Wed"));
        data.add(new HorizontalCalender(13, "June", "Thu"));
        data.add(new HorizontalCalender(14, "June", "Fri"));
        data.add(new HorizontalCalender(15, "June", "Sat"));
        data.add(new HorizontalCalender(16, "June", "Sun"));
        data.add(new HorizontalCalender(17, "June", "Mon"));
        data.add(new HorizontalCalender(18, "June", "Tue"));
        data.add(new HorizontalCalender(19, "June", "Wed"));
        data.add(new HorizontalCalender(20, "June", "Thu"));
        data.add(new HorizontalCalender(21, "June", "Fri"));
        data.add(new HorizontalCalender(22, "June", "Sat"));
        data.add(new HorizontalCalender(23, "June", "Sun"));
        data.add(new HorizontalCalender(24, "June", "Mon"));
        data.add(new HorizontalCalender(25, "June", "Tue"));
        data.add(new HorizontalCalender(26, "June", "Wed"));
        data.add(new HorizontalCalender(27, "June", "Thu"));
        data.add(new HorizontalCalender(28, "June", "Fri"));
        data.add(new HorizontalCalender(29, "June", "Sat"));
        data.add(new HorizontalCalender(30, "June", "Sun"));

        data.add(new HorizontalCalender(1, "July", "Mon"));
        data.add(new HorizontalCalender(2, "July", "Tue"));
        data.add(new HorizontalCalender(3, "July", "Wed"));
        data.add(new HorizontalCalender(4, "July", "Thu"));
        data.add(new HorizontalCalender(5, "July", "Fri"));
        data.add(new HorizontalCalender(6, "July", "Sat"));
        data.add(new HorizontalCalender(7, "July", "Sun"));
        data.add(new HorizontalCalender(8, "July", "Mon"));
        data.add(new HorizontalCalender(9, "July", "Tue"));
        data.add(new HorizontalCalender(10, "July", "Wed"));
        data.add(new HorizontalCalender(11, "July", "Thu"));
        data.add(new HorizontalCalender(12, "July", "Fri"));
        data.add(new HorizontalCalender(13, "July", "Sat"));
        data.add(new HorizontalCalender(14, "July", "Sun"));
        data.add(new HorizontalCalender(15, "July", "Mon"));
        data.add(new HorizontalCalender(16, "July", "Tue"));
        data.add(new HorizontalCalender(17, "July", "Wed"));
        data.add(new HorizontalCalender(18, "July", "Thu"));
        data.add(new HorizontalCalender(19, "July", "Fri"));
        data.add(new HorizontalCalender(20, "July", "Sat"));
        data.add(new HorizontalCalender(21, "July", "Sun"));
        data.add(new HorizontalCalender(22, "July", "Mon"));
        data.add(new HorizontalCalender(23, "July", "Tue"));
        data.add(new HorizontalCalender(24, "July", "Wed"));
        data.add(new HorizontalCalender(25, "July", "Thu"));
        data.add(new HorizontalCalender(26, "July", "Fri"));
        data.add(new HorizontalCalender(27, "July", "Sat"));
        data.add(new HorizontalCalender(28, "July", "Sun"));
        data.add(new HorizontalCalender(29, "July", "Mon"));
        data.add(new HorizontalCalender(30, "July", "Tue"));
        data.add(new HorizontalCalender(31, "July", "Wed"));


        data.add(new HorizontalCalender(1, "Aug", "Thu"));
        data.add(new HorizontalCalender(2, "Aug", "Fri"));
        data.add(new HorizontalCalender(3, "Aug", "Sat"));
        data.add(new HorizontalCalender(4, "Aug", "Sun"));
        data.add(new HorizontalCalender(5, "Aug", "Mon"));
        data.add(new HorizontalCalender(6, "Aug", "Tue"));
        data.add(new HorizontalCalender(7, "Aug", "Wed"));
        data.add(new HorizontalCalender(8, "Aug", "Thu"));
        data.add(new HorizontalCalender(9, "Aug", "Fri"));
        data.add(new HorizontalCalender(10, "Aug", "Sat"));
        data.add(new HorizontalCalender(11, "Aug", "Sun"));
        data.add(new HorizontalCalender(12, "Aug", "Mon"));
        data.add(new HorizontalCalender(13, "Aug", "Tue"));
        data.add(new HorizontalCalender(14, "Aug", "Wed"));
        data.add(new HorizontalCalender(15, "Aug", "Thu"));
        data.add(new HorizontalCalender(16, "Aug", "Fri"));
        data.add(new HorizontalCalender(17, "Aug", "Sat"));
        data.add(new HorizontalCalender(18, "Aug", "Sun"));
        data.add(new HorizontalCalender(19, "Aug", "Mon"));
        data.add(new HorizontalCalender(20, "Aug", "Tue"));
        data.add(new HorizontalCalender(21, "Aug", "Wed"));
        data.add(new HorizontalCalender(22, "Aug", "Thu"));
        data.add(new HorizontalCalender(23, "Aug", "Fri"));
        data.add(new HorizontalCalender(24, "Aug", "Sat"));
        data.add(new HorizontalCalender(25, "Aug", "Sun"));
        data.add(new HorizontalCalender(26, "Aug", "Mon"));
        data.add(new HorizontalCalender(27, "Aug", "Tue"));
        data.add(new HorizontalCalender(28, "Aug", "Wed"));
        data.add(new HorizontalCalender(29, "Aug", "Thu"));
        data.add(new HorizontalCalender(30, "Aug", "Fri"));
        data.add(new HorizontalCalender(31, "Aug", "Sat"));

        data.add(new HorizontalCalender(1, "Sep", "Sun"));
        data.add(new HorizontalCalender(2, "Sep", "Mon"));
        data.add(new HorizontalCalender(3, "Sep", "Tue"));
        data.add(new HorizontalCalender(4, "Sep", "Wed"));
        data.add(new HorizontalCalender(5, "Sep", "Thu"));
        data.add(new HorizontalCalender(6, "Sep", "Fri"));
        data.add(new HorizontalCalender(7, "Sep", "Sat"));
        data.add(new HorizontalCalender(8, "Sep", "Sun"));
        data.add(new HorizontalCalender(9, "Sep", "Mon"));
        data.add(new HorizontalCalender(10, "Sep", "Tue"));
        data.add(new HorizontalCalender(11, "Sep", "Wed"));
        data.add(new HorizontalCalender(12, "Sep", "Thu"));
        data.add(new HorizontalCalender(13, "Sep", "Fri"));
        data.add(new HorizontalCalender(14, "Sep", "Sat"));
        data.add(new HorizontalCalender(15, "Sep", "Sun"));
        data.add(new HorizontalCalender(16, "Sep", "Mon"));
        data.add(new HorizontalCalender(17, "Sep", "Tue"));
        data.add(new HorizontalCalender(18, "Sep", "Wed"));
        data.add(new HorizontalCalender(19, "Sep", "Thu"));
        data.add(new HorizontalCalender(20, "Sep", "Fri"));
        data.add(new HorizontalCalender(21, "Sep", "Sat"));
        data.add(new HorizontalCalender(22, "Sep", "Sun"));
        data.add(new HorizontalCalender(23, "Sep", "Mon"));
        data.add(new HorizontalCalender(24, "Sep", "Tue"));
        data.add(new HorizontalCalender(25, "Sep", "Wed"));
        data.add(new HorizontalCalender(26, "Sep", "Thu"));
        data.add(new HorizontalCalender(27, "Sep", "Fri"));
        data.add(new HorizontalCalender(28, "Sep", "Sat"));
        data.add(new HorizontalCalender(29, "Sep", "Sun"));
        data.add(new HorizontalCalender(30, "Sep", "Mon"));


        data.add(new HorizontalCalender(1, "Oct", "Tue"));
        data.add(new HorizontalCalender(2, "Oct", "Wed"));
        data.add(new HorizontalCalender(3, "Oct", "Thu"));
        data.add(new HorizontalCalender(4, "Oct", "Fri"));
        data.add(new HorizontalCalender(5, "Oct", "Sat"));
        data.add(new HorizontalCalender(6, "Oct", "Sun"));
        data.add(new HorizontalCalender(7, "Oct", "Mon"));
        data.add(new HorizontalCalender(8, "Oct", "Tue"));
        data.add(new HorizontalCalender(9, "Oct", "Wed"));
        data.add(new HorizontalCalender(10, "Oct", "Thu"));
        data.add(new HorizontalCalender(11, "Oct", "Fri"));
        data.add(new HorizontalCalender(12, "Oct", "Sat"));
        data.add(new HorizontalCalender(13, "Oct", "Sun"));
        data.add(new HorizontalCalender(14, "Oct", "Mon"));
        data.add(new HorizontalCalender(15, "Oct", "Tue"));
        data.add(new HorizontalCalender(16, "Oct", "Wed"));
        data.add(new HorizontalCalender(17, "Oct", "Thu"));
        data.add(new HorizontalCalender(18, "Oct", "Fri"));
        data.add(new HorizontalCalender(19, "Oct", "Sat"));
        data.add(new HorizontalCalender(20, "Oct", "Sun"));
        data.add(new HorizontalCalender(21, "Oct", "Mon"));
        data.add(new HorizontalCalender(22, "Oct", "Tue"));
        data.add(new HorizontalCalender(23, "Oct", "Wed"));
        data.add(new HorizontalCalender(24, "Oct", "Thu"));
        data.add(new HorizontalCalender(25, "Oct", "Fri"));
        data.add(new HorizontalCalender(26, "Oct", "Sat"));
        data.add(new HorizontalCalender(27, "Oct", "Sun"));
        data.add(new HorizontalCalender(28, "Oct", "Mon"));
        data.add(new HorizontalCalender(29, "Oct", "Tue"));
        data.add(new HorizontalCalender(30, "Oct", "Wed"));
        data.add(new HorizontalCalender(31, "Oct", "Thu"));


        data.add(new HorizontalCalender(1, "Nov", "Fri"));
        data.add(new HorizontalCalender(2, "Nov", "Sat"));
        data.add(new HorizontalCalender(3, "Nov", "Sun"));
        data.add(new HorizontalCalender(4, "Nov", "Mon"));
        data.add(new HorizontalCalender(5, "Nov", "Tue"));
        data.add(new HorizontalCalender(6, "Nov", "Wed"));
        data.add(new HorizontalCalender(7, "Nov", "Thu"));
        data.add(new HorizontalCalender(8, "Nov", "Fri"));
        data.add(new HorizontalCalender(9, "Nov", "Sat"));
        data.add(new HorizontalCalender(10, "Nov", "Sun"));
        data.add(new HorizontalCalender(11, "Nov", "Mon"));
        data.add(new HorizontalCalender(12, "Nov", "Tue"));
        data.add(new HorizontalCalender(13, "Nov", "Wed"));
        data.add(new HorizontalCalender(14, "Nov", "Thu"));
        data.add(new HorizontalCalender(15, "Nov", "Fri"));
        data.add(new HorizontalCalender(16, "Nov", "Sat"));
        data.add(new HorizontalCalender(17, "Nov", "Sun"));
        data.add(new HorizontalCalender(18, "Nov", "Mon"));
        data.add(new HorizontalCalender(19, "Nov", "Tue"));
        data.add(new HorizontalCalender(20, "Nov", "Wed"));
        data.add(new HorizontalCalender(21, "Nov", "Thu"));
        data.add(new HorizontalCalender(22, "Nov", "Fri"));
        data.add(new HorizontalCalender(23, "Nov", "Sat"));
        data.add(new HorizontalCalender(24, "Nov", "Sun"));
        data.add(new HorizontalCalender(25, "Nov", "Mon"));
        data.add(new HorizontalCalender(26, "Nov", "Tue"));
        data.add(new HorizontalCalender(27, "Nov", "Wed"));
        data.add(new HorizontalCalender(28, "Nov", "Thu"));
        data.add(new HorizontalCalender(29, "Nov", "Fri"));
        data.add(new HorizontalCalender(30, "Nov", "Sat"));


        data.add(new HorizontalCalender(1, "Dec", "Sun"));
        data.add(new HorizontalCalender(2, "Dec", "Mon"));
        data.add(new HorizontalCalender(3, "Dec", "Tue"));
        data.add(new HorizontalCalender(4, "Dec", "Wed"));
        data.add(new HorizontalCalender(5, "Dec", "Thu"));
        data.add(new HorizontalCalender(6, "Dec", "Fri"));
        data.add(new HorizontalCalender(7, "Dec", "Sat"));
        data.add(new HorizontalCalender(8, "Dec", "Sun"));
        data.add(new HorizontalCalender(9, "Dec", "Mon"));
        data.add(new HorizontalCalender(10, "Dec", "Tue"));
        data.add(new HorizontalCalender(11, "Dec", "Wed"));
        data.add(new HorizontalCalender(12, "Dec", "Thu"));
        data.add(new HorizontalCalender(13, "Dec", "Fri"));
        data.add(new HorizontalCalender(14, "Dec", "Sat"));
        data.add(new HorizontalCalender(15, "Dec", "Sun"));
        data.add(new HorizontalCalender(16, "Dec", "Mon"));
        data.add(new HorizontalCalender(17, "Dec", "Tue"));
        data.add(new HorizontalCalender(18, "Dec", "Wed"));
        data.add(new HorizontalCalender(19, "Dec", "Thu"));
        data.add(new HorizontalCalender(20, "Dec", "Fri"));
        data.add(new HorizontalCalender(21, "Dec", "Sat"));
        data.add(new HorizontalCalender(22, "Dec", "Sun"));
        data.add(new HorizontalCalender(23, "Dec", "Mon"));
        data.add(new HorizontalCalender(24, "Dec", "Tue"));
        data.add(new HorizontalCalender(25, "Dec", "Wed"));
        data.add(new HorizontalCalender(26, "Dec", "Thu"));
        data.add(new HorizontalCalender(27, "Dec", "Fri"));
        data.add(new HorizontalCalender(28, "Dec", "Sat"));
        data.add(new HorizontalCalender(29, "Dec", "Sun"));
        data.add(new HorizontalCalender(30, "Dec", "Mon"));
        data.add(new HorizontalCalender(31, "Dec", "Tue"));

        HorizontalCalenderAdapter adapter = new HorizontalCalenderAdapter(data, this, this);
        recyclerView.setAdapter(adapter);
        if (position > 4) {
            recyclerView.scrollToPosition(position);
        } else {
            recyclerView.scrollToPosition(position);
        }
    }

    @Override
    public void onItemClicked(int pos, String name) {
        String formattedPos = String.format("%02d", pos);
        String selectedDate = String.format("%s,%s", formattedPos, name);

        tasks.clear();
        tasks = dbHandler.readTODOList(selectedDate);
        taskAdapter=new TaskAdapter(tasks,this);
        rvTasks.setAdapter(taskAdapter);
        textView.setText("Pending Tasks ("+tasks.size()+")");
    }

    @Override
    protected void onResume() {
        super.onResume();
        tasks.clear();
        tasks = dbHandler.readTODOList(formattedDate);
        taskAdapter=new TaskAdapter(tasks,this);
        rvTasks.setAdapter(taskAdapter);
        textView.setText("Pending Tasks ("+tasks.size()+")");
    }
}