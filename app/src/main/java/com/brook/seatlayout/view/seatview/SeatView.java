package com.brook.seatlayout.view.seatview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.brook.seatlayout.view.seatview.seatmodel.SeatRow;

import java.util.HashMap;

/**
 * Created by Brook on 16/7/15.
 * Description:
 */
public class SeatView extends SeatRootView {

    private static final String TAG = SeatView.class.getSimpleName();

    private int mThisWidth; // 控件的宽度
    private int mThisHeight; // 控件的高度

    private int mLastX, mLastY;
    private int moveX, moveY, mMoveX, mMoveY;

    private GestureDetector mGestureDetector;

    public SeatView(Context context) {
        this(context, null);
    }

    public SeatView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetector(new OnSingleTapListener());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mThisWidth = w;
        mThisHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!mMeasure) {
            int seatWidth = (mThisWidth - getPaddingLeft() - getPaddingRight()) / mSeatRowModel.maxColumnCount;
            int seatHeight = (mThisHeight - getPaddingTop() - getPaddingBottom()) / mSeatRowModel.maxRowCount;
            if (seatWidth < seatHeight) {
                mSeatMinWidth = Math.min(mSeatMinWidth, seatWidth) * mScale;
                mSeatMinHeight = mSeatMinWidth / mWthScale;
            } else {
                mSeatMinHeight = Math.min(mSeatMinHeight, seatHeight) * mScale;
                mSeatMinWidth = mSeatMinHeight * mWthScale;
            }

            mSeatMinWidthTemp = mSeatMinWidth;
            mSeatMinHeightTemp = mSeatMinHeight;
            mMeasure = true;
        }

        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());

        // 计算中间文字的高度
        mCenterScreenPaint.getTextBounds(mCenterStr, 0, mCenterStr.length(), mCenterStrRect);

        float top, left, bottom = 0, right = 0;
        float width = 0;
        for (int row = 0; row < mSeatRowModel.seatRows.size(); row++) {
            SeatRow seatRow = mSeatRowModel.seatRows.get(row);
            if (seatRow.rowNum != -1) { // 当前排是正确的时候才绘制
                // 绘制每行，高度为=中间屏幕上下Padding的高度+屏幕中央文字的高度+屏幕到座位的距离+座位和座位间隙的高度
                top = mScreenToSeatSpace + mCenterStrRect.height() + mCenterPaddingTop * 2 + (mSeatMinHeight + mSeatSpace) * row; // 座位的顶部Y
                bottom = mScreenToSeatSpace + mCenterStrRect.height() + mCenterPaddingTop * 2 + mSeatMinHeight * (row + 1) + mSeatSpace * row; // 座位的底部Y

                // 绘制每列
                String[] columns = seatRow.columnIds.split("\\|");
                width = Math.max(width, mSeatMinWidth * columns.length + mSeatSpace * (columns.length - 1));
                for (int column = 0; column < columns.length; column++) {
                    if (!"ZL".equals(columns[column])) { // 不是走道才绘制座位
                        left = (mSeatMinWidth + mSeatSpace) * column; // + (int) ((mMoveX - mMoveXTemp) * (1 - mLastScale * mScale / mScaleTemp));// + (int) (mMoveXTemp * (1 - mLastScale * mScale) * 0.5f); // 座位的左侧X
                        right = mSeatMinWidth * (column + 1) + mSeatSpace * column;// + (int) ((mMoveX - mMoveXTemp) * (1 - mLastScale * mScale / mScaleTemp));// + (int) (mMoveXTemp * (1 - mLastScale * mScale) * 0.5f); // 座位的右侧X
                        mSeatRectF.set(left, top, right, bottom);
                        canvas.drawBitmap(mOptionalBitmap, null, mSeatRectF, null);

                        // 绘制选择的座位
                        if (mSelectedSeats.containsKey(row) && mSelectedSeats.get(row).containsKey(column)) {
                            canvas.drawBitmap(mSelectedBitmap, null, mSeatRectF, null);
                        }

                        // 绘制座位编号
                        String seatStr = String.valueOf(columns[column]);
                        if (seatStr.startsWith("0")) {
                            seatStr = seatStr.substring(1, seatStr.length());
                        }
                        mTextPaint.setTextSize(mSeatTextSize);
                        mTextPaint.getTextBounds(seatStr, 0, seatStr.length(), mSeatStrRect);
                        canvas.drawText(seatStr, mSeatRectF.centerX() - mSeatStrRect.width() / 2.0f - 1f, mSeatRectF.centerY() + mSeatStrRect.height() / 2.0f - 4, mTextPaint);
                    }
                }
            }

            mSeatWidth = right;
            mSeatHeight = bottom;
            if (mLastScale == 1.0f) {
                mSeatWidthTemp = mSeatMinWidth;
                mSeatHeightTemp = mSeatMinHeight;
            }
        }
        mCanScrollY = mSeatHeight + getPaddingTop() - mThisHeight + mLastSeatSpace > 0; // 多出一个座位的距离
        mCanScrollHeight = (int) (mSeatHeight + getPaddingTop() - mThisHeight + mLastSeatSpace);
        if (mCanScrollHeight < 0) {
            mCanScrollHeight = 0;
        }

        mCanScrollX = mSeatWidth + getPaddingLeft() - mThisWidth + mLastSeatSpace > 0; // 多出一个座位的距离
        mCanScrollWidth = (int) (mSeatWidth + getPaddingLeft() - mThisWidth + mLastSeatSpace);
        if (mCanScrollWidth < 0) {
            mCanScrollWidth = 0;
        }
        if (mLastScale == 1.0f) {
            mCanMoveXTemp = mCanScrollWidth;
            mCanMoveYTemp = mCanScrollHeight;
        }

        // 绘制屏幕中央，使之居于座位的中间
        mCenterScreenPaint.setColor(0xffc1bcbc);
        mCenterRectF.set(width / 2.0f - mCenterStrRect.width() / 2.0f - mCenterPaddingLeft,
                0, width / 2.0f + mCenterStrRect.width() / 2.0f + mCenterPaddingLeft, mCenterStrRect.height() + mCenterPaddingTop * 2);
        canvas.drawRoundRect(mCenterRectF, 10, 10, mCenterScreenPaint);

        // 绘制屏幕中央文字
        mCenterScreenPaint.setColor(0xff000000);
        canvas.drawText(mCenterStr, width / 2.0f - mCenterStrRect.width() / 2.0f,
                (mCenterStrRect.height() + mCenterPaddingTop * 2) / 2.0f + mCenterStrRect.height() / 2.0f, mCenterScreenPaint);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mGestureDetector.onTouchEvent(event);
        if (event.getPointerCount() == 2) {
            // 当两个手指按下的时候
            switch (event.getAction()) {
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_POINTER_2_DOWN:
                    mPointX = event.getX();
                    mPointX1 = event.getX(1);
                    mPointY = event.getY();
                    mPointY1 = event.getY(1);
                    mCanMove = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    float x_ = event.getX();
                    float x_1 = event.getX(1);
                    float y_ = event.getY();
                    float y_1 = event.getY(1);
                    double xy = Math.sqrt((mPointX - mPointX1) * (mPointX - mPointX1) + (mPointY - mPointY1) * (mPointY - mPointY1));
                    double xy1 = Math.sqrt((x_ - x_1) * (x_ - x_1) + (y_ - y_1) * (y_ - y_1));
                    if (Math.abs(xy1 - xy) / 2 > 20) {
                        mScale = (float) ((xy + (xy1 - xy) / 2) / xy);
                        if (mLastScale * mScale > 1.5f) {
                            mScale = 1.0f;
                            mLastScale = 1.5f;
                            mSeatMinWidth = mSeatMinWidthTemp * mLastScale;
                            mSeatMinHeight = mSeatMinHeightTemp * mLastScale;
                            mSeatTextSize = mSeatTextSizeTemp * mLastScale;
                            mScaleTemp = 1.5f;
                        } else if (mLastScale * mScale <= 1.0f) {
                            mScale = 1.0f;
                            mLastScale = 1.0f;
                            mSeatMinWidth = mSeatMinWidthTemp;
                            mSeatMinHeight = mSeatMinHeightTemp;
                            mSeatTextSize = mSeatTextSizeTemp;
                            mScaleTemp = 1.0f;
                            mMoveX = mCanMoveXTemp;
                        } else {
                            mSeatMinWidth = mSeatMinWidthTemp * mLastScale * mScale;
                            mSeatMinHeight = mSeatMinHeightTemp * mLastScale * mScale;
                            mSeatTextSize = mSeatTextSizeTemp * mLastScale * mScale;
                            mSeatWidthTemp = mSeatMinWidthTemp * mScaleTemp;
                            // 判断四个方向上面是否可以移动，可以移动时，向相反方向缩放，不可以移动时，向这个方向缩放
                            if (mCanScrollWidth <= mMoveX && mCanScrollX) { // 右边不可移动
//                                mMoveX = mMoveX - (mCanMoveXTemp - mCanScrollWidth);
                                mMoveX = mCanScrollWidth;
                                if (mCanScrollWidth - mCanMoveXTemp <= 0) {
                                    scrollBy(mCanScrollWidth - mCanMoveXTemp, 0);
                                }
//                                if (mMoveX >= mCanScrollWidth) {
//                                    mMoveX = mCanScrollWidth;
//                                } else if (mMoveX <= 0) {
//                                    mMoveX = 0;
//                                }
                                mLastMoveXTemp = mMoveX;
                                Log.e(TAG, "==mCanScrollWidth==" + mMoveX);
                            }

                            if (mMoveX <= 0 && mCanScrollX) { // 左侧不可移动
                                mMoveX = 0;
                            }

                            if (mCanScrollHeight <= mMoveY && mCanScrollY) { // 底部不可移动
//                                mMoveY = mMoveY - (mCanMoveYTemp - mCanScrollHeight);
                                mMoveY = mCanScrollHeight;
                                if (mCanScrollHeight - mCanMoveYTemp <= 0) {
                                    scrollBy(0, mCanScrollHeight - mCanMoveYTemp);
                                }
                                mLastMoveYTemp = mMoveY;
                            }

                            if (mMoveY <= 0 && mCanScrollY) { // 顶部不可移动
                                mMoveY = 0;
                            }

                            invalidate();
                            mScaleTemp = mLastScale * mScale;

                            mCanMove = false;
                            mCanMoveXTemp = mCanScrollWidth;
                            mCanMoveYTemp = mCanScrollHeight;
                        }
                    }
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    mLastScale = mSeatMinWidth / mSeatMinWidthTemp;
                    mLastX = (int) event.getX(1);
                    mLastY = (int) event.getY(1);
                    mCanMove = true;
                    break;
                case MotionEvent.ACTION_POINTER_2_UP:
                    mLastScale = mSeatMinWidth / mSeatMinWidthTemp;
                    mLastX = (int) event.getX();
                    mLastY = (int) event.getY();
                    mCanMove = true;
                    break;
            }
        } else if (event.getPointerCount() == 1) {
            // 当是一个手指按下的时候
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastX = (int) event.getX();
                    mLastY = (int) event.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mCanMove) {
                        moveX = (int) (mLastX - event.getX());
                        moveY = (int) (mLastY - event.getY());
                        mMoveX = mMoveX + moveX;
                        mMoveY = mMoveY + moveY;
                        // 判断Y轴是否可以滑动
                        if (mCanScrollY) {
                            if (mMoveY > mCanScrollHeight) { // 当滑动的距离>可以滑动的距离时，最多可以滑动值 = 可以滑动的距离
                                mMoveY = mCanScrollHeight;
                                // 防止滑动的值突然变化较大时(即上一次的mMoveY < mCanScrollHeight，然后突然超过这个值)，使content滑动到最大处
                                moveY = mCanScrollHeight - mLastMoveYTemp;
                            } else if (mMoveY < 0) { // 当滑动的距离<0，最多可以滑动值 = 0;即滑动到顶部之后不可以向下滑动
                                mMoveY = 0;
                                // 防止滑动的值突然变化较大时(即上一次的mMoveY >= 0，然后突然小于0)，使content滑动到0处
                                moveY = -mLastMoveYTemp;
                            }

                        } else if (mLastMoveYTemp > 0) {
                            mMoveY = 0;
                            moveY = -mLastMoveYTemp;
                        } else {
                            mMoveY = 0;
                            moveY = 0;
                        }

                        Log.e(TAG, "==ACTION_MOVE==" + mMoveX);
                        Log.e(TAG, "==mCanScrollWidth==" + mCanScrollWidth);
                        // 处理方式与Y轴方向一致
                        if (mCanScrollX) {
                            if (mMoveX >= mCanScrollWidth) {
                                mMoveX = mCanScrollWidth;
                                moveX = mCanScrollWidth - mLastMoveXTemp;
                            } else if (mMoveX < 0) {
                                mMoveX = 0;
                                moveX = -mLastMoveXTemp;
                            } else if (mMoveX == 0) {
                                mMoveX = 0;
                                moveX = 0;
                            }
                        } else if (mLastMoveXTemp > 0) {
                            mMoveX = 0;
                            moveX = -mLastMoveXTemp;
                        } else {
                            mMoveX = 0;
                            moveX = 0;
                        }

                        Log.e(TAG, "==ACTION_MOVE .......==" + mMoveX);
                        scrollBy(moveX, moveY);

                        // 记录上一次的滑动距离
                        mLastMoveYTemp = mMoveY;
                        mLastMoveXTemp = mMoveX;
                        Log.e(TAG, "==mMoveX==" + mMoveX);

                        mLastX = (int) event.getX();
                        mLastY = (int) event.getY();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
        }

        return true;
    }

    public class OnSingleTapListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            float height = y + mMoveY - getPaddingTop() - mCenterRectF.height() - mScreenToSeatSpace; // 在座位范围内
            float width = x + mMoveX - getPaddingLeft(); // 在座位范围内
            if (height > 0 && width > 0) {

                int row = (int) ((height) / (mSeatMinHeight + mSeatSpace));
                int column = (int) ((width) / (mSeatMinWidth + mSeatSpace));

                if (((row + 1) * mSeatMinHeight + row * mSeatSpace + getPaddingTop() + mCenterRectF.height() + mScreenToSeatSpace) >= (y + mMoveY) &&
                        ((column + 1) * mSeatMinWidth + column * mSeatSpace + getPaddingLeft()) >= (x + mMoveX)) {

                    if (mSelectedSeats.containsKey(row)) {
                        if (mSelectedSeats.get(row).containsKey(column)) {
                            mSelectedSeats.get(row).remove(column);
                        } else {
                            HashMap<Integer, Integer> map = mSelectedSeats.get(row);
                            map.put(column, 1);
                        }
                    } else {
                        HashMap<Integer, Integer> map = new HashMap<>();
                        map.put(column, 1);
                        mSelectedSeats.put(row, map);
                    }
                    invalidate();
                }
            }
            return super.onSingleTapUp(e);
        }
    }
}