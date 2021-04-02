package com.example.memo.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import com.example.memo.entity.Group;

public class GroupDao {
    private Context context;
    private String gId,item;

    List<Group> groups=new ArrayList<>();
    private Boolean defalutGroup=false;     //若false， 代表数据库中的默认分组功能还未初始化
    private Boolean manageGroup=false;      //若false， 代表数据库中的管理分组功能还未初始化
    private Boolean newGroup=false;         //若false， 代表数据库中的新建分组功能还未初始化

    public GroupDao(Context context){
        this.context=context;
    }

   // SQLiteDatabase db=new DBHelper(
   //         context, "memo.db", null, 2).getWritableDatabase();
    /**
     * 获取所有组名
     * @return 组的集合
     */
    public List<Group> getAllGroups(){
        SQLiteDatabase db=new DBHelper(
                         context, "memo.db", null, 2).getWritableDatabase();
        String[] colums={"gId","item"};
        Cursor cursor=db.query("groupsTable",colums,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                //遍历Cursor对象
                gId=cursor.getString(cursor.getColumnIndex("gId"));
                item=cursor.getString(cursor.getColumnIndex("item"));
                if(item.equals("默认分组")){
                    defalutGroup=true;
                }else if(item.equals("管理分组")){
                    manageGroup=true;
                }else if(item.equals("新建分组")){
                    newGroup=true;
                }
                Group group=new Group();
                group.setgId(Integer.valueOf(gId));
                group.setItem(item);
                groups.add(group);
            }while (cursor.moveToNext());
        }
        if(defalutGroup ==false||manageGroup ==false||newGroup ==false){
            if(defalutGroup ==false){
                db.execSQL("insert into groupsTable(item) values(?)",new String[]{
                        "默认分组"
                });
            }
            if(manageGroup ==false){
                db.execSQL("insert into groupsTable(item) values(?)",new String[]{
                        "管理分组"
                });
            }
            if(newGroup ==false){
                db.execSQL("insert into groupsTable(item) values(?)",new String[]{
                        "新建分组"
                });
            }
            cursor=db.query("groupsTable",colums,null,null,null,null,null);
            if(cursor.moveToFirst()){
                do{
                    //遍历Cursor对象
                    gId=cursor.getString(cursor.getColumnIndex("gId"));
                    item=cursor.getString(cursor.getColumnIndex("item"));
                    if(item.equals("默认分组")){
                        defalutGroup=true;
                    }else if(item.equals("管理分组")){
                        manageGroup=true;
                    }else if(item.equals("新建分组")){
                        newGroup=true;
                    }
                    Group group=new Group();
                    group.setgId(Integer.valueOf(gId));
                    group.setItem(item);
                    groups.add(group);
                }while (cursor.moveToNext());
            }
        }

        cursor.close();//释放资源
        db.close();//关闭连接

        return groups;
    }
    /**
     * 获取所有普通的组名
     * @return 组的集合
     */
    public List<Group> getNormalGroups(){
        SQLiteDatabase db=new DBHelper(
                context, "memo.db", null, 2).getWritableDatabase();
        String[] colums={"gId","item"};
        Cursor cursor=db.query("groupsTable",colums,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                //遍历Cursor对象
                gId=cursor.getString(cursor.getColumnIndex("gId"));
                item=cursor.getString(cursor.getColumnIndex("item"));
                if(!item.equals("默认分组")&&!item.equals("管理分组")&&!item.equals("新建分组")){
                    Group group=new Group();
                    group.setgId(Integer.valueOf(gId));
                    group.setItem(item);
                    groups.add(group);
                }

            }while (cursor.moveToNext());
        }


        cursor.close();//释放资源
        db.close();//关闭连接

        return groups;
    }
    /**
     * 插入一条组名
     * @param groupName 组名
     * @return 成功1 失败0
     */
    public Integer insertGroup(String groupName){
        SQLiteDatabase db=new DBHelper(
                context, "memo.db", null, 2).getWritableDatabase();
        String []colums={"gId,item"};
        Cursor cursor=db.query("groupsTable",colums,"item='"+groupName+"'",null,null,null,null);
        if(cursor.getCount()==0){
            db.execSQL("insert into groupsTable (item) values (?)",
                    new String[]{groupName});
            db.close();
            return 1;
        }
        return 0;
    }

    /**
     * 删除一条组名
     * @param gId 组id
     * @return
     */
    public Integer deleteGroup(Integer gId){
        SQLiteDatabase db=new DBHelper(
                context, "memo.db", null, 2).getWritableDatabase();
        db.execSQL("delete from memosTable where groupId="+gId);
        db.execSQL("delete from groupsTable where gId="+gId);
        return 1;
    }

    /**
     * 更新一条组名
     * @param gId
     * @param item
     * @return
     */
    public Integer updateGroup(Integer gId,String item){
        SQLiteDatabase db=new DBHelper(
                context, "memo.db", null, 2).getWritableDatabase();

        db.execSQL("update groupsTable set item=? where gId=?",new String[]{
                item,String.valueOf(gId)
        });
        return 1;
    }

    public String getGroupItem(Integer gId){
        SQLiteDatabase db=new DBHelper(
                context, "memo.db", null, 2).getWritableDatabase();
        String[] colums={"gId","item"};
        String groupName = "默认分组";
        Cursor cursor=db.query("groupsTable",colums,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                //遍历Cursor对象
                if(gId == Integer.parseInt(cursor.getString(cursor.getColumnIndex("gId")))) {
                    groupName = cursor.getString(cursor.getColumnIndex("item"));
                    break;
                }
            }while (cursor.moveToNext());
        }


        cursor.close();//释放资源
        db.close();//关闭连接

        return groupName;
    }

}
