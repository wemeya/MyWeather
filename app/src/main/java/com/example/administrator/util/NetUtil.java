package com.example.administrator.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;

/**
 * Created by Administrator on 2015/3/20.
 */
public class NetUtil {
    public static final int NETWORK_NONE=0;
    public static final int NETWORK_WIFI=1;
    public static final int NETWORK_MOBILE=2;

    public static int getNetworkState(Context context){
        ConnectivityManager conManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //wifi
        State state=conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if (state==State.CONNECTED||state==State.CONNECTING){
            return NETWORK_WIFI;
        }
        //Mobile
        state=conManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        if(state==State.CONNECTING||state==State.CONNECTED){
            return NETWORK_MOBILE;
        }
        return NETWORK_NONE;
    }
}
