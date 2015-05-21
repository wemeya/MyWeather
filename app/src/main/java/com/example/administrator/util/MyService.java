package com.example.administrator.util;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;


/**
 * Created by Administrator on 2015/4/16.
 */
public class MyService extends Service {
    private static final String TAG="MyService";
    int counter=0;
    private String responseStr;
    public URL[] urls;
    public String cityCode;
    static final int UPDATE_INTERVAL=3600000;
    private Timer timer=new Timer();
    private final IBinder binder=new MyBinder();
    public class MyBinder extends Binder{
      public MyService getService(){
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //BindService 不会调用该方法，startService()调用
//        Log.d(TAG,"onStartCommand");
//        Log.d(TAG,flags+"");
//        Log.d(TAG,startId+"");
//        if(intent==null)return START_NOT_STICKY;
//        cityCode=intent.getStringExtra("citycode");
//        doSomethingRepeatedly();
//        //new DoBackgroundTask().execute(urls);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
    }
    public void doSomethingRepeatedly(){
       timer.scheduleAtFixedRate(new TimerTask() {
           @Override
           public void run() {
               GetNetData();
               if(responseStr==null||responseStr.length()==0)return;
               Log.d(TAG,responseStr);
               Intent broadcastIntent =new Intent();
               broadcastIntent.putExtra("weather_info",responseStr);
               broadcastIntent.setAction("WEATHER_UPDATED_ACTION");
               getBaseContext().sendBroadcast(broadcastIntent);
           }
       },0,UPDATE_INTERVAL);
    }
    private void GetNetData(){
        if(cityCode==null)return;
        if(NetUtil.getNetworkState(this)==NetUtil.NETWORK_NONE){
            Toast.makeText(this,"网络挂了!",Toast.LENGTH_SHORT).show();
            return;
        }//设置请求时间timeout
        HttpParams httpParams=new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams,5000);
        HttpConnectionParams.setSoTimeout(httpParams,10000);
        HttpClient httpClient=new DefaultHttpClient(httpParams);
        HttpGet httpGet=new HttpGet("http://wthrcdn.etouch.cn/WeatherApi?citykey="+cityCode);
        //httpGet.setParams(httpParams);
        HttpResponse httpResponse=null;
        try {
            httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = httpResponse.getEntity();
                InputStream inputStream = httpEntity.getContent();
                inputStream=new GZIPInputStream(inputStream);
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder responseStrBuilder =new StringBuilder();
                String string;
                while((string=bufferedReader.readLine())!=null){
                    responseStrBuilder.append(string);
                }
                responseStr=responseStrBuilder.toString();
            }else if(httpResponse.getStatusLine().getStatusCode()==408){
                return;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private class DoBackgroundTask extends AsyncTask<URL,Integer,Long>{

        @Override
        protected Long doInBackground(URL... params) {
            int count=params.length;
            long totalBytesDownloaded=0;
            for (int i=0;i<count;i++){
                totalBytesDownloaded+=DownloadFile(params[i]);
                publishProgress((int)(((i+1)/(float)count)*100));
            }
            return totalBytesDownloaded;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.d("Downloading files",String.valueOf(values[0]+"% downloaded"));
            Toast.makeText(getBaseContext(),String.valueOf(values[0]+"% downloaded")
            ,Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            Toast.makeText(getBaseContext(),"Downloaded "+ aLong +" bytes",
                    Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }
    private int DownloadFile(URL url){
        try {
            Thread.sleep(5000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return 100;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timer!=null){
            timer.cancel();
        }
        Log.d(TAG,"onDestroy");
        Toast.makeText(this,"Service Destroyed",Toast.LENGTH_LONG).show();
    }
}
