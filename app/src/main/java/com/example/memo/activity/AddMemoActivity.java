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

    private EditText memoTitle,memoContent,locationAddContent;     //????????????????????????????????????
    private TextView clockTime;                 //??????????????????
    private ImageButton addMemo,clock,locationEdit;          //???????????????????????????
    private ImageView takePhoto,photo,choosePhoto;           //???????????????????????????
    private Spinner spinnerItem;                //?????????????????????
    List<Group>spinnerList;                     //???????????????id?????????;
    List<String> groups = new ArrayList<>(); //????????????
    private Memo memo=new Memo();
    private DBHelper dbHelper;                  //??????????????????????????????
    private SQLiteDatabase db;                  //??????????????????CRUD???????????????
    private Calendar calendar;                  //??????
    Integer groupId;                            //???ID

    private Time time=new Time();               //??????
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
                        // ??????????????????????????????
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
                    // ???????????????????????????
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4?????????????????????????????????????????????
                        handleImageOnKitKat(data);
                    } else {
                        // 4.4??????????????????????????????????????????
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
         * ???????????????
         */
        dbHelper = new DBHelper(this, "memo.db", null, 2);
        db=dbHelper.getWritableDatabase();

        /**
         * ????????????
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
         * ??????????????????????????????
         */
        setSpinnerItem();


        /**
         * ????????????
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
         * ??????
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
         * ????????????
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
         * ???????????????????????????
         */
        clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar= Calendar.getInstance();
                getAlarmTime();
            }
        });



        /**
         * ??????????????????????????????????????????
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
                    Toast.makeText(AddMemoActivity.this,"????????????",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(AddMemoActivity.this, MemoActivity.class);
                    startActivity(intent);
                    finish();
                }else
                    new AlertDialog.Builder(AddMemoActivity.this)
                            .setTitle("???????????????????????????????????????")
                            .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setMessage("??????????????????????????????")
                            .show();
            }
        });
    }

    /**
     * ???????????????????????????????????????
     */
    private void setSpinnerItem() {

        spinnerList=groupDao.getAllGroups();
        for(Group group:spinnerList){
            groups.add(group.getItem());
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//????????????
        spinnerItem.setAdapter(adapter); //??????
        spinnerItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {    //??????????????????
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                groupId=spinnerList.get(position).getgId(); //?????????ID
                if(spinnerList.get(position).getItem().equals("????????????")){
                    Intent intent=new Intent(AddMemoActivity.this,GroupManageActivity.class);
                    startActivity(intent);//????????????????????????????????????????????????Activity???
                }
                if(spinnerList.get(position).getItem().equals("????????????")){
                    newGroupDialog();   //?????????????????????????????????????????????????????????
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * ????????????
     */
    private void newGroupDialog() {
        final AlertDialog.Builder newGroupDialog = new AlertDialog.Builder(this);//????????????????????????
        final View groupLayout = View.inflate(AddMemoActivity.this, R.layout.new_group, null);//?????????????????????????????????
        newGroupDialog.setTitle("????????????");
        newGroupDialog.setView(groupLayout);//?????????????????????
        newGroupDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText newGroup = groupLayout.findViewById(R.id.DialogNewGroup);
                final String groupName = newGroup.getText().toString().trim();         //???????????????
                if (!groupName.equals("") && groupName != null) {
                    if (groupDao.insertGroup(groupName) == 1) {//????????????
                        groups.add(groupName);
                        spinnerItem.setSelection(groups.size()-1,true);//??????spinner?????????"???????????????"
                        spinnerList.add(new Group(groupName,spinnerList.get(spinnerList.size()-1).getgId()+1));
                        Toast.makeText(AddMemoActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(AddMemoActivity.this, "?????????????????????????????????", Toast.LENGTH_SHORT).show();
                    onStart();
                } else Toast.makeText(AddMemoActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                onStart();

            }
        });
        newGroupDialog.create();
        newGroupDialog.show();
    }
    /**
     * ????????????  https://www.jb51.net/article/157883.htm
     */
    public void getAlarmTime(){
        DatePickerDialog dpd= new DatePickerDialog(AddMemoActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                time.setYear(String.valueOf(year));         //???????????????
                time.setMonth(String.valueOf(month+1));      //???????????????0 ??????
                time.setDay(String.valueOf(dayOfMonth));      //?????????????????????

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
                                }                                       //?????????????????????

                                clockTime.setText(time.getTime());      //?????????????????????????????????

                                Intent intent=new Intent(AddMemoActivity.this,AlarmService.class);
                                memo.setIsAlarm(1); //??????????????????
                                memo.setAlarmTime(time.getTime()); //????????????
                                intent.putExtra("startTime",time.getClockTime());//????????????????????????
                                intent.putExtra("title",memo.getTitle());
                                intent.putExtra("mId",memo.getmId());             //???mId????????????,??????????????????
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
     * ???????????????????????????
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
        // ??????????????????
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
    }

    public static String createFileName(){
        String fileName = "";
        //??????????????????
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dataFormat = new SimpleDateFormat("'IMG'_yyyyMMdd-HHmmss");
        fileName = dataFormat.format(date) + ".jpg";
        return fileName;
    }

    /**
     * ???????????????????????????
     */
    private void openAlbum() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if(intent.resolveActivity(getPackageManager())!=null)
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_PICTURE);
    }

    /**
     * ??????????????????URI??????
     */
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // ?????????document?????????Uri????????????document id??????
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // ????????????????????????id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.
                    getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content: downloads/public_downloads"), Long.valueOf(docId));imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // ?????????content?????????Uri??????????????????????????????
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // ?????????file?????????Uri?????????????????????????????????
            imagePath = uri.getPath();
        }
        displayImage(imagePath); // ??????????????????????????????
    }
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // ??????Uri???selection??????????????????????????????
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
