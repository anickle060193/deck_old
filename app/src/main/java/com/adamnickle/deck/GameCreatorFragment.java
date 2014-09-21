package com.adamnickle.deck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.Deck;
import com.adamnickle.deck.Game.GameSettings;

import java.util.HashMap;


public class GameCreatorFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
{
    private static final String TAG = "GameCreatorFragment";

    public static final String EXTRA_GAME = "game";

    private static final String KEY_PREF_GAME_NAME = "pref_game_name";
    private static final String KEY_PREF_MIN_PLAYERS = "pref_min_players";
    private static final String KEY_PREF_MAX_PLAYERS = "pref_max_players";
    private static final String KEY_PREF_POINTS_CATEGORY = "pref_card_points_category";
    private static final String KEY_PREF_TRACK_POINTS = "pref_track_points";
    private static final String KEY_PREF_POINTS_VALUE_STYLE = "pref_points_value_style";
    private static final String KEY_PREF_CUSTOM_POINTS_STYLE_SCREEN = "pref_custom_points_value_screen";
    private static final String KEY_PREF_CARD_POINTS_SET_METHOD = "pref_card_points_set_method";
    private static final String KEY_PREF_DEAL_FULL_DECK = "pref_deal_full_amount";
    private static final String KEY_PREF_INITIAL_CARDS_PER_PLAYER = "pref_initial_cards_per_player";
    private static final String KEY_PREF_SUITS_CATEGORY = "pref_suits_category";
    private static final String KEY_PREF_RANKS_CATEGORY = "pref_ranks_category";
    private static final String KEY_PREF_CARDS_CATEGORY = "pref_cards_category";
    private static final String KEY_PREF_CARDS_SPADES_CATEGORY = "pref_cards_spades_screen";
    private static final String KEY_PREF_CARDS_HEARTS_CATEGORY = "pref_cards_hearts_screen";
    private static final String KEY_PREF_CARDS_CLUBS_CATEGORY = "pref_cards_clubs_screen";
    private static final String KEY_PREF_CARDS_DIAMONDS_CATEGORY = "pref_cards_diamonds_screen";

    private PreferenceScreen mCardPointsValueScreen;
    private HashMap< String, PreferenceGroup > mCardPointsPreferences;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        Log.d( TAG, "+++ ON CREATE +++" );

        super.onCreate( savedInstanceState );
        setRetainInstance( true );
        setHasOptionsMenu( true );
        addPreferencesFromResource( R.xml.game_creator_preferences );
        PreferenceManager.setDefaultValues( this.getActivity(), R.xml.game_creator_preferences, true );

        mCardPointsPreferences = new HashMap< String, PreferenceGroup >();

        createCardPointsStylePreferences();

        setPreferenceChangeListener( this.getPreferenceScreen() );
        initializePreferences( this.getPreferenceScreen() );
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        inflater.inflate( R.menu.game_creator, menu );
        super.onCreateOptionsMenu( menu, inflater );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.done:
                returnResult();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    public void createCardPointsStylePreferences()
    {
        PreferenceScreen customPointsValueScreen = (PreferenceScreen) this.findPreference( KEY_PREF_CUSTOM_POINTS_STYLE_SCREEN );
        PreferenceCategory category;
        EditTextPreference pref;

        category = (PreferenceCategory) this.findPreference( KEY_PREF_SUITS_CATEGORY );
        for( int i = 0; i < Deck.SUITS; i++ )
        {
            pref = getEditTextPreference(
                    "pref_set_point_suit_" + Deck.SUIT_STRINGS[ i ],
                    Deck.SUIT_STRINGS[ i ],
                    "0",
                    InputType.TYPE_CLASS_NUMBER
            );
            category.addPreference( pref );
        }
        mCardPointsPreferences.put( "By Suit", category );
        customPointsValueScreen.removePreference( category );

        category = (PreferenceCategory) this.findPreference( KEY_PREF_RANKS_CATEGORY );
        for( int i = 0; i < Deck.RANKS; i++ )
        {
            pref = getEditTextPreference(
                    "pref_set_point_rank_" + Deck.RANK_STRINGS[ i ],
                    Deck.RANK_STRINGS[ i ],
                    "0",
                    InputType.TYPE_CLASS_NUMBER
            );
            category.addPreference( pref );
        }
        mCardPointsPreferences.put( "By Rank", category );
        customPointsValueScreen.removePreference( category );

        category = (PreferenceCategory) this.findPreference( KEY_PREF_CARDS_CATEGORY );
        PreferenceScreen spades = (PreferenceScreen) this.findPreference( KEY_PREF_CARDS_SPADES_CATEGORY );
        PreferenceScreen hearts = (PreferenceScreen) this.findPreference( KEY_PREF_CARDS_HEARTS_CATEGORY );
        PreferenceScreen clubs = (PreferenceScreen) this.findPreference( KEY_PREF_CARDS_CLUBS_CATEGORY );
        PreferenceScreen diamonds = (PreferenceScreen) this.findPreference( KEY_PREF_CARDS_DIAMONDS_CATEGORY );
        Card card;
        for( int i = 0; i < Deck.CARD_COUNT; i++ )
        {
            card = new Card( i );
            pref = getEditTextPreference(
                    "pref_set_point_card_" + i,
                    card.toString(),
                    "0",
                    InputType.TYPE_CLASS_NUMBER
            );
            switch( card.getSuit() )
            {
                case Deck.SPADES:
                    spades.addPreference( pref );
                    break;
                case Deck.HEARTS:
                    hearts.addPreference( pref );
                    break;
                case Deck.CLUBS:
                    clubs.addPreference( pref );
                    break;
                case Deck.DIAMONDS:
                    diamonds.addPreference( pref );
                    break;
            }
        }
        mCardPointsPreferences.put( "By Card", category );
        customPointsValueScreen.removePreference( category );
    }

    private void setPreferenceChangeListener( PreferenceGroup preferenceGroup )
    {
        int count = preferenceGroup.getPreferenceCount();
        Preference pref;
        for( int i = 0; i < count; i++ )
        {
            pref = preferenceGroup.getPreference( i );
            pref.setOnPreferenceChangeListener( this );
            if( pref instanceof PreferenceGroup )
            {
                setPreferenceChangeListener( (PreferenceGroup) pref );
            }
        }
    }

    private void initializePreferences( PreferenceGroup preferenceGroup )
    {
        Preference pref;
        Object value;
        for( int i = 0; i < preferenceGroup.getPreferenceCount(); i++ )
        {
            pref = preferenceGroup.getPreference( i );

            if( pref instanceof PreferenceGroup )
            {
                initializePreferences( (PreferenceGroup) pref );
            }
            else if( pref instanceof ListPreference )
            {
                value = ( (ListPreference) pref ).getValue();
                pref.getOnPreferenceChangeListener().onPreferenceChange( pref, value );
            }
            else if( pref instanceof EditTextPreference )
            {
                value = ( (EditTextPreference) pref ).getText();
                pref.getOnPreferenceChangeListener().onPreferenceChange( pref, value );
            }
            else if( pref instanceof CheckBoxPreference )
            {
                value = ( (CheckBoxPreference) pref ).isChecked();
                pref.getOnPreferenceChangeListener().onPreferenceChange( pref, value );
            }
        }
    }

    @Override
    public boolean onPreferenceChange( Preference preference, Object newValue )
    {
        String key = preference.getKey();

        if( KEY_PREF_MIN_PLAYERS.equals( key ) )
        {
            int maxPlayers = Integer.parseInt( ( (EditTextPreference) this.findPreference( KEY_PREF_MAX_PLAYERS ) ).getText() );
            int newMinPlayers = Integer.parseInt( (String) newValue );

            if( 0 < newMinPlayers && newMinPlayers <= maxPlayers )
            {
                preference.setSummary( newMinPlayers + " players" );
            }
            else
            {
                String title = "Invalid Input";
                String message = "Minimum players must be greater than 0 and less than maximum players allowed.";
                showAlert( this.getActivity(), title, message );
                return false;
            }
        }
        else if( KEY_PREF_MAX_PLAYERS.equals( key ) )
        {
            int minPlayers = Integer.parseInt( ( (EditTextPreference) this.findPreference( KEY_PREF_MIN_PLAYERS ) ).getText() );
            int newMaxPlayers = Integer.parseInt( (String) newValue );

            if( minPlayers <= newMaxPlayers && newMaxPlayers < 8 )
            {
                preference.setSummary( newMaxPlayers + " players" );
            }
            else
            {
                String title = "Invalid Input";
                String message = "Maximum players allowed must be greater than minimum required players and less than 8.";
                showAlert( this.getActivity(), title, message );
                return false;
            }
        }
        else if( KEY_PREF_INITIAL_CARDS_PER_PLAYER.equals( key ) )
        {
            int cards = Integer.parseInt( (String) newValue );
            if( 0 <= cards && cards <= Deck.CARD_COUNT )
            {
                preference.setSummary( cards + " cards" );
            }
            else
            {
                String title = "Invalid input";
                String message = "Number of cards to deal must be between 0 and " + Deck.CARD_COUNT;
                showAlert( this.getActivity(), title, message );
                return false;
            }
        }
        else if( key.startsWith( "pref_set_point" ) )
        {
            preference.setSummary( newValue + " points" );
        }
        else
        {
            if( preference instanceof EditTextPreference || preference instanceof ListPreference )
            {
                preference.setSummary( (String) newValue );
            }
        }

        if( KEY_PREF_POINTS_VALUE_STYLE.equals( key ) )
        {
            String style = (String) newValue;
            if( style.equals( "Custom" ) )
            {
                if( mCardPointsValueScreen != null )
                {
                    PreferenceGroup parent = (PreferenceGroup) this.findPreference( KEY_PREF_POINTS_CATEGORY );
                    parent.addPreference( mCardPointsValueScreen );
                    mCardPointsValueScreen = null;
                }
            }
            else
            {
                if( mCardPointsValueScreen == null )
                {
                    PreferenceScreen removePreference = (PreferenceScreen) this.findPreference( KEY_PREF_CUSTOM_POINTS_STYLE_SCREEN );
                    PreferenceGroup parent = (PreferenceGroup) this.findPreference( KEY_PREF_POINTS_CATEGORY );
                    parent.removePreference( removePreference );
                    mCardPointsValueScreen = removePreference;
                }
            }
        }

        if( KEY_PREF_CARD_POINTS_SET_METHOD.equals( key ) )
        {
            PreferenceScreen screen = (PreferenceScreen) this.findPreference( KEY_PREF_CUSTOM_POINTS_STYLE_SCREEN );
            while( screen.getPreferenceCount() > 1 )
            {
                screen.removePreference( screen.getPreference( 1 ) );
            }
            screen.addPreference( mCardPointsPreferences.get( newValue ) );
        }

        return true;
    }

    private EditTextPreference getEditTextPreference( String key, String title, String defaultValue, int inputType )
    {
        EditTextPreference editTextPreference = new EditTextPreference( this.getActivity() );
        editTextPreference.setTitle( title );
        editTextPreference.setPersistent( false );
        editTextPreference.setDefaultValue( defaultValue );
        editTextPreference.setSummary( defaultValue );
        editTextPreference.setKey( key );
        editTextPreference.getEditText().setInputType( inputType );
        editTextPreference.setDialogTitle( title );
        editTextPreference.setOnPreferenceChangeListener( this );
        this.onPreferenceChange( editTextPreference, defaultValue );
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

    public void returnResult()
    {
        Log.d( TAG, "returnResult()" );

        GameSettings gameSettings = new GameSettings();
        gameSettings.GameName = ( (EditTextPreference) this.findPreference( KEY_PREF_GAME_NAME ) ).getText();
        gameSettings.MinimumPlayers = Integer.parseInt( ( (EditTextPreference) this.findPreference( KEY_PREF_MIN_PLAYERS ) ).getText() );
        gameSettings.MaximumPlayers = Integer.parseInt( ( (EditTextPreference) this.findPreference( KEY_PREF_MAX_PLAYERS ) ).getText() );
        gameSettings.TrackPoints = ( (CheckBoxPreference) this.findPreference( KEY_PREF_TRACK_POINTS ) ).isChecked();
        if( gameSettings.TrackPoints )
        {
            gameSettings.CardPointValues = getCardPointValues();
        }
        else
        {
            gameSettings.CardPointValues = null;
        }
        gameSettings.DealFullDeck = ( (CheckBoxPreference) this.findPreference( KEY_PREF_DEAL_FULL_DECK ) ).isChecked();
        if( !gameSettings.DealFullDeck )
        {
            gameSettings.InitialCardsPerPlayer = Integer.parseInt( ( (EditTextPreference) this.findPreference( KEY_PREF_INITIAL_CARDS_PER_PLAYER ) ).getText() );
        }


        Intent resultIntent = new Intent();
        resultIntent.putExtra( EXTRA_GAME, gameSettings );
        this.getActivity().setResult( Activity.RESULT_OK, resultIntent );
        this.getActivity().finish();
    }

    private int getIdentifierForPointStyle( String style )
    {
        String identifierName = "card_point_values_" + style.replace( ' ', '_' ).toLowerCase();
        return getResources().getIdentifier( identifierName, "array", this.getActivity().getPackageName() );
    }

    private int[] getCardPointValues()
    {
        int values[];
        ListPreference pointValueStylePreference = (ListPreference) this.findPreference( KEY_PREF_POINTS_VALUE_STYLE );
        String pointStyle = pointValueStylePreference.getValue();
        if( pointStyle.equals( "Custom" ) )
        {
            values = new int[ Deck.CARD_COUNT ];
            ListPreference cardPointsSetMethodPreference = (ListPreference) this.findPreference( KEY_PREF_CARD_POINTS_SET_METHOD );
            String setMethod = cardPointsSetMethodPreference.getValue();
            String key;
            EditTextPreference pref;
            int value;
            if( setMethod.equals( "By Suit" ) )
            {
                for( int i = 0; i < Deck.SUITS; i++ )
                {
                    key = "pref_set_point_suit_" + Deck.SUIT_STRINGS[ i ];
                    pref = (EditTextPreference) this.findPreference( key );
                    value = Integer.parseInt( pref.getText() );
                    for( int j = i * Deck.RANKS; j < i * Deck.RANKS + Deck.RANKS; j++ )
                    {
                        values[ j ] = value;
                    }
                }
            }
            else if( setMethod.equals( "By Rank" ) )
            {
                for( int i = 0; i < Deck.RANKS; i++ )
                {
                    key = "pref_set_point_rank_" + Deck.RANK_STRINGS[ i ];
                    pref = (EditTextPreference) this.findPreference( key );
                    value = Integer.parseInt( pref.getText() );
                    for( int j = i; j < Deck.CARD_COUNT; j += Deck.RANKS )
                    {
                        values[ j ] = value;
                    }
                }
            }
            else if( setMethod.equals( "By Card" ) )
            {
                for( int i = 0; i < Deck.CARD_COUNT; i++ )
                {
                    key = "pref_set_point_card_" + i;
                    pref = (EditTextPreference) this.findPreference( key );
                    value = Integer.parseInt( pref.getText() );
                    values[ i ] = value;
                }
            }
        }
        else
        {
            values = getResources().getIntArray( getIdentifierForPointStyle( pointStyle ) );
        }
        return values;
    }
}
