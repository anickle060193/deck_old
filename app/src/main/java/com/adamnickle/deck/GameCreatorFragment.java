package com.adamnickle.deck;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.adamnickle.deck.Game.GameSettings;


public class GameCreatorFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
{
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        setRetainInstance( true );
        setHasOptionsMenu( true );

        getActivity().setResult( Activity.RESULT_CANCELED );

        addPreferencesFromResource( R.xml.game_creator_preferences );

        initializePreferences();
    }

    private void initializePreferences()
    {
        PreferenceManager.setDefaultValues( this.getActivity(), R.xml.game_creator_preferences, true );

        bindPreferenceSummaryToValue( findPreference( GameSettings.KEY_PREF_DRAW_PILE_COUNT ) );
        bindPreferenceSummaryToValue( findPreference( GameSettings.KEY_PREF_DISCARD_PILE_COUNT ) );
    }

    private void bindPreferenceSummaryToValue( Preference preference )
    {
        preference.setOnPreferenceChangeListener( this );

        this.onPreferenceChange(
                preference,
                PreferenceManager
                        .getDefaultSharedPreferences( preference.getContext() )
                        .getString( preference.getKey(), "" ) );
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

    public void returnResult()
    {
        this.getActivity().setResult( Activity.RESULT_OK );
        this.getActivity().finish();
    }

    @Override
    public boolean onPreferenceChange( Preference preference, Object newValue )
    {
        final String key = preference.getKey();
        if( GameSettings.KEY_PREF_DRAW_PILE_COUNT.equals( key ) )
        {
            preference.setSummary( Integer.parseInt( (String) newValue ) + " piles" );
        }
        else if( GameSettings.KEY_PREF_DISCARD_PILE_COUNT.equals( key ) )
        {
            preference.setSummary( Integer.parseInt( (String) newValue ) + " piles" );
        }
        return true;
    }
}
