package com.example.administrator.myweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.umeng.analytics.MobclickAgent;


public class LocationActivity extends Activity {
    public LocationClient mLocationClient=null;
    private TextView textView;
    private String province,city,citycode,district;
    BaiduMap mBaiduMap;
    MapView mapView=null;
    boolean isFirstLoc=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_location);//先加载布局再实例化控件。
        textView=(TextView)findViewById(R.id.test_location);
        mapView=(MapView)findViewById(R.id.bmapView);
        mBaiduMap=mapView.getMap();
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL,true,null));
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //定位初始化
        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        InitLocation();
        mLocationClient.start();
        int i=mLocationClient.requestLocation();
        Log.d("LocationActivity",i+"");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        mLocationClient.stop();
        super.onStop();
    }
    public BDLocationListener myListener=new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mapView == null)
                return;
            StringBuffer sb = new StringBuffer(256);
            sb.append("\ntime : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            sb.append("\nprovince : ");
            sb.append(location.getProvince());
            sb.append("\ncity : ");
            sb.append(location.getCity());
            sb.append("\ncitycode : ");
            sb.append(location.getCityCode());
            sb.append("\ndistrict : ");
            sb.append(location.getDistrict());
            if (location.getLocType() == BDLocation.TypeGpsLocation){
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\ndirection : ");
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append(location.getDirection());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
            }
            province=location.getProvince();
            city=location.getCity();
            citycode=location.getCityCode();
            district=location.getDistrict();
            textView.setText(sb.toString());
            Log.i("BaiduLocationApiDem", sb.toString());
            // map view 销毁后不在处理新接收的位置
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            }
        }
    };

    private void InitLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("gcj02");
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        mLocationClient.setLocOption(option);
    }
    public void onLocationBtnClik(View view){
        Intent intent=new Intent();
        intent.putExtra("province",province);
        intent.putExtra("city",city);
        intent.putExtra("citycode",citycode);
        intent.putExtra("district",district);
        setResult(RESULT_OK,intent);
        finish();
    }
}
