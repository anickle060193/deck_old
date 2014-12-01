package com.adamnickle.deck;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ScratchPadIO
{
    private static final String SCRATCH_PAD_SAVE_DIRECTORY = "deck_scratch_pads";
    private static final String SCRATCHPAD_FILE_NAME_PREFIX = "scratch_pad_";
    private static final String SCRATCHPAD_FILE_NAME_PATTERN = SCRATCHPAD_FILE_NAME_PREFIX + "%d.png";
    private static int SCRATCH_PAD_SAVE_NUMBER = 1;

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

    private synchronized static File getNextScratchPadFile( Context context, boolean writing )
    {
        File scratchPad = null;
        if( ( writing && isExternalStorageWritable() ) || ( !writing && isExternalStorageReadable() ) )
        {
            final File scratchPadDirectory = getScratchPadStorageDirectory( context );
            do
            {
                scratchPad = new File( scratchPadDirectory, String.format( SCRATCHPAD_FILE_NAME_PATTERN, SCRATCH_PAD_SAVE_NUMBER ) );
                SCRATCH_PAD_SAVE_NUMBER++;
            }
            while( scratchPad.exists() );
        }

        return scratchPad;
    }

    public static interface Callback
    {
        public void onSuccess();
        public void onFail();
    }

    public static void saveScratchPad( final Context context, final Bitmap bitmap, final Callback callback )
    {
        new Thread()
        {
            @Override
            public void run()
            {

                final File output = getNextScratchPadFile( context, true );
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

    public static Bitmap openScratchPad( File scratchPad )
    {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inMutable = true;
        return BitmapFactory.decodeFile( scratchPad.getPath(), o );
    }

    public static ScratchPadCardAdapter getScratchPadCards( Context context )
    {
        final File scratchPads = getScratchPadStorageDirectory( context );
        final File[] scratchPadFiles = scratchPads.listFiles( new FilenameFilter()
        {
            @Override
            public boolean accept( File file, String s )
            {
                return s.startsWith( SCRATCHPAD_FILE_NAME_PREFIX );
            }
        } );
        if( scratchPadFiles == null || scratchPadFiles.length == 0 )
        {
            return null;
        }
        else
        {
            return new ScratchPadCardAdapter( context, scratchPadFiles );
        }
    }

    public static class ScratchPadCardAdapter extends RecyclerView.Adapter<ScratchPadCardAdapter.ScratchPadHolder>
    {
        private final Context mContext;
        private ArrayList<File> mData;
        private ScratchPadOnClickListener mListener;

        public ScratchPadCardAdapter( Context context, File[] data )
        {
            this( context, Arrays.asList( data ) );
        }

        public ScratchPadCardAdapter( Context context, List<File> data )
        {
            mContext = context;
            mData = new ArrayList<File>( data );
        }

        public void setScratchPadOnClickListener( ScratchPadOnClickListener listener )
        {
            mListener = listener;
        }

        @Override
        public ScratchPadHolder onCreateViewHolder( ViewGroup viewGroup, int i )
        {
            return new ScratchPadHolder( LayoutInflater.from( mContext ).inflate( R.layout.scratchpad_card_layout, viewGroup, false ) );
        }

        @Override
        public void onBindViewHolder( final ScratchPadHolder scratchPadHolder, final int i )
        {
            final File scratchPad = getItem( i );
            Picasso.with( mContext ).load( scratchPad ).into( scratchPadHolder.Image, new com.squareup.picasso.Callback()
            {
                @Override
                public void onSuccess()
                {
                    scratchPadHolder.Loading.setVisibility( View.GONE );
                    scratchPadHolder.Image.setVisibility( View.VISIBLE );
                }

                @Override
                public void onError()
                {
                }
            } );
            scratchPadHolder.Card.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    if( mListener != null )
                    {
                        mListener.onScratchPadClick( scratchPad );
                    }
                }
            } );
            scratchPadHolder.Card.setOnTouchListener( new SwipeDismissTouchListener( scratchPadHolder.Card, null, new SwipeDismissTouchListener.OnDismissCallback()
            {
                @Override
                public void onDismiss( View view, Object token )
                {
                    ScratchPadCardAdapter.this.removeItemAt( i );
                }
            } ) );

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
            final File scratchPad = mData.get( i );
            if( scratchPad != null && scratchPad.delete() )
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

        public class ScratchPadHolder extends RecyclerView.ViewHolder
        {
            CardView Card;
            ImageView Image;
            ProgressBar Loading;

            public ScratchPadHolder( View itemView )
            {
                super( itemView );

                Card = (CardView) itemView;
                Image = (ImageView) itemView.findViewById( R.id.image );
                Loading = (ProgressBar) itemView.findViewById( R.id.loading );
            }
        }

        public interface ScratchPadOnClickListener
        {
            public void onScratchPadClick( File scratchPad );
        }
    }
}
