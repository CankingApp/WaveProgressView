package com.canking.waveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import net.canking.waveview.R;

/**
 * 水波圆形view
 */
public class WaveProgressView extends View {
    private static final int MSG_WAVE = 10;
    /**
     * 默认内环半径100dp
     */
    private int mRadius = dpToPx(100);

    /**
     * 外环的宽度
     */
    private int mStrokeWidth = dpToPx(20);

    private Paint mPaintOutCircle;
    private Paint mPaintBorderCircle;
    private Paint mPaintInnerCircle;
    private Paint mPaintText;
    private Paint mPaintTextTips;
    private Paint mPaintWater;

    /**
     * 当前进度，或者百分比
     */
    private int progress = 30;

    /**
     * 中心文字的范围大小
     */
    private Rect mRectProgress = new Rect();

    /**
     * 提示文字的范围大小
     */
    private Rect mRectTips = new Rect();

    private String mTipString;
    private int mBorderSize;

    /**
     * View中心点的坐标
     */
    private int mCenterXY;

    /**
     * 记录所有的波纹的点的位置
     */
    private Path mPath = new Path();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mOffsetX = mOffsetX + 3;
            if (mOffsetX > 10000) {
                mOffsetX = 0;
            }
            invalidate();
        }
    };

    /**
     * 每次重新绘制的时候，X轴的偏移量
     */
    private float mOffsetX = 0;

    /**
     * 波浪的高度
     */
    private double mWaveHeight = 5;

    private boolean isWaving = true;

    public WaveProgressView(Context context) {
        this(context, null);
    }

    public WaveProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context cxt, AttributeSet attrs) {
        TypedArray ta = cxt.obtainStyledAttributes(attrs, R.styleable.WaveViewStyle);
        int waveColor = ta.getColor(R.styleable.WaveViewStyle_waveColor, 0xFF45A348);
        int innerBgColor = ta.getColor(R.styleable.WaveViewStyle_innerBgColor, 0x9045A348);
        int outBgColor = ta.getColor(R.styleable.WaveViewStyle_innerBgColor, 0xFF4CAF50);
        int mainTextColor = ta.getColor(R.styleable.WaveViewStyle_mainTextSize, 0xFFFFFFFF);
        int mainTextSize = ta.getDimensionPixelOffset(R.styleable.WaveViewStyle_mainTextSize, spToPx(40));
        mTipString = ta.getString(R.styleable.WaveViewStyle_topInfoTextValue);
        ta.recycle();

        if (TextUtils.isEmpty(mTipString)) mTipString = cxt.getString(R.string.tip_info_value);
        mBorderSize = dpToPx(1);

        mPaintOutCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintOutCircle.setDither(true);
        mPaintOutCircle.setColor(outBgColor);
        mPaintOutCircle.setStyle(Paint.Style.STROKE);
        mPaintOutCircle.setStrokeWidth(mStrokeWidth);

        mPaintBorderCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBorderCircle.setDither(true);
        mPaintBorderCircle.setColor(0xF0ffffff);
        mPaintBorderCircle.setStyle(Paint.Style.STROKE);
        mPaintBorderCircle.setStrokeWidth(mBorderSize);

        mPaintInnerCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintInnerCircle.setDither(true);
        mPaintInnerCircle.setColor(innerBgColor);
        mPaintInnerCircle.setStyle(Paint.Style.FILL);

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText.setDither(true);
        mPaintText.setColor(mainTextColor);
        mPaintText.setTextSize(mainTextSize);

        mPaintTextTips = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTextTips.setDither(true);
        mPaintTextTips.setColor(mainTextColor);
        mPaintTextTips.setTextSize(spToPx(15));
        mPaintTextTips.getTextBounds(mTipString, 0, mTipString.length(), mRectTips);

        mPaintWater = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintWater.setDither(true);
        mPaintWater.setColor(waveColor);
        mPaintWater.setStyle(Paint.Style.FILL);
    }

    /**
     * 布局最好是一个正方形，绘制的区域将在屏幕的中心位置
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width, height;

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = mRadius * 2 + mStrokeWidth * 2;
        }

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = mRadius * 2 + mStrokeWidth * 2;
        }

        int size = Math.min(width, height);
        setMeasuredDimension(size, size);

        mCenterXY = size / 2;

        if (mCenterXY <= mRadius + mStrokeWidth) {
            mRadius = mCenterXY - mStrokeWidth;
        }
    }

    public void setWaveColor(int color) {
        mPaintWater.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //        super.onDraw(canvas);

        //        //绘制内圆
        canvas.drawCircle(mCenterXY, mCenterXY, mRadius, mPaintInnerCircle);

        //绘制波浪
        createWavePath(progress);
        canvas.drawPath(mPath, mPaintWater);

        //绘制中间的边界的元环
        canvas.drawCircle(mCenterXY, mCenterXY, mRadius + mStrokeWidth / 2, mPaintOutCircle);

        //绘制中间的边界的元环
        canvas.drawCircle(mCenterXY, mCenterXY, mRadius + dpToPx(2) - mBorderSize, mPaintBorderCircle);

        //绘制中心的文字
        String text = progress + "%";
        mPaintText.getTextBounds(text, 0, text.length(), mRectProgress);
        canvas.drawText(text, (getWidth() - mRectProgress.width()) / 2,
                (getHeight() + mRectProgress.height()) / 2, mPaintText);

        //绘制tips的文字
        canvas.drawText(mTipString, (getWidth() - mRectTips.width()) / 2,
                (getHeight() / 3 + mRectProgress.height()) / 2, mPaintTextTips);
        if (isWaving) {
            mHandler.sendEmptyMessageDelayed(MSG_WAVE, 10);
        }
    }

    /**
     * 创建波浪，主要是通过path来创建
     */
    private void createWavePath(int progress) {
        mPath.reset();
        float absY;
        float sweepAngle;
        float startAngle;
        float absX;
        if (progress >= 50) {
            absY = mRadius * 2 * (progress * 1.0f / 100);
            float angle = (float) (Math.asin((absY - mRadius) * 1.0f / mRadius) * 180 / Math.PI);
            absX = (float) (mRadius * Math.cos(angle * Math.PI / 180));
            sweepAngle = angle * 2 + 180;
            startAngle = -angle;
        } else {
            absY = mRadius * 2 * (progress * 1.0f / 100);
            float angle = (float) (Math.acos((mRadius - absY) * 1.0f / mRadius) * 180 / Math.PI);
            absX = (float) (mRadius * Math.sin(angle * Math.PI / 180));
            sweepAngle = angle * 2;
            startAngle = 90 - angle;
        }

        int startX = (int) (mRadius - absX) + mStrokeWidth;

        float x, y;
        for (int i = 0; i < absX * 2; i++) {
            x = i + startX;
            y = (float) (mWaveHeight * Math.sin((i * 1.5f + mOffsetX) / mRadius * Math.PI)) + (mRadius * 2
                    - absY) + mStrokeWidth;
            if (i == 0) {
                mPath.moveTo(x, y);
            } else {
                mPath.quadTo(x, y, x + 1, y);
            }
        }
        RectF rectF = new RectF(mStrokeWidth, mStrokeWidth, mRadius * 2 + mStrokeWidth,
                mRadius * 2 + mStrokeWidth);
        mPath.arcTo(rectF, startAngle, sweepAngle);
        mPath.close();
    }

    /**
     * 设置进度
     */
    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    public void setTipInfoText(String str) {
        mTipString = str;
    }

    /**
     * 开始动画
     */
    public void startWave() {
        if (!isWaving && mHandler != null) {
            mHandler.sendEmptyMessage(MSG_WAVE);
        }
        isWaving = true;
    }

    /**
     * 关闭动画
     */
    public void stopWave() {
        if (mHandler != null) {
            mHandler.removeMessages(MSG_WAVE);
        }
        isWaving = false;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private int spToPx(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }
}
