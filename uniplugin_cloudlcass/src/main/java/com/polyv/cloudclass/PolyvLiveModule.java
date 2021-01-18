package com.polyv.cloudclass;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.easefun.polyv.businesssdk.model.video.PolyvPlayBackVO;
import com.easefun.polyv.businesssdk.service.PolyvLoginManager;
import com.easefun.polyv.cloudclass.chat.PolyvChatApiRequestHelper;
import com.easefun.polyv.cloudclass.config.PolyvLiveChannelType;
import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.PolyvLiveStatusVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackListType;
import com.easefun.polyv.cloudclassdemo.watch.PolyvCloudClassHomeActivity;
import com.easefun.polyv.foundationsdk.net.PolyvResponseBean;
import com.easefun.polyv.foundationsdk.net.PolyvResponseExcutor;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.polyv.cloudclass.utils.JsonOptionUtil;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.HttpException;

/*
默认参与者为false，暂无开放接口
*/
public class PolyvLiveModule extends WXModule {

    private String TAG = "PolyvLiveModule";

    private Disposable liveDetailDisposable;

    @JSMethod(uiThread = true)
    public void startLive(JSONObject options, final JSCallback callback) {

        if (options == null)
            return;

        final String channelID = JsonOptionUtil.getString(options, "channelId", "");
//        final boolean isParticipant = JsonOptionUtil.getBoolean(options, "isParticipant", false);

        if (TextUtils.isEmpty(PolyvConfigModule.USER_ID)) {
            if (callback != null) {
                JSONObject err = new JSONObject();
                err.put("errMsg", "请先通过setConfig初始化");
                callback.invoke(err);
            }
            return;
        }

        requestLiveStatus(channelID, callback);
    }

    @JSMethod(uiThread = true)
    public void startPlayback(JSONObject options, final JSCallback callback) {

        if (options == null)
            return;


        //设置参数
        final String videoId = JsonOptionUtil.getString(options, "videoId", "");
        final String channelId = JsonOptionUtil.getString(options, "channelId", "");
        final boolean isVideoList = JsonOptionUtil.getBoolean(options, "videoType", false);
        final int videoType = isVideoList ? PolyvPlaybackListType.VOD : PolyvPlaybackListType.PLAYBACK;

        if (TextUtils.isEmpty(PolyvConfigModule.USER_ID)) {
            callbackResultOnce(callback, false, "请先通过setConfig初始化");
            return;
        }

        PolyvLoginManager.getPlayBackType(videoId, new PolyvrResponseCallback<PolyvPlayBackVO>() {
            @Override
            public void onSuccess(PolyvPlayBackVO playBack) {
                if (mWXSDKInstance == null || !(mWXSDKInstance.getContext() instanceof Activity)) {
                    callbackResultOnce(callback, false, "context is not Activity");
                    return;
                }

                switch (playBack.getLiveType()) {
                    case 0:
                        PolyvCloudClassHomeActivity.startActivityForPlayBack((Activity) mWXSDKInstance.getContext(),
                                videoId, channelId, PolyvConfigModule.USER_ID, true, videoType);

                        callbackResultOnce(callback, true, "");
                        break;
                    case 1:
                        if (isVideoList) {
                            callbackResultOnce(callback, false, "三分屏场景暂不支持使用点播列表播放");
                            return;
                        }
                        PolyvCloudClassHomeActivity.startActivityForPlayBack((Activity) mWXSDKInstance.getContext(),
                                videoId, channelId, PolyvConfigModule.USER_ID, false, videoType);
                        callbackResultOnce(callback, true, "");
                        break;
                    default:
                        callbackResultOnce(callback, false, "只支持云课堂类型频道或普通直播类型频道");
                        break;
                }

            }

            @Override
            public void onFailure(PolyvResponseBean<PolyvPlayBackVO> responseBean) {
                super.onFailure(responseBean);
                Log.e(TAG, responseBean.toString());
                callbackResultOnce(callback, false, responseBean.toString());
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                Log.e(TAG, e.getMessage() + "");
                callbackResultOnce(callback, false, exception2Message(e));
            }
        });


    }

    private void requestLiveStatus(final String channelId, final JSCallback callback) {
        PolyvResponseExcutor.excuteUndefinData(PolyvApiManager.getPolyvLiveStatusApi().geLiveStatusJson(channelId)
                , new PolyvrResponseCallback<PolyvLiveStatusVO>() {
                    @Override
                    public void onSuccess(PolyvLiveStatusVO statusVO) {

                        PolyvLiveChannelType channelType = null;
                        try {
                            channelType = PolyvLiveChannelType.mapFromServerString(statusVO.getChannelType());
                        } catch (PolyvLiveChannelType.UnknownChannelTypeException e) {
                            callbackResultOnce(callback, false, "未知的频道类型");
                            e.printStackTrace();
                            return;
                        }
                        if (channelType != PolyvLiveChannelType.CLOUD_CLASS && channelType != PolyvLiveChannelType.NORMAL) {
                            callbackResultOnce(callback, false, "只支持云课堂类型频道或普通直播类型频道");
                            return;
                        }
                        final boolean isAlone = channelType == PolyvLiveChannelType.NORMAL;//是否有ppt


                        requestLiveDetail(channelId, new Consumer<String>() {
                            @Override
                            public void accept(String rtcType) throws Exception {
                                if (mWXSDKInstance != null && mWXSDKInstance.getContext() instanceof Activity) {
                                    callbackResultOnce(callback, true, "");
                                    PolyvCloudClassHomeActivity.startActivityForLiveWithParticipant((Activity) mWXSDKInstance.getContext(), channelId,
                                            PolyvConfigModule.USER_ID, isAlone, false, rtcType);
                                } else {
                                    callbackResultOnce(callback, false, "context is not Activity");
                                }
                            }
                        }, callback);


                    }

                    @Override
                    public void onFailure(PolyvResponseBean<PolyvLiveStatusVO> responseBean) {
                        super.onFailure(responseBean);
                        Log.e(TAG, responseBean.toString());
                        callbackResultOnce(callback, false, responseBean.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e(TAG, e.getMessage() + "");
                        callbackResultOnce(callback, false, exception2Message(e));
                    }
                });
    }

    private void requestLiveDetail(String channelId, final Consumer<String> onSuccess, final JSCallback callback) {
        if (liveDetailDisposable != null) {
            liveDetailDisposable.dispose();
        }
        liveDetailDisposable = PolyvResponseExcutor.excuteUndefinData(PolyvChatApiRequestHelper.getInstance()
                .requestLiveClassDetailApi(channelId), new PolyvrResponseCallback<PolyvLiveClassDetailVO>() {
            @Override
            public void onSuccess(PolyvLiveClassDetailVO polyvLiveClassDetailVO) {
                try {
                    onSuccess.accept(polyvLiveClassDetailVO.getData().getRtcType());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                callbackResultOnce(callback, false, exception2Message(e));
            }
        });
    }

    private void callbackResultOnce(JSCallback callback, boolean isSuccess, String errMsg) {
        if (callback != null) {
            JSONObject data = new JSONObject();
            data.put("isSuccess", isSuccess);
            data.put("errMsg", errMsg);
            callback.invoke(data);
        }
    }

    private String exception2Message(Throwable e) {
        if (e instanceof HttpException) {
            try {
                String message = ((HttpException) e).response().errorBody().string();
                return message;
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return e.getMessage();
    }

}
