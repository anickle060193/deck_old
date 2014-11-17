package com.adamnickle.deck.Game;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;

import com.adamnickle.deck.CardResources;
import com.adamnickle.deck.R;

import java.util.Arrays;

public final class DeckSettings
{
    private DeckSettings() { }

    public static final String PLAYER_NAME = "player_name_pref";

    public static final String CARD_BACK = "card_back_pref";
    public static final String DEFAULT_CARD_BACK = "Blue Back";

    public static final String BACKGROUND = "background_pref";
    public static final String DEFAULT_BACKGROUND_VALUE = "White";

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

    public static int getCardBackResource( Context context )
    {
        final String cardBack = PreferenceManager
                .getDefaultSharedPreferences( context )
                .getString( CARD_BACK, DEFAULT_CARD_BACK )
                .toLowerCase();

        if( cardBack.startsWith( "blue" ) )
        {
            return CardResources.BLUE_CARD_BACK;
        }
        else
        {
            return CardResources.RED_CARD_BACK;
        }
    }
}
