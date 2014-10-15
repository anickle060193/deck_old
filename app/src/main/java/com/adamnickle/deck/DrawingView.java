package com.adamnickle.deck;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class DrawingView extends View
{
    private static final int DRAWING_STROKE_WIDTH = 10;
    private static final int ERASER_STROKE_WIDTH = 80;

    private Bitmap mBitmap;
    private final Path mDrawPath;
    private final Paint mDrawingPaint;
    private final Paint mCanvasPaint;
    private Canvas mCanvas;
    private RectF mPathBounds;
    private boolean mEraser;
    private boolean mDrawEraser;
    private int mX;
    private int mY;

    public DrawingView( Context context )
    {
        this( context, null );
    }

    public DrawingView( Context context, AttributeSet attrs )
    {
        this( context, attrs, 0 );
    }

    public DrawingView( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );

        mDrawPath = new Path();
        mDrawingPaint = new Paint();
        mDrawingPaint.setColor( Color.BLACK );
        mDrawingPaint.setAntiAlias( true );
        mDrawingPaint.setStrokeWidth( DRAWING_STROKE_WIDTH );
        mDrawingPaint.setStyle( Paint.Style.STROKE );
        mDrawingPaint.setStrokeJoin( Paint.Join.ROUND );
        mDrawingPaint.setStrokeCap( Paint.Cap.ROUND );

        mCanvasPaint = new Paint( Paint.DITHER_FLAG );
        mPathBounds = new RectF();
        mEraser = false;
        mX = 0;
        mY = 0;
        mDrawEraser = false;
    }

    public void setBitmap( Bitmap bitmap )
    {
        mBitmap = bitmap;
        mCanvas = new Canvas( mBitmap );
    }

    public void toggleEraser()
    {
        if( mEraser )
        {
            mDrawingPaint.setColor( Color.BLACK );
            mDrawingPaint.setStrokeWidth( DRAWING_STROKE_WIDTH );
        }
        else
        {
            mDrawingPaint.setColor( getResources().getColor( R.color.DrawingBackground ) );
            mDrawingPaint.setStrokeWidth( ERASER_STROKE_WIDTH );
        }
        mEraser = !mEraser;
    }

    public boolean isEraser()
    {
        return mEraser;
    }

    public void clearDrawing()
    {
        if( mCanvas != null )
        {
            mCanvas.drawColor( getResources().getColor( R.color.DrawingBackground ) );
        }
    }

    @Override
    protected void onDraw( Canvas canvas )
    {
        if( mBitmap != null )
        {
            canvas.drawBitmap( mBitmap, 0, 0, mCanvasPaint );
            canvas.drawPath( mDrawPath, mDrawingPaint );
            if( mDrawEraser )
            {
                final int color = mDrawingPaint.getColor();
                mDrawingPaint.setColor( getResources().getColor( R.color.PaleGreen ) );
                canvas.drawPoint( mX, mY, mDrawingPaint );
                mDrawingPaint.setColor( color );
            }
        }
    }

    @Override
    public boolean onTouchEvent( MotionEvent event )
    {
        if( mCanvas == null )
        {
            return true;
        }

        mX = (int) event.getX();
        mY = (int) event.getY();

        switch( event.getAction() )
        {
            case MotionEvent.ACTION_DOWN:
                mDrawEraser = mEraser;
                mDrawPath.moveTo( mX, mY );
                break;

            case MotionEvent.ACTION_MOVE:
                mDrawPath.lineTo( mX, mY );
                break;

            case MotionEvent.ACTION_UP:
                mDrawPath.computeBounds( mPathBounds, false );
                if( mPathBounds.width() < 10 || mPathBounds.height() < 10 )
                {
                    mCanvas.drawPoint( mPathBounds.left, mPathBounds.top, mDrawingPaint );
                }
                mCanvas.drawPath( mDrawPath, mDrawingPaint );
                mDrawPath.reset();
                mDrawEraser = false;
                break;

            default:
                return false;
        }
        invalidate();
        return true;
    }
}
