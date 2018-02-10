package com.example.adapterlibrary;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lipeng on 18/2/10.
 * <p>
 * 下拉加载更多.
 */

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static final int TYPE_COMMON_VIEW = 10001;
    private static final int TYPE_EMPTY_VIEW = 10002;
    private static final int TYPE_FOOTER_VIEW = 20000;

    protected final Context mContext;
    private final List<T> mDatas;
    private final boolean mOpenLoadMore;//是否开启加载更多
    private View mEmptyView;//空数据界面View
    private boolean isRemoveEmptyView;//是否移除空界面
    private RelativeLayout mFooterLayout;//header view (加载更多)
    private boolean isAutoLoadMore = false;//是否自动加载，当数据不满一屏幕会自动加载
    private boolean isReset;//开始重新加载数据
    private View mLoadingView; //分页加载中view
    private boolean isLoading;//是否正在加载更多
    private OnLoadMoreListener mLoadMoreListener;
    private RelativeLayout.LayoutParams mParams;


    public BaseAdapter(Context context, List<T> datas, boolean openLoadMore) {
        mContext = context;
        mDatas = datas == null ? new ArrayList<T>() : datas;
        mOpenLoadMore = openLoadMore;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh = null;
        switch (viewType) {
            case TYPE_FOOTER_VIEW:
                if (mFooterLayout == null) {
                    mFooterLayout = new RelativeLayout(mContext);
                }
                vh = ViewHolder.create(mFooterLayout);
                break;
            case TYPE_EMPTY_VIEW:
                vh = ViewHolder.create(mEmptyView);
                break;
        }
        return vh;
    }

    @Override
    public int getItemCount() {
        if (mDatas.isEmpty() && mEmptyView != null) {
            return 1;
        }
        return mDatas.size() + getFooterCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (mDatas.isEmpty()) {
            if (mEmptyView != null && !isRemoveEmptyView) {
                return TYPE_EMPTY_VIEW;
            }
            return TYPE_COMMON_VIEW;
        }
        if (isFooterView(position)) {
            return TYPE_FOOTER_VIEW;
        }
        return getViewType(position, mDatas.get(position));
    }

    protected abstract int getViewType(int position, T data);

    public int getFooterCount() {
        return mOpenLoadMore && !mDatas.isEmpty() ? 1 : 0;
    }

    private boolean isFooterView(int position) {
        return mOpenLoadMore && position >= getItemCount() - 1;
    }

    public void addFooterView(View footerView) {
        if (footerView == null) {
            return;
        }
        if (mFooterLayout == null) {
            mFooterLayout = new RelativeLayout(mContext);
        }
        removeFooterView();
        if (mParams == null) {
            mParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        mFooterLayout.addView(footerView, mParams);
    }

    public void removeFooterView() {
        mFooterLayout.removeAllViews();
    }

    /**
     * StaggeredGridLayoutManager模式时，HeaderView、FooterView可占据一行
     */
    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        if (isFooterView(position)) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    /**
     * GridLayoutManager模式时， HeaderView、FooterView可占据一行，判断RecyclerView是否到达底部
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) layoutManager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (isFooterView(position)) {
                        return gridManager.getSpanCount();
                    }
                    return 1;
                }
            });
        }
        startLoadMore(recyclerView, layoutManager);
    }

    private void startLoadMore(RecyclerView recyclerView, final RecyclerView.LayoutManager layoutManager) {
        if (!mOpenLoadMore || mLoadMoreListener == null) {
            return;
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.w("tag@@", "SCROLL_STATE_IDLE ==> " + "findLastVisibleItemPosition(layoutManager) + 1:"
                            + findLastVisibleItemPosition(layoutManager) + "   getItemCount():" + getItemCount()
                            + "   getFooterCount:" + getFooterCount());
                    if (!isAutoLoadMore && findLastVisibleItemPosition(layoutManager) + 1 == getItemCount() - getFooterCount()) {
                        scrollLoadMore();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isAutoLoadMore && findLastVisibleItemPosition(layoutManager) + 1 == getItemCount() - getFooterCount()) {
                    if (mDatas.isEmpty() && mEmptyView != null) {
                        return;
                    }
                    scrollLoadMore();
                } else if (isAutoLoadMore) {
                    isAutoLoadMore = false;
                }
            }
        });
    }

    private int findLastVisibleItemPosition(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
            return Util.findMax(lastVisibleItemPositions);
        }
        return -1;
    }

    private void scrollLoadMore() {
        if (isReset) {
            return;
        }
        if (mFooterLayout.getChildAt(0) == mLoadingView && !isLoading) {
            if (mLoadMoreListener != null) {
                isLoading = true;
                showLoaddingView();
                mLoadMoreListener.onLoadMore(false);
            }
        }
    }

    public void setLoadingView(View loadingView) {
        mLoadingView = loadingView;
        addFooterView(mLoadingView);
    }

    public void setLoadingView(int loadingId) {
        setLoadingView(Util.inflate(mContext, loadingId));
    }

    public int getDataCount() {
        return mDatas.size();
    }

    protected List<T> getAllData() {
        return mDatas;
    }

    public void setNewData(List<T> datas) {
        hideLoaddingView();
        if (isReset) {
            isReset = false;
        }
        isLoading = false;
        mDatas.clear();
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    /**
     * 添加新数据
     */
    public void addInsertHeaderData(List<T> datas) {
        hideLoaddingView();
        isLoading = false;
        mDatas.addAll(0, datas);
        notifyItemRangeInserted(0, datas.size());
    }

    /**
     * 添加历史更多数据
     */
    public void addInsertFooterData(List<T> datas) {
        hideLoaddingView();
        isLoading = false;
        int size = mDatas.size();
        mDatas.addAll(datas);
        notifyItemInserted(size);
    }

    protected boolean isCommonItemView(int viewType) {
        return viewType != TYPE_EMPTY_VIEW && viewType != TYPE_FOOTER_VIEW;
    }

    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        mLoadMoreListener = loadMoreListener;
    }

    public void loadEnd() {
        removeFooterView();
    }

    private void hideLoaddingView() {
        mLoadingView.setVisibility(View.GONE);
    }

    private void showLoaddingView() {
        mLoadingView.setVisibility(View.VISIBLE);
    }
}
