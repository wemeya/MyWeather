package com.example.administrator.util;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.example.administrator.bean.City;
import com.example.administrator.myweather.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/4/7.
 */
public class AdapterUtil extends ArrayAdapter {
    private List<City> cityList=new ArrayList<>();
    private LayoutInflater layoutInflater;
    private TextView adpterTextView,pinyinTextView;


    public AdapterUtil(Context context, int resource, int textViewResourceId, List objects) {
        super(context, resource, textViewResourceId, objects);
        this.cityList=objects;
        //layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater=LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        City city=cityList.get(position);
        if(convertView==null){
            convertView=layoutInflater.inflate(R.layout.selectcity_list,null);
        }
        adpterTextView=(TextView)convertView.findViewById(R.id.citylisttext);
        pinyinTextView=(TextView)convertView.findViewById(R.id.pinyintext);
        adpterTextView.setText(city.getCity());
        pinyinTextView.setText(city.getAllPY());
        return convertView;
    }
}
