package com.example.lenovo.jiazhihu.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.lenovo.jiazhihu.MyApplication.getMyapplicationContext;

/**
 * Created by lenovo on 2017/7/29.
 * Okhttp的简单封装，实现网络请求与硬盘级缓存
 * 仅根据项目需要封装了异步get方法
 */

public class OkHttpUtil {

    private static OkHttpUtil mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mHandler;
    private Context mContext;

    private static final long cacheSize = 1024*1024*10;//缓存文件最大限制大小10M
    private  Cache cache;

    private OkHttpUtil(){
        mOkHttpClient = new OkHttpClient();
        //使用ApplicationContext防止内存泄漏
        mContext  = getMyapplicationContext();
        cache = new Cache(
                new File(mContext.getCacheDir().toString()), cacheSize);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.SECONDS);  //设置连接超时时间
        builder.writeTimeout(5, TimeUnit.SECONDS);//设置写入超时时间
        builder.readTimeout(5, TimeUnit.SECONDS);//设置读取数据超时时间
        builder.retryOnConnectionFailure(false);//设置不进行连接失败重试
        builder.cache(cache);
        builder.addNetworkInterceptor(new CacheInterceptor());//知乎不支持缓存，设置拦截器
        mOkHttpClient = builder.build();

        mHandler = new Handler(Looper.getMainLooper());
    }
    //获取已用缓存大小
    public long getCacheSize(){
        long t = -1;
        try {
            t = cache.size();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return t;
    }
    //清除缓存
    public void clearCache(){
        try {
            cache.evictAll();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     *单例模式获取实例
     * */
    public static OkHttpUtil getInstance(){
        if(mInstance == null){
            synchronized (OkHttpUtil.class){
                if (mInstance == null){
                    mInstance = new OkHttpUtil();
                }
            }
        }
        return mInstance;
    }
    /**
     * String类型对外异步get
     */
    public static void getAsyncString(String url, DataCallBack dataCallBack){
        getInstance()._getAsyncString(url, dataCallBack);
    }
    /**
     * String类型对外异步get 设置缓存读取方式
     */
    public static void getAsyncString(String url, DataCallBack dataCallBack, CacheControl model){
        getInstance()._getAsyncString(url, dataCallBack, model);
    }
    /**
     * String类型内部异步get
     */
    private void _getAsyncString(String url, final DataCallBack dataCallBack){
        final Request request = new Request
                .Builder()
                .url(url).build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                deliverDateFailure(request, e, dataCallBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = null;
                try{
                    result = response.body().string();
                }catch (IOException e){
                    deliverDateFailure(request, e, dataCallBack);
                }
                deliverDateSuccess(result, dataCallBack);
            }
        });
    }
    /**
     * String类型内部异步get 设置缓存读取方式
     */
    private void _getAsyncString(String url, final DataCallBack dataCallBack, CacheControl model){
        final Request request = new Request
                .Builder().cacheControl(model)
                .url(url).build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                deliverDateFailure(request, e, dataCallBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = null;
                try{
                    result = response.body().string();
                }catch (IOException e){
                    deliverDateFailure(request, e, dataCallBack);
                }
                deliverDateSuccess(result, dataCallBack);
            }
        });
    }

    /**
     * 数据分发失败调用
     */
    private void deliverDateFailure(final Request request, final IOException e, final DataCallBack dataCallBack){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (dataCallBack != null){
                    dataCallBack.requestFailure(request,e);
                }
            }
        });
    }
    /**
     * 数据分发成功调用
     */
    private void deliverDateSuccess(final String result, final DataCallBack dataCallBack){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (dataCallBack != null){
                    try {
                        dataCallBack.requestSuccess(result);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    /**
     * Bitmap类型对外异步get
     */
    public static void getAsyncBitmap(String url, BitmapCallBack bitmapCallBack){
        getInstance()._getAsyncBitmap(url, bitmapCallBack);
    }
    /**
     * Bitmap类型内部异步get
     */
    private void _getAsyncBitmap(String url, final BitmapCallBack bitmapCallBack){
        final Request request = new Request
                .Builder().cacheControl(new CacheControl.Builder().maxAge(3600*24*7, TimeUnit.SECONDS).build())
                .url(url).build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                deliverBitmapFailure(request, e, bitmapCallBack);
            }

            @Override
            public void onResponse(Call call, Response response) {
                Bitmap result;

                    result = BitmapFactory.decodeStream(response.body().byteStream());

                deliverBitmapSuccess(result, bitmapCallBack);
            }
        });
    }
    /**
     * 图片分发失败调用
     */
    private void deliverBitmapFailure(final Request request, final IOException e, final BitmapCallBack bitmapCallBack){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (bitmapCallBack != null){
                    bitmapCallBack.requestFailure(request,e);
                }
            }
        });
    }
    /**
     * 图片分发成功调用
     */
    private void deliverBitmapSuccess(final Bitmap result, final BitmapCallBack bitmapCallBack){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (bitmapCallBack != null){
                    try {
                        bitmapCallBack.requestSuccess(result);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    /**
     * 数据回调接口
     */
    public interface DataCallBack {
        void requestFailure(Request request, IOException e);
        void requestSuccess(String result) throws Exception;
    }
    /**
     * 图片回调接口
     */
    public interface BitmapCallBack {
        void requestFailure(Request request, IOException e);
        void requestSuccess(Bitmap result) throws Exception;
    }
    /**
     *服务器不支持缓存的情况下使用拦截器
     */
    public class CacheInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            Response response1 = response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    //cache for 7 days
                    .header("Cache-Control", "max-age=" + 3600 * 24 * 7)
                    .build();
            return response1;
        }
    }
    //检查网络状态
    public static boolean isNetworkConnected() {
        Context context = getMyapplicationContext();
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
    //String类型异步get 根据网络状态选择cache模式
    public static void getAsyncStringByNetCondition(String url, final DataCallBack dataCallBack){
        if(isNetworkConnected()){
            getAsyncString(url, dataCallBack, CacheControl.FORCE_NETWORK);
        }else{
            getAsyncString(url, dataCallBack, CacheControl.FORCE_CACHE);
        }
    }
}
