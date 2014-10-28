package com.adamnickle.deck;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;

import java.io.File;

import de.keyboardsurfer.android.widget.crouton.Style;


public class ScratchPadFragment extends Fragment
{
    private ScratchPadView mScratchPadView;
    private Bitmap mBitmap;
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
        if( mScratchPadView == null )
        {
            mScratchPadView = (ScratchPadView) inflater.inflate( R.layout.drawing_layout, container, false );
        }
        else
        {
            ( (ViewGroup) mScratchPadView.getParent() ).removeView( mScratchPadView );
        }

        mScratchPadView.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
                {
                    mScratchPadView.getViewTreeObserver().removeOnGlobalLayoutListener( this );
                }
                else
                {
                    mScratchPadView.getViewTreeObserver().removeGlobalOnLayoutListener( this );
                }

                createBitmap();
            }
        } );

        return mScratchPadView;
    }

    private void createBitmap()
    {
        final int drawingViewWidth = mScratchPadView.getWidth();
        final int drawingViewHeight = mScratchPadView.getHeight();

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
        mScratchPadView.setScratchPadBitmap( mBitmap );
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
        if( mDrawerLayout.isDrawerOpen( GravityCompat.END ) )
        {
            inflater.inflate( R.menu.scratch_pad, menu );
            menu.findItem( R.id.actionSaveScratchPad ).setIcon( Icons.getScratchPadSave( getActivity() ) );
            menu.findItem( R.id.actionLoadScratchPad ).setIcon( Icons.getScratchPadLoad( getActivity() ) );
            menu.findItem( R.id.actionClearScratchPad ).setIcon( Icons.getDeleteAction( getActivity() ) );
            menu.findItem( R.id.actionCloseScratchPad ).setIcon( Icons.getCloseAction( getActivity() ) );
        }
    }

    @Override
    public void onPrepareOptionsMenu( Menu menu )
    {
        super.onPrepareOptionsMenu( menu );

        if( mDrawerLayout.isDrawerOpen( GravityCompat.END ) )
        {
            final boolean isEraser = mScratchPadView.isEraser();
            menu.findItem( R.id.actionEraser ).setVisible( !isEraser );
            menu.findItem( R.id.actionPen ).setVisible( isEraser );
            menu.findItem( R.id.actionSetPaintColor ).setVisible( !isEraser );
        }
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.actionPen:
            case R.id.actionEraser:
                mScratchPadView.toggleEraser();
                getActivity().invalidateOptionsMenu();
                return true;

            case R.id.actionCloseScratchPad:
                mDrawerLayout.closeDrawer( GravityCompat.END );
                return true;

            case R.id.actionClearScratchPad:
                mScratchPadView.clearDrawing();
                return true;

            case R.id.actionSaveScratchPad:
                handleSaveScratchPadClick();
                return true;

            case R.id.actionLoadScratchPad:
                handleLoadScratchPadClick();
                return true;

            case R.id.actionSetStrokeSize:
                handleSetStrokeSize( item );
                return true;

            case R.id.actionSetPaintColor:
                handleSetPaintColor();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void handleSetPaintColor()
    {
        final ViewGroup contentView = (ViewGroup) LayoutInflater.from( getActivity() ).inflate( R.layout.color_picker_layout, null );

        final ColorPicker colorPicker = (ColorPicker) contentView.findViewById( R.id.colorPicker );
        final SVBar svBar = (SVBar) contentView.findViewById( R.id.saturationValueBar );

        colorPicker.addSVBar( svBar );
        colorPicker.setColor( mScratchPadView.getPaintColor() );
        colorPicker.setOldCenterColor( mScratchPadView.getPaintColor() );

        DialogHelper
                .createBlankAlertDialog( getActivity(), "Select paint color:" )
                .setView( contentView )
                .setNegativeButton( "Cancel", null )
                .setPositiveButton( "OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialogInterface, int i )
                    {
                        mScratchPadView.setPaintColor( colorPicker.getColor() );
                    }
                } ).show();
    }

    private void handleSetStrokeSize( MenuItem item )
    {
        final ViewGroup contentView = (ViewGroup) LayoutInflater.from( getActivity() ).inflate( R.layout.seekbar_popup, null );

        final SeekBar seekBar = (SeekBar) contentView.findViewById( R.id.seekBar );
        final ImageView sizeDisplay = (ImageView) contentView.findViewById( R.id.sizeDisplay );

        int initialStrokeSize = mScratchPadView.getStrokeSize();

        final CircleDrawable circleDrawable = new CircleDrawable();
        circleDrawable.setColor( mScratchPadView.getPaintColor() );
        circleDrawable.setSize( initialStrokeSize );

        sizeDisplay.setImageDrawable( circleDrawable );

        seekBar.setMax( getResources().getDimensionPixelSize( R.dimen.max_stroke_size ) );
        seekBar.setProgress( initialStrokeSize );
        seekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged( SeekBar seekBar, int progress, boolean userChanged )
            {
                circleDrawable.setSize( progress );
            }

            @Override
            public void onStartTrackingTouch( SeekBar seekBar ) { }

            @Override
            public void onStopTrackingTouch( SeekBar seekBar ) { }
        } );

        DialogHelper
                .createBlankAlertDialog( getActivity(), "Select paint size:" )
                .setView( contentView )
                .setNegativeButton( "Cancel", null )
                .setPositiveButton( "OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialogInterface, int i )
                    {
                        mScratchPadView.setStrokeSize( seekBar.getProgress() );
                    }
                } )
                .show();
    }

    private void handleSaveScratchPadClick()
    {
        DialogHelper.createEditTextDialog( getActivity(), "Enter scratch pad name:", "Scratch Pad Save", "OK", "Cancel", new DialogHelper.OnEditTextDialogClickListener()
        {
            @Override
            public void onPositiveButtonClick( DialogInterface dialogInterface, String text )
            {
                ScratchPadIO.saveScratchPad( getActivity(), text, mBitmap, new ScratchPadIO.Callback()
                {
                    @Override
                    public void onSuccess()
                    {
                        DialogHelper.displayNotification( getActivity(), "Scratch Pad save successful.", Style.CONFIRM );
                    }

                    @Override
                    public void onFail()
                    {
                        DialogHelper.displayNotification( getActivity(), "Scratch Pad save not successful.", Style.ALERT );
                    }
                } );
            }
        } ).show();
    }

    private void handleLoadScratchPadClick()
    {
        final AlertDialog dialog = DialogHelper
                .createBlankAlertDialog( getActivity(), "Select scratch pad to load:" )
                .setPositiveButton( "Close", null )
                .create();

        final ListView scratchpadListView = ScratchPadIO.getScratchPadListView( getActivity() );
        if( scratchpadListView != null )
        {
            scratchpadListView.setOnItemClickListener( new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick( AdapterView< ? > adapterView, View view, int i, long l )
                {
                    final File scratchPad = (File) adapterView.getItemAtPosition( i );
                    Bitmap bitmap = ScratchPadIO.openScratchPad( getActivity(), scratchPad );
                    if( bitmap != null )
                    {
                        final Bitmap temp = mBitmap;
                        mBitmap = bitmap;
                        mScratchPadView.setScratchPadBitmap( mBitmap );
                        temp.recycle();
                        DialogHelper.displayNotification( getActivity(), "Scratch pad load successful.", Style.CONFIRM );
                    }
                    else
                    {
                        DialogHelper.displayNotification( getActivity(), "Scratch pad load unsuccessful.", Style.ALERT );
                    }
                    dialog.dismiss();
                }
            } );
            scratchpadListView.getAdapter().registerDataSetObserver( new DataSetObserver()
            {
                @Override
                public void onChanged()
                {
                    if( scratchpadListView.getAdapter().getCount() == 0 )
                    {
                        if( dialog.isShowing() )
                        {
                            dialog.dismiss();
                            DialogHelper
                                    .createBlankAlertDialog( getActivity(), "Select scratch pad to load:" )
                                    .setMessage( "There are no scratch pads to load." )
                                    .setPositiveButton( "Close", null )
                                    .show();
                        }
                    }
                }
            } );
            dialog.setView( scratchpadListView );
        }
        else
        {
            dialog.setMessage( "There are no scratch pads to load" );
        }
        dialog.show();
    }

    public static class CircleDrawable extends Drawable
    {
        private final Paint mPaint;

        public CircleDrawable()
        {
            mPaint = new Paint();
            mPaint.setAntiAlias( true );
            mPaint.setStyle( Paint.Style.STROKE );
            mPaint.setStrokeJoin( Paint.Join.ROUND );
            mPaint.setStrokeCap( Paint.Cap.ROUND );
        }

        public void setColor( int color )
        {
            mPaint.setColor( color );
            invalidateSelf();
        }

        public void setSize( int size )
        {
            mPaint.setStrokeWidth( size );
            invalidateSelf();
        }

        @Override
        public void draw( Canvas canvas )
        {
            final float x = canvas.getWidth() / 2.0f;
            final float y = canvas.getHeight() / 2.0f;
            canvas.drawCircle( x, y, 1, mPaint );
        }

        @Override
        public void setAlpha( int i )
        {
            mPaint.setAlpha( i );
            invalidateSelf();
        }

        @Override
        public void setColorFilter( ColorFilter colorFilter )
        {
            mPaint.setColorFilter( colorFilter );
            invalidateSelf();
        }

        @Override
        public int getOpacity()
        {
            return mPaint.getAlpha();
        }
    }
}
