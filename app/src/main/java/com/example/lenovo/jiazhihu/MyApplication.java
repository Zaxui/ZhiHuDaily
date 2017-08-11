package com.example.lenovo.jiazhihu;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.example.lenovo.jiazhihu.util.OkHttpUtil;
//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;

/**
 * Created by lenovo on 2017/7/30.
 * 获取全局Context
 */

public class MyApplication extends Application {
    private static Context context;

    //private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();

        //refWatcher = LeakCanary.install(this);
    }
    public static Context getMyapplicationContext(){
        return context;
    }
    public static void getNetCondition(){
        if (!OkHttpUtil.isNetworkConnected())
            Toast.makeText(context, "网络状况不佳", Toast.LENGTH_SHORT).show();
    }

//    public static RefWatcher getRefWatcher(Context context) {
//        MyApplication application = (MyApplication) context.getApplicationContext();
//        return application.refWatcher;
//    }

}
