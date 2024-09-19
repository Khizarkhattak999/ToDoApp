package com.example.todoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTodoActivity extends AppCompatActivity {
    ImageView ivCamera,ivImage;
    TextInputEditText etTitle,etDescription;
    DatePicker datePicker;
    TimePicker timePicker;
    MaterialButton btnAdd;
    private static final int PERMISSION_ID = 44;
    private static final int pic_id = 123;
    byte[] byteArray;
    DBHandler dbHandler;
    AddTask addTask;
    private int selectedYear, selectedMonth, selectedDay;
    private int selectedHour, selectedMinute;
    private boolean isDateSelected = false;
    private boolean isTimeSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_todo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        dbHandler = new DBHandler(AddTodoActivity.this);
        addTask = new AddTask();
        ivCamera.setOnClickListener(view -> {
            if (checkPermissions()) {
                openCamera();
            } else {
                requestPermissions();
            }
        });

        btnAdd.setOnClickListener(view -> {
            AddTODOItem();
        });

        timeAndDatePicker();
    }

    private void timeAndDatePicker() {
        datePicker.setMinDate(System.currentTimeMillis());
        datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    selectedYear = year;
                    selectedMonth = monthOfYear;
                    selectedDay = dayOfMonth;
                    isDateSelected = true;
                });
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
            isTimeSelected = true;
        });
    }

    private void AddTODOItem() {
        String titleName = etTitle.getText().toString();
        String titleDesc = etDescription.getText().toString();

        if (TextUtils.isEmpty(titleName) || TextUtils.isEmpty(titleDesc)) {
            Toast.makeText(getApplicationContext(), "Please Fill All The Fields", Toast.LENGTH_SHORT).show();
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd,MMM", Locale.getDefault());
        calendar.setTimeInMillis(currentTimeMillis);
        if (isDateSelected) {
            calendar.set(Calendar.YEAR, selectedYear);
            calendar.set(Calendar.MONTH, selectedMonth);
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
        }

        // Use the selected time if provided
        if (isTimeSelected) {
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            calendar.set(Calendar.MINUTE, selectedMinute);
            calendar.set(Calendar.SECOND, 0);
        }
        String selectedDateFormatted = sdf.format(calendar.getTime());
        long taskDateTime = calendar.getTimeInMillis();
        int requestCode = (int) (System.currentTimeMillis() & 0xFFFFFFF);

        AlarmHelper.scheduleAlarm(this, taskDateTime, titleName, titleDesc,  requestCode);

        dbHandler.AddNewTODOItem(titleName, titleDesc, byteArray, taskDateTime,selectedDateFormatted);
        Toast.makeText(AddTodoActivity.this, "Task Added to TO-DO List", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        }, 2000);
    }
    private void initViews() {
        ivCamera=findViewById(R.id.ibCamer);
        ivImage=findViewById(R.id.ivUserImg);
        etTitle=findViewById(R.id.etTitleName);
        etDescription=findViewById(R.id.etTitleDesc);
        datePicker=findViewById(R.id.datePicker);
        timePicker=findViewById(R.id.timePicker);
        btnAdd=findViewById(R.id.btnSave);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, pic_id);
        }
    }


    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(AddTodoActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(AddTodoActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(AddTodoActivity.this, new String[]{
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_ID);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permissions are required to use the camera", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                if (photo != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                    ivImage.setImageBitmap(photo);
                }
            }
        }
    }
}