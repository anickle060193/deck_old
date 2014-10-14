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

        mDrawingViewLaidOut = false;
        mOverViewLaidOut = false;
        mDrawingView.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                Log.d( "DrawingFragment", "mDrawingView onGlobalLayout" );
                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
                {
                    mDrawingView.getViewTreeObserver().removeOnGlobalLayoutListener( this );
                }
                else
                {
                    mDrawingView.getViewTreeObserver().removeGlobalOnLayoutListener( this );
                }
                mDrawingViewLaidOut = true;
                createDrawingBitmap();
            }
        } );
        mOverView.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                Log.d( "DrawingFragment", "mOverView onGlobalLayout" );
                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
                {
                    mDrawingView.getViewTreeObserver().removeOnGlobalLayoutListener( this );
                }
                else
                {
                    mDrawingView.getViewTreeObserver().removeGlobalOnLayoutListener( this );
                }
                mOverViewLaidOut = true;
                createDrawingBitmap();
            }
        } );

        return mView;
    }

    private void createDrawingBitmap()
    {
        if( !mDrawingViewLaidOut || !mOverViewLaidOut ) return;

        Log.d( "DrawingFragment", "createDrawingFragment" );

        final int drawingViewWidth = mDrawingView.getWidth();
        final int drawingViewHeight = mDrawingView.getHeight();

        final int overViewWidth = mOverView.getWidth();
        final int overViewHeight = mOverView.getHeight();

        final float maxBitmapWidth;
        final float maxBitmapHeight;

        if( getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT )
        {
            maxBitmapWidth = (float) drawingViewHeight / overViewHeight * overViewWidth;
            maxBitmapHeight = drawingViewHeight;
        }
        else
        {
            maxBitmapHeight = (float) drawingViewWidth / overViewWidth * overViewHeight;
            maxBitmapWidth = drawingViewWidth;
        }

        final int max = (int) Math.max( maxBitmapWidth, maxBitmapHeight );
        final Bitmap bitmap;
        if( mBitmap == null || ( max > Math.max( mBitmap.getWidth(), mBitmap.getHeight() ) ) )
        {
            bitmap = Bitmap.createBitmap( max, max, Bitmap.Config.ARGB_8888 );
        }
        else
        {
            return;
        }

        if( mBitmap == null )
        {
            mBitmap = bitmap;
        }
        else
        {
            Canvas canvas = new Canvas( bitmap );
            canvas.drawBitmap( mBitmap, 0, 0, null );
            mBitmap.recycle();
            mBitmap = bitmap;
        }

        mDrawingView.setDrawingBitmap( mBitmap );
        Log.d( "DrawingFragment", "Bitmap Width: " + mBitmap.getWidth() + ", Height: " + mBitmap.getHeight() );
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
        super.onCreateOptionsMenu( menu, inflater );
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
}
