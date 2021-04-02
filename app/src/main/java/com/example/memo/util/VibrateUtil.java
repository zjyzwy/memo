package com.example.memo.util;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

public class VibrateUtil {

    /**
     * 让手机在响铃时以我们自己设定的pattern[]模式振动
     * long pattern[] = {0, 200000, 0, 200000};
     */
    public static void vibrate(Context context, long[] pattern, int repeat){
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        if(vib.hasVibrator()){
            vib.vibrate(pattern,repeat);
        }
    }

    /**
     * 取消震动
     */
    public static void virateCancle(Context context){
        //关闭震动
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.cancel();
    }
}