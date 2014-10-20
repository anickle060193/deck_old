package com.adamnickle.deck;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class ScratchPadView extends View
{
    private static final int DRAWING_STROKE_WIDTH = 10;
    private static final int ERASER_STROKE_WIDTH = 80;

    private Bitmap mBitmap;
    private final Path mDrawPath;
    private Paint mCurrentPaint;
    private final Paint mDrawingPaint;
    private final Paint mErasingPaint;
    private final Paint mEraserPointPaint;
    private final Paint mCanvasPaint;
    private Canvas mCanvas;
    private RectF mPathBounds;
    private boolean mEraser;
    private boolean mDrawEraser;
    private int mX;
    private int mY;

    public ScratchPadView( Context context )
    {
        this( context, null );
    }

    public ScratchPadView( Context context, AttributeSet attrs )
    {
        this( context, attrs, 0 );
    }

    public ScratchPadView( Context context, AttributeSet attrs, int defStyleAttr )
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

        mEraserPointPaint = new Paint( mDrawingPaint );
        mEraserPointPaint.setStrokeWidth( ERASER_STROKE_WIDTH );
        mEraserPointPaint.setColor( getResources().getColor( R.color.PaleGreen ) );

        mErasingPaint = new Paint( mDrawingPaint );
        //mErasingPaint.setColor( Color.TRANSPARENT );
        mErasingPaint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.CLEAR ) );
        mErasingPaint.setStrokeWidth( ERASER_STROKE_WIDTH );

        mCurrentPaint = mDrawingPaint;

        mCanvasPaint = new Paint( Paint.DITHER_FLAG );
        mPathBounds = new RectF();
        mEraser = false;
        mX = 0;
        mY = 0;
        mDrawEraser = false;
    }

    public void setScratchPadBitmap( Bitmap bitmap )
    {
        mBitmap = bitmap;
        mCanvas = new Canvas( mBitmap );
        invalidate();
    }

    public void toggleEraser()
    {
        mEraser = !mEraser;
        if( mEraser )
        {
            mCurrentPaint = mErasingPaint;
        }
        else
        {
            mCurrentPaint = mDrawingPaint;
        }
    }

    public boolean isEraser()
    {
        return mEraser;
    }

    public void clearDrawing()
    {
        if( mCanvas != null )
        {
            mCanvas.drawColor( Color.TRANSPARENT, PorterDuff.Mode.CLEAR );
            invalidate();
        }
    }

    @Override
    protected void onDraw( Canvas canvas )
    {
        if( mBitmap != null )
        {
            mCanvas.drawPath( mDrawPath, mCurrentPaint );
            canvas.drawBitmap( mBitmap, 0, 0, mCanvasPaint );
            if( mDrawEraser )
            {
                canvas.drawPoint( mX, mY, mEraserPointPaint );
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
                mCanvas.drawPath( mDrawPath, mCurrentPaint );
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
