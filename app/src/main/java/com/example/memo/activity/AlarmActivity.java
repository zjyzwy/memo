package com.example.memo.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.example.memo.R;

public class AlarmActivity extends BaseActivity {

    MediaPlayer mAlarmMusic;  //音乐
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Intent intent=getIntent();

        //使用广播发送通知
        Intent intentAlarm = new Intent("com.example.memo.ALARM");
        sendBroadcast(intentAlarm);

        //播放音乐
        mAlarmMusic = MediaPlayer.create(this, R.raw.alarm);
        mAlarmMusic.setLooping(true);
        mAlarmMusic.start();
        new AlertDialog.Builder(AlarmActivity.this).setTitle(intent.getStringExtra("title"))
                .setView(R.layout.activity_alarm)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAlarmMusic.stop();
                        AlarmActivity.this.finish();       //点击确定后音乐停止，该活动结束
                    }
                }).show();
    }
}
