package com.polyv.cloudclass;

import android.app.Application;

import com.easefun.polyv.cloudclass.config.PolyvLiveSDKClient;

import io.dcloud.weex.AppHookProxy;

public class PolyvAppProxy implements AppHookProxy {
    String TAG = "PolyvAppProxy";

    @Override
    public void onCreate(Application application) {
        PolyvLiveSDKClient liveSDKClient = PolyvLiveSDKClient.getInstance();
        liveSDKClient.initContext(application);
        liveSDKClient.enableHttpDns(false);
        liveSDKClient.enableIPV6(true);

    }


}
