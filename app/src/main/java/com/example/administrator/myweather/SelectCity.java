package com.example.administrator.myweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.administrator.app.MyApplication;
import com.example.administrator.bean.City;
import com.example.administrator.db.CityDB;
import com.example.administrator.util.AdapterUtil;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/3/31.
 */
public class SelectCity extends Activity implements View.OnClickListener {
    private ImageView mBackbtn;
    private EditText searchedit;
    private ListView listView;
    private AdapterUtil listArrayAdapter;
    private static final String TAG = "SelectCity";
    private MyApplication myApplication;
    private List<City> mSelectCityList;
    public static final int UPDATECITYLIST = 1;
    private Handler updateCityListHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATECITYLIST) {
                //updateCityList((List)msg.obj);
            }
        }
    };
    private void updateCityList(){
//        List<String> cityStringList=new ArrayList<>();
//        for (int i=0;i<mSelectCityList.size();i++){
//            cityStringList.add(mSelectCityList.get(i).getCity());
//        }
        listArrayAdapter = new AdapterUtil(SelectCity.this, R.layout.selectcity_list, R.id.citylisttext, mSelectCityList);
        listView.setAdapter(listArrayAdapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        mBackbtn = (ImageView) findViewById(R.id.title_back);
        listView = (ListView) findViewById(R.id.selectcitylist);
        searchedit=(EditText)findViewById(R.id.search_edit);
        TextWatcher mTextWatcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Pattern pattern=Pattern.compile(s.toString().toUpperCase().trim());
                List<City> filterCityList=new ArrayList<>();
                for(int i=0;i<mSelectCityList.size();i++){
                    City city=mSelectCityList.get(i);
                    Matcher matcher=pattern.matcher(city.getCity()+city.getAllPY());
                    if(matcher.find())
                        filterCityList.add(city);
                    listArrayAdapter=new AdapterUtil(SelectCity.this, R.layout.selectcity_list, R.id.citylisttext, filterCityList);
                    listView.setAdapter(listArrayAdapter);
                }
                //listArrayAdapter.getFilter().filter(s);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        searchedit.addTextChangedListener(mTextWatcher);
        myApplication=(MyApplication)this.getApplication();
        mSelectCityList=myApplication.getmCityList();
        updateCityList();
        mBackbtn.setOnClickListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City mselectedCity=(City)listArrayAdapter.getItem(position);
                Log.d(TAG,mselectedCity.getCity());
                Log.d(TAG,mselectedCity.getAllFirstPY());
                Log.d(TAG,mselectedCity.getNumber());
                Log.d(TAG,mselectedCity.getFirstPY());
                Log.d(TAG,mselectedCity.getProvince());
                Log.d(TAG,mselectedCity.getAllPY());
                Intent intent=new Intent();
                Bundle bundle=new Bundle();
                bundle.putParcelable("selectedcity",mselectedCity);
                intent.putExtras(bundle);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_back:
                finish();
                break;
            default:
                break;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
}
