package com.example.memo.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Date;

import com.example.memo.activity.AlarmActivity;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class AlarmService extends Service {
    private long startTime=0;    //闹钟时间
    private int mId=0;          //备忘信息的Id
    private String title;
    public AlarmService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    /**
     * 闹钟响铃
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTime=intent.getLongExtra("startTime",0);    //闹钟响起的时间
        mId=intent.getIntExtra("mId",0);    //闹钟响起的时间
        title=intent.getStringExtra("title");//备忘录信息的标题

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("闹钟响铃", "时间为" + new Date().toString());
            }
        }).start();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);   //闹钟管理器

        long triggerAtTime =startTime;
        // intent.putExtra("memoTitle",memoTitle);
        Intent alarmActivity = new Intent(this, AlarmActivity.class);
        alarmActivity.putExtra("title",title);
        //跳到该页面.如果该PendingIntent已经存在，则用新传入的Intent更新当前的数据。如果第四个参数为0，则为不传递任何数据
        //闹钟常用类
        PendingIntent pi = PendingIntent.getActivity(this, mId, alarmActivity, FLAG_UPDATE_CURRENT);
        manager.set( AlarmManager.RTC_WAKEUP,triggerAtTime, pi);//休眠状态下也将在时间下启动
        return super.onStartCommand(intent, flags, startId);
    }
}
