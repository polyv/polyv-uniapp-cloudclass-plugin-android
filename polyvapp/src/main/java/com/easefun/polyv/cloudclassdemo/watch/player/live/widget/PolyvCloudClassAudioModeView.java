package com.easefun.polyv.cloudclassdemo.watch.player.live.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassAudioModeView;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.LogUtils;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * date: 2019/6/14 0014
 *
 * @author hwj
 * description 只听音频View
 */
public class PolyvCloudClassAudioModeView extends FrameLayout implements IPolyvCloudClassAudioModeView {
    //3.5秒加载一次
    private static final int ANIMATE_ONCE_DURATION = 3500;
    //listener
    private OnChangeVideoModeListener onChangeVideoModeListener;
    //view
    private ImageView ivAnimation;
    private TextView tvPlayVideo;
    //animation
    private AnimationDrawable animationDrawable;
    private Disposable readDrawableDisposable;

    public PolyvCloudClassAudioModeView(@NonNull Context context) {
        this(context, null);
    }

    public PolyvCloudClassAudioModeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvCloudClassAudioModeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setVisibility(INVISIBLE);

        LayoutInflater.from(context).inflate(R.layout.polyv_cloud_class_audio_mode_view, this);

        initView();
    }

    private void initView() {
        ivAnimation = findViewById(R.id.iv_animation);
        tvPlayVideo = findViewById(R.id.tv_play_video);

        tvPlayVideo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onChangeVideoModeListener != null) {
                    onChangeVideoModeListener.onClickPlayVideo();
                }
            }
        });


    }

    public void setOnChangeVideoModeListener(OnChangeVideoModeListener li) {
        onChangeVideoModeListener = li;
    }

    private void startAnimation() {
        if (animationDrawable != null) {
            return;
        }
        animationDrawable = new AnimationDrawable();
        readDrawableDisposable = Observable.just("1")
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        LogUtils.d(Thread.currentThread().getName());
                        int drawableCount = 30;
                        for (int i = 1; i <= drawableCount; i++) {
                            String drawableName = "sound" + String.valueOf(10000 + i).substring(1);
                            int drawableId = PolyvCloudClassAudioModeView.this.getResources().getIdentifier(drawableName, "drawable", PolyvCloudClassAudioModeView.this.getContext().getPackageName());
                            if (drawableId != 0) {
                                Drawable drawable = PolyvCloudClassAudioModeView.this.getResources().getDrawable(drawableId);
                                animationDrawable.addFrame(drawable, ANIMATE_ONCE_DURATION / drawableCount);
                            }
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        LogUtils.d(Thread.currentThread().getName());
                        animationDrawable.setOneShot(false);
                        ivAnimation.setImageDrawable(animationDrawable);
                        animationDrawable.start();
                    }
                });
    }

    private void releaseAnimationDrawable() {
        ivAnimation.setImageDrawable(null);
        animationDrawable = null;
        if (readDrawableDisposable != null) {
            readDrawableDisposable.dispose();
        }
    }

    //--------------IPolyvCloudClassAudioModeView----------------------

    @Override
    public void onShow() {
        setVisibility(VISIBLE);
        startAnimation();
    }

    @Override
    public void onHide() {
        setVisibility(GONE);
        if (animationDrawable != null) {
            animationDrawable.stop();
        }
        releaseAnimationDrawable();
    }


    @Override
    public View getRoot() {
        return this;
    }

    //Listener
    public interface OnChangeVideoModeListener {
        void onClickPlayVideo();
    }
}
