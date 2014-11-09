package com.adamnickle.deck;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
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
    private ViewGroup mContent;
    private ScratchPadView mScratchPadView;
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
        setHasOptionsMenu( false );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mContent == null )
        {
            mContent = (ViewGroup) inflater.inflate( R.layout.scratchpad_layout, container, false );
            mScratchPadView = (ScratchPadView) mContent.findViewById( R.id.scratchpadView );
        }
        else
        {
            ( (ViewGroup) mContent.getParent() ).removeView( mContent );
        }

        return mContent;
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
        inflater.inflate( R.menu.scratch_pad, menu );
        final Context context = getActivity();
        menu.findItem( R.id.actionSaveScratchPad ).setIcon( Icons.getScratchPadSave( context ) );
        menu.findItem( R.id.actionLoadScratchPad ).setIcon( Icons.getScratchPadLoad( context ) );
        menu.findItem( R.id.actionClearScratchPad ).setIcon( Icons.getDeleteAction( context ) );
        menu.findItem( R.id.actionCloseScratchPad ).setIcon( Icons.getCloseAction( context ) );
        menu.findItem( R.id.actionUndo ).setIcon( Icons.getScratchPadUndo( context ) );
        menu.findItem( R.id.actionRedo ).setIcon( Icons.getScratchPadRedo( context ) );
    }

    @Override
    public void onPrepareOptionsMenu( Menu menu )
    {
        final boolean isEraser = mScratchPadView.isEraser();
        menu.findItem( R.id.actionEraser ).setVisible( !isEraser );
        menu.findItem( R.id.actionPen ).setVisible( isEraser );
        menu.findItem( R.id.actionSetPaintColor ).setVisible( !isEraser );
        menu.findItem( R.id.actionUndo ).setEnabled( mScratchPadView.canUndo() );
        menu.findItem( R.id.actionRedo ).setEnabled( mScratchPadView.canRedo() );
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
                handleSetStrokeSize();
                return true;

            case R.id.actionSetPaintColor:
                handleSetPaintColor();
                return true;

            case R.id.actionUndo:
                mScratchPadView.undo();
                return true;

            case R.id.actionRedo:
                mScratchPadView.redo();
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

    private void handleSetStrokeSize()
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
            public void onStartTrackingTouch( SeekBar seekBar )
            {
            }

            @Override
            public void onStopTrackingTouch( SeekBar seekBar )
            {
            }
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
                ScratchPadIO.saveScratchPad( getActivity(), text, mScratchPadView.getBitmap(), new ScratchPadIO.Callback()
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
                        mScratchPadView.setScratchPadBitmap( bitmap );
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
