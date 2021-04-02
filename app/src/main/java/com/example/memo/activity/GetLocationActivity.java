package com.example.memo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.memo.R;

import java.util.ArrayList;
import java.util.List;

public class GetLocationActivity extends AppCompatActivity {

    private static final String TAG = "GetLocationActivity";

    public LocationClient mLocationClient;

    private StringBuilder currentPosition;        //定位信息

    private ImageButton comminLocation;

    private MapView mapView;

    private BaiduMap baiduMap;

    private boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_get_location);
        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(GetLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(GetLocationActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(GetLocationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(GetLocationActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

        comminLocation = findViewById(R.id.commitLocation);
        comminLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentREQUEST_CODE = getIntent();
                if(intentREQUEST_CODE.getStringExtra("requestCode").equals("1")) {
                    Intent intent = new Intent(GetLocationActivity.this, AddMemoActivity.class);
                    intent.putExtra("location", currentPosition.toString());
                    setResult(RESULT_OK, intent);
                }else if(intentREQUEST_CODE.getStringExtra("requestCode").equals("2")) {
                    Intent intent = new Intent(GetLocationActivity.this, UpdateMemoActivity.class);
                    intent.putExtra("location", currentPosition.toString());
                    setResult(RESULT_OK, intent);
                }
                GetLocationActivity.this.finish();
            }
        });

        /**
         * 实现在地图上点击获取位置并展现图标
         */
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //获取经纬度
                double latitude = latLng.latitude;
                double longitude = latLng.longitude;
                LatLng point = new LatLng(latitude, longitude);
                baiduMap.clear();
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.map);
                OverlayOptions options = new MarkerOptions().position(point).icon(bitmap);
                baiduMap.addOverlay(options);
                // 创建地理编码检索实例
                GeoCoder geoCoder = GeoCoder.newInstance();
                OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
                    // 反地理编码查询结果回调函数
                    @Override
                    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                        if (result == null|| result.error != SearchResult.ERRORNO.NO_ERROR) {
                            // 没有检测到结果
                            Toast.makeText(GetLocationActivity.this, "抱歉，未能找到结果",Toast.LENGTH_LONG).show();
                        }
                        if(currentPosition.length()!=0) {
                            currentPosition.replace(0, currentPosition.length(), result.getAddress());
                            Toast.makeText(GetLocationActivity.this, "您选择的位置是：" + currentPosition, Toast.LENGTH_LONG).show();
                        }
                    }
                    // 地理编码查询结果回调函数
                    @Override
                    public void onGetGeoCodeResult(GeoCodeResult result) {
                        if (result == null
                                || result.error != SearchResult.ERRORNO.NO_ERROR) {
                            Log.e(TAG, result.error.toString());
                            // 没有检测到结果
                        }
                    }
                };
                // 设置地理编码检索监听
                geoCoder.setOnGetGeoCodeResultListener(listener);
                //通过经纬度获取地址
                geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(point));
            }

            @Override
            public void onMapPoiClick(MapPoi mapPoi) {

            }
        });
    }


    private void navigateTo(final BDLocation location) {
        if (isFirstLocate) {
            Toast.makeText(GetLocationActivity.this, "当前位置：" + currentPosition, Toast.LENGTH_LONG).show();
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(19f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.
                Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    //初始化位置
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用定位功能", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            currentPosition = new StringBuilder();
            currentPosition.append(location.getCountry());
            currentPosition.append(location.getProvince());
            currentPosition.append(location.getCity());
            currentPosition.append(location.getDistrict());
            currentPosition.append(location.getStreet());
            if (location.getLocType() == BDLocation.TypeGpsLocation
                    || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);
            }
        }
    }
}
