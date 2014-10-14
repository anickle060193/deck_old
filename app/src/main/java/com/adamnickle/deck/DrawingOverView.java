package com.adamnickle.deck;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class DrawingOverView extends View
{
    private int mX;
    private int mY;

    public DrawingOverView( Context context )
    {
        this( context, null );
    }

    public DrawingOverView( Context context, AttributeSet attrs )
    {
        this( context, attrs, 0 );
    }

    public DrawingOverView( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );

        mX = 0;
        mY = 0;
    }

    @Override
    protected void onDraw( Canvas canvas )
    {
        Log.d( "DrawingOverView", "onDraw" );
        super.onDraw( canvas );
    }

    @Override
    public boolean onTouchEvent( MotionEvent event )
    {
        Log.d( "DrawingOverView", "onTouchEvent" );
        mX = (int) event.getX();
        mY = (int) event.getY();

        switch( event.getAction() )
        {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                break;

            default:
                return false;
        }
        invalidate();
        return true;
    }
}
