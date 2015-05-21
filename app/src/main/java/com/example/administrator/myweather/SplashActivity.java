package com.example.administrator.myweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by Administrator on 2015/4/27.
 */
public class SplashActivity extends Activity {
    private Boolean isFirstin=false;
    private static final int GO_HOME=1000;
    private static final int GO_GUIDE=1001;
    private static final long SPLASH_DELAY_MILLI=3000;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case GO_HOME:
                    goHome();
                    break;
                case GO_GUIDE:
                    goGuide();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        init();
    }
    private void init(){
        SharedPreferences sharedPreferences=getSharedPreferences("first_pref",MODE_PRIVATE);
        isFirstin=sharedPreferences.getBoolean("isFirstIn", true);
        if (isFirstin){
            // 使用Handler的postDelayed方法，3秒后执行跳转到MainActivity
            handler.sendEmptyMessageDelayed(GO_GUIDE,SPLASH_DELAY_MILLI);
        }
        else {
            handler.sendEmptyMessageDelayed(GO_HOME,SPLASH_DELAY_MILLI);
        }
    }
    private void goHome(){
        Intent intent=new Intent(SplashActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void goGuide(){
        Intent intent=new Intent(SplashActivity.this,GuideActivity.class);
        startActivity(intent);
        finish();
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
