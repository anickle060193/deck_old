package com.adamnickle.deck;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;


public class DrawingFragment extends Fragment
{
    private View mView;
    private DrawingView mDrawingView;
    private DrawingOverView mOverView;
    private Bitmap mBitmap;
    private DrawerLayout mDrawerLayout;
    private boolean mDrawingViewLaidOut;
    private boolean mOverViewLaidOut;

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
        mView = inflater.inflate( R.layout.drawing_layout, container, false );
        mDrawingView = (DrawingView) mView.findViewById( R.id.drawingView );
        mOverView = (DrawingOverView) mView.findViewById( R.id.drawingOverView );

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

                createBitmap();
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

                createBitmap();
            }
        } );

        return mView;
    }

    private void createBitmap()
    {
        if( !mDrawingViewLaidOut || !mOverViewLaidOut )
        {
            return;
        }

        final int drawingViewWidth = mDrawingView.getWidth();
        final int drawingViewHeight = mDrawingView.getHeight();

        final int bitmapWidth = mBitmap != null ? mBitmap.getWidth() : 0;
        final int bitmapHeight = mBitmap != null ? mBitmap.getHeight() : 0;

        final int width = Math.max( drawingViewWidth, bitmapWidth );
        final int height = Math.max( drawingViewHeight, bitmapHeight );

        if( width > bitmapWidth || height > bitmapHeight )
        {
            final Bitmap bitmap = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );

            if( mBitmap != null )
            {
                final Canvas canvas = new Canvas( bitmap );
                canvas.drawBitmap( mBitmap, 0, 0, null );
                mBitmap.recycle();
            }

            mBitmap = bitmap;
        }
        mDrawingView.setBitmap( mBitmap );
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
