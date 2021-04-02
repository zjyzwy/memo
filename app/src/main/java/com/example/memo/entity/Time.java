package com.example.memo.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Time {
    private String year,month,day,hour,minute;
    private String msg="";

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }
    public String getTime(){
        msg=year+"-"+month+"-"+day+" "+hour+":"+minute;
        return msg;
    }


    /**
     * 时间转时间戳（毫秒）
     * @return
     */
    public long getClockTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmm");
        long timeLong = 0;
        try {
            if(Integer.valueOf(month) <10){ month="0"+month;}
            if(Integer.valueOf(day) <10){ day="0"+day;}
            if(Integer.valueOf(hour) <10){ hour="0"+hour;}
            if(Integer.valueOf(minute) <10){minute="0"+minute;}
            timeLong = sdf.parse(year+""+month+""+day+""+hour+""+minute+"").getTime();
        } catch (ParseException e) { e.printStackTrace(); }
        return timeLong;
    }
    @Override
    public String toString() {
        return "Time{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                '}';
    }
}
