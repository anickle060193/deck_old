package com.adamnickle.deck;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Stack;


public class ScratchPadView extends View
{
    public static final int DEFAULT_PAINT_COLOR = Color.BLACK;

    private final int MAX_STROKE_SIZE;
    private final int MIN_STROKE_SIZE;

    private Bitmap mBaseBitmap;
    private Bitmap mCacheBitmap;
    private final Path mDrawPath;
    private Paint mCurrentPaint;
    private final Paint mDrawingPaint;
    private final Paint mErasingPaint;
    private final Paint mEraserPointPaint;
    private final Canvas mCacheCanvas;
    private RectF mPathBounds;
    private boolean mEraser;
    private boolean mDrawEraser;
    private int mX;
    private int mY;
    private final Stack<DrawingStep> mDrawingSteps;
    private final Stack<DrawingStep> mUndoneDrawingSteps;

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

        MAX_STROKE_SIZE = getResources().getDimensionPixelSize( R.dimen.max_stroke_size );
        MIN_STROKE_SIZE = getResources().getDimensionPixelSize( R.dimen.min_stroke_size );

        mDrawingPaint = new Paint();
        mDrawingPaint.setColor( DEFAULT_PAINT_COLOR );
        mDrawingPaint.setAntiAlias( true );
        mDrawingPaint.setStrokeWidth( getResources().getDimensionPixelSize( R.dimen.default_paint_stroke_size ) );
        mDrawingPaint.setStyle( Paint.Style.STROKE );
        mDrawingPaint.setStrokeJoin( Paint.Join.ROUND );
        mDrawingPaint.setStrokeCap( Paint.Cap.ROUND );

        mEraserPointPaint = new Paint( mDrawingPaint );
        mEraserPointPaint.setStrokeWidth( getResources().getDimensionPixelSize( R.dimen.default_eraser_stroke_size ) );
        mEraserPointPaint.setColor( getResources().getColor( R.color.PaleGreen ) );

        mErasingPaint = new Paint( mDrawingPaint );
        mErasingPaint.setStrokeWidth( getResources().getDimensionPixelSize( R.dimen.default_eraser_stroke_size ) );
        mErasingPaint.setColor( Color.TRANSPARENT );
        mErasingPaint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.CLEAR ) );

        mCurrentPaint = mDrawingPaint;

        mPathBounds = new RectF();
        mEraser = false;
        mX = 0;
        mY = 0;
        mDrawEraser = false;
        mDrawingSteps = new Stack< DrawingStep >();
        mUndoneDrawingSteps = new Stack< DrawingStep >();
        mCacheCanvas = new Canvas();
    }

    public static class DrawingStep
    {
        private Paint Paint;
        Path Path;
        float X;
        float Y;

        private DrawingStep( Paint paint )
        {
            this.Paint = new Paint( paint );
        }

        public static DrawingStep create( Paint paint, Path path )
        {
            DrawingStep drawingStep = new DrawingStep( paint );
            drawingStep.Path = new Path( path );
            drawingStep.X = 0.0f;
            drawingStep.Y = 0.0f;
            return drawingStep;
        }

        public static DrawingStep create( Paint paint, float x, float y )
        {
            DrawingStep drawingStep = new DrawingStep( paint );
            drawingStep.Path = null;
            drawingStep.X = x;
            drawingStep.Y = y;
            return drawingStep;
        }

        public void drawToCanvas( Canvas canvas )
        {
            if( Path == null )
            {
                canvas.drawPoint( X, Y, Paint );
            }
            else
            {
                canvas.drawPath( Path, Paint );
            }
        }
    }

    public boolean canUndo()
    {
        return !mDrawingSteps.empty();
    }

    public void undo()
    {
        if( !mDrawingSteps.empty() )
        {
            mUndoneDrawingSteps.push( mDrawingSteps.pop() );
            invalidate();

            if( getContext() instanceof Activity )
            {
                ( (Activity) getContext() ).invalidateOptionsMenu();
            }
        }
    }

    public boolean canRedo()
    {
        return !mUndoneDrawingSteps.empty();
    }

    public void redo()
    {
        if( !mUndoneDrawingSteps.empty() )
        {
            mDrawingSteps.push( mUndoneDrawingSteps.pop() );
            invalidate();

            if( getContext() instanceof Activity )
            {
                ( (Activity) getContext() ).invalidateOptionsMenu();
            }
        }
    }

    @SuppressLint( "DrawAllocation" )
    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom )
    {
        super.onLayout( changed, left, top, right, bottom );

        if( changed )
        {
            final int drawingViewWidth = right - left;
            final int drawingViewHeight = bottom - top;

            final int bitmapWidth = mBaseBitmap != null ? mBaseBitmap.getWidth() : 0;
            final int bitmapHeight = mBaseBitmap != null ? mBaseBitmap.getHeight() : 0;

            final int width = Math.max( drawingViewWidth, bitmapWidth );
            final int height = Math.max( drawingViewHeight, bitmapHeight );

            if( width > bitmapWidth || height > bitmapHeight )
            {
                final Bitmap bitmap = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );

                if( mBaseBitmap != null )
                {
                    final Canvas canvas = new Canvas( bitmap );
                    canvas.drawBitmap( mBaseBitmap, 0, 0, null );
                    mBaseBitmap.recycle();
                }
                mBaseBitmap = bitmap;

                if( mCacheBitmap != null )
                {
                    mCacheBitmap.recycle();
                }
                mCacheBitmap = Bitmap.createBitmap( mBaseBitmap.getWidth(), mBaseBitmap.getHeight(), Bitmap.Config.ARGB_8888 );
                mCacheCanvas.setBitmap( mCacheBitmap );
                invalidate();
            }
        }
    }

    public void setScratchPadBitmap( Bitmap bitmap )
    {
        mDrawingSteps.clear();
        mUndoneDrawingSteps.clear();

        if( mBaseBitmap != null )
        {
            mBaseBitmap.recycle();
        }
        mBaseBitmap = bitmap;

        if( mCacheBitmap != null
         && mCacheBitmap.getWidth() == mBaseBitmap.getWidth()
         && mCacheBitmap.getHeight() == mBaseBitmap.getHeight() )
        {
            clearCacheBitmap();
        }
        else
        {
            if( mCacheBitmap != null )
            {
                mCacheBitmap.recycle();
            }
            mCacheBitmap = Bitmap.createBitmap( mBaseBitmap.getWidth(), mBaseBitmap.getHeight(), Bitmap.Config.ARGB_8888 );
            mCacheCanvas.setBitmap( mCacheBitmap );
        }
        invalidate();
    }

    public Bitmap getBitmap()
    {
        return mCacheBitmap;
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
        if( strokeSize < MIN_STROKE_SIZE )
        {
            strokeSize = MIN_STROKE_SIZE;
        }
        else if( strokeSize > MAX_STROKE_SIZE )
        {
            strokeSize = MAX_STROKE_SIZE;
        }
        mCurrentPaint.setStrokeWidth( strokeSize );
        if( isEraser() )
        {
            mEraserPointPaint.setStrokeWidth( strokeSize );
        }
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

    private void clearCacheBitmap()
    {
        mCacheCanvas.drawColor( Color.TRANSPARENT, PorterDuff.Mode.CLEAR );
    }

    public void clearDrawing()
    {
        mDrawingSteps.clear();
        mUndoneDrawingSteps.clear();
        clearCacheBitmap();
        if( mBaseBitmap != null )
        {
            mBaseBitmap.recycle();
            mBaseBitmap = null;
        }
        invalidate();

        if( getContext() instanceof Activity )
        {
            ( (Activity) getContext() ).invalidateOptionsMenu();
        }
        System.gc();
    }

    @Override
    protected void onDraw( Canvas canvas )
    {
        clearCacheBitmap();

        if( mBaseBitmap != null )
        {
            mCacheCanvas.drawBitmap( mBaseBitmap, 0, 0, null );
        }

        for( DrawingStep drawingStep : mDrawingSteps )
        {
            drawingStep.drawToCanvas( mCacheCanvas );
        }
        mCacheCanvas.drawPath( mDrawPath, mCurrentPaint );

        if( mDrawEraser )
        {
            mCacheCanvas.drawPoint( mX, mY, mEraserPointPaint );
        }

        canvas.drawBitmap( mCacheBitmap, 0, 0, null );
    }

    @Override
    public boolean onTouchEvent( @NonNull MotionEvent event )
    {
        mX = (int) event.getX();
        mY = (int) event.getY();

        switch( event.getActionMasked() )
        {
            case MotionEvent.ACTION_DOWN:
                mDrawEraser = mEraser;
                mDrawPath.moveTo( mX, mY );
                break;

            case MotionEvent.ACTION_MOVE:
                mDrawPath.lineTo( mX, mY );
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDrawPath.computeBounds( mPathBounds, false );
                if( mPathBounds.width() < 10 || mPathBounds.height() < 10 )
                {
                    mDrawingSteps.push( DrawingStep.create( mCurrentPaint, mPathBounds.left, mPathBounds.top ) );
                }
                else
                {
                    mDrawingSteps.push( DrawingStep.create( mCurrentPaint, mDrawPath ) );
                }

                if( getContext() instanceof Activity )
                {
                    ( (Activity) getContext() ).invalidateOptionsMenu();
                }
                mUndoneDrawingSteps.clear();
                mDrawPath.reset();
                mDrawEraser = false;
                break;
        }
        invalidate();
        return true;
    }
}
