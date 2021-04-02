package com.example.memo.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;

import com.example.memo.R;
import com.example.memo.util.VibrateUtil;

import java.io.File;

public class BaseActivity extends AppCompatActivity {

    private AlarmReceiver receiver;
    private NotificationManager manager;
    private boolean isVirating = true; //震动

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //动态注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.memo.ALARM");
        receiver = new AlarmReceiver();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver != null) {
            unregisterReceiver(receiver);
            //关闭震动
            if (isVirating) {//防止多次关闭抛出异常，这里加个参数判断一下
                isVirating = false;
                VibrateUtil.virateCancle(BaseActivity.this);
            }
            manager.cancel(1);//关闭通知
            receiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 使用广播发送通知
     */
    class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {
            // 开启震动
            VibrateUtil.vibrate(BaseActivity.this, new long[]{1000, 5000, 3000, 5000}, 0);
            //发送通知
            Intent intentNotification = new Intent(BaseActivity.this, NotificationActivity.class);
            PendingIntent pi = PendingIntent.getActivity(BaseActivity.this, 0, intentNotification, 0);
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //高版本需要渠道,用于解决过时问题
            if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                //只在Android O之上需要渠道，这里的第一个参数要和下面的channelId一样
                NotificationChannel notificationChannel = new NotificationChannel("1","name",NotificationManager.IMPORTANCE_HIGH);
                //如果这里用IMPORTANCE_NOENE就需要在系统的设置里面开启渠道，通知才能正常弹出
                manager.createNotificationChannel(notificationChannel);
            }
            final Notification notification = new NotificationCompat.Builder(BaseActivity.this,"1")
                    .setContentTitle(intent.getStringExtra("title"))
                    .setContentText("设置的备忘录时间到了")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.alarm)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.alarm))
                    .setContentIntent(pi)
                    .setSound(Uri.fromFile(new File("/system/media/audio/ringtones/Luna.ogg")))
                    .setVibrate(new long[]{0, 1000, 1000, 1000})
                    .setLights(Color.GREEN, 1000, 1000)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("Learn how to build notifications, send and sync data, and use voice actions. Get the official Android IDE and developer tools to build apps for Android."))
                    .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.big_image)))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .build();
            manager.notify(1, notification);
        }

    }

}
