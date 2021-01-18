package com.easefun.polyv.commonui.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.easefun.polyv.foundationsdk.permission.PolyvPermissionListener;
import com.easefun.polyv.foundationsdk.permission.PolyvPermissionManager;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class PolyvBaseActivity extends AppCompatActivity implements PolyvPermissionListener {
    private final static int APP_STATUS_KILLED = 0; // 表示应用是被杀死后在启动的
    private final static int APP_STATUS_RUNNING = 1; // 表示应用时正常的启动流程
    private static int APP_STATUS = APP_STATUS_KILLED; // 记录App的启动状态
    private final int myRequestCode = 13333;
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    protected CompositeDisposable disposables;
    protected PolyvPermissionManager permissionManager;
    protected boolean isCreateSuccess;
    // </editor-fold>

    public static void showKickTips(final Activity activity, String... message) {
        new AlertDialog.Builder(activity)
                .setTitle("温馨提示")
                .setMessage(message != null && message.length > 0 ? message[0] : "您未被授权观看本直播！")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    public static boolean showReloginTip(final Activity activity, String channelId, String... message) {
        new AlertDialog.Builder(activity)
                .setTitle("温馨提示")
                .setMessage(message != null && message.length > 0 ? message[0] : "您未被授权观看本直播！")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                })
                .setCancelable(false)
                .show();
        return true;
    }

    // <editor-fold defaultstate="collapsed" desc="处理聊天室用户被踢的相关方法">
    public boolean isInitialize() {
        return isCreateSuccess;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="处理异常启动时的相关方法">
    private String getLaunchActivityName() {
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(getPackageName());
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(resolveIntent, 0);
        if (resolveInfos != null)
            for (ResolveInfo resolveInfo : resolveInfos) {
                return resolveInfo.activityInfo.name;
            }
        return null;
    }

    private int getTaskActivityCount() {
        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        if (am == null)
            return -1;
        try {
            // get the info from the currently running task
            List<ActivityManager.RunningTaskInfo> taskInfos = am.getRunningTasks(1);
            if (taskInfos != null)
                for (ActivityManager.RunningTaskInfo taskInfo : taskInfos) {
                    return taskInfo.numActivities;
                }
        } catch (Exception e) {
        }
        return -1;
    }

    public boolean restartApp() {
        try {
            Intent intent = new Intent(this, Class.forName(getLaunchActivityName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } catch (Exception e) {
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Activity方法">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.putParcelable("android:support:fragments", null);
            savedInstanceState.putParcelable("android:fragments", null);
        }
        super.onCreate(savedInstanceState);
        isCreateSuccess = false;
        boolean launchActivityItBaseActivity = false;
        try {
            launchActivityItBaseActivity = getLaunchActivityName() != null && PolyvBaseActivity.class.isAssignableFrom(Class.forName(getLaunchActivityName()));//父/等
        } catch (Exception e) {
        }
        if (!launchActivityItBaseActivity || (getClass().getName().equals(getLaunchActivityName()) && getTaskActivityCount() < 2)) {
            APP_STATUS = APP_STATUS_RUNNING;
        }
        if (APP_STATUS == APP_STATUS_KILLED && restartApp()) { // 非正常启动流程，直接重新初始化应用界面
            return;
        }
        disposables = new CompositeDisposable();
        permissionManager = PolyvPermissionManager.with(this)
                .addRequestCode(myRequestCode)
                .setPermissionsListener(this);
        isCreateSuccess = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == myRequestCode && resultCode == Activity.RESULT_CANCELED)
            permissionManager.request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case myRequestCode:
                permissionManager.onPermissionResult(permissions, grantResults);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposables != null) {
            disposables.dispose();
            disposables = null;
        }
        if (permissionManager != null) {
            permissionManager.destroy();
            permissionManager = null;
        }
    }

    //新增的findViewById()方法，用于兼容support 25
    @SuppressWarnings("unchecked")
    public <T extends View> T findView(@IdRes int id) {
        return (T) super.findViewById(id);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PolyvPermissionListener实现">
    @Override
    public void onGranted() {
    }

    @Override
    public void onDenied(String[] permissions) {
        permissionManager.showDeniedDialog(this, permissions);
    }

    @Override
    public void onShowRationale(String[] permissions) {
        permissionManager.showRationaleDialog(this, permissions);
    }
    // </editor-fold>
}
