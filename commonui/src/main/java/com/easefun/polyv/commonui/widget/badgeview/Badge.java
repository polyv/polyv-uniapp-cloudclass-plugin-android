package com.easefun.polyv.commonui.widget.badgeview;

import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * @author chqiu
 * Email:qstumn@163.com
 */

public interface Badge {

    int getBadgeNumber();

    Badge setBadgeNumber(int badgeNum);

    String getBadgeText();

    Badge setBadgeText(String badgeText);

    boolean isExactMode();

    Badge setExactMode(boolean isExact);

    boolean isShowShadow();

    Badge setShowShadow(boolean showShadow);

    Badge stroke(int color, float width, boolean isDpValue);

    int getBadgeBackgroundColor();

    Badge setBadgeBackgroundColor(int color);

    Badge setBadgeBackground(Drawable drawable, boolean clip);

    Drawable getBadgeBackground();

    Badge setBadgeBackground(Drawable drawable);

    int getBadgeTextColor();

    Badge setBadgeTextColor(int color);

    Badge setBadgeTextSize(float size, boolean isSpValue);

    float getBadgeTextSize(boolean isSpValue);

    Badge setBadgePadding(float padding, boolean isDpValue);

    float getBadgePadding(boolean isDpValue);

    boolean isDraggable();

    int getBadgeGravity();

    Badge setBadgeGravity(int gravity);

    Badge setGravityOffset(float offset, boolean isDpValue);

    Badge setGravityOffset(float offsetX, float offsetY, boolean isDpValue);

    float getGravityOffsetX(boolean isDpValue);

    float getGravityOffsetY(boolean isDpValue);

    Badge setOnDragStateChangedListener(OnDragStateChangedListener l);

    PointF getDragCenter();

    Badge bindTarget(View view);

    View getTargetView();

    void hide(boolean animate);

    interface OnDragStateChangedListener {
        int STATE_START = 1;
        int STATE_DRAGGING = 2;
        int STATE_DRAGGING_OUT_OF_RANGE = 3;
        int STATE_CANCELED = 4;
        int STATE_SUCCEED = 5;

        void onDragStateChanged(int dragState, Badge badge, View targetView);
    }
}
