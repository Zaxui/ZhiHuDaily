package com.example.lenovo.jiazhihu.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lenovo.jiazhihu.R;
import com.example.lenovo.jiazhihu.activity.ContentActivity;
import com.example.lenovo.jiazhihu.model.Latest;
import com.example.lenovo.jiazhihu.model.StoriesEntity;
import com.example.lenovo.jiazhihu.util.ImageLoadUtil;
import com.example.lenovo.jiazhihu.util.isReadUtils;
import com.example.lenovo.jiazhihu.view.CycleViewPager;

import java.util.List;

import static android.R.color.black;
import static android.R.color.darker_gray;
import static com.example.lenovo.jiazhihu.MyApplication.getMyapplicationContext;

/**
 * Created by lenovo on 2017/7/29.
 * 新闻条目适配器
 */

public class NewsItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<StoriesEntity> mEntities;
    private  List<Latest.TopStoriesEntity> mHead;
    private Context context;
    //糟糕的分类方式
    public enum ITEM_TYPE {
        ITEM_TYPE_STORY,//新闻
        ITEM_TYPE_TOPIC,//“今日热闻”与时间标题
        ITEM_TYPE_HEADER//图片轮播头
    }

    public NewsItemAdapter(List<StoriesEntity> s, List<Latest.TopStoriesEntity> head, Context context){
        this.mEntities = s;
        this.mHead = head;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        //不含headr与footer
        return mEntities.size();
    }

    public StoriesEntity getItem(int dex) {
        return mEntities.get(dex);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position)
    {
        //类型检查和类型转换感觉很恶心
        if (holder instanceof StoriesViewHolder) {
            ((StoriesViewHolder) holder).tv_title.setText(mEntities.get(position).getTitle());
            if(mEntities.get(position).getImages() != null){
                //使用tag防止图片错乱
                ((StoriesViewHolder) holder).iv_title.setTag(mEntities.get(position).getImages().get(0));
                //加载默认图片消除ViewHolder复用时的加载重复
                ((StoriesViewHolder) holder).iv_title.setImageResource(darker_gray);
                //异步加载
                ImageLoadUtil.getInstance(
                        1, ImageLoadUtil.Type.LIFO).loadImage(mEntities.get(position).getImages().get(0)
                        , ((StoriesViewHolder) holder).iv_title, true);
            }else{
                ((StoriesViewHolder) holder).iv_title.setVisibility(View.GONE);
            }
            //是否浏览过，低效的检查处理
            if (isReadUtils.checkRead(context, mEntities.get(position).getId() + "")){
                ((StoriesViewHolder) holder).tv_title.setTextColor(context.getResources().getColor(darker_gray));
            }else {
                ((StoriesViewHolder) holder).tv_title.setTextColor(context.getResources().getColor(black));
            }
        } else if (holder instanceof TopicViewHolder) {
            ((TopicViewHolder) holder).tv_topic.setText(mEntities.get(position).getTitle());
        } else {
            //图片轮播器加载
            if (mHead.size() > 1){
                ((HeaderViewHolder) holder).cycleViewPager.setIndicators(R.mipmap.ad_select,
                        R.mipmap.ad_unselect);
                ((HeaderViewHolder) holder).cycleViewPager.setData(mHead,
                        new CycleViewPager.ImageCycleViewListener() {
                            @Override
                            public void onImageClick(Latest.TopStoriesEntity entity, int position, View imageView) {
                                StoriesEntity storiesEntity = new StoriesEntity();
                                storiesEntity.setId(entity.getId());
                                storiesEntity.setTitle(entity.getTitle());
                                Intent intent = new Intent(context, ContentActivity.class);
                                intent.putExtra("entity", storiesEntity);
                                context.startActivity(intent);
                            }
                        });
            }else {
                //只有一张就不轮了
                ((HeaderViewHolder) holder).cycleViewPager.setCycle(false);
                ((HeaderViewHolder) holder).cycleViewPager.setData(mHead);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if(viewType == ITEM_TYPE.ITEM_TYPE_STORY.ordinal()){
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.stories_item, viewGroup,false);
            return new StoriesViewHolder(v);
        }
        else if (viewType == ITEM_TYPE.ITEM_TYPE_TOPIC.ordinal()){
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.topic_item, viewGroup,false);
            return new TopicViewHolder(v);
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.header_item, viewGroup,false);
            return new HeaderViewHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mEntities.get(position).getType() ==
                ITEM_TYPE.ITEM_TYPE_HEADER.ordinal())
            return ITEM_TYPE.ITEM_TYPE_HEADER.ordinal();
        if(mEntities.get(position).getType() == 0)
            return ITEM_TYPE.ITEM_TYPE_STORY.ordinal();
        else return ITEM_TYPE.ITEM_TYPE_TOPIC.ordinal();
    }

    public static class StoriesViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_title;
        public ImageView iv_title;

        public StoriesViewHolder(View view){
            super(view);
            tv_title = (TextView) view.findViewById(R.id.tv_title);
            iv_title = (ImageView) view.findViewById(R.id.iv_title);
        }

    }

    public static class TopicViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_topic;

        public TopicViewHolder(View view){
            super(view);
            tv_topic = (TextView) view.findViewById(R.id.tv_topic);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public CycleViewPager cycleViewPager;

        public HeaderViewHolder(View view){
            super(view);
            cycleViewPager = (CycleViewPager) view.findViewById(R.id.cycle_view);
        }
    }
}
