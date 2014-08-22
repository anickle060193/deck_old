package com.adamnickle.deck;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;


public class GameCreatorActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String KEY_PREF_GAME_NAME = "pref_game_name";
    public static final String KEY_PREF_MIN_PLAYERS = "pref_min_players";
    public static final String KEY_PREF_MAX_PLAYERS = "pref_max_players";
    public static final String KEY_PREF_TRACK_POINTS = "pref_track_points";
    public static final String KEY_PREF_POINTS_VALUE_STYLE = "pref_points_value_style";

    public static final String KEY_CARD_POINTS_SCREEN = "pref_card_points_value_screen";

    private static final String[] PREF_KEYS = {
            KEY_PREF_GAME_NAME,
            KEY_PREF_MIN_PLAYERS,
            KEY_PREF_MAX_PLAYERS,
            KEY_PREF_TRACK_POINTS,
            KEY_PREF_POINTS_VALUE_STYLE,
    };

    private static final String DEFAULT_PREF_GAME_NAME = "New Game";
    private static final String DEFAULT_PREF_MIN_PLAYERS = "2";
    private static final String DEFAULT_PREF_MAX_PLAYERS = "4";
    private static final boolean DEFAULT_PREF_TRACK_POINTS = false;
    private static final String DEFAULT_PREF_POINTS_VALUE_STYLE = "Face Value";

    private GameCreatorFragment mGameCreatorFragment;
    private SharedPreferences mSharedPreferences;
    private int mLastMinimumPlayers;
    private int mLastMaximumPlayers;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        mGameCreatorFragment = new GameCreatorFragment();
        if( savedInstanceState == null )
        {
            getFragmentManager()
               .beginTransaction()
               .replace( android.R.id.content, mGameCreatorFragment )
               .commit();
        }
        PreferenceManager.setDefaultValues( this, R.xml.game_creator_preferences, true );
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( this );
        mLastMaximumPlayers = Integer.parseInt( mSharedPreferences.getString( KEY_PREF_MIN_PLAYERS, DEFAULT_PREF_MIN_PLAYERS ) );
        mLastMaximumPlayers = Integer.parseInt( mSharedPreferences.getString( KEY_PREF_MAX_PLAYERS, DEFAULT_PREF_MAX_PLAYERS ) );
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mGameCreatorFragment.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
    }

    @Override
    protected void onPause()
    {
        mGameCreatorFragment.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        PreferenceManager.getDefaultSharedPreferences( this ).edit().clear().apply();
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
    {
        if( KEY_PREF_MIN_PLAYERS.equals( key ) )
        {
            int minPlayers = Integer.parseInt( sharedPreferences.getString( key, DEFAULT_PREF_MIN_PLAYERS ) );
            if( 0 < minPlayers && minPlayers < mLastMaximumPlayers )
            {
                mLastMinimumPlayers = minPlayers;
            }
            else
            {
                mSharedPreferences.edit().putString( key, Integer.toString( mLastMinimumPlayers ) ).apply();
                String title = "Invalid Input";
                String message = "Minimum players must be greater than 0 and less than maximum players allowed.";
                showAlert( this, title, message );
            }
            mGameCreatorFragment.findPreference( key ).setSummary( sharedPreferences.getString( key, DEFAULT_PREF_MIN_PLAYERS ) );
        }
        else if( KEY_PREF_MAX_PLAYERS.equals( key ) )
        {
            int maxPlayers = Integer.parseInt( sharedPreferences.getString( key, DEFAULT_PREF_MAX_PLAYERS ) );
            if( mLastMinimumPlayers < maxPlayers && maxPlayers < 8 )
            {
                mLastMaximumPlayers = maxPlayers;
            }
            else
            {
                mSharedPreferences.edit().putString( KEY_PREF_MAX_PLAYERS, Integer.toString( mLastMaximumPlayers ) ).apply();
                String title = "Invalid Input";
                String message = "Maximum players allowed must be greater than minimum required players and less than 8.";
                showAlert( this, title, message );
            }
            mGameCreatorFragment.findPreference( key ).setSummary( sharedPreferences.getString( key, DEFAULT_PREF_MAX_PLAYERS ) );
        }
        else
        {
            Preference preference = mGameCreatorFragment.findPreference( key );
            if( preference instanceof EditTextPreference
             || preference instanceof ListPreference )
            {
                preference.setSummary( sharedPreferences.getString( key, "" ) );
            }

        }
    }

    private void showAlert( Context context, String title, String message )
    {
        new AlertDialog.Builder( context )
            .setTitle( title )
            .setMessage( message )
            .setPositiveButton( "OK", null )
            .show();
    }

    public static class GameCreatorFragment extends PreferenceFragment
    {
        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            addPreferencesFromResource( R.xml.game_creator_preferences );

            GameCreatorActivity gameCreatorActivity = (GameCreatorActivity)getActivity();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( gameCreatorActivity );
            for( String key : PREF_KEYS )
            {
                gameCreatorActivity.onSharedPreferenceChanged( sharedPreferences, key );
            }

            PreferenceScreen screen = (PreferenceScreen)this.findPreference( KEY_CARD_POINTS_SCREEN );
            for( int i = 0; i < 52; i++ )
            {
                EditTextPreference editTextPreference = new EditTextPreference( gameCreatorActivity );
                editTextPreference.setTitle( "Card " + i + " point value" );
                editTextPreference.setKey( "card_" + i + "_point_value" );
                editTextPreference.setPersistent( false );
                screen.addPreference( editTextPreference );
            }
        }
    }
}
