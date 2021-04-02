package com.example.memo.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.example.memo.entity.Group;
import com.example.memo.entity.Memo;
import com.example.memo.entity.Time;
import com.example.memo.service.AlarmService;

public class AddMemoActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_TAKE_PICTURE = 1000;
    private static final int REQUEST_CODE_CHOOSE_PICTURE = 2000;
    private Uri imageUri = null;

    private EditText memoTitle,memoContent,locationAddContent;     //备忘录的标题、内容、位置
    private TextView clockTime;                 //闹钟提醒时间
    private ImageButton addMemo,clock,locationEdit;          //备忘录信息提交按钮
    private ImageView takePhoto,photo,choosePhoto;           //拍照、打开图库功能
    private Spinner spinnerItem;                //下拉分组的标签
    List<Group>spinnerList;                     //存放分组的id和组名;
    List<String> groups = new ArrayList<>(); //存放组名
    private Memo memo=new Memo();
    private DBHelper dbHelper;                  //创建数据库的帮助对象
    private SQLiteDatabase db;                  //对数据库进行CRUD操作的对象
    private Calendar calendar;                  //日期
    Integer groupId;                            //组ID

    private Time time=new Time();               //时间
    GroupDao groupDao=new GroupDao(AddMemoActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_memo);
        init();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK && data != null) {
                    String returnedData = data.getStringExtra("location");
                    //Toast.makeText(this,returnedData+"AddMemo",Toast.LENGTH_SHORT).show();
                    locationAddContent.setText(returnedData);
                }
                break;
            case REQUEST_CODE_TAKE_PICTURE:
                if(resultCode==RESULT_OK) {
                    try {
                        // 将拍摄的照片显示出来
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        photo.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }else if(imageUri!=null){
                    photo.setImageURI(imageUri);
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

    private void init() {
        /**
         * 连接数据库
         */
        dbHelper = new DBHelper(this, "memo.db", null, 2);
        db=dbHelper.getWritableDatabase();

        /**
         * 绑定控件
         */
        addMemo=findViewById(R.id.commit);
        memoTitle=findViewById(R.id.memoTitle);
        memoContent=findViewById(R.id.memoContent);
        locationAddContent = findViewById(R.id.locationAddContent);
        takePhoto = findViewById(R.id.takePhoto);
        photo = findViewById(R.id.photo);
        choosePhoto = findViewById(R.id.choosePhoto);
        clockTime=findViewById(R.id.clockTime);
        spinnerItem=findViewById(R.id.group);
        clock=findViewById(R.id.clock);
        locationEdit=findViewById(R.id.locationEdit);

        /**
         * 初始化分组的下拉列表
         */
        setSpinnerItem();


        /**
         * 获取定位
         */
        locationEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddMemoActivity.this,GetLocationActivity.class);
                //startActivity(intent);
                intent.putExtra("requestCode","1");
                startActivityForResult(intent,1);
            }
        });

        /**
         * 拍照
         */
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> permissionList = new ArrayList<>();
                if (ContextCompat.checkSelfPermission(AddMemoActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(Manifest.permission.INTERNET);
                }
                if (ContextCompat.checkSelfPermission(AddMemoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(Manifest.permission.CAMERA);
                }
                if (!permissionList.isEmpty()) {
                    String [] permissions = permissionList.toArray(new String[permissionList.size()]);
                    ActivityCompat.requestPermissions(AddMemoActivity.this, permissions, 1);
                }
                openCamera();
            }
        });

        /**
         * 打开图库
         */
        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(AddMemoActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddMemoActivity.this, new
                            String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
                } else {
                    openAlbum();
                }
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
         * 提交按钮的点击事件，增加记录
         */
        addMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!((memoTitle.getText().toString().equals(""))&& (memoContent.getText().toString().equals(""))&&(locationAddContent.getText().toString().equals("")))){
                    memo.setTitle(memoTitle.getText().toString());
                    memo.setContent(memoContent.getText().toString());
                    memo.setLocation(locationAddContent.getText().toString());
                    memo.setImageUri(imageUri);

                    Date date=new Date();
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    String now=sdf.format(date);
                    memo.setCreateTime(now);

                    memo.setAlarmTime(time.getTime());
                    memo.setGroupId(groupId);


                    ContentValues values=new ContentValues();
                    values.put("title",memo.getTitle());
                    values.put("content",memo.getContent());
                    values.put("createTime",memo.getCreateTime());
                    values.put("alarmTime",memo.getAlarmTime());
                    values.put("isAlarm",memo.getIsAlarm());
                    values.put("location",memo.getLocation());
                    values.put("groupId",memo.getGroupId());
                    if(memo.getImageUri()!=null)
                        values.put("imageUri",memo.getImageUri().toString());
                    else
                        values.put("imageUri","");

                    db.insert("memosTable","null",values);
                    Toast.makeText(AddMemoActivity.this,"添加成功",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(AddMemoActivity.this, MemoActivity.class);
                    startActivity(intent);
                    finish();
                }else
                    new AlertDialog.Builder(AddMemoActivity.this)
                            .setTitle("备忘录至少需要填写一项内容")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setMessage("请填写您的备忘录内容")
                            .show();
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
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//设置样式
        spinnerItem.setAdapter(adapter); //适配
        spinnerItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {    //子项选中事件
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                groupId=spinnerList.get(position).getgId(); //保存组ID
                if(spinnerList.get(position).getItem().equals("管理分组")){
                    Intent intent=new Intent(AddMemoActivity.this,GroupManageActivity.class);
                    startActivity(intent);//选中的是“管理分组”就跳到另一个Activity。
                }
                if(spinnerList.get(position).getItem().equals("新建分组")){
                    newGroupDialog();   //选中的是“新建分组”就弹出对话框新建。
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
        final View groupLayout = View.inflate(AddMemoActivity.this, R.layout.new_group, null);//新建分组对话框中的布局
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
                        Toast.makeText(AddMemoActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(AddMemoActivity.this, "添加失败，该组名已存在", Toast.LENGTH_SHORT).show();
                    onStart();
                } else Toast.makeText(AddMemoActivity.this, "分组名不能为空", Toast.LENGTH_SHORT).show();
                onStart();

            }
        });
        newGroupDialog.create();
        newGroupDialog.show();
    }
    /**
     * 得到时间  https://www.jb51.net/article/157883.htm
     */
    public void getAlarmTime(){
        DatePickerDialog dpd= new DatePickerDialog(AddMemoActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                time.setYear(String.valueOf(year));         //设置年份。
                time.setMonth(String.valueOf(month+1));      //月份默认为0 开始
                time.setDay(String.valueOf(dayOfMonth));      //保存选中的日期

            }
        }, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dpd.show();
        dpd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                TimePickerDialog timePickerDialog= new TimePickerDialog(AddMemoActivity.this,
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

                                Intent intent=new Intent(AddMemoActivity.this,AlarmService.class);
                                memo.setIsAlarm(1); //设置启动闹钟
                                memo.setAlarmTime(time.getTime()); //闹钟时间
                                intent.putExtra("startTime",time.getClockTime());//把时间戳传给服务
                                intent.putExtra("title",memo.getTitle());
                                intent.putExtra("mId",memo.getmId());             //把mId传给服务,设置多个闹钟
                                startService(intent);

                                Toast.makeText(AddMemoActivity.this,time.getTime(),Toast.LENGTH_LONG).show();

                            }
                        },calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true);
                timePickerDialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddMemoActivity.this,MemoActivity.class);
        startActivity(intent);
        finish();

    }

    /**
     * 调用照相机拍摄照片
     */
    private void openCamera() {
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
            imageUri = FileProvider.getUriForFile(AddMemoActivity.this,
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
            photo.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }
}
