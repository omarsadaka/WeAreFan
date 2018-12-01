package com.organizers_group.stadfm.Utils;


import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;

public abstract class EndlessNestedScrollViewListener extends  NestedScrollView {

    OnMyScrollChangeListener myScrollChangeListener;

    public EndlessNestedScrollViewListener(Context context) {
        super(context);
    }

    public EndlessNestedScrollViewListener(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EndlessNestedScrollViewListener(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (myScrollChangeListener!=null)
        {
            if (t>oldt)
            {
                myScrollChangeListener.onScrollDown();
            }
            else if (t<oldt){
                myScrollChangeListener.onScrollUp();
            }
            View view = (View) getChildAt(getChildCount()-1);
            int diff = (view.getBottom()-(getHeight()+getScrollY()));
            if (diff == 0 ) {
                myScrollChangeListener.onBottomReached();
            }
        }
    }

    public OnMyScrollChangeListener getMyScrollChangeListener() {
        return myScrollChangeListener;
    }

    public void setMyScrollChangeListener(OnMyScrollChangeListener myScrollChangeListener) {
        this.myScrollChangeListener = myScrollChangeListener;
    }

    public interface OnMyScrollChangeListener
    {
        public void onScrollUp();
        public void onScrollDown();
        public void onBottomReached();
    }
}