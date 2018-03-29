package layerview.lyd.com.scrolllayerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by luyd on 2018/3/22.
 */

public class ShareRecyclerView extends RecyclerView {

    private ShareBottomLayout mShareLayout;

    public ShareRecyclerView(Context context) {
        super(context);
    }

    public ShareRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ShareRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setShareLayout(ShareBottomLayout layout) {
        this.mShareLayout = layout;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private float downY = -1, pointY = -1;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = e.getRawY();
                pointY = e.getRawY();
                mShareLayout.setPointYFromChild(pointY);
                mShareLayout.down(e);
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointY == -1) {
                    pointY = e.getRawY();
                    downY = pointY;
                    mShareLayout.setPointYFromChild(e.getRawY());
                    return super.onTouchEvent(e);
//                    if (mShareLayout != null) {
//                        return mShareLayout.isTop() ? true : false;
//                    }
                }
                boolean touch;
                if (mShareLayout != null) {
                    touch = mShareLayout.isEnableTouch(e.getRawY() < pointY);
                    if (touch) {
                        mShareLayout.move(e);
                        mShareLayout.setPointYFromChild(pointY);
                    } else {
                        mShareLayout.setPointYFromChild(e.getRawY());
                        super.onTouchEvent(e);
                    }
                }
                pointY = e.getRawY();
                return true;
            case MotionEvent.ACTION_UP:
                //同步点击后up的z坐标数据
                if (mShareLayout != null) {
                    mShareLayout.up(e);
                }
                pointY = -1;
                downY = -1;
                return false;
            case MotionEvent.ACTION_CANCEL:
                pointY = -1;
                downY = -1;
                break;
        }
        return super.onTouchEvent(e);
    }

    public boolean onTouchUp(MotionEvent e) {
        return super.onTouchEvent(e);
    }

    public boolean onTouchCancel(MotionEvent e) {
        e.setAction(MotionEvent.ACTION_CANCEL);
        return super.onTouchEvent(e);
    }
}
