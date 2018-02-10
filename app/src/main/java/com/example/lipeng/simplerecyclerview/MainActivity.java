package com.example.lipeng.simplerecyclerview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.adapterlibrary.OnItemClickListener;
import com.example.adapterlibrary.OnLoadMoreListener;
import com.example.adapterlibrary.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private StockDetailAdapter mAdapter;
    private Button btnInsertedFooter;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btnInsertedFooter = (Button) findViewById(R.id.btn_inserted_footer);
        mRecyclerView = (RecyclerView) findViewById(R.id.rcv);
        //初始化 adapter
        mAdapter = new StockDetailAdapter(this, null, true);
        //初始化 EmptyView
        View emptyView = LayoutInflater.from(this).inflate(R.layout.empty_layout, (ViewGroup) mRecyclerView.getParent(), false);
        mAdapter.setEmptyView(emptyView);
        //初始化 开始加载更多的loading View
        mAdapter.setLoadingView(R.layout.load_loading_layout);
        //设置加载更多触发的事件监听
        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(boolean isReload) {
                loadMore();
            }
        });
        //设置item点击事件监听
        mAdapter.setOnItemClickListener(new OnItemClickListener<String>() {
            @Override
            public void onItemClick(ViewHolder viewHolder, String data, int position) {
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            }
        });

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);//列表再底部开始展示，反转后由上面开始展示
        mLayoutManager.setReverseLayout(true);//列表翻转
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        initData();
    }

    private void initData() {

        btnInsertedFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> data = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    data.add("new李鹏" + i);
                }
                int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
                mAdapter.addInsertHeaderData(data);

                if (firstVisibleItemPosition == 0) {
                    mRecyclerView.scrollToPosition(0);
                }
            }
        });

        //延时3s刷新列表
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<String> data = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    data.add("lipeng  " + i);
                }
                //刷新数据
                mAdapter.setNewData(data);
            }
        }, 1000);

    }


    private void loadMore() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /*if (mAdapter.getItemCount() > 40) {
                    mAdapter.loadEnd();
                } else {
                    final List<String> data = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        data.add("更多数据 : lipeng  " + (mAdapter.getDataCount() + i));
                    }
                    //刷新数据
                    mAdapter.addInsertHeaderData(data);
                    mRecyclerView.scrollToPosition(0);
                }*/
                final List<String> data = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    data.add("更多数据 : lipeng  " + (mAdapter.getDataCount() + i));
                }
                //刷新数据
                mAdapter.addInsertFooterData(data);
            }
        }, 1000);
    }

}
