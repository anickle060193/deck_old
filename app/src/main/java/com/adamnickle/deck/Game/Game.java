package com.adamnickle.deck.Game;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;

import ru.noties.debug.Debug;

public class Game implements Parcelable
{
    private static final String GAME_FOLDER = "deck_custom_games";

    private static final String NAME_DRAW_PILES = "draw_piles";
    private static final String NAME_DISCARD_PILES = "discard_piles";
    private static final String NAME_GAME_NAME = "game_name";

    public static final int MAX_DRAW_PILES = 4;
    public static final int MAX_DISCARD_PILES = 4;

    public String GameName = null;
    public int DrawPiles = 0;
    public int DiscardPiles = 0;

    public Game()
    {
    }

    private Game( Parcel in )
    {
        this.DrawPiles = in.readInt();
        this.DiscardPiles = in.readInt();
        this.GameName = in.readString();
    }

    @Override
    public void writeToParcel( Parcel destination, int flags )
    {
        destination.writeInt( this.DrawPiles );
        destination.writeInt( this.DiscardPiles );
        destination.writeString( this.GameName );
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public String toString()
    {
        return this.GameName;
    }

    public String toJSON()
    {
        final StringWriter stringWriter = new StringWriter();
        try
        {
            Game.writeGame( this, stringWriter );
        }
        catch( IOException e )
        {
            Debug.e( "Could not write the Game to JSON.", e );
        }
        return stringWriter.toString();
    }

    public static final Parcelable.Creator<Game> CREATOR = new Creator<Game>()
    {
        @Override
        public Game createFromParcel( Parcel source )
        {
            return new Game( source );
        }

        @Override
        public Game[] newArray( int size )
        {
            return new Game[ size ];
        }
    };

    public static ArrayList<Game> getSavedCustomGames( Context context )
    {
        final File file = new File( context.getFilesDir(), GAME_FOLDER );
        final File[] gameFiles = file.listFiles();
        if( gameFiles == null || gameFiles.length == 0 )
        {
            return new ArrayList<>();
        }
        else
        {
            final ArrayList<Game> games = new ArrayList<>( gameFiles.length );
            for( File gameFile : gameFiles )
            {
                games.add( Game.openGame( gameFile ) );
            }
            return games;
        }
    }

    public static Game openGame( File file )
    {
        Game game = null;
        FileReader input = null;
        try
        {
            input = new FileReader( file );
            game = Game.readGame( input );
        }
        catch( IOException io )
        {
            Debug.e( "Failed to open Game.", io );
        }
        finally
        {
            if( input != null )
            {
                try
                {
                    input.close();
                }
                catch( IOException e )
                {
                    Debug.e( "Could not close Game input.", e );
                }
            }
        }

        return game;
    }

    private static String getGameFileName( Game game )
    {
        return game.GameName.replace( ' ', '_' ) + "__" + android.text.format.DateFormat.format( "yyyy-MM-dd__HH:mm:ss", Calendar.getInstance() );
    }

    public static boolean saveGame( Context context, Game game  )
    {
        if( game.GameName.isEmpty() || game.GameName.contains( " " ) )
        {
            return false;
        }

        boolean success = true;
        FileWriter output = null;
        try
        {
            File gamesDirectory = new File( context.getApplicationContext().getFilesDir(), GAME_FOLDER );
            if( !gamesDirectory.exists() || !gamesDirectory.isDirectory() )
            {
                if( !gamesDirectory.mkdirs() )
                {
                    throw new IOException( "Could not create Deck game folder." );
                }
            }


            File saveFile = new File( gamesDirectory, getGameFileName( game ) );
            output = new FileWriter( saveFile );

            success = Game.writeGame( game, output );
        }
        catch( IOException e )
        {
            Debug.e( "Failed to save game.", e );
            success = false;
        }
        finally
        {
            try
            {
                if( output != null )
                {
                    output.close();
                }
            }
            catch( IOException io )
            {
                Debug.e( "Could not close Game output.", io );
            }
        }
        return success;
    }

    private static Game readGame( Reader input ) throws IOException
    {
        Game game = new Game();

        JsonReader reader = new JsonReader( input );

        reader.beginObject();

        while( reader.hasNext() )
        {
            String name = reader.nextName();
            switch( name )
            {
                case NAME_DRAW_PILES:
                    game.DrawPiles = reader.nextInt();
                    break;

                case NAME_DISCARD_PILES:
                    game.DiscardPiles = reader.nextInt();
                    break;

                case NAME_GAME_NAME:
                    game.GameName = reader.nextString();
                    break;

                default:
                    reader.skipValue();
                    break;
            }
        }

        reader.endObject();

        return game;
    }

    private static boolean writeGame( Game game, Writer output ) throws IOException
    {
        JsonWriter writer = new JsonWriter( output );

        writer.setIndent( "  " );
        writer.beginObject();
        writer.name( NAME_GAME_NAME ).value( game.GameName );
        writer.name( NAME_DRAW_PILES ).value( game.DrawPiles );
        writer.name( NAME_DISCARD_PILES ).value( game.DiscardPiles );
        writer.endObject();

        return true;
    }
}
