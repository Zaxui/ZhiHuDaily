package com.example.lenovo.jiazhihu.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.lenovo.jiazhihu.R;
import com.example.lenovo.jiazhihu.fragment.MainFragment;
import com.example.lenovo.jiazhihu.fragment.NewsFragment;
import com.example.lenovo.jiazhihu.util.OkHttpUtil;

import static android.R.drawable.ic_dialog_email;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MainFragment mMainFragment;//展示首页
    private NewsFragment mNewsFragment;//展示其他日报
    private boolean isMain = true;//当前是否加载知乎日报首页，否则加载其他日报
    private long firstTime;//用于退出计时

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.downloaded_toolbar);
        toolbar.setTitle("假·知乎日报");
        setSupportActionBar(toolbar);

        replaceFragment();
        //为fab添加点击事件实现返回顶部
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMain){
                    mMainFragment.scroolToTop();
                }else {
                    mNewsFragment.scroolToTop();
                }
            }
        });
        //侧滑导航页加载
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_1);
        navigationView.setNavigationItemSelectedListener(this);
        //为了显示缓存使用量
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                String size = convertFileSize(OkHttpUtil.getInstance().getCacheSize());
                navigationView.getMenu().findItem(R.id.disc_cache_size).setTitle("当前使用缓存： " + size);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.SwipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //设置监听实现下拉刷新
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isMain){
                    mMainFragment.refreshData();
                }else {
                    mNewsFragment.refreshData();
                }
                //结束后停止刷新
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void replaceFragment() {
        if (isMain){
            if (mMainFragment == null)
            mMainFragment = new MainFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main_frame_layout,
                    mMainFragment, "latest").commit();
        }else {
            if (mNewsFragment == null)
            mNewsFragment = new NewsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main_frame_layout,
                    mNewsFragment, "news").commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            final AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setIcon(ic_dialog_email);
            builder.setMessage("如有交流意向或涉及侵权请联系我:\nzhang.xui@foxmail.com");
            builder.setPositiveButton("复制邮箱地址", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ClipboardManager myClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    ClipData myClip;
                    myClip = ClipData.newPlainText("email", "zhang.xui@foxmail.com");
                    myClipboard.setPrimaryClip(myClip);
                    Toast.makeText(MainActivity.this, "已复制邮箱地址到剪贴板", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }
    //侧边导航栏设置
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_1) {
            // Handle the camera action
            isMain = true;
            replaceFragment();
            mMainFragment.refreshData();
        } else if (id == R.id.nav_2) {
            loadNewsFragment("2");
        } else if (id == R.id.nav_3) {
            loadNewsFragment("3");
        } else if (id == R.id.nav_4) {
            loadNewsFragment("4");
        } else if (id == R.id.nav_5) {
            loadNewsFragment("5");
        } else if (id == R.id.nav_6) {
            loadNewsFragment("6");
        } else if (id == R.id.nav_7) {
            loadNewsFragment("7");
        }else if (id == R.id.nav_8) {
            loadNewsFragment("8");
        } else if (id == R.id.nav_9) {
            loadNewsFragment("9");
        } else if (id == R.id.nav_10) {
            loadNewsFragment("10");
        }else if (id == R.id.nav_11) {
            loadNewsFragment("11");
        } else if (id == R.id.nav_12) {
            loadNewsFragment("12");
        } else if (id == R.id.nav_13) {
            loadNewsFragment("13");
        }else
        if (id == R.id.nav_downloaded) {
            Intent intent = new Intent(this, DownLoadedActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in,
                    android.R.anim.fade_out);
        } else if (id == R.id.nav_cache_clear) {
            OkHttpUtil.getInstance().clearCache();
            Toast.makeText(this, "缓存已清空", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //传递日报id 加载非主页日报
    private void loadNewsFragment(String s){
        isMain = false;
        replaceFragment();
        mNewsFragment.setNewsType(s);
        mNewsFragment.refreshData();
    }
    /**
     * long转文件单位大小
     * @param size
     * @return
     */
    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        //%.2f 即是保留两位小数的浮点数，后面跟上对应单位就可以了，不得不说java很方便
        if (size >= gb) {
            return String.format("%.2f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            //如果大于100MB就不用保留小数位啦
            return String.format(f > 100 ? "%.0f MB" : "%.2f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            //如果大于100kB就不用保留小数位了
            return String.format(f > 100 ? "%.0f KB" : "%.2f KB", f);
        } else
            return String.format("%d B", size);
    }
}
