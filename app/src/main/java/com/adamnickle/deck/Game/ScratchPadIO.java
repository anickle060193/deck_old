package com.adamnickle.deck.Game;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.adamnickle.deck.DialogHelper;
import com.adamnickle.deck.Icons;
import com.adamnickle.deck.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

public final class ScratchPadIO
{
    private static final String SCRATCH_PAD_SAVE_DIRECTORY = "deck_scratch_pads";
    private static final String SCRATCH_PAD_SAVE_PREFIX = "scratch_pad_";
    private static final String SCRATCH_PAD_SAVE_EXTENSION = ".png";

    private ScratchPadIO() { }

    private static boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals( state );
    }

    private static boolean isExternalStorageReadable()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals( state ) || Environment.MEDIA_MOUNTED_READ_ONLY.equals( state );
    }

    private static File getScratchPadStorageDirectory( Context context )
    {
        File file = new File( context.getExternalFilesDir( Environment.DIRECTORY_PICTURES ), SCRATCH_PAD_SAVE_DIRECTORY );
        if( !file.isDirectory() && !file.mkdirs() )
        {
            return null;
        }
        return file;
    }

    private static File getScratchPadFile( Context context, String scratchPadName, boolean writing )
    {
        if( ( writing && isExternalStorageWritable() ) || ( !writing && isExternalStorageReadable() ) )
        {
            if( !scratchPadName.isEmpty() && !scratchPadName.contains( " " ) && isExternalStorageWritable() )
            {
                return new File( getScratchPadStorageDirectory( context ), SCRATCH_PAD_SAVE_PREFIX + scratchPadName + SCRATCH_PAD_SAVE_EXTENSION );
            }
        }
        return null;
    }

    private static String getScratchPadNameFromFile( File scratchPad )
    {
        final String scratchPadName = scratchPad.getName();
        return scratchPadName.substring( SCRATCH_PAD_SAVE_PREFIX.length(), scratchPadName.length() - SCRATCH_PAD_SAVE_EXTENSION.length() );
    }

    public static interface Callback
    {
        public void onSuccess();
        public void onFail();
    }

    public static void saveScratchPad( final Context context, final String scratchPadName, final Bitmap bitmap, final Callback callback )
    {
        new Thread()
        {
            @Override
            public void run()
            {

                final File output = getScratchPadFile( context, scratchPadName, true );
                if( output == null )
                {
                    callback.onFail();
                    return;
                }

                boolean success = false;
                FileOutputStream out = null;
                try
                {
                    if( output.createNewFile() )
                    {
                        out = new FileOutputStream( output );
                        bitmap.compress( Bitmap.CompressFormat.PNG, 90, out );
                    }
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
                finally
                {
                    if( out != null )
                    {
                        try
                        {
                            out.close();
                            success = true;
                        }
                        catch( IOException e )
                        {
                            e.printStackTrace();
                        }
                    }
                }
                if( success )
                {
                    callback.onSuccess();
                }
                else
                {
                    callback.onFail();
                }
            }
        }.start();
    }

    public static Bitmap openScratchPad( Context context, File scratchPad )
    {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inMutable = true;
        return BitmapFactory.decodeFile( scratchPad.getPath(), o );
    }

    public static ListView getScratchPadListView( Context context )
    {
        final File scratchPads = getScratchPadStorageDirectory( context );
        final File[] scratchPadFiles = scratchPads.listFiles( new FilenameFilter()
        {
            @Override
            public boolean accept( File file, String s )
            {
                return s.startsWith( SCRATCH_PAD_SAVE_PREFIX );
            }
        } );
        if( scratchPadFiles == null || scratchPadFiles.length == 0 )
        {
            return null;
        }
        else
        {
            final ScratchPadSwipeAdapter swipeAdapter = new ScratchPadSwipeAdapter( context, scratchPadFiles );
            final ListView listView = new ListView( context );
            listView.setAdapter( swipeAdapter );
            return listView;
        }
    }

    public static class ScratchPadSwipeAdapter extends DialogHelper.SwipeArrayAdapter<File>
    {
        public ScratchPadSwipeAdapter( Context context, File[] files )
        {
            super( context, R.layout.two_line_swipe, files );
        }

        @Override
        public void fillValues( int position, View view )
        {
            ScratchPadHolder holder = (ScratchPadHolder) view.getTag();
            if( holder == null )
            {
                holder = new ScratchPadHolder();
                holder.Name = (TextView) view.findViewById( R.id.name );
                holder.DateTime = (TextView) view.findViewById( R.id.dateTime );
                holder.InfoButton = (ImageButton) view.findViewById( R.id.infoButton );
                holder.InfoButton.setImageDrawable( Icons.getGameSaveSwipeInfo( getContext() ) );
                holder.InfoButton.setVisibility( View.GONE );
                holder.DeleteButton = (ImageButton) view.findViewById( R.id.deleteButton );
                holder.DeleteButton.setImageDrawable( Icons.getGameSaveDelete( getContext() ) );
                holder.Under = view.findViewById( R.id.under );
                view.setTag( holder );
            }

            final File scratchPad = getItem( position );
            holder.Name.setText( getScratchPadNameFromFile( scratchPad ) );
            holder.DateTime.setText( DateFormat.format( "h:mm aa - MMMM d, yyyy", scratchPad.lastModified() ) );
            holder.DeleteButton.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    if( ScratchPadSwipeAdapter.this.removeItem( scratchPad ) )
                    {
                        scratchPad.delete();
                    }
                }
            } );
        }

        public class ScratchPadHolder
        {
            TextView Name;
            TextView DateTime;
            ImageButton InfoButton;
            ImageButton DeleteButton;
            View Under;
        }
    }
}
