package com.example.memo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.List;

import com.example.memo.R;
import com.example.memo.adapter.GroupAdapter;
import com.example.memo.dao.GroupDao;
import com.example.memo.entity.Group;

public class GroupManageActivity extends AppCompatActivity {
    private ListView manageGroup;     //列表
    private List<Group> groups;       //分组list
    private ImageButton deleteBtn;     //删除按钮
    Group group;                    //组 对象
    GroupDao groupDao=new GroupDao(GroupManageActivity.this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manage);
        init();
    }
    private void init(){

        /**
         * 绑定控件
         */
        manageGroup = findViewById(R.id.groupManage);
        deleteBtn=findViewById(R.id.delete);
        /**
         * 显示所有分组
         */

        groups=groupDao.getNormalGroups();

        /**
         * 自定义ListView
         */
        GroupAdapter groupAdapter=new GroupAdapter(GroupManageActivity.this,R.layout.delete_group,groups);
        manageGroup.setAdapter(groupAdapter);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(GroupManageActivity.this,MemoActivity.class);
        startActivity(intent);
        finish();

    }
}
