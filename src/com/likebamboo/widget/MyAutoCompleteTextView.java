
package com.likebamboo.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.AutoCompleteTextView;

/**
 * <p>
 * 自定义AutoCompleteTextView，
 * <p>
 * 支持在0输入下，显示提示信息
 * <p>
 * http://blog.csdn.net/conant1989/article/details/7357647
 * <p>
 * http://blog.sina.com.cn/s/blog_54109a5801012pmi.html
 * 
 * @author likebamboo
 * @create 2013-07-15
 */
public class MyAutoCompleteTextView extends AutoCompleteTextView {

    public MyAutoCompleteTextView(Context context) {
        super(context);
    }

    public MyAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        performFiltering(getText(), KeyEvent.KEYCODE_UNKNOWN);
    }
}
