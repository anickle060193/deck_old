package com.adamnickle.deck.Game;


import android.app.AlertDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.adamnickle.deck.DialogHelper;
import com.adamnickle.deck.Icons;
import com.adamnickle.deck.R;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

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

    public static ListView getGameSaveListView( Context context )
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
            final GameSaveSwipeAdapter gameSaveSwipeAdapter = new GameSaveSwipeAdapter( context, gameSaveFiles );
            final ListView listView = new ListView( context );
            listView.setAdapter( gameSaveSwipeAdapter );
            return listView;
        }
    }

    public static class GameSaveSwipeAdapter extends DialogHelper.SwipeArrayAdapter<File>
    {
        private static final View.OnClickListener NO_CLICK_LISTENER = new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {

            }
        };

        public GameSaveSwipeAdapter( Context context, File[] gameSaves )
        {
            super( context, R.layout.two_line_swipe, gameSaves );
        }

        @Override
        public void fillValues( int position, View view )
        {
            GameSaveHolder holder = (GameSaveHolder) view.getTag();
            if( holder == null )
            {
                holder = new GameSaveHolder();
                holder.GameSaveName = (TextView) view.findViewById( R.id.name );
                holder.GameSaveDateTime = (TextView) view.findViewById( R.id.dateTime );
                holder.InfoButton = (ImageButton) view.findViewById( R.id.infoButton );
                holder.InfoButton.setImageDrawable( Icons.getGameSaveSwipeInfo( getContext() ) );
                holder.DeleteButton = (ImageButton) view.findViewById( R.id.deleteButton );
                holder.DeleteButton.setImageDrawable( Icons.getGameSaveDelete( getContext() ) );
                holder.Under = view.findViewById( R.id.under );
                view.setTag( holder );
            }

            final File gameSave = getItem( position );
            holder.GameSaveName.setText( getGameSaveNameFromFile( gameSave ) );
            holder.GameSaveDateTime.setText( DateFormat.format( "h:mm aa - MMMM d, yyyy", gameSave.lastModified() ) );
            holder.Under.setOnClickListener( NO_CLICK_LISTENER );
            holder.InfoButton.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    final HashMap< String, CardHolder > cardHolders = new HashMap< String, CardHolder >();
                    if( GameSaveIO.openGameSave( gameSave, cardHolders, cardHolders ) )
                    {
                        StringBuilder s = new StringBuilder();
                        s.append( DateFormat.format( "h:mm aa - MMM d, yyyy", gameSave.lastModified() ) ).append( "\n\n" );
                        s.append( "Players:\n" );
                        for( CardHolder cardHolder : cardHolders.values() )
                        {
                            s.append( "\t" ).append( cardHolder.getName() ).append( "\n" );
                        }
                        s.deleteCharAt( s.length() - 1 );

                        new AlertDialog.Builder( getContext() )
                                .setTitle( getGameSaveNameFromFile( gameSave ) )
                                .setMessage( s.toString() )
                                .setPositiveButton( "Close", null )
                                .show();
                    }
                }
            } );
            holder.DeleteButton.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    if( GameSaveSwipeAdapter.this.removeItem( gameSave ) )
                    {
                        gameSave.delete();
                    }
                }
            } );
        }

        public class GameSaveHolder
        {
            TextView GameSaveName;
            TextView GameSaveDateTime;
            ImageButton InfoButton;
            ImageButton DeleteButton;
            View Under;
        }
    }
}
