
package com.likebamboo.activity;

import android.app.Application;

import com.likebamboo.db.SearchDatabaseHelper;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        // 初始化数据库
        SearchDatabaseHelper.initDatabase(getApplicationContext());
    }

}
