package com.adamnickle.deck;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.PopupWindow;

import com.larswerkman.holocolorpicker.ColorPicker;

import java.io.File;

import de.keyboardsurfer.android.widget.crouton.Style;


public class ScratchPadFragment extends Fragment
{
    private View mView;
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
        if( mView == null )
        {
            mView = inflater.inflate( R.layout.drawing_layout, container, false );
            mScratchPadView = (ScratchPadView) mView.findViewById( R.id.drawingView );
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

        return mView;
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
        inflater.inflate( R.menu.scratch_pad, menu );
        menu.findItem( R.id.actionSaveScratchPad ).setIcon( Icons.getScratchPadSave( getActivity() ) );
        menu.findItem( R.id.actionLoadScratchPad ).setIcon( Icons.getScratchPadLoad( getActivity() ) );
    }

    @Override
    public void onPrepareOptionsMenu( Menu menu )
    {
        super.onPrepareOptionsMenu( menu );

        final boolean isEraser = mScratchPadView.isEraser();
        menu.findItem( R.id.actionEraser ).setVisible( !isEraser );
        menu.findItem( R.id.actionPen ).setVisible( isEraser );
        menu.findItem( R.id.actionSetPaintColor ).setVisible( !isEraser );
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
                //handleSetStrokeSize( item );
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
        final ViewGroup viewGroup = DialogHelper.createColorPickerLayout( getActivity(), mScratchPadView.getPaintColor() );
        final ColorPicker colorPicker = (ColorPicker) viewGroup.findViewById( DialogHelper.COLOR_PICKER_VIEW_ID );
        DialogHelper
                .createBlankAlertDialog( getActivity(), "Select paint color:" )
                .setView( viewGroup )
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
        View menuItemView = getActivity().findViewById( item.getGroupId() );
        PopupWindow popupWindow = new PopupWindow( 80, 80 );
        popupWindow.setClippingEnabled( false );
        popupWindow.showAsDropDown( menuItemView );
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
}
