package com.adamnickle.deck;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.adamnickle.deck.Game.Game;


public class GameCreatorActivity extends ActionBarActivity
{
    public static final String EXTRA_GAME = "game";

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        this.setResult( RESULT_CANCELED );
        setContentView( R.layout.content );

        if( savedInstanceState == null )
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add( R.id.content, new GameCreatorFragment(), GameCreatorFragment.class.getName() )
                    .commit();
        }
    }

    public static class GameCreatorFragment extends ListFragment
    {
        private ListAdapter mListAdapter;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            setRetainInstance( true );
            setHasOptionsMenu( true );

            if( mListAdapter == null )
            {
                final Holder[] items = new Holder[]{ new Holder( "Number of draw piles", " piles" ), new Holder( "Number of discard piles", " piles" ) };
                mListAdapter = new ArrayAdapter< Holder >( getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, items )
                {
                    @Override
                    public View getView( int position, View convertView, ViewGroup parent )
                    {
                        Holder holder = getItem( position );
                        ViewGroup viewGroup = (ViewGroup) super.getView( position, convertView, parent );
                        ( (TextView) viewGroup.findViewById( android.R.id.text2 ) ).setText( holder.getValueString() );
                        return viewGroup;
                    }
                };
            }

            this.setListAdapter( mListAdapter );
        }

        @Override
        public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
        {
            super.onCreateOptionsMenu( menu, inflater );
            inflater.inflate( R.menu.game_creator, menu );
        }

        @Override
        public boolean onOptionsItemSelected( MenuItem item )
        {
            switch( item.getItemId() )
            {
                case R.id.done:
                    final Game game = new Game();
                    game.DrawPiles = ( (Holder) getListAdapter().getItem( 0 ) ).Value;
                    game.DiscardPiles = ( (Holder) getListAdapter().getItem( 1 ) ).Value;

                    final Intent result = new Intent();
                    result.putExtra( GameCreatorActivity.EXTRA_GAME, game );
                    getActivity().setResult( Activity.RESULT_OK, result );
                    getActivity().finish();
                    return true;

                default:
                    return super.onOptionsItemSelected( item );
            }
        }

        @Override
        public void onListItemClick( ListView l, View v, final int position, long id )
        {
            final View view = LayoutInflater.from( getActivity() ).inflate( R.layout.numberpicker_layout, null );

            final NumberPicker numberPicker = (NumberPicker) view.findViewById( R.id.numberPicker );
            numberPicker.setMinValue( 0 );
            numberPicker.setMaxValue( 4 );
            numberPicker.setWrapSelectorWheel( false );

            DialogHelper.createBlankAlertDialog( getActivity(), ( (TextView) v.findViewById( android.R.id.text1 ) ).getText().toString() )
                    .setView( view )
                    .setPositiveButton( "OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialog, int which )
                        {
                            ( (Holder) getListAdapter().getItem( position ) ).Value = numberPicker.getValue();
                            ( (ArrayAdapter) getListAdapter() ).notifyDataSetChanged();
                        }
                    } )
                    .setNegativeButton( "Cancel", null )
                    .show();
        }

        public static class Holder
        {
            public String Prompt;
            public int Value;
            public String ValueSuffix;

            public Holder( String prompt, String valueSuffix )
            {
                this.Prompt = prompt;
                this.Value = 0;
                this.ValueSuffix = valueSuffix;
            }

            public String getValueString()
            {
                return Integer.toString( Value ) + ValueSuffix;
            }

            @Override
            public String toString()
            {
                return Prompt;
            }
        }
    }
}
