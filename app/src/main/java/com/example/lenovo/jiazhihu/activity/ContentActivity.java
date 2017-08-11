package com.example.lenovo.jiazhihu.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.lenovo.jiazhihu.R;
import com.example.lenovo.jiazhihu.database.WebCacheDbHelper;
import com.example.lenovo.jiazhihu.model.Content;
import com.example.lenovo.jiazhihu.model.StoriesEntity;
import com.example.lenovo.jiazhihu.util.ImageLoadUtil;
import com.example.lenovo.jiazhihu.util.OkHttpUtil;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Request;

/**
 * 用于展示新闻内容
 */

public class ContentActivity extends AppCompatActivity {
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private Button bt_comment;
    private Button bt_download;

    private StoriesEntity entity;
    private ImageView iv;//标题栏图片
    private WebCacheDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        entity = (StoriesEntity) getIntent().getSerializableExtra("entity");
        iv = (ImageView) findViewById(R.id.iv);
        //下载与评论按钮的初始化
        bt_comment = (Button) findViewById(R.id.btn_comment);
        bt_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCommentActivity();
            }
        });
        bt_download = (Button) findViewById(R.id.btn_download);
        bt_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downLoadStory();
            }
        });
        //配置滑动标题栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        CollapsingToolbarLayout mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        if (entity.getTitle().length() > 9){
            mCollapsingToolbarLayout.setTitle(entity.getTitle().substring(0, 9)+"…");
        }else {
            mCollapsingToolbarLayout.setTitle(entity.getTitle());
        }

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 开启DOM storage API 功能
        mWebView.getSettings().setDomStorageEnabled(true);
        // 开启database storage API功能
        mWebView.getSettings().setDatabaseEnabled(true);
        // 开启Application Cache功能
        mWebView.getSettings().setAppCacheEnabled(true);
        //使WebView能够在自身响应超链接点击，而不必打开浏览器
        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url); return true;
            }
            //上述方法已被android7.0废弃，但能兼容旧版本api。新版本方法如下，需要api21。
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                view.loadUrl(request.getUrl().toString());
//                return true;
//            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.progress_content);

        OkHttpUtil.getAsyncStringByNetCondition("http://news-at.zhihu.com/api/4/news/"+ entity.getId(),
                new OkHttpUtil.DataCallBack() {
                    @Override
                    public void requestFailure(Request request, IOException e) {
                        readStoty();
                    }
                    @Override
                    public void requestSuccess(String result) throws Exception {
                        if (!result.equals(""))
                            parseJson(result);
                        else readStoty();
                    }
                });
    }

    public void startCommentActivity(){
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra("id", Integer.toString(entity.getId()));
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }
    //解析并加载
    private void parseJson(String responseString) {
        Gson gson = new Gson();
        Content content = gson.fromJson(responseString, Content.class);
        //离线或无首部图片则加载默认图片
        if (content.getImage() != null) {
            iv.setTag(content.getImage());
            ImageLoadUtil.getInstance(1, ImageLoadUtil.Type.LIFO).loadImage(content.getImage()
                    , iv, true);
        }else {
            iv.setImageResource(R.drawable.bk_header);
        }
        //加载css样式表
        String css = "<link rel=\"stylesheet\" href=\"file:///android_asset/css/news.css\" type=\"text/css\">";
        String html = "<html><head>" + css + "</head><body>" + content.getBody() + "</body></html>";
        html = html.replace("<div class=\"img-place-holder\">", "");
        mProgressBar.setVisibility(View.GONE);
        mWebView.loadDataWithBaseURL("x-data://base", html, "text/html", "UTF-8", null);
    }
    //点击下载时，再一次发出请求
    public void downLoadStory(){
        OkHttpUtil.getAsyncStringByNetCondition("http://news-at.zhihu.com/api/4/news/"+ entity.getId(),
                new OkHttpUtil.DataCallBack() {
                    @Override
                    public void requestFailure(Request request, IOException e) {
                        Toast.makeText(getApplicationContext(), "网络状况不佳，保存失败", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void requestSuccess(String result) throws Exception {
                        if (!result.equals(""))
                            saveStoty(result);
                        else Toast.makeText(getApplicationContext(), "网络状况不佳，保存失败", Toast.LENGTH_SHORT).show();

                    }
                });
    }
    //保存当前文章到数据库（我的收藏）
    public void saveStoty(String string){
        dbHelper = new WebCacheDbHelper(this, 1);
        SQLiteDatabase db =dbHelper.getWritableDatabase();
        Log.i("dbHelper","db.save");
        string = string.replaceAll("'", "''");
        String title = entity.getTitle();
        db.execSQL("replace into Cache(newsId,title,json) values(" + entity.getId() + ",'"+
                title +"','" + string + "')");
        db.close();
        Toast.makeText(this, "文章已保存", Toast.LENGTH_SHORT).show();
    }
    //从数据库加载离线文章
    public void readStoty(){
        bt_comment.setVisibility(View.GONE);
        bt_download.setVisibility(View.GONE);

        dbHelper = new WebCacheDbHelper(this, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from Cache where newsId = " + entity.getId(), null);
        if (cursor.moveToFirst()) {
            String json = cursor.getString(cursor.getColumnIndex("json"));
            parseJson(json);
            Toast.makeText(this, "加载离线文章", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "文章加载失败，请检查网络", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        db.close();
    }
}
