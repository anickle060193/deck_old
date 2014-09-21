package com.adamnickle.deck;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class DeckSettingsActivity extends Activity
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setupActionBar();
        getFragmentManager().beginTransaction()
                .replace( android.R.id.content, new DeckPreferenceFragment() )
                .commit();
    }

    private void setupActionBar()
    {
        ActionBar actionBar = getActionBar();
        if( actionBar != null )
        {
            actionBar.setDisplayHomeAsUpEnabled( true );
        }
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask( this );
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private static Preference.OnPreferenceChangeListener sPreferenceChangeListener = new Preference.OnPreferenceChangeListener()
    {
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
    };

    private static void bindPreferenceSummaryToValue( Preference preference )
    {
        preference.setOnPreferenceChangeListener( sPreferenceChangeListener );

        sPreferenceChangeListener.onPreferenceChange( preference,
                PreferenceManager
                        .getDefaultSharedPreferences( preference.getContext() )
                        .getString( preference.getKey(), "" ) );
    }

    public static class DeckPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            addPreferencesFromResource( R.xml.deck_settings_preferences );
            setHasOptionsMenu( true );
            setRetainInstance( true );

            bindPreferenceSummaryToValue( findPreference( DeckSettings.PLAYER_NAME ) );
            bindPreferenceSummaryToValue( findPreference( DeckSettings.BACKGROUND ) );
        }

        @Override
        public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
        {
            super.onCreateOptionsMenu( menu, inflater );
            inflater.inflate( R.menu.deck_settings, menu );
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
