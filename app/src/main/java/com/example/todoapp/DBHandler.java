package com.example.todoapp;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class DBHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "tododb";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "todotasks";
    private static final String ID_TASK = "id";
    private static final String TITLE_NAME = "name";
    private static final String TITLE_DESC = "description";
    private static final String TITLE_IMG = "img";
    private static final String DATE_AND_TIME = "date";
    private static final String CURRENT_DATE = "added_date";


    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_TASK + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TITLE_NAME + " TEXT,"
                + TITLE_DESC + " TEXT,"
                + DATE_AND_TIME + " INTEGER,"
                + CURRENT_DATE + " TEXT,"
                + TITLE_IMG + " BLOB)";

        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public void AddNewTODOItem(String titleName, String titleDescription, byte[] taskImg, long date,String currentDate) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TITLE_NAME, titleName);
        values.put(TITLE_DESC, titleDescription);
        values.put(TITLE_IMG, taskImg);
        values.put(DATE_AND_TIME, date);
        values.put(CURRENT_DATE,currentDate);

        database.insert(TABLE_NAME, null, values);
        database.close();
    }

    public ArrayList<AddTask> readTODOList(String selectedDate) {
        SQLiteDatabase database = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CURRENT_DATE + " = ?";
        Cursor cursor = database.rawQuery(query, new String[]{selectedDate});
        ArrayList<AddTask> addTODOModels = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String titleName = cursor.getString(cursor.getColumnIndex(TITLE_NAME));
                @SuppressLint("Range") String titleDesc = cursor.getString(cursor.getColumnIndex(TITLE_DESC));
                @SuppressLint("Range") byte[] taskImg = cursor.isNull(cursor.getColumnIndex(TITLE_IMG)) ? null : cursor.getBlob(cursor.getColumnIndex(TITLE_IMG));
                @SuppressLint("Range") long date = cursor.getLong(cursor.getColumnIndex(DATE_AND_TIME));
                @SuppressLint("Range") String currentDate = cursor.getString(cursor.getColumnIndex(CURRENT_DATE));

                addTODOModels.add(new AddTask(titleName, titleDesc, taskImg, date,currentDate));
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return addTODOModels;
    }

    public void UpdateTask(String titleNameOriginal, String titleName, String titleDescription,
                           byte[] taskImg) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TITLE_NAME, titleName);
        values.put(TITLE_DESC, titleDescription);
        values.put(TITLE_IMG, taskImg);

        database.update(TABLE_NAME, values, "name=?", new String[]{titleNameOriginal});
        database.close();
    }

    public void DeleteTask(String titleName) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_NAME, "name=?", new String[]{titleName});
        database.close();

    }
}