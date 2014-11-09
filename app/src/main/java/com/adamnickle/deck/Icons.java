package com.adamnickle.deck;


import android.content.Context;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

public final class Icons
{
    private final static int GAME_SAVE_ICON_SIZE = 30;

    private static IconDrawable GAME_SAVE_INFO;
    public static IconDrawable getGameSaveSwipeInfo( Context context )
    {
        if( GAME_SAVE_INFO == null )
        {
            GAME_SAVE_INFO = new IconDrawable( context, Iconify.IconValue.fa_info_circle )
                    .colorRes( android.R.color.white )
                    .sizeDp( GAME_SAVE_ICON_SIZE );
        }
        return GAME_SAVE_INFO;
    }

    private static IconDrawable GAME_SAVE_DELETE;
    public static IconDrawable getGameSaveDelete( Context context )
    {
        if( GAME_SAVE_DELETE == null )
        {
            GAME_SAVE_DELETE = new IconDrawable( context, Iconify.IconValue.fa_trash_o )
                    .colorRes( android.R.color.black )
                    .sizeDp( GAME_SAVE_ICON_SIZE );
        }
        return GAME_SAVE_DELETE;
    }

    private static IconDrawable SCRATCH_PAD_SAVE;
    public static IconDrawable getScratchPadSave( Context context )
    {
        if( SCRATCH_PAD_SAVE == null )
        {
            SCRATCH_PAD_SAVE = new IconDrawable( context, Iconify.IconValue.fa_save )
                    .colorRes( R.color.ModerateCyan )
                    .actionBarSize();
        }
        return SCRATCH_PAD_SAVE;
    }

    private static IconDrawable SCRATCH_PAD_LOAD;
    public static IconDrawable getScratchPadLoad( Context context )
    {
        if( SCRATCH_PAD_LOAD == null )
        {
            SCRATCH_PAD_LOAD = new IconDrawable( context, Iconify.IconValue.fa_folder_open_o )
                    .colorRes( R.color.ModerateCyan )
                    .actionBarSize();
        }
        return SCRATCH_PAD_LOAD;
    }

    private static IconDrawable DELETE_ACTION;
    public static IconDrawable getDeleteAction( Context context )
    {
        if( DELETE_ACTION == null )
        {
            DELETE_ACTION = new IconDrawable( context, Iconify.IconValue.fa_trash_o )
                    .colorRes( R.color.ModerateCyan )
                    .actionBarSize();
        }
        return DELETE_ACTION;
    }

    private static IconDrawable CLOSE_ACTION;
    public static IconDrawable getCloseAction( Context context )
    {
        if( CLOSE_ACTION == null )
        {
            CLOSE_ACTION = new IconDrawable( context, Iconify.IconValue.fa_times )
                    .actionBarSize()
                    .colorRes( R.color.ModerateCyan );
        }
        return CLOSE_ACTION;
    }

    private static IconDrawable SCRATCH_PAD_UNDO;
    public static IconDrawable getScratchPadUndo( Context context )
    {
        if( SCRATCH_PAD_UNDO == null )
        {
            SCRATCH_PAD_UNDO = new IconDrawable( context, Iconify.IconValue.fa_undo )
                    .actionBarSize()
                    .colorRes( R.color.ModerateCyan );
        }
        return SCRATCH_PAD_UNDO;
    }

    private static IconDrawable SCRATCH_PAD_REDO;
    public static IconDrawable getScratchPadRedo( Context context )
    {
        if( SCRATCH_PAD_REDO == null )
        {
            SCRATCH_PAD_REDO = new IconDrawable( context, Iconify.IconValue.fa_repeat )
                    .actionBarSize()
                    .colorRes( R.color.ModerateCyan );
        }
        return SCRATCH_PAD_REDO;
    }
}
