package com.example.administrator.myweather;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.baidu.speechsynthesizer.SpeechSynthesizer;
import com.baidu.speechsynthesizer.SpeechSynthesizerListener;
import com.baidu.speechsynthesizer.publicutility.SpeechError;
import com.example.administrator.app.MyApplication;
import com.example.administrator.bean.City;
import com.example.administrator.bean.TodayWeather;
import com.example.administrator.util.MyService;
import com.example.administrator.util.NetUtil;
import com.example.administrator.util.PinYinUtil;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;


public class MainActivity extends ActionBarActivity implements View.OnClickListener{

    private ImageView mUpdateBtn,mShareBtn,mSelectCitybtn,mLocationBtn;
    private TextView cityTv,timeTv,humidityTv,weekTv,pmDataTv,pmQualityTv,
            temperatureTv,climateTv,windTv;
    private ImageView weatherImg,pmImg;
    private ProgressBar updateProgressBar;
    private List<City> cityList;
    private static final int UPDATE_TODAY_WEATHER=1;
    private static final String TAG="MainActivity";
    private City selectedCity;
    private TodayWeather todayWeather,yesterdayWeather,tomorrowWeather,thirddayWeather,fourthdayWeather,fifthdayWeather;
    private SharedPreferences sharedPreferences;
    private MyService myService;
    private IntentFilter intentFilter;
    private Intent serviceIntent;
    private Button btnRead;
    private TextToSpeech mTTS;
    private ServiceConnection serviceConn= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //调用顺序bindService→onCreate→onBind→onServiceConnected
            myService=((MyService.MyBinder)service).getService();
            myService.cityCode=selectedCity.getNumber();
            myService.doSomethingRepeatedly();
            Log.d(TAG,"onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService=null;
        }
    };
    private BroadcastReceiver intentReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            parseXML(intent.getStringExtra("weather_info"));
            if(todayWeather!=null) {
                Message serviceMessage = new Message();
                serviceMessage.what = UPDATE_TODAY_WEATHER;
                serviceMessage.obj = todayWeather;
                mHandler.sendMessage(serviceMessage);
            }
        }
    };
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity->onCreate");
        setContentView(R.layout.weather_info);
        cityList=((MyApplication) this.getApplication()).getmCityList();
        initView();
        serviceIntent=new Intent(MainActivity.this,MyService.class);
        //serviceIntent.putExtra("citycode",selectedCity.getNumber());
        //先启动Service再绑定，这样Activity退出解绑以后Service还在后台运行。
        startService(serviceIntent);
        bindService(serviceIntent,serviceConn, Context.BIND_AUTO_CREATE);

    }
    void initView(){
        todayWeather=new TodayWeather();
        selectedCity=new City("北京","北京","101010100","B","BEIJING","BJ");
        mUpdateBtn=(ImageView)findViewById(R.id.title_update_btn);
        mShareBtn=(ImageView)findViewById(R.id.title_share);
        mSelectCitybtn=(ImageView)findViewById(R.id.title_city);
        mLocationBtn=(ImageView)findViewById(R.id.title_location);
        updateProgressBar=(ProgressBar)findViewById(R.id.title_update_progress);
        cityTv=(TextView)findViewById(R.id.city);
        timeTv=(TextView)findViewById(R.id.time);
        humidityTv=(TextView)findViewById(R.id.humidity);
        weekTv=(TextView)findViewById(R.id.week_today);
        pmDataTv=(TextView)findViewById(R.id.pm_data);
        pmQualityTv=(TextView)findViewById(R.id.pm_2_5_quality);
        pmImg=(ImageView)findViewById(R.id.pm_2_5_img);
        temperatureTv=(TextView)findViewById(R.id.temperature);
        climateTv=(TextView)findViewById(R.id.climate);
        windTv=(TextView)findViewById(R.id.wind);
        weatherImg=(ImageView)findViewById(R.id.weather_img);
        mUpdateBtn.setOnClickListener(this);
        mShareBtn.setOnClickListener(this);
        mSelectCitybtn.setOnClickListener(this);
        mLocationBtn.setOnClickListener(this);
        btnRead= (Button) findViewById(R.id.btn_title_read);
        btnRead.setOnClickListener(this);
        mTTS=new TextToSpeech(this,new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status==TextToSpeech.SUCCESS){
                    //设置朗读语言
                    int supported=mTTS.setLanguage(Locale.SIMPLIFIED_CHINESE);
                    if(supported!=mTTS.LANG_AVAILABLE&&supported!=
                            mTTS.LANG_COUNTRY_AVAILABLE){
                        //Toast.makeText(MainActivity.this,"不支持当前语言",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        sharedPreferences=getSharedPreferences("exitcity",MODE_PRIVATE);
        //第一次安装没有首选项为空
        if(sharedPreferences.getString("city","")==""){
            cityTv.setText("N/A");
            timeTv.setText("N/A");
            humidityTv.setText("N/A");
            pmDataTv.setText("N/A");
            pmQualityTv.setText("N/A");
            weekTv.setText("N/A");
            temperatureTv.setText("N/A");
            climateTv.setText("N/A");
            windTv.setText("N/A");
        }
        else {
            cityTv.setText(sharedPreferences.getString("city", ""));
            selectedCity.setNumber(sharedPreferences.getString("citycode",""));
            timeTv.setText(sharedPreferences.getString("time", ""));
            humidityTv.setText(sharedPreferences.getString("humidity", ""));
            pmDataTv.setText(sharedPreferences.getString("pmdata", ""));
            pmQualityTv.setText(sharedPreferences.getString("pmquality", ""));
            weekTv.setText(sharedPreferences.getString("week", ""));
            temperatureTv.setText(sharedPreferences.getString("temperature", ""));
            climateTv.setText(sharedPreferences.getString("climate", ""));
            windTv.setText(sharedPreferences.getString("wind", ""));
            updateClimateTypeImg(sharedPreferences.getString("climate",""),weatherImg);
            updatePm25Img(sharedPreferences.getString("pmdata",""));
            //试试第一次更新用服务来启动
            //queryWeatherCode(selectedCity.getNumber());
        }
    }
    void updatePm25Img(String pmdata){
        if(pmdata==""||pmdata==null||pmdata.equals("N/A"))return;
        int pmValue=Integer.parseInt(pmdata.trim());
        String pmImgStr="0_50";
        if (pmValue>50&&pmValue<201){
            int startV=(pmValue-1)/50*50+1;
            int endV=((pmValue-1)/50+1)*50;
            pmImgStr=Integer.toString(startV)+"_"+endV;
        }
        else if(pmValue>=201&&pmValue<301){
            pmImgStr="201_300";
        }
        else if(pmValue>=301){
            pmImgStr="greater_300";
        }
        Class aClass=R.drawable.class;
        int pmImgId=-1;
        try{
            Field pmField=aClass.getField("biz_plugin_weather_"+pmImgStr);
            Object pmImgO=pmField.get(new Integer(0));
            pmImgId=(int)pmImgO;
        }catch (Exception e){
            if(pmImgId==-1)
                pmImgId=R.drawable.biz_plugin_weather_0_50;
        }finally {
            Drawable drawable=getResources().getDrawable(pmImgId);
            pmImg.setImageDrawable(drawable);
        }
    }
    void updateClimateTypeImg(String climate,ImageView imageView){
        if(climate==""||climate==null||climate.equals("N/A"))return;
        String typeImg="biz_plugin_weather_"+ PinYinUtil.ConvertToSpell(climate.trim());
        Class aClass=R.drawable.class;
        int typeId=-1;
        try{
            Field field=aClass.getField(typeImg);
            Object value=field.get(new Integer(0));
            typeId=(int)value;
        }catch (Exception e){
            //e.printStackTrace();
            if(typeId==-1)
                typeId=R.drawable.biz_plugin_weather_qing;
        }finally {
            Drawable drawable=getResources().getDrawable(typeId);
            imageView.setImageDrawable(drawable);
        }
    }
    void updateTodayWeather(TodayWeather todayWeather){
        Log.d(TAG,todayWeather.toString());
        Log.d(TAG,yesterdayWeather.toString());
        Log.d(TAG,tomorrowWeather.toString());
        Log.d(TAG,thirddayWeather.toString());
        Log.d(TAG,fourthdayWeather.toString());
        Log.d(TAG,fifthdayWeather.toString());
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+"发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力："+todayWeather.getFengli());
        updatePm25Img(todayWeather.getPm25());
        updateClimateTypeImg(todayWeather.getType(),weatherImg);
        TodayWeather[] sevenWeathers=new TodayWeather[]{yesterdayWeather,todayWeather,
                tomorrowWeather,thirddayWeather,fourthdayWeather,fifthdayWeather};
        updateSevendaysWeather(sevenWeathers);
        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
        setViewVisibility();
    }
    void updateSevendaysWeather(TodayWeather[] weathers){
        LinearLayout msevendaysLayout=(LinearLayout)findViewById(R.id.gallery_linearlayout);
        LayoutInflater layoutInflater=LayoutInflater.from(this);
        View itemView=null;
        ImageView imageView=null;
        TextView textView_date,textView_high,textView_low,textView_type;
        msevendaysLayout.removeAllViewsInLayout();
        for (int i = 0; i < weathers.length; i++) {
            itemView=layoutInflater.inflate(R.layout.gallery_item, null);
            imageView=(ImageView) itemView.findViewById(R.id.gallery_img);
            textView_date=(TextView)itemView.findViewById(R.id.gallery_weekday);
            textView_high=(TextView) itemView.findViewById(R.id.gallery_high_temper);
            textView_low=(TextView) itemView.findViewById(R.id.gallery_low_temper);
            textView_type=(TextView) itemView.findViewById(R.id.gallery_climate_text);
            updateClimateTypeImg(weathers[i].getType(),imageView);
            Log.d("MainActivity",weathers[i].getType());
            textView_date.setText(weathers[i].getDate());
            textView_high.setText(weathers[i].getHigh());
            textView_low.setText(weathers[i].getLow());
            textView_type.setText(weathers[i].getType());
            msevendaysLayout.addView(itemView);
        }
    }
    void exitMyWeather(){
        sharedPreferences=getSharedPreferences("exitcity",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("city",cityTv.getText().toString());
        editor.putString("citycode",selectedCity.getNumber());
        editor.putString("time",timeTv.getText().toString());
        editor.putString("humidity",humidityTv.getText().toString());
        editor.putString("week",weekTv.getText().toString());
        editor.putString("pmdata",pmDataTv.getText().toString());
        editor.putString("pmquality",pmQualityTv.getText().toString());
        //editor.putString("pmimg",pmImg.gets().toString());
        editor.putString("temperature",temperatureTv.getText().toString());
        editor.putString("climate",climateTv.getText().toString());
        editor.putString("wind",windTv.getText().toString());
        //editor.putString("weatherimg",weatherImg.getText().toString());
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        intentFilter=new IntentFilter();
        intentFilter.addAction("WEATHER_UPDATED_ACTION");
        registerReceiver(intentReceiver,intentFilter);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(intentReceiver);
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTTS!=null)
            mTTS.shutdown();
        Log.d(TAG,"onDestroy");
        unbindService(serviceConn);
       // stopService(serviceIntent);
        exitMyWeather();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.title_update_btn:
                //SharedPreferences sharedPreferences=getSharedPreferences("config",MODE_PRIVATE);
                //String cityCode=sharedPreferences.getString("main_city","101010100");
                String cityCode=selectedCity.getNumber();
                mUpdateBtn.setVisibility(View.GONE);
                updateProgressBar.setVisibility(View.VISIBLE);
                Log.d("myWeather",cityCode);
                queryWeatherCode(cityCode);
                break;
            case R.id.title_share:
                Intent intent =new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,todayWeather.getCity()+"天气： "+todayWeather.getType()+
                   ", 温度： "+todayWeather.getLow()+"-"+todayWeather.getHigh()+
                   ", 风力： "+todayWeather.getFengli());
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent,"分享天气"));
                //intent.setClass(MainActivity.this,ServiceActivity.class);
                //startActivity(intent);
                break;
            case R.id.title_city:
                Intent selectCity=new Intent(MainActivity.this,SelectCity.class);
                startActivityForResult(selectCity,1);
                break;
            case R.id.title_location:
                Intent location=new Intent(MainActivity.this,LocationActivity.class);
                startActivityForResult(location, 2);
                break;
            case R.id.btn_title_read:
                if(NetUtil.getNetworkState(this)==NetUtil.NETWORK_NONE){
                    Toast.makeText(this,"语音播报须在联网环境下，请检查您的网络设置!",Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String readString = todayWeather.getCity() + "天气： " + todayWeather.getType() +
                                ", 温度： " + todayWeather.getLow() + "-" + todayWeather.getHigh() +
                                ", 风力： " + todayWeather.getFengli();
                        // 注：第二个参数当前请传入任意非空字符串即可
                        SpeechSynthesizer speechSynthesizer = new SpeechSynthesizer(getApplicationContext(), "holder", new SpeechSynthesizerListener() {
                            @Override
                            public void onStartWorking(SpeechSynthesizer speechSynthesizer) {

                            }

                            @Override
                            public void onSpeechStart(SpeechSynthesizer speechSynthesizer) {

                            }

                            @Override
                            public void onNewDataArrive(SpeechSynthesizer speechSynthesizer, byte[] bytes, boolean b) {

                            }

                            @Override
                            public void onBufferProgressChanged(SpeechSynthesizer speechSynthesizer, int i) {

                            }

                            @Override
                            public void onSpeechProgressChanged(SpeechSynthesizer speechSynthesizer, int i) {

                            }

                            @Override
                            public void onSpeechPause(SpeechSynthesizer speechSynthesizer) {

                            }

                            @Override
                            public void onSpeechResume(SpeechSynthesizer speechSynthesizer) {

                            }

                            @Override
                            public void onCancel(SpeechSynthesizer speechSynthesizer) {

                            }

                            @Override
                            public void onSynthesizeFinish(SpeechSynthesizer speechSynthesizer) {

                            }

                            @Override
                            public void onSpeechFinish(SpeechSynthesizer speechSynthesizer) {

                            }

                            @Override
                            public void onError(SpeechSynthesizer speechSynthesizer, SpeechError speechError) {

                            }
                        });
                        speechSynthesizer.setApiKey("CvNxAUMkTTsujGUQQMAq2eqn", "jVDwGc1MbRUR6jwU2eqPFG8U1pXKr1KO");
                        speechSynthesizer.speak(readString);
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1&&resultCode==RESULT_OK){
            selectedCity=data.getParcelableExtra("selectedcity");
            queryWeatherCode(selectedCity.getNumber());
            myService.cityCode=selectedCity.getNumber();
        }
        else if (requestCode==2&&resultCode==RESULT_OK){
            City city;
            for(int i=0;i<cityList.size();i++){
                city=cityList.get(i);
                if(data.getStringExtra("district").contains(city.getCity())){
                    Log.d(TAG,"Location City!!!!!");
                    Toast.makeText(MainActivity.this,city.getProvince()+city.getCity()+city.getNumber()
                    ,Toast.LENGTH_LONG).show();
                    queryWeatherCode(city.getNumber());
                    selectedCity.setNumber(city.getNumber());
                    selectedCity.setCity(city.getCity());
                    selectedCity.setProvince(city.getProvince());
                    selectedCity.setFirstPY(city.getFirstPY());
                    selectedCity.setAllFirstPY(city.getAllFirstPY());
                    selectedCity.setAllPY(city.getAllPY());
                    myService.cityCode=selectedCity.getNumber();
                    break;
                }
            }

        }
    }
    private void setViewVisibility(){
        updateProgressBar.setVisibility(View.GONE);
        mUpdateBtn.setVisibility(View.VISIBLE);
    }
    private void queryWeatherCode(String cityCode){
        final String address="http://wthrcdn.etouch.cn/WeatherApi?citykey="+cityCode;
        Log.d("myWeather",address);
        if(NetUtil.getNetworkState(this)==NetUtil.NETWORK_NONE){
            Toast.makeText(this,"网络挂了!",Toast.LENGTH_SHORT).show();
            setViewVisibility();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    HttpParams httpParams=new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams,5000);
                    HttpConnectionParams.setSoTimeout(httpParams,10000);
                    HttpClient httpClient=new DefaultHttpClient(httpParams);
                    HttpGet httpGet=new HttpGet(address);
                    HttpResponse httpResponse=httpClient.execute(httpGet);
                    //else 获取数据超时
                    if (httpResponse.getStatusLine().getStatusCode()==200){
                        HttpEntity httpEntity=httpResponse.getEntity();
                        InputStream responseStream=httpEntity.getContent();
                        responseStream =new GZIPInputStream(responseStream);
                        BufferedReader reader=new BufferedReader(new InputStreamReader(responseStream));
                        StringBuilder response=new StringBuilder();
                        String str;
                        while ((str=reader.readLine())!=null){
                            response.append(str);
                        }
                        String responseStr=response.toString();
                        Log.d("myWeather",responseStr);
                        //todayWeather=parseXML(responseStr);
                        parseXML(responseStr);
                        if (todayWeather!=null){
                            Log.d("myapp2",todayWeather.toString());
                            Message msg=new Message();
                            msg.what=UPDATE_TODAY_WEATHER;
                            msg.obj=todayWeather;
                            mHandler.sendMessage(msg);
                        }
                    }else {
                        Toast.makeText(MainActivity.this,"网络延迟!",Toast.LENGTH_LONG).show();
                        setViewVisibility();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void parseXML(String xmldata){
        try {
            int fengxiangCount=0;
            int fengliCount=0;
            int dateCount=0;
            int highCount=0;
            int lowCount=0;
            int typeCount=0;
            XmlPullParserFactory fac=XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser=fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType=xmlPullParser.getEventType();
            Log.d("myapp2","parseXML");
            while(eventType!=XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather=new TodayWeather();
                            yesterdayWeather=new TodayWeather();
                            tomorrowWeather=new TodayWeather();
                            thirddayWeather=new TodayWeather();
                            fourthdayWeather=new TodayWeather();
                            fifthdayWeather=new TodayWeather();
                        }
                        if (todayWeather!=null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                                Log.d("myapp2", "city: " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                                Log.d("myapp2", "updatetime: " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                                Log.d("myapp2", "shidu: " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                                Log.d("myapp2", "wendu: " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                                Log.d("myapp2", "pm2.5: " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                                Log.d("myapp2", "quality: " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                Log.d("myapp2", "fengxiang: " + xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                Log.d("myapp2", "fengli: " + xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                Log.d("myapp2", "date: " + xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText());
                                Log.d("myapp2", "high: " + xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText());
                                Log.d("myapp2", "low: " + xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                Log.d("myapp2", "type: " + xmlPullParser.getText());
                                typeCount++;
                            }else if(xmlPullParser.getName().equals("date_1")){
                                eventType=xmlPullParser.next();
                                yesterdayWeather.setDate(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("high_1")){
                                eventType = xmlPullParser.next();
                                yesterdayWeather.setHigh(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("low_1")){
                                eventType = xmlPullParser.next();
                                yesterdayWeather.setLow(xmlPullParser.getText());

                            }else if(xmlPullParser.getName().equals("type_1")){
                                eventType = xmlPullParser.next();
                                yesterdayWeather.setType(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("date")&&dateCount==1){
                                eventType = xmlPullParser.next();
                                tomorrowWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high")&&highCount==1){
                                eventType = xmlPullParser.next();
                                tomorrowWeather.setHigh(xmlPullParser.getText());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low")&&lowCount==1){
                                eventType = xmlPullParser.next();
                                tomorrowWeather.setLow(xmlPullParser.getText());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type")&&typeCount==1){
                                eventType = xmlPullParser.next();
                                tomorrowWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }else if(xmlPullParser.getName().equals("date")&&dateCount==2){
                                eventType=xmlPullParser.next();
                                thirddayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high")&&highCount==2){
                                eventType=xmlPullParser.next();
                                thirddayWeather.setHigh(xmlPullParser.getText());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low")&&lowCount==2){
                                eventType=xmlPullParser.next();
                                thirddayWeather.setLow(xmlPullParser.getText());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type")&&typeCount==2){
                                eventType=xmlPullParser.next();
                                thirddayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }
                            else if(xmlPullParser.getName().equals("date")&&dateCount==3){
                                eventType=xmlPullParser.next();
                                fourthdayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high")&&highCount==3){
                                eventType=xmlPullParser.next();
                                fourthdayWeather.setHigh(xmlPullParser.getText());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low")&&lowCount==3){
                                eventType=xmlPullParser.next();
                                fourthdayWeather.setLow(xmlPullParser.getText());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type")&&typeCount==3){
                                eventType=xmlPullParser.next();
                                fourthdayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }else if(xmlPullParser.getName().equals("date")&&dateCount==4){
                                eventType=xmlPullParser.next();
                                fifthdayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high")&&highCount==4){
                                eventType=xmlPullParser.next();
                                fifthdayWeather.setHigh(xmlPullParser.getText());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low")&&lowCount==4){
                                eventType=xmlPullParser.next();
                                fifthdayWeather.setLow(xmlPullParser.getText());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type")&&typeCount==4){
                                eventType=xmlPullParser.next();//最后有next方法，为什么这里还要调用next()方法
                                fifthdayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType=xmlPullParser.next();
            }
        }catch (Exception e){
            e.printStackTrace();

        }
        //return todayWeather;
    }
}
