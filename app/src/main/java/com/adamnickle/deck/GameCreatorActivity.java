package com.adamnickle.deck;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;

import java.util.HashMap;


public class GameCreatorActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener
{
    public static final String KEY_PREF_GAME_NAME = "pref_game_name";
    public static final String KEY_PREF_MIN_PLAYERS = "pref_min_players";
    public static final String KEY_PREF_MAX_PLAYERS = "pref_max_players";
    public static final String KEY_PREF_POINTS_CATEGORY = "pref_card_points_category";
    public static final String KEY_PREF_TRACK_POINTS = "pref_track_points";
    public static final String KEY_PREF_POINTS_VALUE_STYLE = "pref_points_value_style";
    public static final String KEY_PREF_CUSTOM_POINTS_STYLE_SCREEN = "pref_custom_points_value_screen";
    public static final String KEY_PREF_CARD_POINTS_SET_METHOD = "pref_card_points_set_method";

    private static String PREF_POINTS_VALUE_STYLE_CUSTOM;

    private GameCreatorFragment mGameCreatorFragment;
    private PreferenceScreen mCardPointsValueScreen;
    private HashMap<String, Preference[]> mCardPointsPreferences;

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

        String[] pointValueStyles = getResources().getStringArray( R.array.points_value_styles );
        PREF_POINTS_VALUE_STYLE_CUSTOM = pointValueStyles[ pointValueStyles.length - 1 ];

        mCardPointsPreferences = new HashMap< String, Preference[] >();

        Preference bySuit[] = new Preference[ Deck.SUITS ];
        for( int i = 0; i < Deck.SUITS; i++ )
        {
            EditTextPreference suitPreference = getEditTextPreference(
                    "pref_suit_" + Deck.SUIT_STRINGS[ i ],
                    Deck.SUIT_STRINGS[ i ],
                    "0",
                    InputType.TYPE_CLASS_NUMBER
            );
            bySuit[ i ] = suitPreference;
        }
        mCardPointsPreferences.put( "By Suit", bySuit );

        Preference byRank[] = new Preference[ Deck.RANKS ];
        for( int i = 0; i < Deck.RANKS; i++ )
        {
            EditTextPreference rankPreference = getEditTextPreference(
                    "pref_rank_" + Deck.RANK_STRINGS[ i ],
                    Deck.RANK_STRINGS[ i ],
                    "0",
                    InputType.TYPE_CLASS_NUMBER
            );
            byRank[ i ] = rankPreference;
        }
        mCardPointsPreferences.put( "By Rank", byRank );

        Preference perCard[] = new Preference[ Deck.CARD_COUNT ];
        for( int i = 0; i < Deck.CARD_COUNT; i++ )
        {
            Card card = new Card( i );
            EditTextPreference cardPreference = getEditTextPreference(
                    "pref_card_" + i,
                    card.toString(),
                    "0",
                    InputType.TYPE_CLASS_NUMBER
            );
            perCard[ i ] = cardPreference;
        }
        mCardPointsPreferences.put( "By Card", perCard );
    }

    @Override
    protected void onDestroy()
    {
        PreferenceManager.getDefaultSharedPreferences( this ).edit().clear().apply();
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceChange( Preference preference, Object newValue )
    {
        String key = preference.getKey();

        if( KEY_PREF_MIN_PLAYERS.equals( key ) )
        {
            int maxPlayers = Integer.parseInt( ( (EditTextPreference) mGameCreatorFragment.findPreference( KEY_PREF_MAX_PLAYERS ) ).getText() );
            int newMinPlayers = Integer.parseInt( (String) newValue );

            if( 0 < newMinPlayers && newMinPlayers < maxPlayers )
            {
                preference.setSummary( (String) newValue );
            } else
            {
                String title = "Invalid Input";
                String message = "Minimum players must be greater than 0 and less than maximum players allowed.";
                showAlert( this, title, message );
                return false;
            }
        }
        else if( KEY_PREF_MAX_PLAYERS.equals( key ) )
        {
            int minPlayers = Integer.parseInt( ( (EditTextPreference) mGameCreatorFragment.findPreference( KEY_PREF_MIN_PLAYERS ) ).getText() );
            int newMaxPlayers = Integer.parseInt( (String) newValue );

            if( minPlayers < newMaxPlayers && newMaxPlayers < 8 )
            {
                preference.setSummary( (String) newValue );
            } else
            {
                String title = "Invalid Input";
                String message = "Maximum players allowed must be greater than minimum required players and less than 8.";
                showAlert( this, title, message );
                return false;
            }
        }
        else
        {
            if( preference instanceof EditTextPreference
             || preference instanceof ListPreference )
            {
                preference.setSummary( (String) newValue );
            }
        }

        if( KEY_PREF_POINTS_VALUE_STYLE.equals( key ) )
        {
            String style = (String)newValue;
            if( style.equals( PREF_POINTS_VALUE_STYLE_CUSTOM ) )
            {
                if( mCardPointsValueScreen != null )
                {
                    PreferenceGroup parent = (PreferenceGroup) mGameCreatorFragment.findPreference( KEY_PREF_POINTS_CATEGORY );
                    parent.addPreference( mCardPointsValueScreen );
                    mCardPointsValueScreen = null;
                }
            }
            else
            {
                if( mCardPointsValueScreen == null )
                {
                    PreferenceScreen removePreference = (PreferenceScreen) mGameCreatorFragment.findPreference( KEY_PREF_CUSTOM_POINTS_STYLE_SCREEN );
                    PreferenceGroup parent = (PreferenceGroup) mGameCreatorFragment.findPreference( KEY_PREF_POINTS_CATEGORY );
                    parent.removePreference( removePreference );
                    mCardPointsValueScreen = removePreference;
                }
            }
        }

        if( KEY_PREF_CARD_POINTS_SET_METHOD.equals( key ) )
        {
            PreferenceScreen screen = (PreferenceScreen)mGameCreatorFragment.findPreference( KEY_PREF_CUSTOM_POINTS_STYLE_SCREEN );
            Preference methodPreference = screen.getPreference( 0 );
            screen.removeAll();
            screen.addPreference( methodPreference );
            Preference preferences[] = mCardPointsPreferences.get( (String) newValue );
            for( Preference pref : preferences )
            {
                screen.addPreference( pref );
            }
        }

        return true;
    }

    private EditTextPreference getEditTextPreference( String key, String title, String defaultValue, int inputType )
    {
        EditTextPreference editTextPreference = new EditTextPreference( this );
        editTextPreference.setTitle( title );
        editTextPreference.setPersistent( false );
        editTextPreference.setDefaultValue( defaultValue );
        editTextPreference.setSummary( defaultValue );
        editTextPreference.setKey( key );
        editTextPreference.getEditText().setInputType( inputType );
        editTextPreference.setDialogTitle( title );
        return editTextPreference;
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

            GameCreatorActivity gameCreatorActivity = (GameCreatorActivity) getActivity();
            setPreferenceChangeListener( this.getPreferenceScreen(), gameCreatorActivity );
            initializePreferences( this.getPreferenceScreen(), gameCreatorActivity );
        }

        private void setPreferenceChangeListener( PreferenceGroup preferenceGroup, Preference.OnPreferenceChangeListener preferenceChangeListener )
        {
            int count = preferenceGroup.getPreferenceCount();
            for( int i = 0; i < count; i++ )
            {
                Preference preference = preferenceGroup.getPreference( i );
                preference.setOnPreferenceChangeListener( preferenceChangeListener );
                if( preference instanceof PreferenceGroup )
                {
                    setPreferenceChangeListener( (PreferenceGroup)preference, preferenceChangeListener );
                }
            }
        }

        private void initializePreferences( PreferenceGroup preferenceGroup, GameCreatorActivity gameCreatorActivity )
        {
            for( int i = 0; i < preferenceGroup.getPreferenceCount(); i++ )
            {
                Preference preference = preferenceGroup.getPreference( i );

                if( preference instanceof PreferenceGroup )
                {
                    initializePreferences( (PreferenceGroup)preference, gameCreatorActivity );
                }
                else if( preference instanceof ListPreference )
                {
                    ListPreference listPreference = (ListPreference) preference;
                    String value = listPreference.getValue();
                    gameCreatorActivity.onPreferenceChange( listPreference, value );
                }
                else if( preference instanceof EditTextPreference )
                {
                    EditTextPreference editTextPreference = (EditTextPreference) preference;
                    String value = editTextPreference.getText();
                    gameCreatorActivity.onPreferenceChange( editTextPreference, value );
                }
                else if( preference instanceof CheckBoxPreference )
                {
                    CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
                    boolean value = checkBoxPreference.isChecked();
                    gameCreatorActivity.onPreferenceChange( checkBoxPreference, value );
                }
            }
        }
    }
}
