package com.example.todoapp;


public class AddTask  {
    String titleName = "";
    String titleDesc = "";
    byte[] taskImg;
    int taskID;
    long date;
    String currentDate;

    public AddTask() {
    }

    public AddTask(AddTask addTask) {
    }

    public AddTask(String titleName, String titleDesc, byte[] taskImg,long date,String currentDate) {
        this.titleName = titleName;
        this.titleDesc = titleDesc;
        this.taskImg = taskImg;
        this.date=date;
        this.currentDate=currentDate;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getTitleDesc() {
        return titleDesc;
    }

    public void setTitleDesc(String titleDesc) {
        this.titleDesc = titleDesc;
    }

    public byte[] getTaskImg() {
        return taskImg;
    }

    public void setTaskImg(byte[] taskImg) {
        this.taskImg = taskImg;
    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

}
