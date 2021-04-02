package com.example.memo.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private String memosTable="create table memosTable("
            +"mId integer primary key autoincrement,"
            + "title text,"
            +"content text,"
            +"createTime text,"
            +"alarmTime text,"
            +"isAlarm integer,"
            +"location text,"
            +"groupId integer,"
            +"imageUri text)";
    private String groupsTable="create table groupsTable("
            +"gId integer primary key autoincrement,"
            +"item text)";
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建表
        db.execSQL(memosTable);
        db.execSQL(groupsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists memosTable");     //升级时删表
        db.execSQL("drop table if exists groupsTable");
        onCreate(db);
    }


}
