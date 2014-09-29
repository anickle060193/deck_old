package com.adamnickle.deck.Game;


import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

public final class GameSave
{
    private static final String GAME_SAVE_FOLDER = "deck_game_saves";
    private static final String GAME_SAVE_PREFIX = "deck_game_save";
    private static final String GAME_SAVE_DELIMITER = ";";
    private static final String GAME_SAVE_FILE_EXTENSION = ".json";

    private static final String GAME_SAVE_FILE_NAME_FORMAT = GAME_SAVE_PREFIX + GAME_SAVE_DELIMITER + "%s" + GAME_SAVE_DELIMITER + "%d" + GAME_SAVE_DELIMITER + GAME_SAVE_FILE_EXTENSION;

    private static final String PLAYERS_NAME = "players";
    private static final String LEFT_PLAYERS_NAME = "left_players";

    public String SaveName;
    public Date SavedDate;

    public GameSave( String saveName, Date savedDate )
    {
        SaveName = saveName;
        SavedDate = savedDate;
    }

    public GameSave( String saveName )
    {
        this( saveName, new Date() );
    }

    @Override
    public String toString()
    {
        return SaveName + " - " + SavedDate.toString();
    }

    public static GameSave parseGameSaveFileName( String gameSaveFileName )
    {
        String[] s = gameSaveFileName.split( Pattern.quote( GAME_SAVE_DELIMITER ) );
        return new GameSave( s[ 1 ], new Date( Long.parseLong( s[ 2 ] ) ) );
    }

    public static GameSave[] getGameSaves( Context context )
    {
        final File file = new File( context.getFilesDir(), GAME_SAVE_FOLDER );
        final String[] filesNames = file.list( new FilenameFilter()
        {
            @Override
            public boolean accept( File file, String fileName )
            {
                return fileName.startsWith( GAME_SAVE_PREFIX ) && fileName.endsWith( GAME_SAVE_FILE_EXTENSION );
            }
        } );

        if( filesNames != null )
        {
            final GameSave[] gameSaves = new GameSave[ filesNames.length ];
            for( int i = 0; i < filesNames.length; i++ )
            {
                gameSaves[ i ] = GameSave.parseGameSaveFileName( filesNames[ i ] );
            }
            return gameSaves;
        }
        else
        {
            return new GameSave[ 0 ];
        }
    }

    private static String getGameSaveFileName( GameSave gameSave )
    {
        return String.format( GAME_SAVE_FILE_NAME_FORMAT, gameSave.SaveName, gameSave.SavedDate.getTime() );
    }

    public static boolean openGameSave( Context context, GameSave gameSave, HashMap<String, CardHolder > players, HashMap<String, CardHolder > leftPlayers )
    {
        boolean success = true;
        InputStreamReader inputStreamReader = null;
        try
        {
            File gameSaveDir = new File( context.getApplicationContext().getFilesDir(), GAME_SAVE_FOLDER );
            File saveFile = new File( gameSaveDir, getGameSaveFileName( gameSave ) );

            inputStreamReader = new FileReader( saveFile );
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
            Log.e( "ServerGameConnection", "Failed to save game.", io );
            success = false;
        }
        catch( IllegalStateException se )
        {
            Log.e( "ServerGameConnection", "Failed to save game.", se );
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
                throw new RuntimeException( io );
            }
        }

        return success;
    }

    public static boolean saveGame( Context context, GameSave gameSave, CardHolder[] players, CardHolder[] leftPlayers )
    {
        boolean success = true;
        OutputStreamWriter outputStreamWriter = null;
        try
        {
            File gameSaveDir = new File( context.getApplicationContext().getFilesDir(), GAME_SAVE_FOLDER );
            if( !gameSaveDir.exists() || !gameSaveDir.isDirectory() )
            {
                if( !gameSaveDir.mkdirs())
                {
                    throw new IOException( "Could not create Deck game save folder." );
                }
            }


            File saveFile = new File( gameSaveDir, getGameSaveFileName( gameSave ) );
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
            Log.e( "ServerGameConnection", "Failed to save game.", io );
            success = false;
        }
        catch( IllegalStateException se )
        {
            Log.e( "ServerGameConnection", "Failed to save game.", se );
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
                throw new RuntimeException( io );
            }
        }
        return success;
    }
}
