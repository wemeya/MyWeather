package com.example.administrator.myweather;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.example.administrator.util.MyService;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Administrator on 2015/4/16.
 */
public class ServiceActivity extends Activity {
    MyService serviceBinder;
    Intent i;
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceBinder=((MyService.MyBinder)service).getService();
            try {
                URL[] urls=new URL[]{
                        new URL("http://www.amazon.com/somefiles.pdf"),
                        new URL("http://www.wrox.com/somefiles.pdf"),
                        new URL("http://www.google.com/somefiles.pdf"),
                        new URL("http://www.learn2develop.net/somefiles.pdf")
                };
                serviceBinder.urls=urls;
            }catch (MalformedURLException e){
                e.printStackTrace();
            }
            startService(i);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBinder=null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
    }

    public void startService(View view){
        i=new Intent(ServiceActivity.this,MyService.class);
        bindService(i,connection, Context.BIND_AUTO_CREATE);

    }
    public void stopService(View view){
        stopService(new Intent(getBaseContext(),MyService.class));
    }
}
