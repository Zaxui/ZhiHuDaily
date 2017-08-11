package com.example.lenovo.jiazhihu.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.jiazhihu.others.EndLessOnScrollListener;
import com.example.lenovo.jiazhihu.others.ItemClickListener;
import com.example.lenovo.jiazhihu.MyApplication;
import com.example.lenovo.jiazhihu.R;
import com.example.lenovo.jiazhihu.activity.ContentActivity;
import com.example.lenovo.jiazhihu.adapter.NewsItemAdapter;
import com.example.lenovo.jiazhihu.model.Before;
import com.example.lenovo.jiazhihu.model.Latest;
import com.example.lenovo.jiazhihu.model.StoriesEntity;
import com.example.lenovo.jiazhihu.util.OkHttpUtil;
import com.example.lenovo.jiazhihu.util.isReadUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

import static android.R.color.darker_gray;

/**
 * Created by lenovo on 2017/7/29.
 * 用于展示知乎日报首页的Fragment
 */

public class MainFragment extends BaseFragment {

    private NewsItemAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayout mLayoutFail;//加载失败布局
    private ProgressBar mProgressBar;

    private EndLessOnScrollListener mEndLessOnScrollListener;
    private List<StoriesEntity> mStoriesEntities;
    private List<Latest.TopStoriesEntity> mHead;//轮播图数据实体
    private Latest latest;//今日热闻消息容器
    private Before before;//往日新闻消息容器
    //private static Handler handler = new Handler();//线程handler
    private String date;//记录往日新闻日期

    //private final MyThread myThread = new MyThread(this);
    //private final MyHandler myHandler = new MyHandler(this);
//    private static class MyHandler extends Handler {
//        private WeakReference<MainFragment> reference;
//        public MyHandler(MainFragment mainFragment) {
//            reference = new WeakReference<>(mainFragment);
//        }
//        @Override
//        public void handleMessage(Message msg) {
//            MainFragment mainFragment = (MainFragment) reference.get();
//            if(mainFragment != null){
//                switch (msg.what) {//根据收到的消息的what类型处理
//                    case 0:
//                        mainFragment.mProgressBar.setVisibility(View.GONE);
//                        mainFragment.mLayoutFail.setVisibility(View.VISIBLE);
//                        Toast.makeText(mainFragment.mActivity, "网络不佳", Toast.LENGTH_SHORT).show();
//                        break;
//                    case 1:
//                        final StoriesEntity t = new StoriesEntity();
//                        t.setType(NewsItemAdapter.ITEM_TYPE.ITEM_TYPE_TOPIC.ordinal());
//                        t.setTitle("今日热闻");
//                        final StoriesEntity h = new StoriesEntity();
//                        h.setType(NewsItemAdapter.ITEM_TYPE.ITEM_TYPE_HEADER.ordinal());
//                        Latest latest = (Latest) msg.obj;
//                        final List<Latest.TopStoriesEntity> head = latest.getTop_stories();
//                        mainFragment.mStoriesEntities.clear();
//                        mainFragment.mStoriesEntities.add(h);//给header占位，就很尴尬
//                        mainFragment.mStoriesEntities.add(t);
//                        mainFragment.mStoriesEntities.addAll(latest.getStories());
//
//                        mainFragment.mHead.clear();
//                        mainFragment. mHead.addAll(head);
//                        mainFragment.mAdapter.notifyDataSetChanged();
//                        mainFragment.mRecyclerView.setVisibility(View.VISIBLE);
//                        break;
//                    case 2:
//                        Before before = (Before) msg.obj;
//                        final StoriesEntity storiesEntity = new StoriesEntity();
//                        storiesEntity.setType(NewsItemAdapter.ITEM_TYPE.ITEM_TYPE_TOPIC.ordinal());
//                        storiesEntity.setTitle(mainFragment.convertDate(mainFragment.date));
//                        mainFragment.mStoriesEntities.add(storiesEntity);
//                        mainFragment.mStoriesEntities.addAll(before.getStories());
//                        mainFragment.mAdapter.notifyDataSetChanged();
//                        mainFragment.mRecyclerView.setVisibility(View.VISIBLE);
//                        break;
//                    default:
//                        super.handleMessage(msg);//这里最好对不需要或者不关心的消息抛给父类，避免丢失消息
//                        break;
//                }
//            }
//        }
//    }

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mLayoutFail = (LinearLayout) view.findViewById(R.id.layout_fail_main);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_main);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        //添加上拉加载的监听器
        mEndLessOnScrollListener = new EndLessOnScrollListener(mLayoutManager) {
            @Override public void onLoadMore(int currentPage) {
                loadStories(false);
            }
        };
        mRecyclerView.addOnScrollListener(mEndLessOnScrollListener);
        //添加Item点击监听器
        mRecyclerView.addOnItemTouchListener(new ItemClickListener(mRecyclerView,
                new ItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        MyApplication.getNetCondition();
                        //判明类别
                        if (mAdapter.getItemViewType(position) ==
                                NewsItemAdapter.ITEM_TYPE.ITEM_TYPE_STORY.ordinal()) {
                            //记录和区别是否浏览过该新闻
                            if (!isReadUtils.checkInsertRead(getContext(),
                                    mAdapter.getItem(position).getId() + "")) {
                                TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
                                tv_title.setTextColor(getResources().getColor(darker_gray));
                            }
                            StoriesEntity entity = mAdapter.getItem(position);
                            startContentActivity(entity);
                        }
                    }
                    @Override
                    public void onItemLongClick(View view, int position) {
                        onItemClick(view, position);
                    }
                }));
        return view;

    }

    public  void refreshData(){
        loadStories(true);
        mEndLessOnScrollListener.reset();
    }

    public  void scroolToTop(){
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    protected void initData() {

        mStoriesEntities = new ArrayList<>();
        mHead = new ArrayList<>();

        mAdapter = new NewsItemAdapter(mStoriesEntities, mHead, getContext());
        mRecyclerView.setAdapter(mAdapter);

        loadStories(true);
        super.initData();
    }

//    private static class MyThread extends Thread {
//        WeakReference<MainFragment> mMainFragmentRef;
//        boolean type = true;
//
//        public MyThread(MainFragment mainFragment) {
//            mMainFragmentRef = new WeakReference<MainFragment>(mainFragment);
//        }
//        public void setType(boolean type){
//            this.type = type;
//        }
//
//        @Override
//        public void run() {
//            super.run();
//            if (mMainFragmentRef == null)
//                return;
//            if (mMainFragmentRef.get() != null)
//                mMainFragmentRef.get().loadStories(type);
//        }
//    }

    /**
     * 请求、解析、加载新闻
     * @param isLatest 加载类型是否为今日热闻，false则加载往日新闻
     */
    private void loadStories(final boolean isLatest){
        String url;
        if (isLatest){
            url = "latest";
        }else {
            url = "before/" + date;
        }
        OkHttpUtil.getAsyncStringByNetCondition("http://news-at.zhihu.com/api/4/news/" + url,
                new OkHttpUtil.DataCallBack() {
                    @Override
                    public void requestFailure(Request request, IOException e) {
                        requestFail();
                    }

                    @Override
                    public void requestSuccess(String result) throws Exception {
                        //此处不应返回空数据
                        if (!result.equals("") ){
                            //解析与加载
                            parseStoriesJson(result, isLatest);
                        }
                        else {
                            requestFail();
                        }
                    }
                });
    }

    private void requestFail(){
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
        mProgressBar.setVisibility(View.GONE);
        mLayoutFail.setVisibility(View.VISIBLE);
        Toast.makeText(mActivity, "网络不佳", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void parseStoriesJson(String responseString, boolean isLatest){
        Gson gson = new Gson();
        if (isLatest){
            latest = gson.fromJson(responseString, Latest.class);
            date = latest.getDate();

            final List<Latest.TopStoriesEntity> head = latest.getTop_stories();
            final StoriesEntity t = new StoriesEntity();
            t.setType(NewsItemAdapter.ITEM_TYPE.ITEM_TYPE_TOPIC.ordinal());
            t.setTitle("今日热闻");
            final StoriesEntity h = new StoriesEntity();
            h.setType(NewsItemAdapter.ITEM_TYPE.ITEM_TYPE_HEADER.ordinal());
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
            mStoriesEntities.clear();
            mStoriesEntities.add(h);//给header占位，就很尴尬
            mStoriesEntities.add(t);
            mStoriesEntities.addAll(latest.getStories());

            mHead.clear();
            mHead.addAll(head);
            mAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            scroolToTop();
//                }
//            });
        }else {
            before = gson.fromJson(responseString, Before.class);
            if (before == null) {
                return;
            }
            date = before.getDate();

            final StoriesEntity t = new StoriesEntity();
            t.setType(NewsItemAdapter.ITEM_TYPE.ITEM_TYPE_TOPIC.ordinal());
            t.setTitle(convertDate(date));
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
            mStoriesEntities.add(t);
            mStoriesEntities.addAll(before.getStories());
            mAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
//                }
//            });
        }
    }

    private String convertDate(String date) {
        String result = date.substring(0, 4);
        result += "年";
        result += date.substring(4, 6);
        result += "月";
        result += date.substring(6, 8);
        result += "日";
        return result;
    }

    public void startContentActivity(StoriesEntity storiesEntity){
        Intent intent = new Intent(mActivity, ContentActivity.class);
        intent.putExtra("entity", storiesEntity);
        startActivity(intent);
    }

}
