package com.example.memo.activity;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.example.memo.R;
import com.example.memo.dao.DBHelper;
import com.example.memo.dao.GroupDao;
import com.example.memo.dao.MemoDao;
import com.example.memo.entity.Group;
import com.example.memo.entity.Memo;
import com.example.memo.entity.Time;
import com.example.memo.service.AlarmService;

public class UpdateMemoActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_TAKE_PICTURE = 1000;
    private static final int REQUEST_CODE_CHOOSE_PICTURE = 2000;

    private Uri imageUri = null;

    private EditText newTitle,newContent,locationUpdateContent;   //标题和内容输入框
    private ImageButton updateMemo,clock,locationUpdate;   //备忘录信息提交按钮、闹钟按钮
    private ImageView takeUpdatePhoto,photoUpdate,chooseUpdatePhoto;
    private int mId;                        //修改记录的id
    private Spinner spinnerItem;                //下拉分组的标签
    List<Group> spinnerList;                     //存放分组的id和组名;
    ArrayAdapter<String> adapter;       //下拉列表适配器
    List<String> groups = new ArrayList<>(); //存放组名
    private TextView clockTime;             //显示闹钟时间
    private Calendar calendar;              //日期
    Integer groupId;                            //组ID
    private DBHelper dbHelper;                  //创建数据库的帮助对象
    private SQLiteDatabase db;                  //对数据库进行CRUD操作的对象

    private Time time=new Time();                      //时间
    private Memo memo=new Memo();
    GroupDao groupDao=new GroupDao(UpdateMemoActivity.this);
    MemoDao memoDao=new MemoDao(UpdateMemoActivity.this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_memo);
        Intent intent=getIntent();
        mId=intent.getIntExtra("mId",0);       //得到intent传过来的被点击的记录的id
        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK && data != null) {
                    String returnedData = data.getStringExtra("location");
                    locationUpdateContent.setText(returnedData);
                }
                break;
            case REQUEST_CODE_TAKE_PICTURE:
                if (resultCode==RESULT_OK) {
                    try {
                        // 将拍摄的照片显示出来
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        photoUpdate.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }else {
                    photoUpdate.setImageURI(memo.getImageUri());
                }
                break;
            case REQUEST_CODE_CHOOSE_PICTURE:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                    }
                }
            default:
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    private void init(){
        /**
         * 连接数据库
         */
        dbHelper = new DBHelper(this, "memo.db", null, 2);
        db=dbHelper.getWritableDatabase();
        /**
         * 绑定控件
         */
        updateMemo=findViewById(R.id.commit);
        spinnerItem=findViewById(R.id.group);
        newTitle=findViewById(R.id.newTitle);
        newContent=findViewById(R.id.newContent);
        locationUpdateContent = findViewById(R.id.locationUpdateContent);
        clock=findViewById(R.id.clock);
        clockTime=findViewById(R.id.clockTime);
        locationUpdate=findViewById(R.id.locationUpdate);
        photoUpdate = findViewById(R.id.photoUpdate);
        takeUpdatePhoto = findViewById(R.id.takeUpdatePhoto);
        chooseUpdatePhoto = findViewById(R.id.chooseUpdatePhoto);
        /**
         * 获取原备忘信息
         */
        memo=memoDao.selectMemoById(mId);
        groupId=memo.getGroupId();
        imageUri = memo.getImageUri();
        newTitle.setText(memo.getTitle());
        newContent.setText(memo.getContent());
        photoUpdate.setImageURI(memo.getImageUri());
        locationUpdateContent.setText(memo.getLocation());
        if(memo.getIsAlarm()==0){ clockTime.setText(""); }
        else{ clockTime.setText(memo.getAlarmTime()); }

        /**
         * 初始化分组的下拉列表
         */
        setSpinnerItem();

        locationUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateMemoActivity.this,GetLocationActivity.class);
                //startActivity(intent);
                intent.putExtra("requestCode","2");
                startActivityForResult(intent,2);
            }
        });


        takeUpdatePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        chooseUpdatePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlbum();
            }
        });

        /**
         * 闹钟按钮的点击事件
         */
        clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar= Calendar.getInstance();
                getAlarmTime();
            }
        });

        /**
         * 提交更新后的备忘录信息
         */
        updateMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!((newTitle.getText().toString().equals(""))&& (newContent.getText().toString().equals(""))&&(locationUpdateContent.getText().toString().equals("")))){
                    memo.setTitle(newTitle.getText().toString());
                    memo.setContent(newContent.getText().toString());
                    memo.setLocation(locationUpdateContent.getText().toString());
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    String now = sdf.format(date);
                    memo.setCreateTime(now);
                    memo.setGroupId(groupId);
                    memo.setImageUri(imageUri);
                    memoDao.updateMemo(mId,memo);
                    Toast.makeText(UpdateMemoActivity.this,"修改成功",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(UpdateMemoActivity.this, MemoActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    new AlertDialog.Builder(UpdateMemoActivity.this)
                            .setTitle("备忘录至少需要填写一项内容")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setMessage("请填写您的备忘录内容")
                            .show();
                }
            }
        });

    }
    /**
     * 得到下拉列表框内的所有子项
     */
    private void setSpinnerItem() {
        spinnerList=groupDao.getAllGroups();
        for(Group group:spinnerList){
            groups.add(group.getItem());
        }
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//设置样式
        spinnerItem.setAdapter(adapter); //适配

        String selectName = groupDao.getGroupItem(memo.getGroupId());
        for(int i=0;i<spinnerItem.getCount();i++){
            if(spinnerItem.getItemAtPosition(i).equals(selectName)) {
                spinnerItem.setSelection(i, true);
                break;
            }
        }
        spinnerItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {    //子项选中事件

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                groupId=spinnerList.get(position).getgId(); //保存组ID
                if(spinnerList.get(position).getItem().equals("管理分组")){
                    Intent intent=new Intent(UpdateMemoActivity.this,GroupManageActivity.class);
                    startActivity(intent);
                }
                if(spinnerList.get(position).getItem().equals("新建分组")){
                    newGroupDialog();   //选中的是“+新建分组”就弹出对话框新建。
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
        final View groupLayout = View.inflate(UpdateMemoActivity.this, R.layout.new_group, null);//新建分组对话框中的布局
        newGroupDialog.setTitle("新建分组");
        newGroupDialog.setView(groupLayout);//装入自定义布局
        newGroupDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText newGroup = groupLayout.findViewById(R.id.DialogNewGroup);
                final String groupName = newGroup.getText().toString().trim();         //得到分组名
                if (!groupName.equals("") && groupName != null) {
                    if (groupDao.insertGroup(groupName) == 1) {//增加分组
                        groups.add(groupName);
                        spinnerItem.setSelection(groups.size()-1,true);//设置spinner的值为"新增的组名"
                        spinnerList.add(new Group(groupName,spinnerList.get(spinnerList.size()-1).getgId()+1));
                        Toast.makeText(UpdateMemoActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(UpdateMemoActivity.this, "添加失败，该组名已存在", Toast.LENGTH_SHORT).show();
                    onStart();
                } else Toast.makeText(UpdateMemoActivity.this, "分组名不能为空", Toast.LENGTH_SHORT).show();

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
     *得到时间  https://www.jb51.net/article/157883.htm
     */
    public void getAlarmTime(){
        DatePickerDialog dpd= new DatePickerDialog(UpdateMemoActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                time.setYear(String.valueOf(year));
                time.setMonth(String.valueOf(month+1));      //月份默认为0 开始
                time.setDay(String.valueOf(dayOfMonth));      //保存选中的日期

            }
        }, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dpd.show();
        dpd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                TimePickerDialog timePickerDialog= new TimePickerDialog(UpdateMemoActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                if(hourOfDay<10){
                                    time.setHour("0"+String.valueOf(hourOfDay));
                                }else{
                                    time.setHour(String.valueOf(hourOfDay));
                                }
                                if(minute<10){
                                    time.setMinute("0"+String.valueOf(minute));
                                } else{
                                    time.setMinute(String.valueOf(minute));
                                }                                       //保存选中的时间

                                clockTime.setText(time.getTime());      //在文本上显示选中的时间

                                Intent intent=new Intent(UpdateMemoActivity.this,AlarmService.class);
                                memo.setIsAlarm(1); //设置启动闹钟
                                memo.setAlarmTime(time.getTime()); //闹钟时间

                                intent.putExtra("startTime",time.getClockTime());//把时间戳传给服务
                                intent.putExtra("mId",memo.getmId());             //把mId传给服务,设置多个闹钟
                                intent.putExtra("title",memo.getTitle());
                                startService(intent);

                                Toast.makeText(UpdateMemoActivity.this,time.getTime(),Toast.LENGTH_LONG).show();

                            }
                        },calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true);
                timePickerDialog.show();
            }
        });
    }

    /**
     * 调用照相机拍摄照片
     */
    private void openCamera() {
        /*
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null)
            startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);

         */

        File outputImage = new File(getExternalCacheDir(),createFileName());
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(UpdateMemoActivity.this,
                    "com.example.memo.fileProvider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        // 启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
    }

    public static String createFileName(){
        String fileName = "";
        //系统当前时间
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dataFormat = new SimpleDateFormat("'IMG'_yyyyMMdd-HHmmss");
        fileName = dataFormat.format(date) + ".jpg";
        return fileName;
    }

    /**
     * 调用系统图库选照片
     */
    private void openAlbum() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if(intent.resolveActivity(getPackageManager())!=null)
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_PICTURE);
    }

    /**
     * 打开图库时的URI处理
     */
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.
                    getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content: downloads/public_downloads"), Long.valueOf(docId));imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath); // 根据图片路径显示图片
    }
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.
                        Images.Media.DATA));
            }
            cursor.close();
        }
        imageUri = Uri.parse(path);
        return path;
    }
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            photoUpdate.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(UpdateMemoActivity.this,MemoActivity.class);
        startActivity(intent);
        finish();
    }
}
