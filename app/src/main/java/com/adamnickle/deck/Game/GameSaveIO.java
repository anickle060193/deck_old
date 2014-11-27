package com.adamnickle.deck.Game;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adamnickle.deck.BuildConfig;
import com.adamnickle.deck.R;
import com.adamnickle.deck.SwipeDismissTouchListener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ru.noties.debug.Debug;

public final class GameSaveIO
{
    private static final String GAME_SAVE_FOLDER = "deck_game_saves";
    private static final String GAME_SAVE_PREFIX = "deck_game_save_";
    private static final String GAME_SAVE_FILE_EXTENSION = ".json";

    private static final String PLAYERS_NAME = "players";
    private static final String LEFT_PLAYERS_NAME = "left_players";

    private GameSaveIO() { }

    private static String getGameSaveFileName( String gameSaveName )
    {
        return GAME_SAVE_PREFIX + gameSaveName + GAME_SAVE_FILE_EXTENSION;
    }

    private static String getGameSaveNameFromFile( File gameSave )
    {
        final String fileName = gameSave.getName();
        return fileName.substring( GAME_SAVE_PREFIX.length(), fileName.length() - GAME_SAVE_FILE_EXTENSION.length() );
    }

    public static boolean openGameSave( File gameSave, HashMap<String, CardHolder > players, HashMap<String, CardHolder > leftPlayers )
    {
        boolean success = true;
        InputStreamReader inputStreamReader = null;
        try
        {
            inputStreamReader = new FileReader( gameSave );
            JsonReader reader = new JsonReader( inputStreamReader );

            reader.beginObject();
            while( reader.hasNext() )
            {
                String name = reader.nextName();
                if( name.equals( PLAYERS_NAME ) )
                {
                    reader.beginArray();
                    while( reader.hasNext() )
                    {
                        CardHolder player = CardHolder.readFromJson( reader );
                        players.put( player.getID(), player );
                    }
                    reader.endArray();
                }
                else if( name.equals( LEFT_PLAYERS_NAME ) )
                {
                    reader.beginArray();
                    while( reader.hasNext() )
                    {
                        CardHolder player = CardHolder.readFromJson( reader );
                        leftPlayers.put( player.getID(), player );
                    }
                    reader.endArray();
                }
                else
                {
                    reader.skipValue();
                }
            }
            reader.endObject();
        }
        catch( IOException io )
        {
            Debug.e( "Failed to save game.", io );
            success = false;
        }
        catch( IllegalStateException se )
        {
            Debug.e( "Failed to save game.", se );
            success = false;
        }
        finally
        {
            try
            {
                if( inputStreamReader != null )
                {
                    inputStreamReader.close();
                }
            }
            catch( IOException io )
            {
                io.printStackTrace();
            }
        }

        return success;
    }

    public static boolean saveGame( Context context, String gameSaveName, CardHolder[] players, CardHolder[] leftPlayers )
    {
        if( gameSaveName.isEmpty() || gameSaveName.contains( " " ) )
        {
            return false;
        }

        boolean success = true;
        OutputStreamWriter outputStreamWriter = null;
        try
        {
            File gameSaveDir = new File( context.getApplicationContext().getFilesDir(), GAME_SAVE_FOLDER );
            if( !gameSaveDir.exists() || !gameSaveDir.isDirectory() )
            {
                if( !gameSaveDir.mkdirs() )
                {
                    throw new IOException( "Could not create Deck game save folder." );
                }
            }


            File saveFile = new File( gameSaveDir, getGameSaveFileName( gameSaveName ) );
            outputStreamWriter = new FileWriter( saveFile );
            JsonWriter writer = new JsonWriter( outputStreamWriter );

            writer.setIndent( "    " );
            writer.beginObject();

            writer.name( PLAYERS_NAME ).beginArray();
            for( CardHolder player : players )
            {
                player.writeToJson( writer );
            }
            writer.endArray();

            writer.name( LEFT_PLAYERS_NAME ).beginArray();
            for( CardHolder player : leftPlayers )
            {
                player.writeToJson( writer );
            }
            writer.endArray();

            writer.endObject();
        }
        catch( IOException io )
        {
            Debug.e( "Failed to save game.", io );
            success = false;
        }
        catch( IllegalStateException se )
        {
            Debug.e( "Failed to save game.", se );
            success = false;
        }
        finally
        {
            try
            {
                if( outputStreamWriter != null )
                {
                    outputStreamWriter.close();
                }
            }
            catch( IOException io )
            {
                io.printStackTrace();
            }
        }
        return success;
    }

    public static RecyclerView getGameSaveCards( Context context )
    {
        final File file = new File( context.getFilesDir(), GAME_SAVE_FOLDER );
        final File[] gameSaveFiles = file.listFiles( new FilenameFilter()
        {
            @Override
            public boolean accept( File file, String s )
            {
                return s.startsWith( GAME_SAVE_PREFIX );
            }
        } );
        if( gameSaveFiles == null || gameSaveFiles.length == 0 )
        {
            return null;
        }
        else
        {
            final GameSaveCardAdapter adapter = new GameSaveCardAdapter( context, gameSaveFiles );
            final RecyclerView recyclerView = new RecyclerView( context );
            recyclerView.setHasFixedSize( false );
            recyclerView.setAdapter( adapter );
            recyclerView.setLayoutManager( new LinearLayoutManager( context ) );
            return recyclerView;
        }
    }

    public static class GameSaveCardAdapter extends RecyclerView.Adapter<GameSaveCardAdapter.GameSaveHolder>
    {
        private final Context mContext;
        private ArrayList<File> mData;
        private GameSaveOnClickListener mListener;

        public GameSaveCardAdapter( Context context, File[] data )
        {
            this( context, Arrays.asList( data ) );
        }

        public GameSaveCardAdapter( Context context, List<File> data )
        {
            mContext = context;
            mData = new ArrayList<File>( data );
        }

        public void setGameSaveOnClickListener( GameSaveOnClickListener listener )
        {
            mListener = listener;
        }

        @Override
        public GameSaveHolder onCreateViewHolder( ViewGroup viewGroup, int i )
        {
            return new GameSaveHolder( LayoutInflater.from( mContext ).inflate( R.layout.gamesave_card_layout, viewGroup, false ) );
        }

        @Override
        public void onBindViewHolder( final GameSaveHolder gameSaveHolder, final int position )
        {
            final File gameSave = getItem( position );
            gameSaveHolder.Title.setText( getGameSaveNameFromFile( gameSave ) );
            gameSaveHolder.Subtitle.setText( DateFormat.format( "h:mm aa - MMMM d, yyyy", gameSave.lastModified() ) );
            gameSaveHolder.Card.setOnTouchListener( new SwipeDismissTouchListener( gameSaveHolder.Card, null, new SwipeDismissTouchListener.OnDismissCallback()
            {
                @Override
                public void onDismiss( View view, Object token )
                {
                    GameSaveCardAdapter.this.removeItemAt( position );
                }
            } ) );
            gameSaveHolder.Card.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    if( mListener != null )
                    {
                        mListener.onGameSaveClick( gameSave );
                    }
                }
            } );
            final HashMap< String, CardHolder > cardHolders = new HashMap< String, CardHolder >();
            if( GameSaveIO.openGameSave( gameSave, cardHolders, cardHolders ) )
            {
                final StringBuilder sb = new StringBuilder();
                for( CardHolder cardHolder : cardHolders.values() )
                {
                    sb.append( cardHolder.getName() ).append( "\n" );
                    if( BuildConfig.DEBUG )
                    {
                        for( Card card : cardHolder.getCards() )
                        {
                            sb.append( "\t" ).append( card.toString() ).append( "\n" );
                        }
                    }
                }
                sb.deleteCharAt( sb.length() - 1 );
                gameSaveHolder.Players.setText( sb.toString() );
            }
        }

        public File getItem( int i )
        {
            return mData.get( i );
        }

        @Override
        public int getItemCount()
        {
            return mData.size();
        }

        public boolean removeItemAt( int i )
        {
            final File gameSave = mData.get( i );
            if( gameSave != null && gameSave.delete() )
            {
                mData.remove( i );
                //notifyItemRemoved( i );
                notifyDataSetChanged();
                return true;
            }
            else
            {
                return false;
            }
        }

        public class GameSaveHolder extends RecyclerView.ViewHolder
        {
            CardView Card;
            TextView Title;
            TextView Subtitle;
            TextView Players;

            public GameSaveHolder( View itemView )
            {
                super( itemView );

                Card = (CardView) itemView;
                Title = (TextView) itemView.findViewById( R.id.title );
                Subtitle = (TextView) itemView.findViewById( R.id.subtitle );
                Players = (TextView) itemView.findViewById( R.id.playerList );
            }
        }

        public interface GameSaveOnClickListener
        {
            public void onGameSaveClick( File scratchPad );
        }
    }
}
