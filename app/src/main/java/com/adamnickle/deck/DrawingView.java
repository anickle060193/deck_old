package com.adamnickle.deck;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class DrawingView extends View
{
    private static final int DRAWING_STROKE_WIDTH = 10;
    private static final int ERASER_STROKE_WIDTH = 80;

    private Bitmap mPortBitmap;
    private Bitmap mLandBitmap;
    private final Path mDrawPath;
    private final Paint mDrawingPaint;
    private final Paint mCanvasPaint;
    private Canvas mPortCanvas;
    private Canvas mLandCanvas;
    private RectF mPathBounds;
    private boolean mEraser;
    private boolean mDrawEraser;
    private int mX;
    private int mY;
    private Rect mDrawingBounds;
    private boolean mPortrait;

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

        mPortCanvas = new Canvas();
        mLandCanvas = new Canvas();
        mCanvasPaint = new Paint( Paint.DITHER_FLAG );
        mPathBounds = new RectF();
        mEraser = false;
        mX = 0;
        mY = 0;
        mDrawEraser = false;
    }

    public void setBitmap( boolean portrait, Bitmap bitmap )
    {
        if( portrait )
        {
            mPortBitmap = bitmap;
            mPortCanvas.setBitmap( mPortBitmap );
        }
        else
        {
            mLandBitmap = bitmap;
            mLandCanvas.setBitmap( mLandBitmap );
        }
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
        mPortCanvas.drawColor( getResources().getColor( R.color.DrawingBackground ) );
        mLandCanvas.drawColor( getResources().getColor( R.color.DrawingBackground ) );
    }

    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom )
    {
        super.onLayout( changed, left, top, right, bottom );

        if( changed )
        {
            mPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        }
    }

    @Override
    protected void onDraw( Canvas canvas )
    {
        if( mPortrait )
        {
            canvas.drawBitmap( mPortBitmap, 0, 0, mCanvasPaint );
        }
        else
        {
            canvas.drawBitmap( mLandBitmap, 0, 0, mCanvasPaint );
        }
        canvas.drawPath( mDrawPath, mDrawingPaint );
        if( mDrawEraser )
        {
            final int color = mDrawingPaint.getColor();
            mDrawingPaint.setColor( getResources().getColor( R.color.PaleGreen ) );
            canvas.drawPoint( mX, mY, mDrawingPaint );
            mDrawingPaint.setColor( color );
        }
    }

    @Override
    public boolean onTouchEvent( MotionEvent event )
    {
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
                    mPortCanvas.drawPoint( mPathBounds.left, mPathBounds.top, mDrawingPaint );
                    mLandCanvas.drawPoint( mPathBounds.left, mPathBounds.top, mDrawingPaint );
                }
                mPortCanvas.drawPath( mDrawPath, mDrawingPaint );
                mLandCanvas.drawPath( mDrawPath, mDrawingPaint );
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
