package com.example.todoapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;
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

public class UpdateAndDeleteActivity extends AppCompatActivity {
    TextInputEditText etTitle, etDescription;
    ImageView ivCamera, ivImage;
    MaterialButton btnEdit, btnDelete,btnShare;
    DBHandler dbHandler;
    String titleName, titleDesc;
    private static final int PERMISSION_ID = 44;
    private static final int pic_id = 123;
    byte[] byteArray;
    byte[] imgArray;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_and_delete);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        dbHandler = new DBHandler(this);
        builder = new AlertDialog.Builder(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            titleName = extras.getString("title");
            titleDesc = extras.getString("desc");
        }
        byteArray = getIntent().getByteArrayExtra("image");
        if (byteArray!=null) {
            Drawable drawable = new BitmapDrawable(Utility.getPhoto(byteArray));
            ivImage.setImageDrawable(drawable);
        }
        etTitle.setText(titleName);
        etDescription.setText(titleDesc);

        ivCamera.setOnClickListener(view -> {
            if (checkPermissions()) {
                openCamera();
            } else {
                requestPermissions();
            }
        });
        btnEdit.setOnClickListener(view -> updateTaskList());
        btnDelete.setOnClickListener(view -> {
            builder.setMessage("Are you sure you want to delete this task?").setTitle("Delete Task");
            builder.setMessage("Are you sure you want to delete this task?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> {
                        dbHandler.DeleteTask(titleName);
                        Toast.makeText(getApplicationContext(), "Task Deleted From TODO List", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            }
                        }, 2000);
                    })
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.setTitle("Delete Task");
            alert.show();
        });
        btnShare.setOnClickListener(view -> shareTaskTitle());
    }
    private void shareTaskTitle() {
        String shareBody = etTitle.getText().toString();
        if (TextUtils.isEmpty(shareBody)) {
            Toast.makeText(getApplicationContext(), "Task title is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Task");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }


    private void updateTaskList() {
        if (TextUtils.isEmpty(etTitle.getText().toString()) && TextUtils.isEmpty(etDescription.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Please Fill All The Fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imgArray == null) {
            dbHandler.UpdateTask(titleName, etTitle.getText().toString(), etDescription.getText().toString(), byteArray);
        } else {
            dbHandler.UpdateTask(titleName, etTitle.getText().toString(), etDescription.getText().toString(), imgArray);
        }
        Toast.makeText(getApplicationContext(), " Task Updated", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> startActivity(new Intent(getApplicationContext(), MainActivity.class)), 2000);
    }

    private void openCamera() {
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, pic_id);
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(UpdateAndDeleteActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(UpdateAndDeleteActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(UpdateAndDeleteActivity.this, new String[]{
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

    private void initViews() {
        ivCamera = findViewById(R.id.ibCamer);
        ivImage = findViewById(R.id.ivUserImg);
        etTitle = findViewById(R.id.etTitleName);
        etDescription = findViewById(R.id.etTitleDesc);
        btnDelete = findViewById(R.id.btnDelete);
        btnEdit = findViewById(R.id.btnupdate);
        btnShare=findViewById(R.id.btnShare);
    }

}