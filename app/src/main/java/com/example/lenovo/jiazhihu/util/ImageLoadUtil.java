package com.example.lenovo.jiazhihu.util;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.lenovo.jiazhihu.MyApplication;
import com.squareup.leakcanary.RefWatcher;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import okhttp3.Request;

/**
 * Created by lenovo on 2017/7/31.
 * 来自鸿洋
 * 该加载类实现网络请求，内存缓存，图片加载
 * 硬盘级缓存交给Okhttp实现
 */

public class ImageLoadUtil {
    private static ImageLoadUtil mInstance;

    /**
     * 图片的内存级缓存
     */
    private LruCache<String, Bitmap> mLruCache;
    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    private static final int DEAFULT_THREAD_COUNT = 1;
    /**
     * 队列的调度方式
     */
    private Type mType = Type.LIFO;
    /**
     * 任务队列
     */
    private LinkedList<Runnable> mTaskQueue;
    /**
     * 后台轮询线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;
    /**
     * UI线程中的Handler
     */
    private Handler mUIHandler;

    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    private Semaphore mSemaphoreThreadPool;

    //private boolean isDiskCacheEnable = true;

    private static final String TAG = "ImageLoader";

    public enum Type
    {
        FIFO, LIFO;
    }

    /**
     * 单例
     * @param threadCount
     * @param type
     */
    private ImageLoadUtil(int threadCount, Type type)
    {
        init(threadCount, type);
    }
    public static ImageLoadUtil getInstance(int threadCount, Type type)
    {
        if (mInstance == null)
        {
            synchronized (ImageLoadUtil.class)
            {
                if (mInstance == null)
                {
                    mInstance = new ImageLoadUtil(threadCount, type);
                }
            }
        }
        return mInstance;
    }
    /**
     * 初始化
     *
     * @param threadCount
     * @param type
     */
    private void init(int threadCount, Type type)
    {
        initBackThread();

        // 获取我们应用的最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 10;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory)
        {
            @Override
            protected int sizeOf(String key, Bitmap value)
            {
                return value.getRowBytes() * value.getHeight();
            }

        };

        // 创建线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTaskQueue = new LinkedList<Runnable>();
        mType = type;
        mSemaphoreThreadPool = new Semaphore(threadCount);
    }

    /**
     * 初始化后台轮询线程
     */
    private void initBackThread()
    {
        // 后台轮询线程
        mPoolThread = new Thread()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                mPoolThreadHandler = new Handler()
                {
                    @Override
                    public void handleMessage(Message msg)
                    {
                        // 线程池去取出一个任务进行执行
                        mThreadPool.execute(getTask());
                        try
                        {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e)
                        {
                        }
                    }
                };
                // 释放一个信号量
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            };
        };

        mPoolThread.start();
    }
    /**
     * 根据path为imageview设置图片
     *
     * @param path
     * @param imageView
     */
    public void loadImage(final String path, final ImageView imageView,
                          final boolean isFromNet)
    {
        imageView.setTag(path);
        if (mUIHandler == null)
        {
            mUIHandler = new Handler()
            {
                public void handleMessage(Message msg)
                {
                    // 获取得到图片，为imageview回调设置图片
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    Bitmap bm = holder.bitmap;
                    ImageView imageview = holder.imageView;
                    String path = holder.path;
                    // 将path与getTag存储路径进行比较
                    if (imageview.getTag().toString().equals(path))
                    {
                        imageview.setImageBitmap(bm);
                    }
                };
            };
        }

        // 根据path在缓存中获取bitmap
        Bitmap bm = getBitmapFromLruCache(path);

        if (bm != null)
        {
            refreashBitmap(path, imageView, bm);
        } else
        {
            addTask(buildTask(path, imageView, isFromNet));
        }
    }

    private synchronized void addTask(Runnable runnable)
    {
        mTaskQueue.add(runnable);
        // if(mPoolThreadHandler==null)wait();
        try
        {
            if (mPoolThreadHandler == null)
                mSemaphorePoolThreadHandler.acquire();
        } catch (InterruptedException e)
        {
        }
        mPoolThreadHandler.sendEmptyMessage(0x110);
    }

    /**
     * 从任务队列取出一个方法
     *
     * @return
     */
    private Runnable getTask()
    {
        if (mType == Type.FIFO)
        {
            return mTaskQueue.removeFirst();
        } else if (mType == Type.LIFO)
        {
            return mTaskQueue.removeLast();
        }
        return null;
    }
    /**
     * 根据传入的参数，新建一个任务
     *
     * @param path
     * @param imageView
     * @param isFromNet 是否为网络加载
     * @return
     */
    private Runnable buildTask(final String path, final ImageView imageView,
                               final boolean isFromNet)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                Bitmap bm = null;
                if (isFromNet)
                {
                    //网络请求与硬盘级缓存交给okhttp
                    OkHttpUtil.getAsyncBitmap(path, new OkHttpUtil.BitmapCallBack() {
                        @Override
                        public void requestFailure(Request request, IOException e) {

                        }

                        @Override
                        public void requestSuccess(Bitmap result) throws Exception {
                            addBitmapToLruCache(path, result);
                            refreashBitmap(path, imageView, result);
                        }
                    });
                } else
                {
                    //这里写本地加载时要做的内容
                }
                mSemaphoreThreadPool.release();
            }


        };
    }
/**
 * 本地加载/压缩
 */
//    private Bitmap loadImageFromLocal(final String path,
//                                      final ImageView imageView)
//    {
//        Bitmap bm;
//        // 加载图片
//        // 图片的压缩
//        // 1、获得图片需要显示的大小
//        ImageSize imageSize = ImageSizeUtil.getImageViewSize(imageView);
//        // 2、压缩图片
//        bm = decodeSampledBitmapFromPath(path, imageSize.width,
//                imageSize.height);
//        return bm;
//    }

    /**
     * 将图片加入LruCache
     * @param path
     * @param bm
     */
    protected void addBitmapToLruCache(String path, Bitmap bm)
    {
        if (getBitmapFromLruCache(path) == null)
        {
            if (bm != null)
                mLruCache.put(path, bm);
        }
    }

    /**
     * 绑定图片与视图
     * @param path
     * @param imageView
     * @param bm
     */
    private void refreashBitmap(final String path, final ImageView imageView,
                                Bitmap bm)
    {
        Message message = Message.obtain();
        ImgBeanHolder holder = new ImgBeanHolder();
        holder.bitmap = bm;
        holder.path = path;
        holder.imageView = imageView;
        message.obj = holder;
        mUIHandler.sendMessage(message);
    }

    /**
     * 从LruCache中获取一张图片，如果不存在就返回null。
     */
    private Bitmap getBitmapFromLruCache(String key) {
        return mLruCache.get(key);
    }

    private class ImgBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }
}
