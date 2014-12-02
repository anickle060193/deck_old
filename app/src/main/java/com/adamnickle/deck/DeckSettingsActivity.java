package com.adamnickle.deck;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.adamnickle.deck.Game.DeckSettings;

public class DeckSettingsActivity extends ActionBarActivity
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.content );

        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        getFragmentManager()
                .beginTransaction()
                .replace( R.id.content, new DeckPreferenceFragment() )
                .commit();
    }

    public static class DeckPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
    {
        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            addPreferencesFromResource( R.xml.deck_settings_preferences );
            setHasOptionsMenu( true );
            setRetainInstance( true );

            bindPreferenceSummaryToValue( findPreference( DeckSettings.PLAYER_NAME ) );
            bindPreferenceSummaryToValue( findPreference( DeckSettings.CARD_BACK ) );
        }

        private void bindPreferenceSummaryToValue( Preference preference )
        {
            preference.setOnPreferenceChangeListener( this );

            this.onPreferenceChange( preference,
                    PreferenceManager
                            .getDefaultSharedPreferences( preference.getContext() )
                            .getString( preference.getKey(), "" ) );
        }

        @Override
        public boolean onPreferenceChange( Preference preference, Object value )
        {
            String stringValue = value.toString();

            if( preference instanceof ListPreference )
            {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue( stringValue );
                preference.setSummary( index >= 0 ? listPreference.getEntries()[ index ].toString() : null );

            }
            else
            {
                preference.setSummary( stringValue );
            }
            return true;
        }

        @Override
        public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
        {
            inflater.inflate( R.menu.deck_settings, menu );
            menu.findItem( R.id.done ).setIcon( Icons.getDoneActionIcon( getActivity() ) );
        }

        @Override
        public boolean onOptionsItemSelected( MenuItem item )
        {
            switch( item.getItemId() )
            {
                case R.id.done:
                    getActivity().finish();
                    return true;

                default:
                    return super.onOptionsItemSelected( item );
            }
        }
    }
}
