package org.dj.djcommonutils.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.github.promeg.pinyinhelper.Pinyin;

import org.dj.djcommonutils.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 作者：DuJianBo on 2017/6/14 15:40
 * 邮箱：jianbo_du@foxmail.com
 */

public class IndexBar extends View {

    //#在最后面（默认的数据源）
    public static String[] INDEX_STRING = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};
    //索引数据源
    private List<String> mIndexDatas = Arrays.asList(INDEX_STRING);


    //View的宽高
    private int mWidth, mHeight;
    //每个index区域的高度
    private int mGapHeight;

    private Paint mPaint;

    //手指按下时的背景色
    private int mPressedBackground;

    //以下是帮助类
    //汉语->拼音，拼音->tag
    private IndexBarDataHelperImpl mDataHelper;

    //以下边变量是外部set进来的
    private TextView mPressedShowTextView;//用于特写显示正在被触摸的index值
    private List<? extends BaseIndexPinyinBean> mSourceDatas;//Adapter的数据源
    private ListView mListView;

    public IndexBar(Context context) {
        this(context, null);
    }

    public IndexBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndexBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        int textSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics());//默认的TextSize
        mPressedBackground = Color.BLACK;//默认按下是纯黑色
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IndexBar, defStyleAttr, 0);
        int n = typedArray.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = typedArray.getIndex(i);
            //modify 2016 09 07 :如果引用成AndroidLib 资源都不是常量，无法使用switch case
            if (attr == R.styleable.IndexBar_indexBarTextSize) {
                textSize = typedArray.getDimensionPixelSize(attr, textSize);
            } else if (attr == R.styleable.IndexBar_indexBarPressBackground) {
                mPressedBackground = typedArray.getColor(attr, mPressedBackground);
            }
        }
        typedArray.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(textSize);
        mPaint.setColor(getResources().getColor(R.color.blue_0079ff));

        //设置index触摸监听器
        setmOnIndexPressedListener(new onIndexPressedListener() {
            @Override
            public void onIndexPressed(int index, String text) {
                if (mPressedShowTextView != null) { //显示hintTexView
                    mPressedShowTextView.setVisibility(View.VISIBLE);
                    mPressedShowTextView.setText(text);
                }
                //滑动Rv
                if (mListView != null) {
                    int position = getPosByTag(text);
                    if (position != -1) {
                        mListView.smoothScrollToPositionFromTop(position,0);
                    }
                }
            }

            @Override
            public void onMotionEventEnd() {
                //隐藏hintTextView
                if (mPressedShowTextView != null) {
                    mPressedShowTextView.setVisibility(View.GONE);
                }
            }
        });

        mDataHelper = new IndexBarDataHelperImpl();
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //取出宽高的MeasureSpec  Mode 和Size
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int measureWidth = 0, measureHeight = 0;//最终测量出来的宽高

        //得到合适宽度：
        Rect indexBounds = new Rect();//存放每个绘制的index的Rect区域
        String index;//每个要绘制的index内容
        for (int i = 0; i < mIndexDatas.size(); i++) {
            index = mIndexDatas.get(i);
            mPaint.getTextBounds(index, 0, index.length(), indexBounds);//测量计算文字所在矩形，可以得到宽高
            measureWidth = Math.max(indexBounds.width(), measureWidth);//循环结束后，得到index的最大宽度
            measureHeight = Math.max(indexBounds.height(), measureHeight);//循环结束后，得到index的最大高度，然后*size
        }

        measureHeight *= mIndexDatas.size();
        switch (wMode) {
            case MeasureSpec.EXACTLY:
                measureWidth = wSize;
                break;
            case MeasureSpec.AT_MOST:
                measureWidth = Math.min(measureWidth, wSize);//wSize此时是父控件能给子View分配的最大空间
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }

        //得到合适的高度：
        switch (hMode) {
            case MeasureSpec.EXACTLY:
                measureHeight = hSize;
                break;
            case MeasureSpec.AT_MOST:
                measureHeight = Math.min(measureHeight, hSize);//wSize此时是父控件能给子View分配的最大空间
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }

        setMeasuredDimension(measureWidth, measureHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int t = getPaddingTop();//top的基准点(支持padding)
        String index;//每个要绘制的index内容
        int textSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics());//默认的TextSize

        mPaint.setTextSize(Math.min(getMeasuredHeight() / mIndexDatas.size() - 2, textSize));

        for (int i = 0; i < mIndexDatas.size(); i++) {
            index = mIndexDatas.get(i);
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();//获得画笔的FontMetrics，用来计算baseLine。因为drawText的y坐标，代表的是绘制的文字的baseLine的位置
            int baseline = (int) ((mGapHeight - fontMetrics.bottom - fontMetrics.top) / 2);//计算出在每格index区域，竖直居中的baseLine值
            canvas.drawText(index, mWidth / 2 - mPaint.measureText(index) / 2, t + mGapHeight * i + baseline, mPaint);//调用drawText，居中显示绘制index
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setBackgroundColor(mPressedBackground);//手指按下时背景变色
                //注意这里没有break，因为down时，也要计算落点 回调监听器
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                //通过计算判断落点在哪个区域：
                int pressI = (int) ((y - getPaddingTop()) / mGapHeight);
                //边界处理（在手指move时，有可能已经移出边界，防止越界）
                if (pressI < 0) {
                    pressI = 0;
                } else if (pressI >= mIndexDatas.size()) {
                    pressI = mIndexDatas.size() - 1;
                }
                //回调监听器
                if (null != mOnIndexPressedListener && pressI > -1 && pressI < mIndexDatas.size()) {
                    mOnIndexPressedListener.onIndexPressed(pressI, mIndexDatas.get(pressI));
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            default:
                setBackgroundResource(android.R.color.transparent);//手指抬起时背景恢复透明
                //回调监听器
                if (null != mOnIndexPressedListener) {
                    mOnIndexPressedListener.onMotionEventEnd();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        if (null == mIndexDatas || mIndexDatas.isEmpty()) {
            return;
        }
        computeGapHeight();
    }

    /**
     * 当前被按下的index的监听器
     */
    public interface onIndexPressedListener {
        void onIndexPressed(int index, String text);//当某个Index被按下

        void onMotionEventEnd();//当触摸事件结束（UP CANCEL）
    }

    private onIndexPressedListener mOnIndexPressedListener;

    public void setmOnIndexPressedListener(onIndexPressedListener mOnIndexPressedListener) {
        this.mOnIndexPressedListener = mOnIndexPressedListener;
    }

    /**
     * 显示当前被按下的index的TextView
     *
     */
    public void setmPressedShowTextView(TextView mPressedShowTextView) {
        this.mPressedShowTextView = mPressedShowTextView;
    }

    public void setListView(ListView listView) {
        this.mListView = listView;
    }

    public void setmSourceDatas(List<? extends BaseIndexPinyinBean> mSourceDatas) {
        this.mSourceDatas = mSourceDatas;
        initSourceDatas();//对数据源进行初始化
    }


    /**
     * 初始化原始数据源，并取出索引数据源
     *
     * @return
     */
    private void initSourceDatas() {
        if (null == mSourceDatas || mSourceDatas.isEmpty()) {
            return;
        }
        //排序sourceDatas
        mDataHelper.sortSourceDatas(mSourceDatas);
    }

    /**
     * 以下情况调用：
     * 1 在数据源改变
     * 2 控件size改变时
     * 计算gapHeight
     */
    private void computeGapHeight() {
        mGapHeight = (mHeight - getPaddingTop() - getPaddingBottom()) / mIndexDatas.size();
    }

    /**
     * 根据传入的pos返回tag
     *
     * @param tag
     * @return
     */
    private int getPosByTag(String tag) {
        if (null == mSourceDatas || mSourceDatas.isEmpty()) {
            return -1;
        }
        if (TextUtils.isEmpty(tag)) {
            return -1;
        }
        for (int i = 0; i < mSourceDatas.size(); i++) {
            if (tag.equals(mSourceDatas.get(i).getBaseIndexTag())) {
                return i;
            }
        }
        return -1;
    }

    public static abstract class BaseIndexPinyinBean {
        private String baseIndexTag;//所属的分类（汉语拼音首字母）
        private String baseIndexPinyin;//拼音
        private String baseIndexPinyinFirst;//全拼首字母

        protected abstract boolean isCity();

        String getBaseIndexPinyinFirst() {
            return baseIndexPinyinFirst;
        }

        void setBaseIndexPinyinFirst(String baseIndexPinyinFirst) {
            this.baseIndexPinyinFirst = baseIndexPinyinFirst;
        }

        public String getBaseIndexTag() {
            return baseIndexTag;
        }

        void setBaseIndexTag(String baseIndexTag) {
            this.baseIndexTag = baseIndexTag;
        }

        String getBaseIndexPinyin() {
            return baseIndexPinyin;
        }

        void setBaseIndexPinyin(String baseIndexPinyin) {
            this.baseIndexPinyin = baseIndexPinyin;
        }

        //需要转化成拼音的目标字段
        public abstract String getTarget();

        @Override
        public String toString() {
            return "BaseIndexPinyinBean{" +
                    "baseIndexTag='" + baseIndexTag + '\'' +
                    ", baseIndexPinyin='" + baseIndexPinyin + '\'' +
                    ", baseIndexPinyinFirst='" + baseIndexPinyinFirst + '\'' +
                    '}';
        }
    }

    public class IndexBarDataHelperImpl {
        /**
         * 如果需要，
         * 字符 转 拼音，
         *
         * @param datas
         */
        public IndexBarDataHelperImpl convert(List<? extends BaseIndexPinyinBean> datas) {
            if (null == datas || datas.isEmpty()) {
                return this;
            }
            int size = datas.size();
            for (int i = 0; i < size; i++) {
                BaseIndexPinyinBean indexPinyinBean = datas.get(i);
                StringBuilder pySb = new StringBuilder();
                StringBuilder pySb1 = new StringBuilder();
                String target = indexPinyinBean.getTarget();//取出需要被拼音化的字段
                //遍历target的每个char得到它的全拼音
                for (int i1 = 0; i1 < target.length(); i1++) {
                    String s;
                    if(indexPinyinBean.isCity()){
                        s = Pinyin.toPinyin(target, "").toUpperCase();
                    } else {
                        s = Pinyin.toPinyin(target.charAt(i1)).toUpperCase();
                    }
                    pySb.append(s);
                    pySb1.append(s.charAt(0));
                }
                indexPinyinBean.setBaseIndexPinyin(pySb.toString());//设置全拼音
                indexPinyinBean.setBaseIndexPinyinFirst(pySb1.toString());
            }
            return this;
        }

        /**
         * 如果需要取出，则
         * 取出首字母->tag,或者特殊字母 "#".
         * 否则，用户已经实现设置好
         *
         * @param datas
         */
        void fillInexTag(List<? extends BaseIndexPinyinBean> datas) {
            if (null == datas || datas.isEmpty()) {
                return;
            }
            int size = datas.size();
            for (int i = 0; i < size; i++) {
                BaseIndexPinyinBean indexPinyinBean = datas.get(i);
                //以下代码设置城市拼音首字母
                String tagString = indexPinyinBean.getBaseIndexPinyin().substring(0, 1);
                if (tagString.matches("[A-Z]")) {//如果是A-Z字母开头
                    indexPinyinBean.setBaseIndexTag(tagString);
                } else {//特殊字母这里统一用#处理
                    indexPinyinBean.setBaseIndexTag("#");
                }
            }
        }

        void sortSourceDatas(List<? extends BaseIndexPinyinBean> datas) {
            if (null == datas || datas.isEmpty()) {
                return;
            }
            convert(datas);
            fillInexTag(datas);
            //对数据源进行排序

            Collections.sort(datas, new Comparator<BaseIndexPinyinBean>() {
                @Override
                public int compare(BaseIndexPinyinBean lhs, BaseIndexPinyinBean rhs) {
                    if (lhs.getBaseIndexTag().equals("#")) {
                        return 1;
                    } else if (rhs.getBaseIndexTag().equals("#")) {
                        return -1;
                    } else if (lhs.getBaseIndexTag().equals(rhs.getBaseIndexTag())) {
                        return 0;
                    } else {
                        return lhs.getBaseIndexTag().compareTo(rhs.getBaseIndexTag());
                    }
                }
            });
        }
    }
}

