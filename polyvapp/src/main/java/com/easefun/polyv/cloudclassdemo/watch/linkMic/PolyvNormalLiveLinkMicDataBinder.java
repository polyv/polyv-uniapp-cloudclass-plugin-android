package com.easefun.polyv.cloudclassdemo.watch.linkMic;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.link.PolyvJoinInfoEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatGroupFragment;
import com.easefun.polyv.cloudclassdemo.watch.linkMic.widget.PolyvSmoothRoundProgressView;
import com.easefun.polyv.commonui.utils.imageloader.PolyvImageLoader;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxTimer;
import com.easefun.polyv.foundationsdk.utils.PolyvAppUtils;
import com.easefun.polyv.foundationsdk.utils.PolyvFormatUtils;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;
import com.plv.rtc.PLVARTCAudioVolumeInfo;
import com.plv.rtc.PLVARTCConstants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author df
 * @create 2018/11/20
 * @Describe 用于展示普通直播的适配器
 */
public class PolyvNormalLiveLinkMicDataBinder extends IPolyvDataBinder {
    private static final String TAG = "PolyvLinkMicDataBinder";
    private static final int CAMERA_VIEW_ID = 817;

    private List<String> uids = new ArrayList<>();
    private Map<String, PolyvJoinInfoEvent> joins = new LinkedHashMap<>();
    private String myUid, teacherId;
    private PolyvJoinInfoEvent teacher;
    private View teacherView;//老师的布局view
    private View teacherParentView;//   teacherParentView:老师外层布局view

    private View ownerView;//自己的布局view


    private boolean cameraOpen = true;

    private List<SurfaceView> cachesView = new ArrayList<>();

    private ViewGroup linkMicFrontView, frontParentView;//直播连麦时，前面两个连麦者的视图


    public PolyvNormalLiveLinkMicDataBinder(String myUid) {
        this.myUid = myUid;

    }

    @Override
    public void bindLinkMicFrontView(ViewGroup linkMicLayoutParent) {
        super.bindLinkMicFrontView(linkMicFrontView);
        this.frontParentView = (ViewGroup) linkMicLayoutParent.getParent();
        View bottomView = frontParentView.findViewById(R.id.link_mic_bottom);
        ViewGroup frontView = frontParentView.findViewById(R.id.link_mic_fixed_position);
        boolean linkMicFrontViewFirstBind = false;

        if (this.linkMicFrontView == null) {
            linkMicFrontViewFirstBind = true;
        }

        this.linkMicFrontView = frontView;

        if (linkMicFrontView != null) {
            this.linkMicFrontView.setVisibility(View.VISIBLE);//isAudio?View.VISIBLE:View.GONE
            if (linkMicFrontViewFirstBind) {
                //处理android 5.x 设备上R.id.link_mic_layout_parent在横屏时异常大，因此横屏时不再和它对齐。
                linkMicFrontView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        if (PolyvScreenUtils.isLandscape(linkMicFrontView.getContext())) {
                            //横屏
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) linkMicFrontView.getLayoutParams();
                            lp.removeRule(RelativeLayout.ALIGN_TOP);
                        } else {
                            //竖屏
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) linkMicFrontView.getLayoutParams();
                            lp.addRule(RelativeLayout.ALIGN_TOP, R.id.link_mic_layout_parent);
                        }
                    }
                });
            }
        }
//        if(bottomView != null ){
//            bottomView.setVisibility(isAudio? View.GONE :View.VISIBLE);
//        }
    }

    @Override
    public void bindItemClickListener(View.OnClickListener clickListener) {

    }

    public void addOwner(String myUid, PolyvJoinInfoEvent owern) {
        //将自己先放在第一位 老师来的顺序可能在后面 老师再放置再第一位 保证前两位一直室老师跟自己
        if (!uids.contains(myUid)) {
            uids.add(0, myUid);
        }
        if (owern == null) {
            PolyvCommonLog.e(TAG, "owern is null");
            owern = new PolyvJoinInfoEvent();
        }
        joins.put(myUid, owern);

        onBindViewHolder(onCreateViewHolder(parentView, 0, true), 0);
    }

    @NonNull
    public PolyvNormalLiveLinkMicDataBinder.PolyvMicHodler onCreateViewHolder(@NonNull ViewGroup parent, final int pos, final boolean isFront) {
        PolyvCommonLog.d(TAG, "onCreateViewHolder:" + pos);
        final ViewGroup child = (ViewGroup) View.inflate(parent.getContext(), R.layout.normal_live_link_mic_item, null);
        final PolyvNormalLiveLinkMicDataBinder.PolyvMicHodler polyvMicHodler = new PolyvNormalLiveLinkMicDataBinder.PolyvMicHodler(child);
        parentView.post(new Runnable() {
            @Override
            public void run() {

                SurfaceView surfaceView = PolyvLinkMicWrapper.getInstance().createRendererView(PolyvAppUtils.getApp());
                surfaceView.setVisibility(View.GONE);
                surfaceView.setId(CAMERA_VIEW_ID);

                if (isFront) {
                    linkMicFrontView.addView(child, pos);
                } else {
//                    frontParentView.setBackgroundColor(Color.parseColor("#D9000000"));
                    ViewGroup.LayoutParams layoutParams = createLayoutParams(pos);
                    parentView.addView(child, Math.max(0, pos - 2), layoutParams);//前排固定两个 ，所以添加的时候要从0开始必须-2
                }
                if (surfaceView != null) {
                    polyvMicHodler.normalLinkMicView.addView(surfaceView);
                }
            }
        });
        return polyvMicHodler;
    }

    private ViewGroup.LayoutParams createLayoutParams(int pos) {
        FrameLayout.LayoutParams childParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int itemWidth = PolyvScreenUtils.dip2px(parentView.getContext(), 60);
        int realPos = Math.max(0, pos - 2);
        childParams.leftMargin = realPos * itemWidth;
        return childParams;
    }

    public void onBindViewHolder(PolyvNormalLiveLinkMicDataBinder.PolyvMicHodler holder, int position) {

        String uid = uids.get(position);

        if (TextUtils.isEmpty(uid)) {
            PolyvCommonLog.e(TAG, "uid is null:" + uids.toString());
            return;
        }
        holder.itemView.setTag(uid);

        PolyvJoinInfoEvent polyvJoinRequestSEvent = joins.get(uid);
        if (polyvJoinRequestSEvent != null) {
            loadAvtar(polyvJoinRequestSEvent.getPic(), polyvJoinRequestSEvent.getUserType(), holder.cover);
            holder.polyvLinkNick.setText(polyvJoinRequestSEvent.getNick());
            if (TextUtils.isEmpty(polyvJoinRequestSEvent.getNick())) {
                holder.normalLinkMicView.setOnClickListener(null);
            }
        }

        if (uid.equals(myUid)) {
            holder.polyvLinkNick.setText("我");
            ownerView = holder.itemView;
            ownerMic = holder.cameraSwitch;

            if (!isAudio) {
                holder.normalLinkMicView.setOnClickListener(null);
                ownerView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        ownerMic.setVisibility(View.VISIBLE);
                        startShowTimer();
                        return false;
                    }
                });
                startShowTimer();
            }
        }
        SurfaceView surfaceView = holder.normalLinkMicView.findViewById(CAMERA_VIEW_ID);
        PolyvCommonLog.d(TAG, "onBindViewHolder:uid :" + uid + "  pos :" + position);

        if (teacher != null && teacher.getUserId().equals(uid)) {
            teacherParentView = holder.itemView;
            teacherView = holder.normalLinkMicView;
            holder.soundRoundView.setVisibility(View.VISIBLE);
            PolyvCommonLog.d(TAG, "cameraOpen:" + cameraOpen);
        }
        long longUid = PolyvFormatUtils.parseLong(uid);
        if (uid.equals(myUid)) {
            PolyvLinkMicWrapper.getInstance().setupLocalVideo(surfaceView,
                    PLVARTCConstants.RENDER_MODE_FIT, (int) longUid);
        } else {
            PolyvLinkMicWrapper.getInstance().setupRemoteVideo(surfaceView,
                    PLVARTCConstants.RENDER_MODE_FIT, (int) longUid);
        }

    }

    public void loadAvtar(String pic, String userType, CircleImageView circleImageView) {
        if (PolyvChatGroupFragment.isTeacherType(userType)) {
            PolyvImageLoader.getInstance()
                    .loadImageNoDiskCache(
                            parentView.getContext(),
                            pic,
                            R.drawable.polyv_default_teacher,
                            R.drawable.polyv_default_teacher,
                            circleImageView
                    );
        } else {
            PolyvImageLoader.getInstance()
                    .loadImageNoDiskCache(
                            parentView.getContext(),
                            pic,
                            R.drawable.polyv_missing_face,
                            R.drawable.polyv_missing_face,
                            circleImageView
                    );
        }
    }

    private void addTeacher(String teacherId, PolyvJoinInfoEvent teacherEvent) {
        this.teacher = teacherEvent;
        this.teacherId = teacherId;
        teacher.setUserId(teacherId);


        if (joins.containsKey(teacherId)) {
            return;
        }
        joins.put(teacherId, teacher);
    }

    public View getOwnerView() {
        return ownerView;
    }

    public PolyvJoinInfoEvent getJoinInfo(String uid) {
        return joins.get(uid);
    }

    public int getItemCount() {
        return uids.size();
    }

    public boolean notifyItemChanged(int pos, boolean mute) {
        View child = null;
        if (pos < 2) {
            child = linkMicFrontView.getChildAt(pos);
        } else {
            child = parentView.getChildAt(pos - 2);
        }
        if (child == null) {
            return false;
        }
        SurfaceView surfaceView = child.findViewById(CAMERA_VIEW_ID);

        surfaceView.setVisibility(mute ? View.INVISIBLE : View.VISIBLE);
        return true;
    }


    public int getJoinsPos(String uid) {
        PolyvJoinInfoEvent joinInfoEvent = joins.get(uid);
        if (joinInfoEvent == null) {
            return -1;
        }
        return joinInfoEvent.getPos();
    }

    @Override
    public synchronized void startAudioWave(PLVARTCAudioVolumeInfo[] speakers, int totalVolume) {
        super.startAudioWave(speakers, totalVolume);
        if (totalVolume == 0) {
            return;
        }
        for (PLVARTCAudioVolumeInfo audioVolumeInfo : speakers) {
            PolyvJoinInfoEvent joinInfoEvent = null;
            if (audioVolumeInfo.uid == 0) {
                joinInfoEvent = joins.get(myUid);
            } else {
                joinInfoEvent = joins.get(audioVolumeInfo.uid + "");
            }
            if (joinInfoEvent == null) {
                PolyvCommonLog.e(TAG, "startAudioWave error useid ：" + audioVolumeInfo.uid);
                return;
            }
            PolyvCommonLog.e(TAG, "startAudioWave uid:" + audioVolumeInfo.uid + "  progess:" + audioVolumeInfo.volume);
            int pos = joinInfoEvent.getPos();
            ViewGroup soundRound = null;
            if (joinInfoEvent.getUserId().equals(teacherId)) {//|| pos == 1
                soundRound = (ViewGroup) linkMicFrontView.getChildAt(pos);
                PolyvSmoothRoundProgressView polyvSmoothRoundProgressView = soundRound.findViewById(R.id.link_mic_sound_around);
                polyvSmoothRoundProgressView.setMaxNum(totalVolume);
                polyvSmoothRoundProgressView.setProgressNum(audioVolumeInfo.volume, 5000);
            }
//            else {
//                soundRound = (ViewGroup) parentView.getChildAt(pos-2);
//            }

        }

    }

    public class PolyvMicHodler extends RecyclerView.ViewHolder {

        public CircleImageView cover;
        public TextView polyvLinkNick;
        public PolyvSmoothRoundProgressView soundRoundView;
        public RelativeLayout normalLinkMicView;
        public ImageView cameraSwitch;

        private Disposable nickShowTimer;

        public PolyvMicHodler(View itemView) {
            super(itemView);
            normalLinkMicView = itemView.findViewById(R.id.normal_live_link_mic_container);
            cover = itemView.findViewById(R.id.link_mic_cover);
            soundRoundView = itemView.findViewById(R.id.link_mic_sound_around);
            polyvLinkNick = itemView.findViewById(R.id.link_mic_nick);
            cameraSwitch = itemView.findViewById(R.id.normal_live_camera_switch);
            cameraSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PolyvLinkMicWrapper.getInstance().switchCamera();
                }
            });
            normalLinkMicView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startNickTimer();
                }

            });

            startNickTimer();
        }

        public void startNickTimer() {
            polyvLinkNick.setVisibility(View.VISIBLE);
            if (nickShowTimer != null) {
                nickShowTimer.dispose();
                nickShowTimer = null;
            }

            nickShowTimer = PolyvRxTimer.delay(3000, new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    polyvLinkNick.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    public synchronized boolean addData(PolyvJoinInfoEvent requestSEvent, boolean updateImmidately) {
        if (requestSEvent == null || joins.containsKey(requestSEvent.getUserId()) || TextUtils.isEmpty(requestSEvent.getUserId())) {
            PolyvCommonLog.d(TAG, "contains userid  || userid is  :");
            return false;
        }
        try {

            if (!uids.contains(requestSEvent.getUserId())) {
                //老师放在第一位ol
                if ("teacher".equals(requestSEvent.getUserType())) {
                    teacherId = requestSEvent.getUserId();
                    addTeacher(teacherId, requestSEvent);
                    uids.add(0, requestSEvent.getUserId());
                } else {
                    uids.add(requestSEvent.getUserId());
                }
            }


            joins.put(requestSEvent.getUserId(), requestSEvent);

            if (updateImmidately) {
                PolyvCommonLog.e(TAG, "update updateImmidately:" + requestSEvent.getUserType());
                if ("teacher".equals(requestSEvent.getUserType())) {
                    requestSEvent.setPos(0);
//                    notifyItemInserted(0);//通知数据与界面重新绑定
                    onBindViewHolder(onCreateViewHolder(parentView, 0, true), 0);
                } else {
                    requestSEvent.setPos(uids.size() - 1);
                    onBindViewHolder(onCreateViewHolder(parentView, uids.size() - 1, false), uids.size() - 1);
//                    notifyItemInserted(uids.size() - 1);//通知数据与界面重新绑定
                }
            }

            PolyvCommonLog.e(TAG, "update :" + requestSEvent.getUserType());
            arrangeDataPos();

        } catch (Exception e) {
            PolyvCommonLog.e(TAG, e.getMessage());
        }


        return true;
    }

    // 对连麦者位置序号重新排序
    private void arrangeDataPos() {

        int length = uids.size();
        for (int i = 0; i < length; i++) {
            PolyvJoinInfoEvent infoEvent = joins.get(uids.get(i));
            infoEvent.setPos(i);
        }
    }

    public void removeData(String uid, boolean removeView) {
        if (TextUtils.isEmpty(uid)) {
            return;
        }
        uids.remove(uid);
        PolyvJoinInfoEvent infoEvent = joins.remove(uid);
        int pos = uids.size();
        if (infoEvent != null) {
            pos = infoEvent.getPos();
        }
        PolyvCommonLog.d(TAG, "remove pos :" + pos);
        if (removeView) {
            notifyItemRemoved(pos - 2);
        }

        arrangeDataPos();

        if (uids.size() == 2) {
            frontParentView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void notifyItemRemoved(int pos) {
        if (parentView != null) {

            if (parentView.getChildAt(pos) != null) {
                parentView.removeViewAt(pos);
            } else {
//                ToastUtils.showLong("notifyItemRemoved:is null"+pos);
            }
        }
    }

    public void clear() {
        clearSurfaceview();
        uids.clear();
        joins.clear();
        if (linkMicFrontView != null) {
            linkMicFrontView.removeAllViews();
        }
        teacherView = null;
        ownerView = null;
    }

    private void clearSurfaceview() {
        for (SurfaceView surfaceView : cachesView) {
            if (surfaceView.getHolder() != null && surfaceView.getHolder().getSurface() != null) {
                surfaceView.getHolder().getSurface().release();
            }
        }
        cachesView.clear();
    }
}
