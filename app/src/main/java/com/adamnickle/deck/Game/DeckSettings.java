package com.adamnickle.deck.Game;


import android.content.res.Resources;
import android.content.res.TypedArray;

import com.adamnickle.deck.R;

import java.util.Arrays;

public final class DeckSettings
{
    private DeckSettings() { }

    public static final String PLAYER_NAME = "player_name_pref";
    public static final String BACKGROUND = "background_pref";
    public static final String CHANGE_LOG_SHOWN_VERSION = "change_log_shown";

    public static int getBackgroundResourceFromString( Resources resources, String backgroundName )
    {
        final String[] backgrounds = resources.getStringArray( R.array.backgrounds );
        final int index = Arrays.asList( backgrounds ).indexOf( backgroundName );
        final TypedArray backgroundResources = resources.obtainTypedArray( R.array.background_drawables );
        final int backgroundResource = backgroundResources.getResourceId( index, android.R.color.white );
        backgroundResources.recycle();
        return backgroundResource;
    }
}
