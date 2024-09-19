package com.example.todoapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    ArrayList<AddTask> addTasks;
    Context context;

    public TaskAdapter(ArrayList<AddTask> addTasks, Context context) {
        this.addTasks = addTasks;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AddTask addTask = addTasks.get(position);
        holder.tvTitle.setText(addTask.getTitleName());
        holder.tvDesc.setText(addTask.getTitleDesc());
        String formattedDate = formatTimestamp(addTask.getDate());
        holder.tvDate.setText(formattedDate);
        if (addTask.getTaskImg() != null) {
            Drawable drawable = new BitmapDrawable(Utility.getPhoto(addTask.getTaskImg()));
            holder.ivImg.setImageDrawable(drawable);
        }
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, UpdateAndDeleteActivity.class);
            intent.putExtra("title", addTask.getTitleName());
            intent.putExtra("desc", addTask.getTitleDesc());
            intent.putExtra("image", addTask.getTaskImg());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return addTasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc,tvDate;
        ImageView ivImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDescription);
            ivImg = itemView.findViewById(R.id.circleImageView);
            tvDate=itemView.findViewById(R.id.tvDate);

        }
    }

    public static String formatTimestamp(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd,MMM yyyy hh:mm:a", Locale.getDefault());
        Date date = new Date(timestamp);
        return dateFormat.format(date);
    }
}
