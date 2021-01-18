package com.easefun.polyv.cloudclassdemo.login;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.PolyvChatDomainManager;
import com.easefun.polyv.businesssdk.model.chat.PolyvChatDomain;
import com.easefun.polyv.businesssdk.model.video.PolyvPlayBackVO;
import com.easefun.polyv.businesssdk.service.PolyvLoginManager;
import com.easefun.polyv.businesssdk.vodplayer.PolyvVodSDKClient;
import com.easefun.polyv.cloudclass.chat.PolyvChatApiRequestHelper;
import com.easefun.polyv.cloudclass.config.PolyvLiveChannelType;
import com.easefun.polyv.cloudclass.config.PolyvLiveSDKClient;
import com.easefun.polyv.cloudclass.config.PolyvVClassGlobalConfig;
import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.PolyvLiveStatusVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackListType;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.PolyvCloudClassHomeActivity;
import com.easefun.polyv.commonui.base.PolyvBaseActivity;
import com.easefun.polyv.commonui.player.widget.PolyvSoftView;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.net.PolyvResponseBean;
import com.easefun.polyv.foundationsdk.net.PolyvResponseExcutor;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.easefun.polyv.linkmic.PolyvLinkMicClient;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.io.IOException;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.adapter.rxjava2.HttpException;

/**
 * @author df
 * @create 2018/8/27
 * @Describe
 */
public class PolyvCloudClassLoginActivity extends PolyvBaseActivity implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="成员变量">
    private ImageView loginLogo;
    private TextView loginLogoText;
    private EditText userId;
    private EditText channelId;
    private EditText appId;
    private EditText appSecert;
    private TextView loginTv;
    private PolyvSoftView softLayout;
    private LinearLayout playbackLayout, liveLayout;
    private EditText playbackVideoId, playbackChannelId;
    private EditText playbackAppId, playbackUserId;
    private EditText playbackAppSecret;
    private RelativeLayout liveGroupLayout;
    private RelativeLayout playbackGroupLayout;
    private Disposable getTokenDisposable, verifyDispose, liveDetailDisposable;
    private ProgressDialog progress;
    private SwitchCompat playbackVodListSwitch;

    //参与者测试
    private EditText etParticipantNickName;
    private EditText etParticipantViewerId;
    //是否是参与者
    private boolean isParticipant = false;

    private static final String TAG = "PolyvCloudClassLoginAct";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.polyv_activity_cloudclass_login);
        initialView();

        setTestData();   // for test
        //测试参与者
//        testParticipant();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkLoginTvSelected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getTokenDisposable != null) {
            getTokenDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initialView() {
        initialTopLayout();
        initialLiveVideoView();
        initialPlayBackVideoView();
        intialLogoView();
    }

    private void initialTopLayout() {
        liveGroupLayout = findViewById(R.id.live_group_layout);
        playbackGroupLayout = findViewById(R.id.playback_group_layout);

        liveGroupLayout.setOnClickListener(this);
        playbackGroupLayout.setOnClickListener(this);

        liveGroupLayout.setSelected(true);
        playbackGroupLayout.setSelected(false);

        progress = new ProgressDialog(this);
        progress.setMessage(getResources().getString(R.string.login_waiting));
        progress.setCanceledOnTouchOutside(false);
        progress.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (getTokenDisposable != null) {
                    getTokenDisposable.dispose();
                }
                if (verifyDispose != null) {
                    verifyDispose.dispose();
                }
                loginTv.setEnabled(true);
                checkLoginTvSelected();
            }
        });
    }

    private void initialLiveVideoView() {
        liveLayout = findViewById(R.id.live_layout);
        userId = findViewById(R.id.user_id);
        channelId = findViewById(R.id.channel_id);
        appId = findViewById(R.id.app_id);
        appSecert = findViewById(R.id.app_secert);

        userId.addTextChangedListener(textWatcher);
        channelId.addTextChangedListener(textWatcher);
        appId.addTextChangedListener(textWatcher);
        appSecert.addTextChangedListener(textWatcher);
    }

    private void initialPlayBackVideoView() {
        playbackLayout = findViewById(R.id.playback_layout);
        playbackVideoId = findViewById(R.id.playback_video_id);
        playbackChannelId = findViewById(R.id.playback_channel_id);
        playbackAppId = findViewById(R.id.playback_app_id);
        playbackUserId = findViewById(R.id.playback_user_id);
        playbackAppSecret = findViewById(R.id.playback_app_secret);
        playbackVodListSwitch = findViewById(R.id.playback_vodlist_sw);

        playbackVideoId.addTextChangedListener(textWatcher);
        playbackChannelId.addTextChangedListener(textWatcher);
        playbackAppId.addTextChangedListener(textWatcher);
        playbackUserId.addTextChangedListener(textWatcher);
        playbackAppSecret.addTextChangedListener(textWatcher);
    }

    private void intialLogoView() {
        loginLogo = findViewById(R.id.login_logo);
        loginLogoText = findViewById(R.id.login_logo_text);
        loginTv = findViewById(R.id.login);

        softLayout = findViewById(R.id.polyv_soft_listener_layout);
        softLayout.setOnKeyboardStateChangedListener(new PolyvSoftView.IOnKeyboardStateChangedListener() {
            @Override
            public void onKeyboardStateChanged(int state) {
                showTitleLogo(state != PolyvSoftView.KEYBOARD_STATE_SHOW);
            }
        });

        loginTv.setOnClickListener(this);
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="textWatcher监听">
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkLoginTvSelected();
        }
    };
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onClick方法">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.login) {
            login();

        } else if (id == R.id.live_group_layout) {
            showLiveGroup();

        } else if (id == R.id.playback_group_layout) {
            showPlayBackGroup();

        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="View显示控制">
    private void showTitleLogo(boolean showlog) {
        loginLogoText.setVisibility(!showlog ? View.VISIBLE : View.GONE);
        loginLogo.setVisibility(showlog ? View.VISIBLE : View.GONE);
    }

    private void showLiveGroup() {
        liveGroupLayout.setSelected(true);
        playbackGroupLayout.setSelected(false);

        liveLayout.setVisibility(View.VISIBLE);
        playbackLayout.setVisibility(View.GONE);
        playbackVodListSwitch.setVisibility(View.GONE);

        loginTv.setSelected(!TextUtils.isEmpty(userId.getText())
                && !TextUtils.isEmpty(appSecert.getText())
                && (!TextUtils.isEmpty(channelId.getText())
                && !TextUtils.isEmpty(appId.getText())));
    }

    private void showPlayBackGroup() {
        liveGroupLayout.setSelected(false);
        playbackGroupLayout.setSelected(true);

        liveLayout.setVisibility(View.GONE);
        playbackLayout.setVisibility(View.VISIBLE);
        playbackVodListSwitch.setVisibility(View.VISIBLE);

        loginTv.setSelected(!isEmpty(playbackAppId)
                && !isEmpty(playbackVideoId));
    }

    private void testParticipant() {
        View participantLoginView = LayoutInflater.from(this).inflate(R.layout.polyv_cloud_class_participant_login, (ViewGroup) findViewById(android.R.id.content), true);
        etParticipantNickName = participantLoginView.findViewById(R.id.polyv_participant_login_nick_name);
        etParticipantViewerId = participantLoginView.findViewById(R.id.polyv_participant_login_viewer_id);
        isParticipant = true;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置测试数据">
    private void setTestData() {
        appId.setText("f9syxhkrbn");
        appSecert.setText("10fa85ce82e34988906c4b1250c0ebd5");
        userId.setText("14da40e138");
        channelId.setText("333328");

        appId.setText("fhtw392viy");
        appSecert.setText("d255c756d527492fade5f8b7a55a873d");
        userId.setText("417cfa5cc1");
        channelId.setText("420578");

        playbackChannelId.setText("420578");
        playbackUserId.setText("417cfa5cc1");
        playbackVideoId.setText("417cfa5cc1da485a29f972a4a9111562_4");
        playbackAppId.setText("fhtw392viy");
        playbackAppSecret.setText("d255c756d527492fade5f8b7a55a873d");
    }

    private void checkLoginTvSelected() {
        if (liveGroupLayout.isSelected()) {
            loginTv.setSelected(!isEmpty(userId) && !isEmpty(appSecert) &&
                    !isEmpty(channelId)
                    && !isEmpty(appId)
            );
        } else {
            loginTv.setSelected(!isEmpty(playbackVideoId)
                    && !isEmpty(playbackAppId)
                    && !isEmpty(playbackUserId)
                    && !isEmpty(playbackChannelId)
            );//
        }
    }

    private boolean isEmpty(TextView v) {
        return TextUtils.isEmpty(v.getText().toString());
    }

    private int getVideoListType() {
        return playbackVodListSwitch.isChecked() ? PolyvPlaybackListType.VOD : PolyvPlaybackListType.PLAYBACK;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="登录处理">
    private void login() {
        if (!loginTv.isSelected()) {
            return;
        }

        loginTv.setEnabled(false);
        loginTv.setSelected(false);
        progress.show();
        if (liveGroupLayout.isSelected()) {
            checkToken(getTrim(userId), getTrim(appSecert),
                    getTrim(channelId), null, getTrim(appId));
        } else {
            checkToken(getTrim(playbackUserId), null, getTrim(playbackChannelId),
                    getTrim(playbackVideoId), getTrim(playbackAppId));
        }
    }

    private String getTrim(EditText playbackUserId) {
        return playbackUserId.getText().toString().trim();
    }

    private void checkToken(final String userId, String appSecret, String channel, final String vid, final String appId) {
        //请求token接口
        getTokenDisposable = PolyvLoginManager.checkLoginToken(userId, appSecret, appId,
                channel, vid,
                new PolyvrResponseCallback<PolyvChatDomain>() {
                    @Override
                    public void onSuccess(PolyvChatDomain responseBean) {
                        if (playbackGroupLayout.isSelected()) {
                            PolyvLinkMicClient.getInstance().setAppIdSecret(appId, playbackAppSecret.getText().toString());
                            PolyvLiveSDKClient.getInstance().setAppIdSecret(appId, playbackAppSecret.getText().toString());
                            PolyvVodSDKClient.getInstance().initConfig(appId, playbackAppSecret.getText().toString());

                            requestPlayBackStatus(userId, vid);
                            return;
                        }

                        PolyvLinkMicClient.getInstance().setAppIdSecret(appId, appSecert.getText().toString());
                        PolyvLiveSDKClient.getInstance().setAppIdSecret(appId, appSecert.getText().toString());
                        PolyvVodSDKClient.getInstance().initConfig(appId, appSecert.getText().toString());

                        requestLiveStatus(userId);

                        PolyvChatDomainManager.getInstance().setChatDomain(responseBean);
                    }

                    @Override
                    public void onFailure(PolyvResponseBean<PolyvChatDomain> responseBean) {
                        super.onFailure(responseBean);
                        failedStatus(responseBean.getMessage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        errorStatus(e);
                    }
                });
    }

    private void requestPlayBackStatus(final String userId, String vid) {
        if (TextUtils.isEmpty(vid)) {
            return;
        }
        verifyDispose = PolyvLoginManager.getPlayBackType(vid, new PolyvrResponseCallback<PolyvPlayBackVO>() {
            @Override
            public void onSuccess(PolyvPlayBackVO playBack) {

                switch (playBack.getLiveType()) {
                    case 0:
                        startActivityForPlayback(userId, true);
                        break;
                    case 1:
                        startActivityForPlayback(userId, false);
                        break;
                    default:
                        ToastUtils.showShort("只支持云课堂类型频道或普通直播类型频道");
                        break;
                }
                progress.dismiss();
            }

            @Override
            public void onFailure(PolyvResponseBean<PolyvPlayBackVO> responseBean) {
                super.onFailure(responseBean);
                failedStatus(responseBean.getMessage());
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                errorStatus(e);
            }
        });
    }

    public void failedStatus(String message) {
        ToastUtils.showLong(message);
        progress.dismiss();
    }

    public void errorStatus(Throwable e) {
        PolyvCommonLog.exception(e);
        progress.dismiss();
        if (e instanceof HttpException) {
            try {
                ToastUtils.showLong(((HttpException) e).response().errorBody().string());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else {
            ToastUtils.showLong(e.getMessage());
        }
    }

    private void requestLiveStatus(final String userId) {
        verifyDispose = PolyvResponseExcutor.excuteUndefinData(PolyvApiManager.getPolyvLiveStatusApi().geLiveStatusJson(channelId.getText().toString())
                , new PolyvrResponseCallback<PolyvLiveStatusVO>() {
                    @Override
                    public void onSuccess(PolyvLiveStatusVO statusVO) {

                        PolyvLiveChannelType channelType = null;
                        try {
                            channelType = PolyvLiveChannelType.mapFromServerString(statusVO.getChannelType());
                        } catch (PolyvLiveChannelType.UnknownChannelTypeException e) {
                            progress.dismiss();
                            ToastUtils.showShort("未知的频道类型");
                            e.printStackTrace();
                            return;
                        }
                        if (channelType != PolyvLiveChannelType.CLOUD_CLASS && channelType != PolyvLiveChannelType.NORMAL) {
                            progress.dismiss();
                            ToastUtils.showShort("只支持云课堂类型频道或普通直播类型频道");
                            return;
                        }
                        final boolean isAlone = channelType == PolyvLiveChannelType.NORMAL;//是否有ppt

                        requestLiveDetail(new Consumer<String>() {
                            @Override
                            public void accept(String rtcType) throws Exception {
                                progress.dismiss();
                                if (isParticipant) {
                                    if ("urtc".equals(rtcType) || TextUtils.isEmpty(rtcType)) {
                                        ToastUtils.showShort("暂不支持该频道观看");
                                        return;
                                    }
                                }
                                if (liveGroupLayout.isSelected()) {
                                    startActivityForLive(userId, isAlone, rtcType);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(PolyvResponseBean<PolyvLiveStatusVO> responseBean) {
                        super.onFailure(responseBean);
                        failedStatus(responseBean.getMessage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        errorStatus(e);
                    }
                });
    }

    private void requestLiveDetail(final Consumer<String> onSuccess) {
        if (liveDetailDisposable != null) {
            liveDetailDisposable.dispose();
        }
        liveDetailDisposable = PolyvResponseExcutor.excuteUndefinData(PolyvChatApiRequestHelper.getInstance()
                .requestLiveClassDetailApi(channelId.getText().toString().trim()), new PolyvrResponseCallback<PolyvLiveClassDetailVO>() {
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
                errorStatus(e);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="startActivity">
    private void startActivityForLive(String userId, boolean isAlone, String rtcType) {
        if (etParticipantNickName != null) {
            String participantNickName = etParticipantNickName.getText().toString();
            String participantViewerId = etParticipantViewerId.getText().toString();
            try {
                Integer.parseInt(participantViewerId);
            } catch (NumberFormatException e) {
                ToastUtils.showShort("参与者Id格式错误");
                return;
            }
            PolyvVClassGlobalConfig.username = participantNickName;
            PolyvVClassGlobalConfig.viewerId = participantViewerId;
        }

        PolyvCloudClassHomeActivity.startActivityForLiveWithParticipant(PolyvCloudClassLoginActivity.this,
                getTrim(channelId), userId, isAlone, isParticipant, rtcType);
    }

    private void startActivityForPlayback(String userId, boolean isNormalLivePlayBack) {
        if (!isNormalLivePlayBack && getVideoListType() == PolyvPlaybackListType.VOD) {
            ToastUtils.showShort("三分屏场景暂不支持使用点播列表播放");
            return;
        }
        PolyvCloudClassHomeActivity.startActivityForPlayBack(PolyvCloudClassLoginActivity.this,
                getTrim(playbackVideoId), getTrim(playbackChannelId), getTrim(playbackUserId), isNormalLivePlayBack, getVideoListType());
    }
    // </editor-fold>

}
