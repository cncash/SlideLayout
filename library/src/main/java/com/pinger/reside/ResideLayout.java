package com.pinger.reside;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;



/**
 * user：  Pinger
 * date：  2017/1/30 14:20
 * desc：  仿酷狗音乐的侧滑菜单，可以拖拽的视图控件
 *
 * ================== API ===================
 *
 *
 *
 *
 *
 * ================= 使用教程 =================
 */
public class ResideLayout extends FrameLayout{

    private static final String TAG = "DragLayout";

    /**
     * 视图拖拽辅助类
     */
    private ViewDragHelper mDragHelper;
    private int mHeight;                    // 控件高度
    private int mWidth;                     // 控件宽度
    private int mSlideRange;                     // 水平方向拖拽的范围
    private ViewGroup mMenuContainer;       // 菜单面板
    private ViewGroup mMainContainer;       // 主面板


    /** 状态集合 */
    public enum Status{
        CLOSE, OPEN, DRAGING
    }

    private Status currentStatus = Status.CLOSE;
    public Status getCurrentStatus() {
        return currentStatus;
    }
    public void setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
    }

    /** 拖拽监听 */
    private PanelSlideListener mPanelSlideListener;
    public interface PanelSlideListener {
        void onPanelOpened();
        void onPanelClosed();
        void onPanelSlide(float percent);
    }

    public PanelSlideListener getPanelSlideListener() {
        return mPanelSlideListener;
    }
    public void setPanelSlideListener(PanelSlideListener panelSlideListener) {
        this.mPanelSlideListener = panelSlideListener;
    }

    public ResideLayout(Context context) {
        this(context, null);
    }

    public ResideLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResideLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /**
         * @param forParent 要进行触摸滑动的父控件
         * @param sensitivity 敏感度, 值越大越敏感,创建ViewDragHelper
         * @param cb 对View的事件发生改变的回调
         */
        mDragHelper = ViewDragHelper.create(this, 0.5f, mCallback);
    }


    ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;  // 被按下的child是否可以被拖拽
        }
        @Override
        public int getViewHorizontalDragRange(View child) {/*返回view水平方向的拖拽距离. > 0 . 决定了松手时动画执行时长, 水平方向是否可以滑动*/
            return mSlideRange;
        }
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if(child == mMainContainer){
                left = fixLeft(left);  // 拖拽的是主面板, 限定拖拽范围
            }
            return left;  // child将要移动到的位置
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if(changedView == mMenuContainer){  // 如果移动的是左面板, 将左面板的变化量转交给主面板, 自己不动
                mMenuContainer.layout(0, 0, mWidth, mHeight);
                int newLeft = mMainContainer.getLeft() + dx;  // 转交变化量dx给主面板

                // 修正左边值
                newLeft = fixLeft(newLeft);
                mMainContainer.layout(newLeft, 0, newLeft + mWidth, mHeight);
            }

            dispatchResideEvent();
            invalidate();
        }
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if(xvel == 0 && mMainContainer.getLeft() > mSlideRange * 0.5f){
                open();
            } else if (xvel > 0) {
                open();
            } else {
                close();
            }

        }
        @Override
        public void onViewDragStateChanged(int state) {/*拖拽状态更新的时候调用*/
            super.onViewDragStateChanged(state);
        }

    };

    /** 修正范围 */
    private int fixLeft(int left) {
        if(left < 0){  // 限定左边界
            return 0;
        }else if (left > mSlideRange) {   // 限定右边界
            return mSlideRange;
        }
        return left;
    }

    /** 伴随动画, 更新状态, 执行回调 */
    protected void dispatchResideEvent() {
        float percent = mMainContainer.getLeft() * 1.0f / mSlideRange;     // 移动百分比

        animViews(percent);  // 对View进行动画处理
        if(mPanelSlideListener != null){
            mPanelSlideListener.onPanelSlide(percent);
        }
        // 更新状态
        Status lastStatus = currentStatus;
        currentStatus = updateStatus(percent);

        // 执行监听回调, 状态变化的时候
        if(lastStatus != currentStatus && mPanelSlideListener != null){
            if(currentStatus == Status.OPEN){
                mPanelSlideListener.onPanelOpened();
            }else if (currentStatus == Status.CLOSE) {
                mPanelSlideListener.onPanelClosed();
            }
        }
    }

    private Status updateStatus(float percent) {
        if(percent == 0){
            return Status.CLOSE;
        }else if (percent == 1.0f) {
            return Status.OPEN;
        }
        return Status.DRAGING;
    }

    /** 计算拖放时的位置变化，使用动画 */
    private void animViews(float percent) {

        // scaleX = 0.5f + 1 * （1.0f - 0.5f）
        // 0.8f --> view的开始位置  ， 1.0f  -- > View的结束位置
        Log.d(TAG, "evaluate: --> " +  evaluate(percent, 0.8f, 1.0f));

        ViewHelper.setScaleX(mMenuContainer, evaluate(percent, 0.8f, 1.0f));
        ViewHelper.setScaleY(mMenuContainer, evaluate(percent, 0.8f, 1.0f));
        ViewHelper.setScaleX(mMainContainer, evaluate(percent, 1.0f, 0.7f));
        ViewHelper.setScaleY(mMainContainer, evaluate(percent, 1.0f, 0.7f));

        ViewHelper.setTranslationX(mMenuContainer, evaluate(percent, - mWidth / 2.0f, 0));
        ViewHelper.setAlpha(mMenuContainer, evaluate(percent, 0.2f, 1.0f));

        // 拖拽时的背景颜色变化
        if (getBackground() == null) return;
        getBackground().setColorFilter((Integer)evaluateColor(percent, Color.TRANSPARENT, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    /** 估值器 */
    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    /** 估算中间颜色 */
    public Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;
        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;
        return ((startA + (int)(fraction * (endA - startA))) << 24) |
                ((startR + (int)(fraction * (endR - startR))) << 16) |
                ((startG + (int)(fraction * (endG - startG))) << 8) |
                ((startB + (int)(fraction * (endB - startB))));
    }


    protected void close() {
        close(true);
    }

    public void close(boolean isSmooth){
        int finalLeft = 0;
        if(isSmooth){
            // 触发一个平滑动画Scroller
            if(mDragHelper.smoothSlideViewTo(mMainContainer, finalLeft, 0)){
                ViewCompat.postInvalidateOnAnimation(this);    // 触发界面重绘
            }
        }else {
            mMainContainer.layout(finalLeft, 0, finalLeft + mWidth, mHeight);
        }
    }

    protected void open() {
        open(true);
    }

    public void open(boolean isSmooth){
        int finalLeft = mSlideRange;
        if(isSmooth){
            if(mDragHelper.smoothSlideViewTo(mMainContainer, finalLeft, 0)){
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }else {
            mMainContainer.layout(finalLeft, 0, finalLeft + mWidth, mHeight);
        }
    }

    /** 维持动画的继续 */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mDragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    /** 转交拦截判断, 触摸事件 */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            mDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();
        mSlideRange = (int) (mWidth * 0.6f);  // 拖动的范围为屏幕宽度的60%
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(getChildCount() < 2){
            throw new IllegalStateException("Your ViewGroup must contains 2 children at least.");
        }
        if(!((getChildAt(0) instanceof ViewGroup) && (getChildAt(1) instanceof ViewGroup))){
            throw new IllegalArgumentException("Your child must be an instance of ViewGroup.");
        }
        mMenuContainer = (ViewGroup) getChildAt(0);
        mMainContainer = (ViewGroup) getChildAt(1);
    }
}