package com.adamnickle.deck.Game;

import android.content.Context;
import android.preference.PreferenceManager;

public abstract class GameSettings
{
    private GameSettings() { }

    public static final String KEY_PREF_DRAW_PILE_COUNT = "pref_draw_pile_count";
    public static final String KEY_PREF_DISCARD_PILE_COUNT = "pref_discard_pile_count";

    public static int getDrawPileCount( Context context )
    {
        return Integer.parseInt( PreferenceManager.getDefaultSharedPreferences( context ).getString( KEY_PREF_DRAW_PILE_COUNT, "0" ) );
    }

    public static int getDiscardPileCount( Context context )
    {
        return Integer.parseInt( PreferenceManager.getDefaultSharedPreferences( context ).getString( KEY_PREF_DISCARD_PILE_COUNT, "0" ) );
    }
}
