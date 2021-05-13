package com.polyv.cloudclass;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.config.PolyvLiveSDKClient;
import com.easefun.polyv.cloudclassdemo.watch.PolyvCloudClassHomeActivity;
import com.easefun.polyv.linkmic.PolyvLinkMicClient;
import com.polyv.cloudclass.utils.JsonOptionUtil;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

public class PolyvConfigModule extends WXModule {

    public static String USER_ID;

    @JSMethod(uiThread = true)
    public void setConfig(JSONObject options, JSCallback callback) {

        if (options == null) {
            if (callback != null) {
                JSONObject err = new JSONObject();
                err.put("isSuccess", false);
                err.put("errMsg", "传入参数不能为空");
                callback.invoke(err);
            }
            return;
        }

        String appId = JsonOptionUtil.getString(options, "appId", "");
        String secret = JsonOptionUtil.getString(options, "appSecret", "");
        USER_ID = JsonOptionUtil.getString(options, "userId", "");

        if (TextUtils.isEmpty(appId)) {
            if (callback != null) {
                JSONObject err = new JSONObject();
                err.put("isSuccess", false);
                err.put("errMsg", "appId 不能为空");
                callback.invoke(err);
            }
            return;
        }

        if (TextUtils.isEmpty(secret)) {
            if (callback != null) {
                JSONObject err = new JSONObject();
                err.put("isSuccess", false);
                err.put("errMsg", "appSecret 不能为空");
                callback.invoke(err);
            }
            return;
        }

        if (TextUtils.isEmpty(USER_ID)) {
            if (callback != null) {
                JSONObject err = new JSONObject();
                err.put("isSuccess", false);
                err.put("errMsg", "userId 不能为空");
                callback.invoke(err);
            }
            return;
        }

        PolyvLinkMicClient.getInstance().setAppIdSecret(appId, secret);
        PolyvLiveSDKClient.getInstance().setAppIdSecret(appId, secret);

        if (callback != null) {
            JSONObject succeed = new JSONObject();
            succeed.put("isSuccess", true);
            callback.invoke(succeed);
        }

    }


    @JSMethod(uiThread = true)
    public void setViewerInfo(JSONObject options, JSCallback callback) {

        if (options == null) {
            if (callback != null) {
                JSONObject err = new JSONObject();
                err.put("isSuccess", false);
                err.put("errMsg", "传入参数不能为空");
                callback.invoke(err);
            }
            return;
        }

//        String android = PolyvUtils.getAndroidId(mWXSDKInstance.getContext());
        String viewerId = JsonOptionUtil.getString(options, "viewerId", "");
        String viewerName = JsonOptionUtil.getString(options, "viewerName", "");
        String viewerImg = JsonOptionUtil.getString(options, "viewerAvatar", "");
        String p4 = JsonOptionUtil.getString(options, "param4", "");
        String p5 = JsonOptionUtil.getString(options, "PARAM5", "");

        if (TextUtils.isEmpty(viewerId)) {
            if (callback != null) {
                JSONObject err = new JSONObject();
                err.put("isSuccess", false);
                err.put("errMsg", "viewerId 不能为空");
                callback.invoke(err);
            }
            return;
        }

        if (TextUtils.isEmpty(viewerImg)) {
            viewerImg = PolyvChatManager.DEFAULT_AVATARURL;
        }


        PolyvCloudClassHomeActivity.setViewerInfo(viewerId, viewerName, viewerImg, p4, p5);

        if (callback != null) {
            JSONObject succeed = new JSONObject();
            succeed.put("isSuccess", true);
            callback.invoke(succeed);
        }
    }

    @JSMethod(uiThread = false)
    public void setMarqueeConfig(JSONObject options, JSCallback callback) {
        if (options == null) {
            if (callback != null) {
                JSONObject err = new JSONObject();
                err.put("isSuccess", false);
                err.put("errMsg", "传入参数不能为空");
                callback.invoke(err);
            }
            return;
        }

        String code = JsonOptionUtil.getString(options, "code", "");
        PolyvCloudClassHomeActivity.setMarqueeCode(code);

        if (callback != null) {
            JSONObject succeed = new JSONObject();
            succeed.put("isSuccess", true);
            callback.invoke(succeed);
        }

    }
}
