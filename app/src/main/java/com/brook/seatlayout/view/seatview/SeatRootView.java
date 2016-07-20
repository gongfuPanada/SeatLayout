package com.brook.seatlayout.view.seatview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;

import com.brook.seatlayout.R;
import com.brook.seatlayout.view.seatview.seatmodel.SeatRowModel;

import java.util.HashMap;

/**
 * Created by Brook on 16/7/15.
 * Description:
 */
public class SeatRootView extends View {
    protected Bitmap mOptionalBitmap;
    protected Bitmap mSelectedBitmap;
    protected float mSeatMinWidth;
    protected float mSeatMinWidthTemp;
    protected float mSeatMinHeightTemp;
    protected float mSeatMinHeight;
    protected float mSeatSpace; // 座位之间的间隔
    protected float mWthScale; // 宽高比
    protected SeatRowModel mSeatRowModel;
    protected Paint mTextPaint;
    protected Paint mCenterScreenPaint; // 屏幕中央画笔
    protected float mCenterScreenTextSize;
    protected String mCenterStr = "屏幕中央";
    protected float mCenterPaddingTop = 18;
    protected float mCenterPaddingLeft = 80;
    protected float mScreenToSeatSpace = 20;
    protected float mLastSeatSpace = 30; // 移动到最后的位置时，座位与屏幕的间隙
    protected float mSeatTextSize;
    protected float mSeatTextSizeTemp;
    protected Rect mSeatStrRect;
    protected Rect mCenterStrRect;
    protected RectF mSeatRectF;
    protected RectF mCenterRectF;

    protected float mSeatWidth; // 座位的总宽度
    protected float mSeatHeight; // 座位的总高度
    protected float mSeatWidthTemp;
    protected float mSeatHeightTemp;
    protected int mTouchSlop;
    protected boolean mCanScrollY; // Y轴是否可以滑动
    protected int mCanScrollHeight; // Y轴可以滑动的距离
    protected int mLastMoveYTemp;
    protected boolean mCanScrollX; // X轴是否可以滑动
    protected int mCanScrollWidth; // X轴可以滑动的距离
    protected int mLastMoveXTemp;
    protected int mCanMoveXTemp;
    protected int mCanMoveYTemp;

    // 缩放相关的数据
    protected float mPointX;
    protected float mPointX1;
    protected float mPointY;
    protected float mPointY1;
    protected float mScale = 1.0f;
    protected float mLastScale;
    protected float mScaleTemp;
    protected boolean mMeasure;
    protected boolean mCanMove = true;
    protected HashMap<Integer, HashMap<Integer, Integer>> mSelectedSeats; // 已选的座位

    public SeatRootView(Context context) {
        this(context, null);
    }

    public SeatRootView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeatRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mOptionalBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.seat_optional);
        mSelectedBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.seat_selected);
        mSeatMinWidth = mOptionalBitmap.getWidth();
        mSeatMinHeight = mOptionalBitmap.getHeight();
        mSeatSpace = mSeatMinWidth / 4;
        mWthScale = mSeatMinWidth / mSeatMinHeight;

        mSeatTextSize = 24;
        mSeatTextSizeTemp = mSeatTextSize;
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mSeatTextSize);
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setStyle(Paint.Style.FILL);

        mCenterScreenTextSize = 35;
        mCenterScreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterScreenPaint.setColor(0xffc1bcbc);
        mCenterScreenPaint.setStyle(Paint.Style.FILL);
        mCenterScreenPaint.setTextSize(mCenterScreenTextSize);

        mSeatRectF = new RectF();
        mSeatStrRect = new Rect();
        mCenterStrRect = new Rect();
        mCenterRectF = new RectF();

        mSelectedSeats = new HashMap<>();
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mLastScale = mScale;
        mScaleTemp = mLastScale * mScale;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void setData(SeatRowModel mSeatRowModel) {
        this.mSeatRowModel = mSeatRowModel;
        postInvalidate();
    }
}


