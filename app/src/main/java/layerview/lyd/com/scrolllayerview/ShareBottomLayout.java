package layerview.lyd.com.scrolllayerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.List;

/**
 * Created by luyd on 2018/3/20.
 */

public class ShareBottomLayout extends FrameLayout{
    private int maxHeight = 800;
    private int initHeight;
    private int mCurrentHeight;
    private FrameLayout content;
    private int screenH;
    private ShareRecyclerView mRecyclerView;
    private OnItemClickListener itemClickListener;

    public ShareBottomLayout(@NonNull Context context) {
        this(context, null);
    }

    public ShareBottomLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShareBottomLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.layout_share_bottom, this);
        content = findViewById(R.id.content);
        mRecyclerView = findViewById(R.id.rv);
        init();
    }

    private List<ResolveInfo> getSystemApps() {
        PackageManager pm = getContext().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_SEND, null);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("text/plain");

        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            Log.d("appInfo", info.activityInfo.packageName + ",activity=" + info.activityInfo.name);
        }
        return list;
    }


    public void setOnItemClick(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    private void init() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenH = getScreenHeight(getContext());
        initHeight = dm.heightPixels * 2 / 5;
        maxHeight = dm.heightPixels * 3 / 4;

        ShareAdapter adapter = new ShareAdapter(getContext(), getSystemApps());
//        adapter.setOnItemClickListener(this);
        mRecyclerView.setShareLayout(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int screenHeight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics dm = new DisplayMetrics();
            display.getRealMetrics(dm);
            screenHeight = dm.heightPixels;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            try {
                screenHeight = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception e) {
                DisplayMetrics dm = new DisplayMetrics();
                display.getMetrics(dm);
                screenHeight = dm.heightPixels;
            }
        }
        return screenHeight;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public void setInitHeight(int initHeight) {
        this.initHeight = initHeight;
    }

    public void show() {
        LayoutParams params = (LayoutParams) content.getLayoutParams();
        params.height = initHeight;
        content.setLayoutParams(params);
        mCurrentHeight = initHeight;

        setVisibility(View.VISIBLE);
        ObjectAnimator ob = ObjectAnimator.ofFloat(this, "translationY", screenH, 0);
        ob.setDuration(500);
        ob.start();
    }

    private float downX, downY, pointX, pointY;
    private boolean moved;
    private VelocityTracker vTracker = null;

    public boolean isEnableTouch(boolean up) {
        if (up) {
            LayoutParams params = (LayoutParams) content.getLayoutParams();
            if (params.height < maxHeight) {
                return true;
            }
        } else {
            if (!mRecyclerView.canScrollVertically(-1)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTop() {
        LayoutParams params = (LayoutParams) content.getLayoutParams();
        if (params.height >= maxHeight) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (vTracker == null) {
                    vTracker = VelocityTracker.obtain();
                } else {
                    vTracker.clear();
                }
                vTracker.addMovement(event);
                pointX = downX = event.getX();
                pointY = downY = event.getY();
                moved = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (vTracker == null) {
                    vTracker = VelocityTracker.obtain();
                }
                vTracker.addMovement(event);
                vTracker.computeCurrentVelocity(1000);
                if (pointY == -1) {
                    pointY = event.getY();
                    return false;
                }
                if (!isPointEnable(event.getX(), event.getY()) && !moved) {
                    pointY = event.getY();
                    return false;
                }
                moved = true;
                if (event.getY() < pointY) {
                    LayoutParams params = (LayoutParams) content.getLayoutParams();
                    if (params.height >= maxHeight) {
                        pointY = event.getY();
                        if (mRecyclerView.canScrollVertically(1)) {
                            return true;
//                            return mRecyclerView.onTouchEvent(event);
                        }
                    } else {
                        int height = (int) (params.height + pointY - event.getY());
                        setLayoutHeight(height);
                        pointY = event.getY();
                        return true;
                    }
                } else {
                    if (mRecyclerView.canScrollVertically(-1)) {
                        pointY = event.getY();
                        mRecyclerView.onTouchEvent(event);
                        return true;
                    }
                    LayoutParams params = (LayoutParams) content.getLayoutParams();
                    int height = (int) (params.height + pointY - event.getY());
                    setLayoutHeight(height);
                    pointY = event.getY();
                    return true;
                }
            case MotionEvent.ACTION_UP:
                if (!moved && !isPointEnable(downX, downY) && !isPointEnable(pointX, pointY)) {
                    dismiss();
                    return true;
                }

                LayoutParams params = (LayoutParams) content.getLayoutParams();
                if (params.height < initHeight) {
                    dismiss();
                } else if (vTracker != null && vTracker.getYVelocity() < -1000) {
                    scrollToInit(params.height, maxHeight);
                } else if (params.height > initHeight && params.height < maxHeight) {
                    scrollToInit(params.height, initHeight);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void setLayoutHeight(int height) {
        LayoutParams params = (LayoutParams) content.getLayoutParams();
        if (height >= maxHeight) {
            height = maxHeight;
        }
        params.height = height;
        content.setLayoutParams(params);
        mCurrentHeight = height;
        requestLayout();
    }

    private void scrollToInit(final int preHeight, final int targetHeight) {
        final int dT = targetHeight - preHeight;
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(300);
        animator.start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                LayoutParams params = (LayoutParams) content.getLayoutParams();
                params.height = (int) (preHeight + value * dT);
                content.setLayoutParams(params);
            }
        });
    }

    public void dismiss() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        ObjectAnimator ob = ObjectAnimator.ofFloat(this, "translationY", 0, dm.heightPixels);
        ob.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
            }
        });
        ob.start();
    }

    public boolean isPointEnable(float x, float y) {
        if (getHeight() - mCurrentHeight < y && y <= content.getBottom()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private float pointFromChild = -1;

    public void setPointYFromChild(float y) {
        this.pointFromChild = y;
    }

    public boolean down(MotionEvent event){
        if (vTracker == null) {
            vTracker = VelocityTracker.obtain();
        } else {
            vTracker.clear();
        }
        vTracker.addMovement(event);
        return true;
    }

    public boolean move(MotionEvent event) {
        if (vTracker == null) {
            vTracker = VelocityTracker.obtain();
        } else {
            vTracker.clear();
        }
        vTracker.addMovement(event);
        vTracker.computeCurrentVelocity(1000);

        if (pointFromChild == -1) {
            pointFromChild = event.getRawY();
            return false;
        }
        moved = true;
        if (event.getRawY() <= pointFromChild) {
            LayoutParams params = (LayoutParams) content.getLayoutParams();
            if (params.height >= maxHeight) {
                pointFromChild = event.getRawY();
                return false;
            } else {
                int height = (int) (params.height + pointFromChild - event.getRawY());
                if (height >= maxHeight) {
                    height = maxHeight;
                }
                params.height = height;
                content.setLayoutParams(params);
                mCurrentHeight = height;
                requestLayout();
                pointFromChild = event.getRawY();
                return true;
            }
        } else {
            if (mRecyclerView.canScrollVertically(-1)) {
                pointFromChild = event.getRawY();
                return false;
            }
            LayoutParams params = (LayoutParams) content.getLayoutParams();
            int height = (int) (params.height + pointFromChild - event.getRawY());
            if (height < 0) {
                height = 0;
            }
            params.height = height;
            content.setLayoutParams(params);
            mCurrentHeight = height;
            requestLayout();
            pointFromChild = event.getRawY();
            return true;
        }
    }

    public boolean up(MotionEvent event) {
        LayoutParams params = (LayoutParams) content.getLayoutParams();
        if (params.height < initHeight) {
            dismiss();
        } else if (vTracker != null && vTracker.getYVelocity() < -1000) {
            scrollToInit(params.height, maxHeight);
            mRecyclerView.onTouchUp(event);
        } else if (params.height > initHeight && params.height < maxHeight) {
            scrollToInit(params.height, initHeight);
            mRecyclerView.onTouchCancel(event);
        }
        mRecyclerView.onTouchCancel(event);
        return true;
    }

    public interface OnItemClickListener {
        void onClick(ResolveInfo info);
    }
}
