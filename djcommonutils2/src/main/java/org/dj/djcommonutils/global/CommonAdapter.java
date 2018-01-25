package org.dj.djcommonutils.global;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * 作者：DuJianBo on 2016/9/22 16:10
 * 邮箱：jianbo_du@foxmail.com
 */
public abstract class CommonAdapter<T> extends BaseAdapter {

    private List<T> datas;
    protected LayoutInflater mInflater;
    protected Context mContext;
    protected final int mItemLayoutId;

    protected CommonAdapter(Context context, List<T> datas, int mItemLayoutId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.datas = datas;
        this.mItemLayoutId = mItemLayoutId;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    public List<T> getDatas() {
        return datas;
    }

    @Override
    public T getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder = getViewHolder(position, convertView,
                parent);
        convert(viewHolder, getItem(position),position);
        return viewHolder.getConvertView();
    }

    public abstract void convert(ViewHolder helper, T item,int postion);

    private ViewHolder getViewHolder(int position, View convertView,
                                     ViewGroup parent) {
        return ViewHolder.get(mContext, convertView, parent, mItemLayoutId,
                position);
    }
}
