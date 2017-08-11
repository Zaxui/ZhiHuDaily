package com.example.lenovo.jiazhihu.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.lenovo.jiazhihu.R;
import com.example.lenovo.jiazhihu.model.StoriesEntity;

import java.util.HashSet;
import java.util.List;

/**
 * Created by lenovo on 2017/7/29.
 */

public class DownLoadedItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<StoriesEntity> mEntities;
    private Context context;
    private boolean isSelecting = false;//是否处于编辑状态
    private HashSet<Integer> mSelectedSet = new HashSet<>();//<pos>, 用于记录已选择的位置集合

    public enum ITEM_TYPE {
        ITEM_TYPE_DOWN_LOADED,//非编辑模式
        ITEM_TYPE_SELECT,//编辑模式
    }

    public DownLoadedItemAdapter(List<StoriesEntity> s, Context context){
        this.mEntities = s;
        this.context = context;
    }

    public StoriesEntity getItem(int dex) {
        return mEntities.get(dex);
    }

    public void setSelecting(boolean isSelecting){
        this.isSelecting = isSelecting;
    }

    public boolean getSelecting(){
        return isSelecting;
    }
    //增加制定位置到已选择集
    public void addSelectedSet(int pos){
        this.mSelectedSet.add(pos);
    }
    //移除指定位置
    public void removeSelectedSet(int pos){
        this.mSelectedSet.remove(pos);
    }
    //全选
    public void SelectAll(){
        this.mSelectedSet.clear();
        int t = mEntities.size();
        for (int pos = 0;pos < t ;pos++){
            this.mSelectedSet.add(pos);
        }
    }
    //全不选
    public void SelectNull(){
        this.mSelectedSet.clear();
    }
    //获取选择集
    public HashSet getSelectedSet(){
        return mSelectedSet;
    }

    @Override
    public int getItemCount() {
        return mEntities.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position)
    {
        if (holder instanceof DonwLoadedViewHolder) {
            ((DonwLoadedViewHolder) holder).tv_title.setText(mEntities.get(position).getTitle());
        } else if (holder instanceof SelectViewHolder) {
            ((SelectViewHolder) holder).tv_title.setText(mEntities.get(position).getTitle());
            if(mSelectedSet.contains(position))
                ((SelectViewHolder) holder).checkBox.setChecked(true);
            else
                ((SelectViewHolder) holder).checkBox.setChecked(false);
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if(viewType == ITEM_TYPE.ITEM_TYPE_DOWN_LOADED.ordinal()){
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.downloaded_item,
                    viewGroup,false);
            return new DonwLoadedViewHolder(v);
        }
        else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.downloaded_item_select,
                    viewGroup,false);
            return new SelectViewHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(!isSelecting)
            return ITEM_TYPE.ITEM_TYPE_DOWN_LOADED.ordinal();
        else
            return ITEM_TYPE.ITEM_TYPE_SELECT.ordinal();
    }

    public static class DonwLoadedViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_title;

        public DonwLoadedViewHolder(View view){
            super(view);
            tv_title = (TextView) view.findViewById(R.id.tv_title);
        }

    }

    public static class SelectViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_title;
        public CheckBox checkBox;

        public SelectViewHolder(View view){
            super(view);
            tv_title = (TextView) view.findViewById(R.id.tv_title);
            checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        }

    }

}
