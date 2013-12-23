
package com.likebamboo.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;

public class SearchDbManager {

    private static final String TABLE_SEARCH_HISTORY = "search_history";

    private static final String KEY_KEYWORDS = "keywords";

    private static final String KEY_TIME = "time";
    
    private static final int DEFAUT_LATEST_COUNT = 5;

    private SQLiteDatabase mDB = null;

    private static SearchDbManager mInstance = null;

    public static SearchDbManager getInstance() {
        if (mInstance == null) {
            mInstance = new SearchDbManager();
        }

        return mInstance;
    }
    
    /**
     * 获取最近的搜索记录（默认前5条）
     * 
     * @return 搜索记录列表
     */
    public ArrayList<String> getLatestHistory() {
        return getLatestHistory(DEFAUT_LATEST_COUNT);
    }

    /**
     * 获取最近的搜索记录
     * 
     * @param count 最近记录条数
     * @return 搜索记录列表
     */
    public ArrayList<String> getLatestHistory(int count) {
        ArrayList<String> historyList = new ArrayList<String>();
        if (count < 1) {
            count = DEFAUT_LATEST_COUNT;
        }
        Cursor cursor = null;
        try {
            mDB = SearchDatabaseHelper.getInstance();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ").append(TABLE_SEARCH_HISTORY);
            sb.append(" ORDER BY ").append(KEY_TIME).append(" DESC");
            sb.append(" LIMIT ").append(count).append(";");
            String sql = sb.toString();
            cursor = mDB.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                String keywords = cursor.getString(cursor.getColumnIndex(KEY_KEYWORDS));
                if(!TextUtils.isEmpty(keywords)){
                historyList.add(keywords);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return historyList;
    }

    /**
     * 添加搜索记录
     * 
     * @param keywords 搜索关键字
     * @return 是否添加成功
     */
    public boolean addHistory(String keywords) {
        if (TextUtils.isEmpty(keywords)) {
            return false;
        }
        try {
            mDB = SearchDatabaseHelper.getInstance();
            mDB.beginTransaction();
            long currentTime = System.currentTimeMillis();
            ContentValues values = new ContentValues();
            values.put(KEY_KEYWORDS, keywords);
            values.put(KEY_TIME, currentTime);
            
            String[] selectionArgs = { keywords };
            mDB.delete(TABLE_SEARCH_HISTORY, KEY_KEYWORDS + " = ?", selectionArgs);
            mDB.insert(TABLE_SEARCH_HISTORY, null, values);
            mDB.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            if (mDB.inTransaction()) {
                mDB.endTransaction();
            }
            return false;
        } finally {
            if (mDB.inTransaction()) {
                mDB.endTransaction();
            }
        }
        return true;
    }
    
       
    /**
     * 清空搜索历史记录
     * 
     */
    public void clearHistory() {
        try {
            mDB = SearchDatabaseHelper.getInstance();
            mDB.delete(TABLE_SEARCH_HISTORY, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }
}
