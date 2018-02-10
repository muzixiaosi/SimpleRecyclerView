package com.example.lipeng.simplerecyclerview;

import android.content.Context;

import com.example.adapterlibrary.CommonBaseAdapter;
import com.example.adapterlibrary.ViewHolder;

import java.util.List;

/**
 * Created by lipeng on 18/2/10.
 */

public class StockDetailAdapter extends CommonBaseAdapter<String> {


    public StockDetailAdapter(Context context, List<String> datas, boolean openLoadMore) {
        super(context, datas, openLoadMore);
    }

    @Override
    protected void convert(ViewHolder holder, String data, int position) {
        holder.setText(R.id.tv_item, data);
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_layout;
    }
}
