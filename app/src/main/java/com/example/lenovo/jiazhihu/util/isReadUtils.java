package com.example.lenovo.jiazhihu.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 用于记录已经浏览过的新闻头
 */
public class isReadUtils {
    private static void putStringToDefault(Context context, String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(key, value).apply();
    }

    private static String getStringFromDefault(Context context, String key, String defValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(key, defValue);
    }
    //检查是否存在
    public static boolean checkRead(Context context, String id){
        String readSequence = getStringFromDefault(context, "read", "");
        if (!readSequence.contains(id)) {
            return false;
        }else
            return true;
    }
    //检查并加入新闻头
    public static boolean checkInsertRead(Context context, String id){
        String readSequence = getStringFromDefault(context, "read", "");
        String[] splits = readSequence.split(",");
        StringBuilder sb = new StringBuilder();
        if (splits.length >= 100) {
            for (int i = 50; i < splits.length; i++) {
                sb.append(splits[i] + ",");
            }
            readSequence = sb.toString();
            putStringToDefault(context, "read", readSequence);
        }
        if (!readSequence.contains(id)) {
            readSequence = readSequence + id + ",";
            putStringToDefault(context, "read", readSequence);
            return false;
        }else
        return true;
    }
}
