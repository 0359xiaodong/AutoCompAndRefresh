
package com.likebamboo.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SearchDatabaseHelper {

    private static final String DATABASE_NAME = "search.db";

    private static final String TAG = "CarDatabaseHelper";

    private static final int DATABASE_VERSION = 1;

    private static SQLiteDatabase db;

    private static Context context;

    private static SQLiteDatabase readDb;

    /** 创建搜索历史记录表 */
    private static final String SEARCH_HISTORY_TABLE_SQL_CREATOR = "CREATE TABLE IF NOT EXISTS [search_history] ("
            + " [keywords] text NOT NULL, " + " [time] integer NOT NULL );";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createDatabase(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqlitedatabase, int oldVersion, int newVersion) {
            Log.d(TAG, "update database");
        }

        private void createDatabase(SQLiteDatabase db) {
            Log.d(TAG, "create database");
            db.execSQL(SEARCH_HISTORY_TABLE_SQL_CREATOR);
        }
    }

    /**
     * 判断某张表是否存在
     * 
     * @param tableName 表名
     * @return
     */
    @SuppressWarnings("unused")
    private static boolean tableIsExist(SQLiteDatabase sqlitedatabase, String tableName) {
        boolean result = false;
        if (tableName == null || tableName.trim().length() < 1 || sqlitedatabase == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select * from " + tableName.trim();
            cursor = sqlitedatabase.rawQuery(sql, null);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    private SearchDatabaseHelper() {

    }

    /**
     * 获取一个数据库连接
     * 
     * @return
     */
    public static SQLiteDatabase getInstance() {
        Data data = MyThreadLocal.get();
        if (data == null) {
            data = new Data();
        }
        if (!data.isUseNewConnected) { // 如果无需创建一个新连接，就使用现有的全局数据库连接
            if (SearchDatabaseHelper.db == null || !SearchDatabaseHelper.db.isOpen()) {
                DatabaseHelper mOpenHelper = new DatabaseHelper(context);
                SearchDatabaseHelper.db = mOpenHelper.getWritableDatabase();
            }
            return SearchDatabaseHelper.db;
        } else {
            if (data.db == null || !data.db.isOpen()) {
                DatabaseHelper openHelper = new DatabaseHelper(context);
                data.db = openHelper.getWritableDatabase();
                MyThreadLocal.set(data);
            }
            return data.db;
        }
    }

    public static SQLiteDatabase getReadableDatabase() {
        if (readDb == null || !readDb.isOpen()) {
            DatabaseHelper openHelper = new DatabaseHelper(context);
            readDb = openHelper.getReadableDatabase();
        }
        return readDb;
    }

    public static void initDatabase(Context context) {
        SearchDatabaseHelper.context = context;
        if (SearchDatabaseHelper.db == null || !SearchDatabaseHelper.db.isOpen()) {
            DatabaseHelper OpenHelper = new DatabaseHelper(context);
            SearchDatabaseHelper.db = OpenHelper.getWritableDatabase();
        }
    }

    public static void set(boolean isUseNewConnected) {
        if (MyThreadLocal.get() == null) {
            Data data = new Data();
            data.isUseNewConnected = isUseNewConnected;
            MyThreadLocal.set(data);
        }

        Data data = MyThreadLocal.get();
        data.isUseNewConnected = isUseNewConnected;
        MyThreadLocal.set(data);
    }

    public static void closeThreadDb() {
        Data data = MyThreadLocal.get();
        if (data != null && data.db != null && data.db.isOpen())
            data.db.close();
    }

    public static void close() {
        if (SearchDatabaseHelper.db != null && SearchDatabaseHelper.db.isOpen()) {
            SearchDatabaseHelper.db.close();
        }
    }

    private static class MyThreadLocal {

        private static ThreadLocal<Data> tLocal = new ThreadLocal<Data>();

        public static void set(Data i) {
            tLocal.set(i);
        }

        public static Data get() {
            return tLocal.get();
        }
    }

    private static class Data {
        public boolean isUseNewConnected = false;

        public SQLiteDatabase db = null;
    }

}
