package com.example.lenovo.jiazhihu.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.lenovo.jiazhihu.others.ItemClickListener;
import com.example.lenovo.jiazhihu.MyApplication;
import com.example.lenovo.jiazhihu.R;
import com.example.lenovo.jiazhihu.adapter.CommentsItemAdapter;
import com.example.lenovo.jiazhihu.model.Comment;
import com.example.lenovo.jiazhihu.model.Comments;
import com.example.lenovo.jiazhihu.util.OkHttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

import static android.R.drawable.arrow_down_float;
import static android.R.drawable.arrow_up_float;

/**
 * Created by lenovo on 2017/8/4.
 * 活动用于查看评论
 */

public class CommentActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private CommentsItemAdapter mAdapter;
    private List<Comment> mComments;
    private String id;
    private Handler handler = new Handler();
    private int shortTopicPos = 1;//短评论位置
    private boolean isShortLoaded = false;//短评论是否加载
    private boolean isLongLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.downloaded_toolbar);
        toolbar.setTitle("热门评论");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        mProgressBar = (ProgressBar) findViewById(R.id.progress_comment);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_comment);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnItemTouchListener(new ItemClickListener(mRecyclerView, new ItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                int t = mAdapter.getItemViewType(position);
                //评论topic点击事件
                if (t != CommentsItemAdapter.ITEM_TYPE.ITEM_TYPE_COMMENT.ordinal()) {
                    MyApplication.getNetCondition();
                    ImageView iv_topic_comment = (ImageView) view.findViewById(R.id.iv_topic_comment);
                    if (t == 1){
                        if (isLongLoaded){
                            mComments.subList(1, shortTopicPos).clear();
                            mAdapter.notifyDataSetChanged();
                            shortTopicPos = 1;
                            isLongLoaded = false;
                            iv_topic_comment.setBackground(getResources().getDrawable(arrow_up_float));
                        }else {
                            loadComments(true);
                            iv_topic_comment.setBackground(getResources().getDrawable(arrow_down_float));
                        }
                    }if (t ==2){
                        if (isShortLoaded){
                            mComments.subList(shortTopicPos+1, mComments.size()).clear();
                            mAdapter.notifyDataSetChanged();
                            isShortLoaded = false;
                            iv_topic_comment.setBackground(getResources().getDrawable(arrow_up_float));
                        }else {
                            loadComments(false);
                            iv_topic_comment.setBackground(getResources().getDrawable(arrow_down_float));
                        }
                    }
                }
            }
            @Override
            public void onItemLongClick(View view, int position) {
                onItemClick(view, position);
            }
        }));
        initData();
    }

    private void initData(){
        mComments = new ArrayList<>();
        //添加 长评论topic
        Comment longTopic = new Comment();
        longTopic.setType(1);
        mComments.add(longTopic);
        //添加 短评论topic
        Comment shortTopic = new Comment();
        shortTopic.setType(2);
        mComments.add(shortTopic);
        mAdapter = new CommentsItemAdapter(mComments);
        mRecyclerView.setAdapter(mAdapter);
        //加载changpinglun
        loadComments(true);
    }
    /**
     * 请求、解析、加载评论
     * @param isLong 加载类型是否为长评论，false则为加载短评论
     */
    private void loadComments(final boolean isLong){
        Thread thread = new Thread(){
            @Override
            public void run() {
                String url;
                if (isLong){
                    url = "/long-comments";
                }
                else{
                    url = "/short-comments";
                }
                OkHttpUtil.getAsyncStringByNetCondition("http://news-at.zhihu.com/api/4/story/" + id +url,
                        new OkHttpUtil.DataCallBack() {
                            @Override
                            public void requestFailure(Request request, IOException e) {
                                requestFail();
                            }

                            @Override
                            public void requestSuccess(String result) throws Exception {
                                if (result.equals(""))
                                {
                                    requestFail();
                                }else {
                                    parseCommentJson(result, isLong);
                                }
                            }
                        });
            }
        };
        thread.start();
    }

    private void requestFail(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CommentActivity.this, "请求失败，请检查网络后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseCommentJson(String responseString, final boolean isLong) {

        Gson gson = new Gson();
        final Comments temp = gson.fromJson(responseString, Comments.class);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (temp.getComments() == null){
                    //在这里写下 返回空评论时应该做的事
                    return;
                }else{
                    //在适当位置插入评论
                    if (isLong) {
                        mComments.addAll(1, temp.getComments());
                        isLongLoaded = true;
                        shortTopicPos = temp.getComments().size() + 1;
                    }else {
                        mComments.addAll(temp.getComments());
                        isShortLoaded = true;
                    }
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                    //短评论加载完自动下滑
                    if (!isLong)
                        mRecyclerView.smoothScrollToPosition(shortTopicPos + 3);
                }
            }
        });
    }
}
