package com.example.todoapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HorizontalCalenderAdapter extends RecyclerView.Adapter<HorizontalCalenderAdapter.ViewHolder> {

    private final List<HorizontalCalender> exerciseModel;
    private final Context context;
    private final OnClicked onClicked;
    public static int dateCount = -1;

    public HorizontalCalenderAdapter(List<HorizontalCalender> exerciseModel, Context context, OnClicked onClicked) {
        this.exerciseModel = exerciseModel;
        this.context = context;
        this.onClicked = onClicked;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_calender, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HorizontalCalender model = exerciseModel.get(position);
        holder.tvDayNumber.setText(String.valueOf(model.getDay()));
        holder.tvDayName.setText(model.getDayName());
        holder.tvMonth.setText(model.getMonth());

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateCount = position;
                Constants.calenderDate = -1;
                onClicked.onItemClicked(model.getDay(), model.getMonth());
                notifyDataSetChanged();
            }
        });

        if (dateCount == position || Constants.calenderDate == position) {
            holder.item.setBackgroundTintList(context.getResources().getColorStateList(R.color.toolbar_bg));
            holder.item2.setBackgroundColor(context.getResources().getColor(R.color.transparent));
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.tvDayName.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.tvMonth.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.item.setBackgroundTintList(context.getResources().getColorStateList(R.color.transparent));
            holder.item2.setBackgroundColor(context.getResources().getColor(R.color.date_bg));
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.tvDayName.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.tvMonth.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return exerciseModel.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDayNumber, tvMonth;
        ConstraintLayout item, item2;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            tvMonth = itemView.findViewById(R.id.tvMonthName);
            item = itemView.findViewById(R.id.item);
            item2 = itemView.findViewById(R.id.item2);
        }
    }
}
