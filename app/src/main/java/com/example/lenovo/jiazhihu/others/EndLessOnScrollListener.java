package com.example.lenovo.jiazhihu.others;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * 实现Recycler View上拉加载的监听器
 */

public abstract class EndLessOnScrollListener extends RecyclerView.OnScrollListener {
    //声明一个LinearLayoutManager
    private LinearLayoutManager mLinearLayoutManager;
    // 当前页，从0开始
    private int currentPage = 0;
    // 已经加载出来的Item的数量
    private int totalItemCount;
    //主要用来存储上一个totalItemCount
    private int previousTotal = 0;
    // 是否正在上拉数据
    private boolean loading = true;
    public void reset(){
        loading = false;
        totalItemCount = mLinearLayoutManager.getItemCount();
        previousTotal = totalItemCount;
    }
    public EndLessOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }
    @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        totalItemCount = mLinearLayoutManager.getItemCount();
        if(loading){
            if(totalItemCount > previousTotal){
                //说明数据已经加载结束
                loading = false;
                previousTotal = totalItemCount;
            }
        }

        if (!loading && totalItemCount <= mLinearLayoutManager.findLastVisibleItemPosition()+1){
            currentPage ++;
            onLoadMore(currentPage);
            loading = true;
        }
    }
    /**
     * 提供一个抽闲方法，在Activity中监听到这个EndLessOnScrollListener
     * 并且实现这个方法
     * */
    public abstract void onLoadMore(int currentPage);
}

