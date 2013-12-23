
package com.likebamboo.widget;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.likebamboo.adapter.SearchSuggestAdapter;
import com.likebamboo.autocompandrefresh.R;
import com.likebamboo.db.SearchDbManager;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * <p>
 * 搜索自动提示AutoCompletTvLayout 的封装。
 * <p>
 * 实现AutoCompleteTextView在0输入情况下显示历史搜索提示:
 * <p>
 * 参考项目：http://blog.csdn.net/iamkila/article/details/7230160
 * <p>
 * 实现在有输入的情况下异步从服务器端加载数据
 * <p>
 * 参考项目：http://download.csdn.net/detail/yangzl2008/4494293#comment
 * <p>
 * 实现下拉AutoCompleteTextView的下拉样式的自定义。 其他参考项目：
 * 
 * <pre>
 * http://stackoverflow.com/questions/3613152/autocompletetextview-and-completionhintview
 * 
 * @author likebamboo
 * @create 2013-7-15
 */
public class SearchAutoCompleteTvLayout extends LinearLayout {
    /** 返回按钮 */
    private ImageView mGoBackIv = null;

    /** 输入框 */
    private MyAutoCompleteTextView mSearchKwsAcTv = null;

    /** 搜索按钮 */
    private Button mSearchBt = null;

    /** 搜索数据 */
    private ArrayList<String> datas = new ArrayList<String>();;

    /** Adapter */
    private SearchSuggestAdapter adapter = null;

    public SearchAutoCompleteTvLayout(Context context) {
        super(context);
    }

    public SearchAutoCompleteTvLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        mGoBackIv = (ImageView)findViewById(R.id.search_edit_back_ib);
        mSearchKwsAcTv = (MyAutoCompleteTextView)findViewById(R.id.search_edit_keywords_AcTv);
        mSearchBt = (Button)findViewById(R.id.search_edit_search_tv);
        mSearchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 将关键字保存到数据库中
                SearchDbManager.getInstance()
                        .addHistory(mSearchKwsAcTv.getText().toString().trim());
                Toast.makeText(getContext(), getContext().getString(R.string.add_keywords),
                        Toast.LENGTH_SHORT).show();
            }
        });
        mSearchKwsAcTv.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    // 当输入框数据为空，显示历史搜索数据
                    mSearchKwsAcTv.performClick();
                } else {
                    // 异步加载网络提示数据
                    String newText = s.toString();
                    new GetSuggestKeyWords().execute(newText);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });
        // 初始化历史搜索记录数据与适配器
        initSearchAdapter();
        // 设置最初的适配器
        mSearchKwsAcTv.setAdapter(adapter);
        mSearchKwsAcTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 当输入框数据为空时，初始化历史搜索记录并显示
                if (mSearchKwsAcTv.getText().toString().trim().length() == 0) {
                    // 取得历史搜索数据
                    datas = SearchDbManager.getInstance().getLatestHistory();
                    // 初始化历史搜索记录数据与适配器
                    initSearchAdapter();
                    mSearchKwsAcTv.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                if (!mSearchKwsAcTv.isPopupShowing()) {
                    mSearchKwsAcTv.showDropDown();
                }
            }
        });
    }

    /**
     * 初始化历史搜索记录数据与适配器
     */
    private void initSearchAdapter() {
        if (adapter == null) {
            adapter = new SearchSuggestAdapter(getContext(), datas);
        } else {
            // 设置Adapter的数据源
            adapter.setData(datas);
        }
    }

    /**
     * 设置返回按钮的点击事件
     * 
     * @param goBackListener 回调监听接口
     */
    public void setBackClickListener(View.OnClickListener goBackListener) {
        mGoBackIv.setOnClickListener(goBackListener);
        showView(mGoBackIv);
    }

    /**
     * 设置搜索按钮的点击事件
     * 
     * @param goBackListener 回调监听接口
     */
    public void setSearchClickListener(View.OnClickListener searchListener) {
        mSearchBt.setOnClickListener(searchListener);
        showView(mSearchBt);
    }

    /**
     * 显示某一个控件
     * 
     * @param v
     */
    private void showView(View v) {
        if (v.getVisibility() != View.VISIBLE) {
            v.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 从服务器端获取提示列表数据
     * 
     * @param keywords
     */
    class GetSuggestKeyWords extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // 只有当按关键字匹配的搜索结果和当前输入框中的字符串相同时，才更新下拉提示信息
            if ((!TextUtils.isEmpty(mSearchKwsAcTv.getText()))
                    && mSearchKwsAcTv.getText().toString().equals(result)) {
                initSearchAdapter();
                mSearchKwsAcTv.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        protected String doInBackground(String... key) {
            String newText = key[0];
            newText = newText.trim();
            newText = URLEncoder.encode(newText);
            try {
                HttpClient hClient = new DefaultHttpClient();
                HttpGet hGet = new HttpGet(
                        "http://en.wikipedia.org/w/api.php?action=opensearch&search=" + newText
                                + "&limit=10&namespace=0&format=json");
                ResponseHandler<String> rHandler = new BasicResponseHandler();
                String result = hClient.execute(hGet, rHandler);
                datas = new ArrayList<String>();
                if (TextUtils.isEmpty(result)) {
                    return key[0];
                }
                JSONArray jArray = new JSONArray(result);
                for (int i = 0; i < jArray.getJSONArray(1).length(); i++) {
                    String SuggestKey = jArray.getJSONArray(1).getString(i);
                    datas.add(SuggestKey);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.w("Error", e.getMessage());
            }
            return key[0];
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            if (datas == null) {
                datas = new ArrayList<String>();
            }
        }

    }
}
