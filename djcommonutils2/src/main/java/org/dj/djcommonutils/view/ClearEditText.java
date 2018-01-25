package org.dj.djcommonutils.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.dj.djcommonutils.R;

/**
 * 作者：DuJianBo on 2016/10/17 15:32
 * 邮箱：jianbo_du@foxmail.com
 */
public class ClearEditText extends AppCompatEditText implements View.OnFocusChangeListener{

    public ClearEditText(Context context) {
        this(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    private Drawable mClearDrawable;// 一键删除的按钮

    @SuppressLint("InlinedApi")
    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initClearDrawable(context);
    }

    @SuppressLint("NewApi")
    private void initClearDrawable(Context context) {
        mClearDrawable = getCompoundDrawables()[2];// 获取EditText的DrawableRight,假如没有设置我们就使用默认的图片
        if (mClearDrawable == null) {
            mClearDrawable = ContextCompat.getDrawable(context, R.drawable.delete);
        }
        mClearDrawable.setBounds(0, 0, (int) getTextSize(), (int) getTextSize());//设置Drawable的宽高和TextSize的大小一致

        setClearIconVisible(false);
        // 设置焦点改变的监听
        setOnFocusChangeListener(this);
    }

    /**
     * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     *
     * @param visible
     */
    public void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mClearDrawable != null && event.getAction() == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            // 判断触摸点是否在水平范围内
            boolean isInnerWidth = (x > (getWidth() - getTotalPaddingRight()))
                    && (x < (getWidth() - getPaddingRight()));
            // 获取删除图标的边界，返回一个Rect对象
            Rect rect = mClearDrawable.getBounds();
            // 获取删除图标的高度
            int height = rect.height();
            int y = (int) event.getY();
            // 计算图标底部到控件底部的距离
            int distance = (getHeight() - height) / 2;
            // 判断触摸点是否在竖直范围内(可能会有点误差)
            // 触摸点的纵坐标在distance到（distance+图标自身的高度）之内，则视为点中删除图标
            boolean isInnerHeight = (y > distance) && (y < (distance + height));
            if (isInnerHeight && isInnerWidth) {
                this.setText("");
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

}
