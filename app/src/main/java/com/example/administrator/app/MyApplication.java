package com.example.administrator.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.util.Log;

import com.example.administrator.bean.City;
import com.example.administrator.db.CityDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/3/31.
 */


public class MyApplication extends Application {

    private static final String TAG="MyAPP";
    private static Application mApplication;
    private CityDB mCityDB;
    private List<City> mCityList;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"MyApplication->onCreate");
        mApplication=this;
        mCityDB=openCityDB();
        initCityList();
    }
    public static Application getInstance(){
        return mApplication;
    }

    public List<City> getmCityList() {
        return mCityList;
    }

    private void initCityList(){
        mCityList=new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareCityList();
            }
        }).start();
    }
    private boolean prepareCityList(){

        mCityList=mCityDB.getAllCity();
//        for (City city:mCityList){
//            String cityName=city.getCity();
//            Log.d(TAG,cityName);
//        }
        return true;
    }
    private CityDB openCityDB(){
        String path="/data"+ Environment.getDataDirectory().getAbsolutePath()
                + File.separator+getPackageName()
                +File.separator+"databases"
                +File.separator
                +CityDB.CITY_DB_NAME;
        File db=new File(path);
        db.getParentFile().mkdirs();
        Log.d(TAG,path);
        if (!db.exists()){
            Log.i("MyApp","db is not exists");
            try {
                InputStream inputStream=getAssets().open("city.db");

                FileOutputStream fileOutputStream=new FileOutputStream(db);
                int len=-1;
                byte[] buffer=new byte[1024];
                while ((len=inputStream.read(buffer))!=-1){
                    fileOutputStream.write(buffer,0,len);
                    fileOutputStream.flush();
                }
                fileOutputStream.close();
                inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
                System.exit(0);
            }

        }
        return new CityDB(this,path);
    }
}
