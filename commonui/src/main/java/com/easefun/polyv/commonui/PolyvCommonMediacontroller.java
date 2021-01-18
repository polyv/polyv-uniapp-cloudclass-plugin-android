package com.easefun.polyv.commonui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.api.common.meidaControl.IPolyvMediaController;
import com.easefun.polyv.businesssdk.api.common.player.microplayer.PolyvCommonVideoView;
import com.easefun.polyv.businesssdk.model.video.PolyvBitrateVO;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveLinesVO;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;

import java.util.List;

/**
 * @author df
 * @create 2018/8/16
 * @Describe 公共的控制类
 */
public abstract class PolyvCommonMediacontroller<T extends PolyvCommonVideoView> extends FrameLayout
        implements IPolyvMediaController<T>, View.OnClickListener {

    //控制栏显示的时间
    protected static final int SHOW_TIME = 5000;
    private static final String TAG = "PolyvCommonMediacontoller";
    private static final String landTag = "land";
    private static final String portraitTag = "portrait";
    protected View rootView, parentView;
    protected TextView bitrateChange;
    protected TextView bitrateChangeLand;
    protected boolean showPPTSubView = true;//ppt显示在副屏
    protected RelativeLayout videoControllerPort;
    protected RelativeLayout videoControllerLand;
    protected Activity context;
    protected T polyvVideoView;
    protected volatile int currentBitratePos;
    protected PolyvBitrateVO polyvLiveBitrateVO;
    //更多
    protected ImageView ivMorePortrait;
    protected ImageView ivMoreLand;
    protected boolean joinLinkMic;
    private ViewGroup contentView, fullVideoViewParent;
    private ViewGroup.LayoutParams portraitLP;//(需要移动的整个播放器布局)在竖屏下的LayoutParams
    private ViewGroup fullVideoView;//需要移动的整个播放器布局
    private Runnable hideTask = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };


    public PolyvCommonMediacontroller(@NonNull Context context) {
        this(context, null);
    }

    public PolyvCommonMediacontroller(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvCommonMediacontroller(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialView();
    }

    protected abstract void initialView();


    /**
     * 切换主副平
     */
    public abstract void changePPTVideoLocation();

    /**
     * @return ppt是否显示在副屏
     */
    public boolean isShowPPTSubView() {
        return showPPTSubView;
    }

    @Override
    public T getMediaPlayer() {
        return polyvVideoView;
    }

    @Override
    public void setMediaPlayer(T player) {

        polyvVideoView = player;
    }

    @Override
    public void changeToLandscape() {
        if (PolyvScreenUtils.isLandscape(context)) {
            return;
        }
//        context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        PolyvScreenUtils.setLandscape(context);
        hide();
    }

    public void setLandscapeController() {
        post(new Runnable() {
            @Override
            public void run() {

//        // 通过移除整个播放器布局到窗口的上层布局中，并改变整个播放器布局的大小，实现全屏的播放器。
//        if (fullVideoView == null) {
//            fullVideoView = ((ViewGroup) polyvVideoView.getParent().getParent());
//            fullVideoViewParent = (ViewGroup) fullVideoView.getParent();
//            contentView = (ViewGroup) context.findViewById(Window.ID_ANDROID_CONTENT);
//        }
//        if (!landTag.equals(fullVideoView.getTag())) {
//            fullVideoView.setTag(landTag);
//            portraitLP = fullVideoView.getLayoutParams();
//            fullVideoViewParent.removeView(fullVideoView);
//            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//            fullVideoView.setLayoutParams(flp);
//            contentView.addView(fullVideoView);
//        }

                ViewGroup.LayoutParams vlp = parentView.getLayoutParams();
                vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                vlp.height = ViewGroup.LayoutParams.MATCH_PARENT;

                videoControllerPort.setVisibility(View.GONE);
                videoControllerLand.setVisibility(View.VISIBLE);
            }
        });


    }

    @Override
    public void changeToPortrait() {
        if (PolyvScreenUtils.isPortrait(context)) {
            return;
        }
//        context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        PolyvScreenUtils.setPortrait(context);
        hide();

    }

    @Override
    public void initialConfig(ViewGroup view) {
        parentView = view;
        changeToPortrait();
    }

    private void setPortraitController() {

        post(new Runnable() {
            @Override
            public void run() {

                ViewGroup.LayoutParams vlp = parentView.getLayoutParams();
                vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                vlp.height = PolyvScreenUtils.getHeight();

                videoControllerLand.setVisibility(View.GONE);
                videoControllerPort.setVisibility(View.VISIBLE);
            }
        });

    }


    @Override
    public void hide() {
        setVisibility(View.GONE);
    }


    @Override
    public boolean isShowing() {
        return isShown();
    }


    @Override
    public void show() {
        show(SHOW_TIME);
    }

    @Override
    public void show(int timeout) {
        setVisibility(VISIBLE);
        if (getHandler() != null) {
            getHandler().removeCallbacks(hideTask);
        }
        postDelayed(hideTask, timeout);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        PolyvCommonLog.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            PolyvScreenUtils.hideStatusBar(context);
            setLandscapeController();
        } else {
            PolyvScreenUtils.showStatusBar(context);
            setPortraitController();
        }
    }


    protected void goneWithAnimation(View view) {
        view.setVisibility(View.GONE);
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.polyv_ltor_right));
    }

    protected void visibleWithAnimation(View view) {
        view.setVisibility(View.VISIBLE);
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.polyv_rtol_right));
    }


    @Override
    public void onClick(View view) {
    }


    @Override
    public void initialBitrate(PolyvBitrateVO bitrateVO) {

    }

    @Override
    public void initialLines(List<PolyvLiveLinesVO> linesVO) {

    }

    public abstract void updatePPTShowStatus(boolean showPPT);
}
