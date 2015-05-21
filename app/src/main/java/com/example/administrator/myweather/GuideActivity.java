package com.example.administrator.myweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.administrator.util.ViewPagerAdapterUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/4/27.
 */
public class GuideActivity extends Activity implements ViewPager.OnPageChangeListener{
    private ViewPager viewPager;
    private List<View> viewList;
    private ViewPagerAdapterUtil vpAdapter;
    private Button btnGuiStart;
    private ImageView[] dots;
    private int[] ids={R.id.iv1,R.id.iv2,R.id.iv3};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        initView();
        initDots();
    }
    private void initView(){
        viewPager=(ViewPager)findViewById(R.id.viewpager);
        viewList=new ArrayList<>();
        LayoutInflater inflater=LayoutInflater.from(this);
        viewList.add(inflater.inflate(R.layout.guide_first, null));
        viewList.add(inflater.inflate(R.layout.guide_second,null));
        viewList.add(inflater.inflate(R.layout.guide_third,null));
        vpAdapter=new ViewPagerAdapterUtil(GuideActivity.this,viewList);
        viewPager.setAdapter(vpAdapter);
        viewPager.setOnPageChangeListener(this);
        btnGuiStart=(Button)viewList.get(2).findViewById(R.id.btn_guide_start);
        btnGuiStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences=getSharedPreferences("first_pref",MODE_PRIVATE);
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean("isFirstIn",false);
                editor.commit();
                Intent i=new Intent(GuideActivity.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
    private void initDots(){
        dots=new ImageView[viewList.size()];
        for(int i=0;i<viewList.size();i++){
            dots[i]=(ImageView)findViewById(ids[i]);
        }
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i=0;i<ids.length;i++){
            if(position==i){
                dots[i].setImageResource(R.drawable.page_indicator_focused);
            }
            else {
                dots[i].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

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
