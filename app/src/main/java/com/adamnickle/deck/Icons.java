package com.adamnickle.deck;


import android.content.Context;

import com.mikpenz.google_material_typeface_library.GoogleMaterial;
import com.mikpenz.iconics.Iconics;
import com.mikpenz.iconics.IconicsDrawable;
import com.mikpenz.iconics.typeface.FontAwesome;

public final class Icons
{
    static
    {
        Iconics.registerFont( new GoogleMaterial() );
    }

    private static IconicsDrawable SCRATCH_PAD_SAVE;
    public static IconicsDrawable getScratchPadSave( Context context )
    {
        if( SCRATCH_PAD_SAVE == null )
        {
            SCRATCH_PAD_SAVE = new IconicsDrawable( context, GoogleMaterial.Icon.gmd_save )
                    .colorRes( R.color.ModerateCyan )
                    .actionBarSize();
        }
        return SCRATCH_PAD_SAVE;
    }

    private static IconicsDrawable SCRATCH_PAD_LOAD;
    public static IconicsDrawable getScratchPadLoad( Context context )
    {
        if( SCRATCH_PAD_LOAD == null )
        {
            SCRATCH_PAD_LOAD = new IconicsDrawable( context, GoogleMaterial.Icon.gmd_folder_open )
                    .colorRes( R.color.ModerateCyan )
                    .actionBarSize();
        }
        return SCRATCH_PAD_LOAD;
    }

    private static IconicsDrawable DELETE_ACTION;
    public static IconicsDrawable getDeleteAction( Context context )
    {
        if( DELETE_ACTION == null )
        {
            DELETE_ACTION = new IconicsDrawable( context, FontAwesome.Icon.faw_trash_o )
                    .colorRes( R.color.ModerateCyan )
                    .actionBarSize();
        }
        return DELETE_ACTION;
    }

    private static IconicsDrawable CLOSE_ACTION;
    public static IconicsDrawable getCloseAction( Context context )
    {
        if( CLOSE_ACTION == null )
        {
            CLOSE_ACTION = new IconicsDrawable( context, GoogleMaterial.Icon.gmd_close )
                    .colorRes( R.color.ModerateCyan )
                    .actionBarSize();
        }
        return CLOSE_ACTION;
    }

    private static IconicsDrawable SCRATCH_PAD_UNDO;
    public static IconicsDrawable getScratchPadUndo( Context context )
    {
        if( SCRATCH_PAD_UNDO == null )
        {
            SCRATCH_PAD_UNDO = new IconicsDrawable( context, FontAwesome.Icon.faw_undo )
                    .colorRes( R.color.ModerateCyan )
                    .actionBarSize();
        }
        return SCRATCH_PAD_UNDO;
    }

    private static IconicsDrawable SCRATCH_PAD_REDO;
    public static IconicsDrawable getScratchPadRedo( Context context )
    {
        if( SCRATCH_PAD_REDO == null )
        {
            SCRATCH_PAD_REDO = new IconicsDrawable( context, FontAwesome.Icon.faw_repeat )
                    .colorRes( R.color.ModerateCyan )
                    .actionBarSize();
        }
        return SCRATCH_PAD_REDO;
    }

    private static IconicsDrawable SCRATCH_PAD_ERASE;
    public static IconicsDrawable getScratchPadEraser( Context context )
    {
        if( SCRATCH_PAD_ERASE == null )
        {
            SCRATCH_PAD_ERASE = new IconicsDrawable( context, FontAwesome.Icon.faw_eraser )
                    .colorRes( R.color.ModerateCyan )
                    .actionBarSize();
        }
        return SCRATCH_PAD_ERASE;
    }

    private static IconicsDrawable SCRATCH_PAD_PEN;
    public static IconicsDrawable getScratchPadPen( Context context )
    {
        if( SCRATCH_PAD_PEN == null )
        {
            SCRATCH_PAD_PEN = new IconicsDrawable( context, FontAwesome.Icon.faw_paint_brush )
                    .colorRes( R.color.ModerateCyan )
                    .actionBarSize();
        }
        return SCRATCH_PAD_PEN;
    }

    private static IconicsDrawable DONE_ACTION_ICON;
    public static IconicsDrawable getDoneActionIcon( Context context )
    {
        if( DONE_ACTION_ICON == null )
        {
            DONE_ACTION_ICON = new IconicsDrawable( context, GoogleMaterial.Icon.gmd_done )
                    .colorRes( R.color.ModerateCyan )
                    .actionBarSize();
        }
        return DONE_ACTION_ICON;
    }
}
