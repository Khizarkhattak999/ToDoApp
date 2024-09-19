package com.example.todoapp;

public class HorizontalCalender {
    int day;
    String month;
    String dayName;

    public HorizontalCalender(int day, String month, String dayName) {
        this.day = day;
        this.month = month;
        this.dayName = dayName;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }
}
