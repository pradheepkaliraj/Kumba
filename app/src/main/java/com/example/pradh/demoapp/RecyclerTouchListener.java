package com.example.pradh.demoapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by pradh on 2/25/2020.
 */

public class RecyclerTouchListener  implements  RecyclerView.OnItemTouchListener{
    private ClickListener clicklistner;
    private GestureDetector gestureDetector;



    public RecyclerTouchListener (Context context, final RecyclerView recyclerView, final ClickListener clickListner) {
        this.clicklistner = clickListner;
        gestureDetector  = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }


            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if(child!=null && clicklistner!=null){
                    clicklistner.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                }
            }
        });
    }


    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        if (child != null && clicklistner != null && gestureDetector.onTouchEvent(e)) {
            clicklistner.onClick(child, rv.getChildAdapterPosition(child));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }



    /** interface class */

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }
}

