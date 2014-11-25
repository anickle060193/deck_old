package com.adamnickle.deck;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import it.gmariotti.changelibs.library.view.ChangeLogListView;


public class InfoActivity extends ActionBarActivity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_info );

        ( (TextView) findViewById( R.id.versionTextView ) ).setText( "Version: " + BuildConfig.VERSION_NAME );

        final ListView listView = (ListView) findViewById( R.id.list );
        final String[] items = { "Change Log" };
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, items );
        listView.setAdapter( adapter );
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick( AdapterView<?> adapterView, View view, int i, long l )
            {
                switch( i )
                {
                    case 0:
                        DialogHelper.createBlankAlertDialog( InfoActivity.this, "Deck Change Log" )
                                .setView( new ChangeLogListView( InfoActivity.this ) )
                                .setPositiveButton( "Close", null )
                                .show();
                        break;
                }
            }
        } );
    }
}
