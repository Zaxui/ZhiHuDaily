package com.example.lenovo.jiazhihu.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.jiazhihu.others.ItemClickListener;
import com.example.lenovo.jiazhihu.R;
import com.example.lenovo.jiazhihu.adapter.DownLoadedItemAdapter;
import com.example.lenovo.jiazhihu.database.WebCacheDbHelper;
import com.example.lenovo.jiazhihu.model.StoriesEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static android.R.drawable.ic_menu_help;

/**
 * Created by lenovo on 2017/8/4.
 * 用于展示和编辑已下载的新闻条目（我的收藏）
 */

public class DownLoadedActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private TextView mSelectionCount;//展示选中条目数量
    private LinearLayout mLayoutDelete;//用于全选和确认的布局
    private CheckBox mCheckBox;//全选
    private TextView mEdit;//编辑按钮

    private DownLoadedItemAdapter mAdapter;
    private List<StoriesEntity> mStoriesEntityList = new ArrayList<>();
    private WebCacheDbHelper mdbHelper = new WebCacheDbHelper(this, 1);;
    private boolean isSelecting = false;//是否处于编辑状态

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_loaded);
        Toolbar toolbar = (Toolbar) findViewById(R.id.downloaded_toolbar);
        toolbar.setTitle("我的收藏");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //下方布局的初始化
        mLayoutDelete = (LinearLayout) findViewById(R.id.layout_delet_loaded);
        final Animation anim_up = AnimationUtils.loadAnimation(this, R.anim.anim_up);
        mCheckBox = (CheckBox) findViewById(R.id.checkbox_all);
        mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckBox.isChecked()){
                    mAdapter.SelectAll();
                    mSelectionCount.setText(mAdapter.getItemCount() + " 项已选");
                }else {
                    mAdapter.SelectNull();
                    mSelectionCount.setText("0 项已选");
                }
                mAdapter.notifyDataSetChanged();
            }
        });
        Button delete = (Button)findViewById(R.id.button_delet);
        final AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setIcon(ic_menu_help);
        builder.setTitle("删除");
        builder.setMessage("确定要删除选中条目么？");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete(mAdapter.getSelectedSet());
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.getSelectedSet().size() == 0){
                    Toast.makeText(DownLoadedActivity.this, "未选择要删除的条目", Toast.LENGTH_SHORT).show();
                }else {
                    builder.show();
                }

            }
        });

        mEdit = (TextView)findViewById(R.id.tv_edit);
        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelecting){
                    mLayoutDelete.setVisibility(View.GONE);
                    mAdapter.SelectNull();
                    mCheckBox.setChecked(false);
                    isSelecting = false;
                    mEdit.setText("编辑");
                }else {
                    mLayoutDelete.setVisibility(View.VISIBLE);
                    mLayoutDelete.startAnimation(anim_up);
                    isSelecting = true;
                    mEdit.setText("取消");
                }
                mSelectionCount.setText("0 项已选");
                mAdapter.setSelecting(isSelecting);
                mAdapter.notifyDataSetChanged();

            }
        });

        mSelectionCount = (TextView) findViewById(R.id.tv_selected_count);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_down_loaded);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnItemTouchListener(new ItemClickListener(mRecyclerView, new ItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(mAdapter.getSelecting()){
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                    if (checkBox.isChecked()){
                        mAdapter.removeSelectedSet(position);
                        checkBox.setChecked(false);
                    }else {
                        mAdapter.addSelectedSet(position);
                        checkBox.setChecked(true);
                    }
                    mSelectionCount.setText(mAdapter.getSelectedSet().size() + " 项已选");
                }else {
                    startContentActivity(mAdapter.getItem(position));
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
        StoriesEntity t;

        SQLiteDatabase db = mdbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from Cache order by id desc" , null);
        if (cursor.moveToFirst()) {
            do{
                int id = cursor.getInt(cursor.getColumnIndex("newsId"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                t = new StoriesEntity();
                t.setTitle(title);
                t.setId(id);
                mStoriesEntityList.add(t);
            }while(cursor.moveToNext());
            mRecyclerView.setVisibility(View.VISIBLE);
        }else {
            mEdit.setVisibility(View.GONE);
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_empty_loaded);
            linearLayout.setVisibility(View.VISIBLE);
        }
        cursor.close();
        db.close();

        mAdapter = new DownLoadedItemAdapter(mStoriesEntityList, this);
        mRecyclerView.setAdapter(mAdapter);

    }
    //在数据库及当前adpter的数据集中删除选中条目
    private void delete(HashSet<Integer> set){
        mdbHelper = new WebCacheDbHelper(this, 1);
        SQLiteDatabase db = mdbHelper.getReadableDatabase();
        int size = set.size();
        int[] a = new int[size];
        int i = 0;
        for (Integer integer : set) {
            a[i++] = integer;
        }
        //逆序删除
        Arrays.sort(a);
        for (i = size; i > 0; i--){
            db.delete("Cache", "newsId=?", new String[]{mStoriesEntityList.get(a[i-1]).getId()+""});
            mStoriesEntityList.remove(a[i-1]);
        }
        db.close();

        mEdit.setText("编辑");
        isSelecting = false;
        mCheckBox.setChecked(false);
        mLayoutDelete.setVisibility(View.INVISIBLE);
        mAdapter.setSelecting(isSelecting);
        mAdapter.SelectNull();
        mAdapter.notifyDataSetChanged();

        if (mStoriesEntityList.size() == 0){
            mEdit.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_empty_loaded);
            linearLayout.setVisibility(View.VISIBLE);
        }
    }

    private void startContentActivity(StoriesEntity storiesEntity){
        Intent intent = new Intent(this, ContentActivity.class);
        intent.putExtra("entity", storiesEntity);
        startActivity(intent);
    }
}
