package com.example.lenovo.jiazhihu.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lenovo.jiazhihu.R;
import com.example.lenovo.jiazhihu.model.Comment;
import com.example.lenovo.jiazhihu.util.ImageLoadUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by lenovo on 2017/7/29.
 * 加载评论的适配器
 */

public class CommentsItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Comment> mComments;

    public enum ITEM_TYPE {
        ITEM_TYPE_COMMENT,//评论
        ITEM_TYPE_TOPIC_LONG,//长评论标题
        ITEM_TYPE_TOPIC_SHORT
    }

    public CommentsItemAdapter(List<Comment> s){
        this.mComments = s;
    }
    @Override
    public int getItemCount() {
        //不含headr与footer
        return mComments.size();
    }

    public Comment getItem(int dex) {
        return mComments.get(dex);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position)
    {
        if (holder instanceof CommentsViewHolder) {
            ((CommentsViewHolder) holder).name.setText(mComments.get(position).getAuthor());
            ((CommentsViewHolder) holder).likes.setText(mComments.get(position).getLikes());
            ((CommentsViewHolder) holder).content.setText(mComments.get(position).getContent());
            ((CommentsViewHolder) holder).time.setText(paserTime(mComments.get(position).getTime()));

            ((CommentsViewHolder) holder).avatar.setTag(mComments.get(position).getAvatar());
            ImageLoadUtil.getInstance(
                    1, ImageLoadUtil.Type.LIFO).loadImage(mComments.get(position).getAvatar()
                    , ((CommentsViewHolder) holder).avatar, true);

        } else if (holder instanceof TopicViewHolder) {
            if (mComments.get(position).getType() == 1){
                ((TopicViewHolder) holder).tv_topic_comment.setText("  长 评 论");
            }
            else {
                ((TopicViewHolder) holder).tv_topic_comment.setText("  短 评 论");
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if(viewType == ITEM_TYPE.ITEM_TYPE_COMMENT.ordinal()){
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comments_item, viewGroup,false);
            return new CommentsViewHolder(v);
        }
        else if(viewType == ITEM_TYPE.ITEM_TYPE_TOPIC_LONG.ordinal()){
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.topic_comment_long, viewGroup,false);
            return new TopicViewHolder(v);
        }else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.topic_comment_short, viewGroup,false);
            return new TopicViewHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        int t = mComments.get(position).getType();
        if (t == 0)
            return ITEM_TYPE.ITEM_TYPE_COMMENT.ordinal();
        else if (t == 1)
            return ITEM_TYPE.ITEM_TYPE_TOPIC_LONG.ordinal();
        else return ITEM_TYPE.ITEM_TYPE_TOPIC_SHORT.ordinal();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        TextView content;
        ImageView avatar;
        TextView name;
        TextView time;
        TextView likes;

        public CommentsViewHolder(View view){
            super(view);
            content = (TextView) view.findViewById(R.id.content_comment);
            avatar = (ImageView) view.findViewById(R.id.iv_comment);
            name = (TextView) view.findViewById(R.id.name_comment);
            time = (TextView) view.findViewById(R.id.date_comment);
            likes = (TextView) view.findViewById(R.id.tv_stars_comment);
        }

    }

    public static class TopicViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_topic_comment;
        public ImageView iv_topic_comment;

        public TopicViewHolder(View view){
            super(view);
            tv_topic_comment = (TextView) view.findViewById(R.id.tv_topic_comment);
            iv_topic_comment = (ImageView) view.findViewById(R.id.iv_topic_comment);
        }
    }

    public static String paserTime(String t){
        int time = Integer.parseInt(t);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date(time * 1000L));
    }
}
