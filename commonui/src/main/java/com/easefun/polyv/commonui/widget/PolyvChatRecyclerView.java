package com.easefun.polyv.commonui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class PolyvChatRecyclerView extends RecyclerView {
    private static final int FLAG_SCROLL = 1;
    //未读item数
    private int unreadCount;
    private TextView unreadView;
    private boolean lastScrollVertically_One;
    private boolean heightZero;
    private boolean hasScrollEvent;
    private OnUnreadCountChangeListener unreadCountChangeListener;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == FLAG_SCROLL) {
                if (getAdapter() != null) {
                    scrollToPosition(getAdapter().getItemCount() - 1);
                }
            }
        }
    };

    public PolyvChatRecyclerView(Context context) {
        this(context, null);
    }

    public PolyvChatRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvChatRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initRecyclerView(RecyclerView recyclerView, Context context) {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLinearLayoutManager);

        closeDefaultAnimator(recyclerView);
    }

    private void closeDefaultAnimator(RecyclerView recyclerView) {
//        recyclerView.getItemAnimator().setAddDuration(0);
        recyclerView.getItemAnimator().setChangeDuration(0);
        recyclerView.getItemAnimator().setMoveDuration(0);
        recyclerView.getItemAnimator().setRemoveDuration(0);
        if (recyclerView.getItemAnimator() instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeMessages(FLAG_SCROLL);
    }

    public void setUnreadView(final TextView unreadView) {
        this.unreadView = unreadView;
        if (unreadView == null)
            return;
        unreadView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideUnredaView();
                if (getAdapter() != null) {
                    if ((getAdapter().getItemCount() - 1) - ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition() <= 10)
                        smoothScrollToPosition(getAdapter().getItemCount() - 1);
                    else
                        scrollToPosition(getAdapter().getItemCount() - 1);
                }
            }
        });
    }

    public void hideUnredaView() {
        if (unreadView != null) {
            unreadView.setVisibility(View.GONE);
        }
        int tempUnreadCount = unreadCount;
        unreadCount = 0;
        if (tempUnreadCount != 0)
            callOnUnreadChange(unreadCount);
    }

    public void addOnScrollListener() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (computeVerticalScrollExtent() > 0) {
                    lastScrollVertically_One = canScrollVertically(1);
                    if (unreadCount >= 2 && getAdapter() != null) {
                        int temp_unreadCount = getAdapter().getItemCount() - 1 - ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
                        if (temp_unreadCount < unreadCount) {
                            unreadCount = temp_unreadCount;
                            if (unreadView != null) {
                                unreadView.setText("有" + unreadCount + "条新信息，点击查看");
                            }
                            callOnUnreadChange(unreadCount);
                        }
                    }
                    if (!lastScrollVertically_One) {
                        hideUnredaView();
                    }
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (t == 0 && b == 0) {
            heightZero = true;
        } else {
            heightZero = false;
            if (hasScrollEvent) {
                hasScrollEvent = false;
                handler.sendEmptyMessage(FLAG_SCROLL);
            }
        }
    }

    public void setOnUnreadCountChangeListener(OnUnreadCountChangeListener listener) {
        unreadCountChangeListener = listener;
    }

    private void callOnUnreadChange(int currentUnreadCount) {
        if (unreadCountChangeListener != null) {
            unreadCountChangeListener.onChange(currentUnreadCount);
        }
    }

    @SuppressLint("SetTextI18n")
    private void changeUnreadViewWithCount(int count) {
        unreadCount += count;
        callOnUnreadChange(unreadCount);
        if (unreadView != null) {
            unreadView.setVisibility(View.VISIBLE);
            unreadView.setText("有" + unreadCount + "条新信息，点击查看");
        }
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    @Override
    public void smoothScrollToPosition(int position) {
        if (heightZero) {
            hasScrollEvent = true;
        } else {
            if (position != RecyclerView.NO_POSITION) {
                super.smoothScrollToPosition(position);
            }
        }
    }

    @Override
    public void scrollToPosition(int position) {
        if (heightZero) {
            hasScrollEvent = true;
        } else {
            super.scrollToPosition(position);
        }
    }

    //boolean isShowMore
    public boolean scrollToBottomOrShowMore(int count) {
        if (heightZero) {
            if (!lastScrollVertically_One) {
                hasScrollEvent = true;
            } else {
                changeUnreadViewWithCount(count);
                return true;
            }
        } else if (!lastScrollVertically_One) {
            if (getAdapter() != null)
                super.scrollToPosition(getAdapter().getItemCount() - 1);
        } else if (getHeight() - getPaddingBottom() - getPaddingTop() < computeVerticalScrollRange()) {//排除item数为0的情况
            changeUnreadViewWithCount(count);
            return true;
        }
        return false;
    }

    public interface OnUnreadCountChangeListener {
        void onChange(int currentUnreadCount);
    }
}
