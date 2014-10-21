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
    public static final int DEFAULT_PAINT_COLOR = Color.BLACK;

    public static final int DEFAULT_STROKE_WIDTH = 10;
    public static final int MAX_STROKE_WIDTH = 100;
    public static final int MIN_STROKE_WIDTH = 1;

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
        mDrawingPaint.setColor( DEFAULT_PAINT_COLOR );
        mDrawingPaint.setAntiAlias( true );
        mDrawingPaint.setStrokeWidth( DEFAULT_STROKE_WIDTH );
        mDrawingPaint.setStyle( Paint.Style.STROKE );
        mDrawingPaint.setStrokeJoin( Paint.Join.ROUND );
        mDrawingPaint.setStrokeCap( Paint.Cap.ROUND );

        mEraserPointPaint = new Paint( mDrawingPaint );
        mEraserPointPaint.setColor( getResources().getColor( R.color.PaleGreen ) );

        mErasingPaint = new Paint( mDrawingPaint );
        mErasingPaint.setColor( Color.TRANSPARENT );
        mErasingPaint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.CLEAR ) );

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

    public int getStrokeSize()
    {
        return (int) mCurrentPaint.getStrokeWidth();
    }

    public void setStrokeSize( int strokeSize )
    {
        if( strokeSize < MIN_STROKE_WIDTH )
        {
            strokeSize = MIN_STROKE_WIDTH;
        }
        else if( strokeSize > MAX_STROKE_WIDTH )
        {
            strokeSize = MAX_STROKE_WIDTH;
        }
        mCurrentPaint.setStrokeWidth( strokeSize );
    }

    public int getPaintColor()
    {
        return mDrawingPaint.getColor();
    }

    public void setPaintColor( int paintColor )
    {
        mDrawingPaint.setColor( paintColor );
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
