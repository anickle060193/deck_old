package com.adamnickle.deck;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


public class DrawingFragment extends Fragment
{
    private DrawingView mDrawingView;
    private Bitmap mDrawingBitmap;
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
        setHasOptionsMenu( true );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mDrawingView == null )
        {
            mDrawingView = new DrawingView( getActivity() );
        }
        else
        {
            container.removeView( mDrawingView );
        }

        return mDrawingView;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState )
    {
        super.onActivityCreated( savedInstanceState );

        mDrawerLayout = (DrawerLayout) getActivity().findViewById( R.id.drawerLayout );
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        inflater.inflate( R.menu.drawing, menu );
    }

    @Override
    public void onPrepareOptionsMenu( Menu menu )
    {
        super.onPrepareOptionsMenu( menu );

        final boolean isEraser = mDrawingView.isEraser();
        menu.findItem( R.id.actionEraser ).setVisible( !isEraser );
        menu.findItem( R.id.actionPen ).setVisible( isEraser );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.actionPen:
            case R.id.actionEraser:
                mDrawingView.toggleEraser();
                getActivity().invalidateOptionsMenu();
                return true;

            case R.id.actionCloseDrawing:
                mDrawerLayout.closeDrawer( GravityCompat.END );
                return true;

            case R.id.actionClearDrawing:
                mDrawingView.clearDrawing();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    public class DrawingView extends View
    {
        private static final int DRAWING_STROKE_WIDTH = 10;
        private static final int ERASER_STROKE_WIDTH = 80;

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

        @Override
        protected void onLayout( boolean changed, int left, int top, int right, int bottom )
        {
            super.onLayout( changed, left, top, right, bottom );

            if( changed )
            {
                final int width = right - left;
                final int height = bottom - top;
                final int layoutMax = Math.max( width, height );
                final int bitmapWidth = mDrawingBitmap != null ? mDrawingBitmap.getWidth() : 0;
                final int bitmapHeight = mDrawingBitmap != null ? mDrawingBitmap.getHeight() : 0;
                final int bitmapMax = Math.max( bitmapWidth, bitmapHeight );
                final int max = Math.max( layoutMax, bitmapMax );

                if( mDrawingBitmap == null || ( mDrawingBitmap.getWidth() < max && mDrawingBitmap.getHeight() < max ) )
                {
                    final Bitmap bitmap = Bitmap.createBitmap( max, max, Bitmap.Config.ARGB_8888 );
                    if( mDrawingBitmap == null )
                    {
                        mDrawingBitmap = bitmap;
                    }
                    else
                    {
                        Canvas canvas = new Canvas( bitmap );
                        canvas.drawBitmap( mDrawingBitmap, 0, 0, null );
                        Bitmap old = mDrawingBitmap;
                        mDrawingBitmap = bitmap;
                        old.recycle();
                    }
                    mCanvas = new Canvas( mDrawingBitmap );
                }

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
            if( mCanvas == null )
            {
                return;
            }

            mCanvas.drawColor( getResources().getColor( R.color.DrawingBackground ) );
        }

        @Override
        protected void onDraw( Canvas canvas )
        {
            if( mDrawingBitmap == null )
            {
                return;
            }

            canvas.drawBitmap( mDrawingBitmap, 0, 0, mCanvasPaint );
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
}
