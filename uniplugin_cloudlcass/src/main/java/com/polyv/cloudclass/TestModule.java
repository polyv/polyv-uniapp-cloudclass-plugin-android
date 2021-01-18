package com.polyv.cloudclass;

import android.app.Activity;
import android.content.Intent;

import com.easefun.polyv.cloudclassdemo.login.PolyvCloudClassLoginActivity;
import com.easefun.polyv.commonui.base.PolyvBaseActivity;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.common.WXModule;

public class TestModule extends WXModule {

    @JSMethod(uiThread = true)
    public void goToLogin() {
        if(mWXSDKInstance != null && mWXSDKInstance.getContext() instanceof Activity) {
            Intent intent = new Intent(mWXSDKInstance.getContext(), PolyvCloudClassLoginActivity.class);
            ((Activity)mWXSDKInstance.getContext()).startActivity(intent);
        }

    }

}
