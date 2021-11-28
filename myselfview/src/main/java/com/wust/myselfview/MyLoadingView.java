package com.wust.myselfview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * ClassName: MyLoadingView <br/>
 * Description: <br/>
 * date: 2021/11/26 20:49<br/>
 *
 * @author yiqi<br />
 * @QQ 1820762465
 * @微信 yiqiideallife
 * @技术交流QQ群 928023749
 */
public class MyLoadingView extends View {

    private Paint mPaint;
    private Bitmap mLogoBp;
    private Bitmap mWhiteBp;
    private int mViewWidth;
    private int mViewHeight;
    private float curYPos;
    private ValueAnimator mVa;
    private int mDuration;
    private int mBgColor;
    private int mStartDirection;
    private int mRepeateCount;
    private int mRepeateMode;

    private enum starDirection {
        BOTTOM,
        TOP,
        COVER
    }

    public MyLoadingView(Context context) {
        this(context, null);
    }

    public MyLoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initPaint();
        initBitmap();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ty = context.obtainStyledAttributes(attrs, R.styleable.MyLoadingView);

        mDuration = ty.getInteger(R.styleable.MyLoadingView_yqDuration, 2000);
        BitmapDrawable imgDw = (BitmapDrawable) ty.getDrawable(R.styleable.MyLoadingView_yqImage);
        mLogoBp = imgDw.getBitmap();
        mBgColor = ty.getColor(R.styleable.MyLoadingView_yqBg, Color.WHITE);
        // 0表示bottom 1表示top
        mStartDirection = ty.getInteger(R.styleable.MyLoadingView_yqStartDirection, 0);
        // -1为默认值 表示无限循环
        mRepeateCount = ty.getInteger(R.styleable.MyLoadingView_yqRepeateCount, -1);
        // 0表示reverse 1表示restart
        mRepeateMode = ty.getInteger(R.styleable.MyLoadingView_yqRepeateMode, 2);

        ty.recycle();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
    }

    private void initBitmap() {
        mWhiteBp = createBpByColor(Color.WHITE);
    }

    private Bitmap createBpByColor(int color) {
        Bitmap b = Bitmap.createBitmap(mLogoBp.getWidth(), mLogoBp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawColor(color);
        return b;
    }

    private int px2dp(int px) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    private int dp2px(int dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        //注意 这里宽高已经变成了像素
        mViewWidth = mViewHeight = Math.min(mViewWidth, mViewHeight);
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //设置背景颜色
        canvas.drawColor(mBgColor);
        //1、将 canvas 中心点移动到View中心 方便绘制
        canvas.translate(mViewWidth / 2, mViewHeight / 2);
        //2、获取图片的高宽比 目的是为了第三步绘制图片的时候不失真
        float scaleRate = mLogoBp.getHeight() * 1.0f / mLogoBp.getWidth();
        //3、绘制图片 Dst
        //3.1、src 是针对图片取值
        Rect src = new Rect(0, 0, mLogoBp.getWidth(), mLogoBp.getHeight());
        //3.2、dst 是针对View（canvas）取值
        RectF dst = new RectF(-px2dp(mViewWidth) / 2, -scaleRate * px2dp(mViewWidth) / 2, px2dp(mViewWidth) / 2, scaleRate * px2dp(mViewWidth) / 2);
        //注意点： 这个千万不能少 如果少了这一句话 你会发现颜色全部是乱套的 这一句话就是新建一个图层
        int sc = canvas.saveLayer(dst, mPaint);
        canvas.drawBitmap(mLogoBp, src, dst, mPaint);
        //4、设置模式
        PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        mPaint.setXfermode(xfermode);
        //5、绘制白色图片 Src
        Rect srcWhite = new Rect(0, 0, mWhiteBp.getWidth(), mWhiteBp.getHeight());
        RectF dstWhite = null;
        if (mStartDirection == starDirection.BOTTOM.ordinal()) {
            if (curYPos == 0.0f) curYPos = dst.bottom;
            dstWhite = new RectF(-px2dp(mViewWidth) / 2, curYPos, px2dp(mViewWidth) / 2, scaleRate * px2dp(mViewWidth) / 2);
        } else if (mStartDirection == starDirection.TOP.ordinal()) {
            if (curYPos == 0.0f) curYPos = dst.top;
            dstWhite = new RectF(-px2dp(mViewWidth) / 2, -scaleRate * px2dp(mViewWidth) / 2, px2dp(mViewWidth) / 2, curYPos);
        } else if (mStartDirection == starDirection.COVER.ordinal()) {
            if (curYPos == 0.0f) curYPos = dst.top;
            dstWhite = new RectF(-px2dp(mViewWidth) / 2, curYPos, px2dp(mViewWidth) / 2, scaleRate * px2dp(mViewWidth) / 2);
        }
        canvas.drawBitmap(mWhiteBp, srcWhite, dstWhite, mPaint);
        //6、消除模式
        mPaint.setXfermode(null);
        //7、恢复图层
        canvas.restoreToCount(sc);
        if (mVa == null) {
            if (mStartDirection == starDirection.BOTTOM.ordinal()) {
                mVa = ValueAnimator.ofFloat(dst.bottom, dst.top);
            } else if (mStartDirection == starDirection.TOP.ordinal() || mStartDirection == starDirection.COVER.ordinal()) {
                mVa = ValueAnimator.ofFloat(dst.top, dst.bottom);
            }
            mVa.setDuration(mDuration);
            mVa.setRepeatCount(mRepeateCount);
            mVa.setRepeatMode(mRepeateMode);
            mVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float value = (float) valueAnimator.getAnimatedValue();
                    curYPos = value;
                    invalidate();
                }
            });
            mVa.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mVa != null) {
            mVa.cancel();
            mVa = null;
        }
    }
}
