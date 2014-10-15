package com.adamnickle.deck;

import android.app.Fragment;
import android.content.res.Configuration;
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
    private Bitmap mLandBitmap;
    private Bitmap mPortBitmap;
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

                createBitmap( getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT );
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

                createBitmap( getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT );
            }
        } );

        return mView;
    }

    private void createBitmap( boolean portrait )
    {
        if( !mDrawingViewLaidOut || !mOverViewLaidOut )
        {
            return;
        }

        if( portrait )
        {
            if( mPortBitmap != null )
            {
                mDrawingView.setBitmap( true, mPortBitmap );
                return;
            }
        }
        else
        {
            if( mLandBitmap != null )
            {
                mDrawingView.setBitmap( false, mLandBitmap );
                return;
            }
        }

        final int drawingViewWidth = mDrawingView.getWidth();
        final int drawingViewHeight = mDrawingView.getHeight();

        final Bitmap bitmap = Bitmap.createBitmap( drawingViewWidth, drawingViewHeight, Bitmap.Config.ARGB_8888 );

        if( portrait )
        {
            mPortBitmap = bitmap;
            mDrawingView.setBitmap( true, mPortBitmap );
            if( mLandBitmap != null )
            {
                final Canvas canvas = new Canvas( mPortBitmap );
                canvas.drawBitmap( mLandBitmap, 0, 0, null );
            }
        }
        else
        {
            mLandBitmap = bitmap;
            mDrawingView.setBitmap( false, mLandBitmap );
            if( mPortBitmap != null )
            {
                final Canvas canvas = new Canvas( mLandBitmap );
                canvas.drawBitmap( mPortBitmap, 0, 0, null );
            }
        }
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
