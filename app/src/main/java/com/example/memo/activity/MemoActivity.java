
package com.example.memo.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.example.memo.R;
import com.example.memo.adapter.MemoAdapter;
import com.example.memo.dao.DBHelper;
import com.example.memo.dao.GroupDao;
import com.example.memo.dao.MemoDao;
import com.example.memo.entity.Group;
import com.example.memo.entity.Memo;

public class MemoActivity extends AppCompatActivity {

    /** 上次点击返回键的时间 */
    private long lastBackPressed;
    /** 两次点击的间隔时间 */
    private static final int QUIT_INTERVAL = 2000;

    private ListView listView;          //listView列表
    Memo memo;
    MemoAdapter memoAdapter;            //备忘录适配器
    ArrayAdapter<String> adapter;       //下拉列表适配器
    private FloatingActionButton fab;   //悬浮按钮, 添加备忘信息
    private DBHelper dbHelper;          //创建数据库操作类
    private SQLiteDatabase db;          //对SQLite数据库进行增删改查的对象
    private Spinner mainSpinner;        //下拉列表
    List<Group> spinnerList;             //存放分组的id和组名;
    List<Memo> memos = new ArrayList<>(); //得到的memos列表
    List<String> groups = new ArrayList<>(); //存放组名
    GroupDao groupDao = new GroupDao(MemoActivity.this);
    MemoDao memoDao = new MemoDao(MemoActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);

        //用于添加Menu
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();//初始化方法

        //判断该app是否打开了通知，如果没有的话就打开手机设置页面
        if (!isNotificationEnabled(MemoActivity.this)) {
            new android.app.AlertDialog.Builder(MemoActivity.this)
                    .setTitle("通知设置")
                    .setMessage("是否打开通知权限")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            gotoSet();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MemoActivity.this,"您已拒绝打开通知权限",Toast.LENGTH_LONG).show();
                        }
                    })
                    .show();
        } else {
            //当前app允许消息通知
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.memo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.exit:
                Toast.makeText(this, "退出成功", Toast.LENGTH_SHORT).show();
                ///Intent intent = new Intent("com.example.memo.FORCE_OFFLINE");
                //sendBroadcast(intent);
                finish();
                break;
            default: super.onOptionsItemSelected(item);
        }
        return true;
    }



    private void init() {

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MemoActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MemoActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MemoActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MemoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MemoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(MemoActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.INTERNET);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MemoActivity.this, permissions, 1);
        }


        /**
         * 初始化数据库 MemoTable
         * 数据库版本默认设置为1
         */
        dbHelper = new DBHelper(this, "memo.db", null, 2);
        db = dbHelper.getWritableDatabase();
        /**
         * 绑定控件
         */
        listView = findViewById(R.id.listView);
        fab = findViewById(R.id.fab);
        mainSpinner = findViewById(R.id.mainSpinner);
        /**
         * 初始化下拉列表
         */
        setMainSpinner();
        /**
         * 显示（默认分组）所有备忘录信息
         */


        memos = memoDao.getAllMemo();
        memoAdapter = new MemoAdapter(MemoActivity.this, R.layout.list_view, memos);//listView 初始适配
        listView.setAdapter(memoAdapter);

        /**
         * ListView点击事件
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                memo = memos.get(position);
                Intent intent = new Intent(MemoActivity.this, UpdateMemoActivity.class);
                intent.putExtra("mId", memo.getmId());
                startActivity(intent);             //跳转到修改页面进行修改
                finish();
            }
        });
        /**
         * 触发事件——添加备忘信息
         */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MemoActivity.this, AddMemoActivity.class);
                startActivity(intent);
                finish();
            }
        });

        /**
         * ListView长按事件
         * return  true 长按执行，false 长按和点击同时进行
         */
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                memo = memos.get(position);
                dialog();
                memos.remove(position);
                return true;
            }
        });
    }

    /**
     * 警告删除事件
     */

    public void dialog(){
        AlertDialog alertDialog=new AlertDialog.Builder(MemoActivity.this)
                .setTitle("删除").setMessage("确认删除该条记录吗?")
                .setCancelable(false)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        memoDao.delMemo(memo.getmId()); //删除数据库的内容
                        memos = memoDao.getAllMemo();
                        memoAdapter.setList(memos);     //刷新布局
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }
    /**
     * 得到下拉列表框内的所有子项
     */
    private void setMainSpinner() {

        spinnerList = groupDao.getAllGroups();

        for (Group group : spinnerList) {
            groups.add(group.getItem());
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//设置样式
        mainSpinner.setAdapter(adapter); //适配

        mainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {    //子项选中事件

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerList.get(position).getItem().equals("默认分组")) { //显示所有分组信息
                    memos = memoDao.getAllMemo();
                    memoAdapter = new MemoAdapter(MemoActivity.this, R.layout.list_view, memos);
                    listView.setAdapter(memoAdapter);

                } else if (spinnerList.get(position).getItem().equals("管理分组")) {
                    Intent intent = new Intent(MemoActivity.this, GroupManageActivity.class);
                    startActivity(intent);
                    finish();
                }else if (spinnerList.get(position).getItem().equals("新建分组")) {
                    newGroupDialog();   //选中的是“+新建分组”就弹出对话框新建。

                }else{
                    memos=memoDao.getGroupMemos(spinnerList.get(position).getgId());//得到该分组的所有记录
                    memoAdapter = new MemoAdapter(MemoActivity.this, R.layout.list_view, memos);//listView 初始适配
                    listView.setAdapter(memoAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 新建分组
     */
    private void newGroupDialog() {
        final AlertDialog.Builder newGroupDialog = new AlertDialog.Builder(this);//创建构造器的对象
        final View groupLayout = View.inflate(MemoActivity.this, R.layout.new_group, null);//新建分组对话框中的布局
        newGroupDialog.setTitle("新建分组");
        newGroupDialog.setView(groupLayout);//装入自定义布局
        newGroupDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText newGroup = groupLayout.findViewById(R.id.DialogNewGroup);
                String groupName = newGroup.getText().toString().trim();         //得到分组名
                if (!groupName.equals("") && groupName != null) {
                    if (groupDao.insertGroup(groupName) == 1) {//增加分组
                        groups.add(groupName);
                        //避免新增成功后点击事件无法生效
                        mainSpinner.setSelection(groups.size()-1,true);//设置spinner的值为"新增的组名"
                        spinnerList.add(new Group(groupName,spinnerList.get(spinnerList.size()-1).getgId()+1));
                        Toast.makeText(MemoActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(MemoActivity.this, "添加失败，该组名已存在", Toast.LENGTH_SHORT).show();
                    onStart();
                } else Toast.makeText(MemoActivity.this, "分组名不能为空", Toast.LENGTH_SHORT).show();


            }
        });

        newGroupDialog.create();
        newGroupDialog.show();
    }

    /**
     * 当新建分组后重新从数据库拿到值，刷新下拉列表
     */
    @Override
    protected void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
    }

    /**
     * 引导用户开启通知
     */
    private void gotoSet() {

        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= 26) {
            // android 8.0引导
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
        } else if (Build.VERSION.SDK_INT >= 21) {
            // android 5.0-7.0
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", getPackageName());
            intent.putExtra("app_uid", getApplicationInfo().uid);
        } else {
            // 其他
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", getPackageName(), null));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private boolean isNotificationEnabled(Context context) {
        boolean isOpened = false;
        try {
            isOpened = NotificationManagerCompat.from(context).areNotificationsEnabled();
        } catch (Exception e) {
            e.printStackTrace();
            isOpened = false;
        }
        return isOpened;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0) {
            long backPressed = System.currentTimeMillis();
            if (backPressed - lastBackPressed > QUIT_INTERVAL) {
                lastBackPressed = backPressed;
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
/*
    @Override
    public void onBackPressed() {
        long backPressed = System.currentTimeMillis();
        if (backPressed - lastBackPressed > QUIT_INTERVAL) {
            lastBackPressed = backPressed;
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_LONG).show();
        } else {
            finish();
            System.exit(0);
        }
    }

 */
}
