package com.example.memo.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import com.example.memo.entity.Memo;

public class MemoDao {
    private Context context;

    public MemoDao(Context context){
        this.context=context;
    }
    /**
     * 插入一条备忘录
     * @param memo
     * @return
     */
    public int insertMemo(Memo memo){
        SQLiteDatabase db=new DBHelper(
                context, "memo.db", null, 2).getWritableDatabase();
        db.execSQL("insert into memosTable (title,content,createTime,alarmTime,isAlarm,location,groupId,imageUri)"
                +" values("+memo.getTitle()
                +", "+memo.getContent()
                +", '"+memo.getCreateTime()
                +"' , "+memo.getAlarmTime()
                +", "+memo.getIsAlarm()
                +","+memo.getLocation()
                +","+memo.getGroupId()
                +", "+memo.getImageUri().toString()+")");
        db.close();
        return  0;
    }

    /**
     * 获取所有备忘录信息
     * @return
     */
    public List<Memo> getAllMemo(){
        SQLiteDatabase db=new DBHelper(
                context, "memo.db", null, 2).getWritableDatabase();
        List<Memo> memos=new ArrayList<>();
        Cursor cursor=db.query("memosTable",null,null,null,
                null,null,"createTime desc");
        if(cursor.moveToFirst()){
            do{
                Memo memo=new Memo();
                memo.setmId(Integer.valueOf(cursor.getString(cursor.getColumnIndex("mId"))));
                memo.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                memo.setContent(cursor.getString(cursor.getColumnIndex("content")));
                memo.setCreateTime(cursor.getString(cursor.getColumnIndex("createTime")));
                memo.setAlarmTime(cursor.getString(cursor.getColumnIndex("alarmTime")));
                memo.setLocation(cursor.getString(cursor.getColumnIndex("location")));
                memo.setImageUri(Uri.parse(cursor.getString(cursor.getColumnIndex("imageUri"))));
                memos.add(memo);
            }while (cursor.moveToNext());
        }
        cursor.close();//释放资源
        db.close();
        return memos;
    }

    /**
     * 删除一条备忘录
     * @param mId 备忘录ID
     * @return
     */
    public int delMemo(Integer mId){
        SQLiteDatabase db=new DBHelper(
                context, "memo.db", null, 2).getWritableDatabase();
        db.execSQL("delete from memosTable where mId=?",
                new String[]{String.valueOf(mId)}
                );
        return 1;
    }

    /**
     * 更新一条备忘录
     * @param memo
     * @param mId
     * @return
     */
    public int updateMemo(Integer mId,Memo memo){
        SQLiteDatabase db=new DBHelper(
                context, "memo.db", null, 2).getWritableDatabase();
        db.execSQL("update memosTable set title=? ,content=? ,createTime=? ,alarmTime=? ,isAlarm=? ,location=?, groupId=?, imageUri=? where mId=?",
                new String[]{
                        memo.getTitle(),memo.getContent(),memo.getCreateTime(),memo.getAlarmTime()
                        ,String.valueOf(memo.getIsAlarm()),memo.getLocation(),String.valueOf(memo.getGroupId()),memo.getImageUri().toString(),String.valueOf(mId)
                });
        return 1;
    }


    /**
     * 查找一条备忘信息
     */
    public Memo selectMemoById(Integer mId){
        SQLiteDatabase db=new DBHelper(
                context, "memo.db", null, 2).getWritableDatabase();
        Memo memo=new Memo();
        Cursor cursor=db.query("memosTable",null,"mId="+mId,null,null,null,null);
        if(cursor.moveToFirst()){

            memo.setmId(Integer.valueOf(cursor.getString(cursor.getColumnIndex("mId"))));
            memo.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            memo.setContent(cursor.getString(cursor.getColumnIndex("content")));
            memo.setCreateTime(cursor.getString(cursor.getColumnIndex("createTime")));
            memo.setAlarmTime(cursor.getString(cursor.getColumnIndex("alarmTime")));
            memo.setLocation(cursor.getString(cursor.getColumnIndex("location")));
            memo.setImageUri(Uri.parse(cursor.getString(cursor.getColumnIndex("imageUri"))));
            if(cursor.getString(cursor.getColumnIndex("isAlarm"))!=null){
                memo.setIsAlarm(Integer.valueOf(cursor.getString(cursor.getColumnIndex("isAlarm"))));
            }else{
                memo.setIsAlarm(0);
            }

            memo.setGroupId(Integer.valueOf(cursor.getString(cursor.getColumnIndex("groupId"))));
        }
        cursor.close();//释放资源
        db.close();
        return memo;
    }

    /**
     * 得到分组的所有记录
     * @param gId 组id
     * @return
     */
    public List<Memo> getGroupMemos(Integer gId){
        SQLiteDatabase db=new DBHelper(
                context, "memo.db", null, 2).getWritableDatabase();
        List<Memo> memos=new ArrayList<>();
        Cursor cursor=db.query("memosTable",null,"groupId="+gId,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                Memo memo=new Memo();
                //遍历Cursor对象
                memo.setmId(Integer.valueOf(cursor.getString(cursor.getColumnIndex("mId"))));
                memo.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                memo.setContent(cursor.getString(cursor.getColumnIndex("content")));
                memo.setCreateTime(cursor.getString(cursor.getColumnIndex("createTime")));
                memo.setAlarmTime(cursor.getString(cursor.getColumnIndex("alarmTime")));
                memo.setLocation(cursor.getString(cursor.getColumnIndex("location")));
                memo.setImageUri(Uri.parse(cursor.getString(cursor.getColumnIndex("imageUri"))));
                if(cursor.getString(cursor.getColumnIndex("isAlarm"))!=null){
                    memo.setIsAlarm(Integer.valueOf(cursor.getString(cursor.getColumnIndex("isAlarm"))));
                }
                // memo.setContent(cursor.getString(cursor.getColumnIndex("content")));
                // memo.setCreateTime(now);
                memos.add(memo);
            }while (cursor.moveToNext());
        }
        cursor.close();//释放资源
        db.close();
        return memos;
    }
}
