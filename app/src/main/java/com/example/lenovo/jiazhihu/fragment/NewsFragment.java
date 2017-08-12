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

import com.example.lenovo.jiazhihu.others.ItemClickListener;
import com.example.lenovo.jiazhihu.MyApplication;
import com.example.lenovo.jiazhihu.R;
import com.example.lenovo.jiazhihu.activity.ContentActivity;
import com.example.lenovo.jiazhihu.adapter.NewsItemAdapter;
import com.example.lenovo.jiazhihu.model.Latest;
import com.example.lenovo.jiazhihu.model.News;
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
 */

public class NewsFragment extends BaseFragment {

    private NewsItemAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private LinearLayout mLayoutFail;
    private ProgressBar mProgressBar;

    private List<StoriesEntity> mStoriesEntities;
    private List<Latest.TopStoriesEntity> mHead;
    private News news;
    //private static Handler handler = new Handler();
    private String urlId = "";

    public void setNewsType(String id) {
        if (id.equals(urlId)){
            return;
        }
        urlId = id;
        if (mStoriesEntities != null){
            mStoriesEntities.clear();
            mAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mLayoutFail = (LinearLayout) view.findViewById(R.id.layout_fail_main);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_main);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnItemTouchListener(new ItemClickListener(mRecyclerView,
                new ItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        MyApplication.getNetCondition();

                        if (mAdapter.getItemViewType(position) ==
                                NewsItemAdapter.ITEM_TYPE.ITEM_TYPE_STORY.ordinal()) {
                            if (!isReadUtils.checkInsertRead(getContext(),
                                    mAdapter.getItem(position).getId() + "")) {
                                TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
                                tv_title.setTextColor(getResources().getColor(darker_gray));
                            }
                            StoriesEntity entity = mAdapter.getItem(position);
                            startNewsActivity(entity);
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
        loadStories();
    }

    public  void scroolToTop(){
        if (mStoriesEntities != null)
                mRecyclerView.scrollToPosition(0);
    }

    @Override
    protected void initData() {

        mStoriesEntities = new ArrayList<>();
        mHead = new ArrayList<>();

        mAdapter = new NewsItemAdapter(mStoriesEntities, mHead, getContext());
        mRecyclerView.setAdapter(mAdapter);

        super.initData();
    }

    /**
     * 请求、解析、加载新闻
     */
    private void loadStories(){
        //请求

        OkHttpUtil.getAsyncStringByNetCondition("http://news-at.zhihu.com/api/4/theme/" + urlId,
                new OkHttpUtil.DataCallBack() {
                    @Override
                    public void requestFailure(Request request, IOException e) {
                        requestFail();
                    }

                    @Override
                    public void requestSuccess(String result) throws Exception {
                        if (!result.equals("") ){
                            //解析与加载
                            parseStoriesJson(result);
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

    private void parseStoriesJson(String responseString){
        Gson gson = new Gson();
        news = gson.fromJson(responseString, News.class);

        final List<Latest.TopStoriesEntity> head = new ArrayList<>();
        Latest.TopStoriesEntity topStoriesEntity =  new Latest.TopStoriesEntity();
        topStoriesEntity.setTitle(news.getDescription());
        topStoriesEntity.setImage(news.getImage());
        head.add(topStoriesEntity);

        final StoriesEntity h = new StoriesEntity();
        h.setType(NewsItemAdapter.ITEM_TYPE.ITEM_TYPE_HEADER.ordinal());

        final List<StoriesEntity> storiesEntities = news.getStories();
        for (StoriesEntity s : storiesEntities){
            s.setType(0);
        }
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
        mStoriesEntities.clear();
        mStoriesEntities.add(h);//给header占位
        mStoriesEntities.addAll(storiesEntities);

        mHead.clear();
        mHead.addAll(head);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.setVisibility(View.VISIBLE);
//            }
//        });
    }

    public void startNewsActivity(StoriesEntity storiesEntity){
        Intent intent = new Intent(mActivity, ContentActivity.class);
        intent.putExtra("entity", storiesEntity);
        startActivity(intent);
    }
}
