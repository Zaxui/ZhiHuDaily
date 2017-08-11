package com.example.lenovo.jiazhihu.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wwjun.wang on 2015/8/19.
 */
public class WebCacheDbHelper extends SQLiteOpenHelper {
    public WebCacheDbHelper(Context context, int version) {
        super(context, "webCache.db", null, version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists Cache (id INTEGER primary key autoincrement," +
                "newsId INTEGER unique,title text,json text)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
