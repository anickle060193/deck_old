package com.adamnickle.deck.Game;

import android.content.Context;
import android.preference.PreferenceManager;

public abstract class GameSettings
{
    private GameSettings() { }

    public static final String KEY_PREF_ENABLE_TABLE_VIEW = "pref_enable_table_view";

    public static boolean getTableViewEnabled( Context context )
    {
        return PreferenceManager.getDefaultSharedPreferences( context ).getBoolean( KEY_PREF_ENABLE_TABLE_VIEW, true );
    }
}
