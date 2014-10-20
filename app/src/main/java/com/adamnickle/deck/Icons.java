package com.adamnickle.deck;


import android.content.Context;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

public final class Icons
{
    private final static int GAME_SAVE_ICON_SIZE = 30;
    private static IconDrawable GAME_SAVE_INFO;
    private static IconDrawable GAME_SAVE_DELETE;
    private static IconDrawable SCRATCH_PAD_SAVE;
    private static IconDrawable SCRATCH_PAD_LOAD;

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
}
