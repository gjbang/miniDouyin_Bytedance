package com.domker.study.androidstudy.database;


import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME="videos.db";
    private static final int DB_VERSION=1;

    public SQLDbHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }


    public SQLDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @SuppressLint("SQLiteString")
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQLCommand.SQL_CREATE_VIDEO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

