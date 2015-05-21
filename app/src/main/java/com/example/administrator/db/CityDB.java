package com.example.administrator.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.example.administrator.bean.City;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/3/31.
 */
public class CityDB {
    private static final String TAG="CityDB";
    public static final String CITY_DB_NAME="city2.db";
    public static final String CITY_TABLE_NAME="city";
    private SQLiteDatabase db;

    public CityDB(Context context,String path){
        db=context.openOrCreateDatabase(CITY_DB_NAME,context.MODE_PRIVATE,null);
    }

    public List<City> getAllCity(){
        List<City> list=new ArrayList<City>();
        Cursor c=db.rawQuery("SELECT * from "+CITY_TABLE_NAME,null);
        while (c.moveToNext()){
            String province=c.getString(c.getColumnIndex("province"));
            String city=c.getString(c.getColumnIndex("city"));
            String number=c.getString(c.getColumnIndex("number"));
            String allPY=c.getString(c.getColumnIndex("allpy"));
            String allFirstPY=c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY=c.getString(c.getColumnIndex("firstpy"));
            City item=new City(province,city,number,firstPY,allPY,allFirstPY);
            list.add(item);
        }
        return list;
    }


}
