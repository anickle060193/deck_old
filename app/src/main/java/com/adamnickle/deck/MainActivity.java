package com.adamnickle.deck;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity
{
    private static final String TAG = "MainActivity";
    public static final String SERVICE_NAME = "Deck Server";

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;

    private static final int CONNECTION_TYPE_NONE = 1;
    private static final int CONNECTION_TYPE_CLIENT = 2;
    private static final int CONNECTION_TYPE_SERVER = 3;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothConnection mBluetoothConnection;

    private TextView mTextView;
    private ScrollView mScrollView;
    private EditText mMessageEditText;
    private int mConnectionType;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        Log.d( TAG, "+++ ON CREATE +++" );

        setContentView( R.layout.activity_main );

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if( mBluetoothAdapter == null )
        {
            Toast.makeText( this, "Application requires Bluetooth. Exiting application.", Toast.LENGTH_LONG ).show();
            finish();
            return;
        }

        mScrollView = (ScrollView) findViewById( R.id.scrollView );
        mTextView = (TextView) findViewById( R.id.textView );
        mTextView.addTextChangedListener( new TextWatcher()
        {
            @Override
            public void beforeTextChanged( CharSequence charSequence, int i, int i2, int i3 ) { }

            @Override
            public void onTextChanged( CharSequence charSequence, int i, int i2, int i3 ) { }

            @Override
            public void afterTextChanged( Editable editable )
            {
                mScrollView.post( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mScrollView.fullScroll( View.FOCUS_DOWN );
                    }
                } );
            }
        } );
        mMessageEditText = (EditText) findViewById( R.id.messageEditText );
        mBluetoothConnection = new BluetoothConnection( this, mHandler );
        setConnectionType( CONNECTION_TYPE_NONE );

        startActivity( new Intent( this, GameCreatorActivity.class ) );
    }

    private void setConnectionType( int connectionType )
    {
        mConnectionType = connectionType;
        this.invalidateOptionsMenu();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d( TAG, "++ ON START ++" );
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d( TAG, "+ ON RESUME +" );
    }

    private void startBluetoothConnection()
    {
        Log.d( TAG, "startBluetoothConnection" );

        if( !mBluetoothAdapter.isEnabled() )
        {
            Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
            startActivityForResult( enableIntent, REQUEST_ENABLE_BLUETOOTH );
        } else
        {
            if( mBluetoothConnection != null )
            {
                mBluetoothConnection.start();
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d( TAG, "- ON PAUSE -" );
    }

    @Override
    protected void onStop()
    {
        Log.d( TAG, "-- ON STOP --" );
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Log.d( TAG, "--- ON DESTROY ---" );

        if( mBluetoothConnection != null )
        {
            mBluetoothConnection.stop();
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.main, menu );

        if( mConnectionType == CONNECTION_TYPE_NONE )
        {
            menu.findItem( R.id.actionCreateServer ).setVisible( true );
            menu.findItem( R.id.actionFinishConnecting ).setVisible( false );
            menu.findItem( R.id.actionRestartConnecting ).setVisible( false );
            menu.findItem( R.id.actionCloseServer ).setVisible( false );
            menu.findItem( R.id.actionFindServer ).setVisible( true );
            menu.findItem( R.id.actionLeaveServer ).setVisible( false );
        }
        else if( mConnectionType == CONNECTION_TYPE_CLIENT )
        {
            menu.findItem( R.id.actionCreateServer ).setVisible( false );
            menu.findItem( R.id.actionFinishConnecting ).setVisible( false );
            menu.findItem( R.id.actionRestartConnecting ).setVisible( false );
            menu.findItem( R.id.actionCloseServer ).setVisible( false );

            if( mBluetoothConnection.getState() == BluetoothConnection.STATE_CONNECTED )
            {
                menu.findItem( R.id.actionFindServer ).setVisible( false );
                menu.findItem( R.id.actionLeaveServer ).setVisible( true );
            }
            else
            {
                menu.findItem( R.id.actionFindServer ).setVisible( true );
                menu.findItem( R.id.actionLeaveServer ).setVisible( false );
            }
        }
        else if( mConnectionType == CONNECTION_TYPE_SERVER )
        {
            menu.findItem( R.id.actionCreateServer ).setVisible( false );

            if( mBluetoothConnection.getState() == BluetoothConnection.STATE_LISTENING )
            {
                menu.findItem( R.id.actionFinishConnecting ).setVisible( false );
                menu.findItem( R.id.actionRestartConnecting ).setVisible( false );
            }
            else if( mBluetoothConnection.getState() == BluetoothConnection.STATE_CONNECTED_LISTENING )
            {
                menu.findItem( R.id.actionFinishConnecting ).setVisible( true );
                menu.findItem( R.id.actionRestartConnecting ).setVisible( false );
            }
            else if( mBluetoothConnection.getState() == BluetoothConnection.STATE_CONNECTED )
            {
                menu.findItem( R.id.actionFinishConnecting ).setVisible( false );
                menu.findItem( R.id.actionRestartConnecting ).setVisible( true );
            }
            menu.findItem( R.id.actionCloseServer ).setVisible( true );
            menu.findItem( R.id.actionFindServer ).setVisible( false );
            menu.findItem( R.id.actionLeaveServer ).setVisible( false );
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch( item.getItemId() )
        {
            case R.id.actionSettings:
                startActivity( new Intent( this, GameCreatorActivity.class ) );
                return true;

            case R.id.actionCreateServer:
                setConnectionType( CONNECTION_TYPE_SERVER );

                mBluetoothConnection.setIsClient( false );
                mBluetoothConnection.ensureDiscoverable();
                startBluetoothConnection();
                return true;

            case R.id.actionFinishConnecting:
                mBluetoothConnection.finishConnecting();
                return true;

            case R.id.actionRestartConnecting:
                mBluetoothConnection.restart();
                return true;

            case R.id.actionCloseServer:
                setConnectionType( CONNECTION_TYPE_NONE );

                mBluetoothConnection.stop();
                return true;

            case R.id.actionFindServer:
                Intent devicesIntent = new Intent( this, DeviceListActivity.class );
                startActivityForResult( devicesIntent, REQUEST_CONNECT_DEVICE );
                return true;

            case R.id.actionLeaveServer:
                setConnectionType( CONNECTION_TYPE_NONE );

                mBluetoothConnection.stop();
                return true;
        }

        return super.onOptionsItemSelected( item );
    }

    private void setStatus( CharSequence subtitle )
    {
        ActionBar actionBar = this.getSupportActionBar();
        if( actionBar != null )
        {
            actionBar.setSubtitle( subtitle );
        }
    }

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage( Message msg )
        {
            switch( msg.what )
            {
                case BluetoothConnection.MESSAGE_STATE_CHANGED:
                    Log.d( TAG, "MESSAGE_STATE_CHANGED: " + msg.arg1 );
                    switch( msg.arg1 )
                    {
                        case BluetoothConnection.STATE_CONNECTED_LISTENING:
                        case BluetoothConnection.STATE_CONNECTED:
                            setStatus( "Connected" );
                            break;
                        case BluetoothConnection.STATE_CONNECTING:
                            setStatus( "Connecting..." );
                            break;
                        case BluetoothConnection.STATE_LISTENING:
                        case BluetoothConnection.STATE_NONE:
                            setStatus( "Not connected" );
                            break;
                    }
                    break;
                case BluetoothConnection.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String( writeBuf );
                    mTextView.setText( mTextView.getText() + "\nMe: " + writeMessage );
                    break;
                case BluetoothConnection.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String( readBuf, 0, msg.arg1 );
                    mTextView.setText( mTextView.getText() + "\nThem: " + readMessage );
                    break;
                case BluetoothConnection.MESSAGE_NEW_DEVICE:
                    Toast.makeText( getApplicationContext(), "Connected to " + ( (BluetoothDevice) msg.obj ).getName(), Toast.LENGTH_LONG ).show();
                    break;
                case BluetoothConnection.MESSAGE_TOAST:
                    Toast.makeText( getApplicationContext(), (String) msg.obj, Toast.LENGTH_LONG ).show();
                    break;
            }
        }
    };

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        Log.d( TAG, "onActivityResult" );

        switch( requestCode )
        {
            case REQUEST_ENABLE_BLUETOOTH:
                if( resultCode == Activity.RESULT_OK )
                {
                    startBluetoothConnection();
                }
                else
                {
                    Log.d( TAG, "Bluetooth not enabled" );
                    Toast.makeText( this, "Bluetooth was not enabled. Application exiting.", Toast.LENGTH_LONG ).show();
                    finish();
                }
                break;

            case REQUEST_CONNECT_DEVICE:
                if( resultCode == Activity.RESULT_OK )
                {
                    setConnectionType( CONNECTION_TYPE_CLIENT );

                    mBluetoothConnection.setIsClient( true );
                    String address = data.getStringExtra( DeviceListActivity.EXTRA_DEVICE_ADDRESS );
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice( address );
                    mBluetoothConnection.connect( device );
                }
                break;
        }
    }

    public void onClick( View view )
    {
        switch( view.getId() )
        {
            case R.id.sendMessageButton:
                String message = mMessageEditText.getText().toString();
                if( message.isEmpty() )
                {
                   message = "Default message text";
                }
                sendData( message.getBytes() );
                mMessageEditText.getText().clear();
                break;
        }
    }

    private void sendData( byte[] data )
    {
        if( !mBluetoothConnection.isConnected() )
        {
            Toast.makeText( this, "Not connected.", Toast.LENGTH_LONG ).show();
            return;
        }

        if( data.length != 0 )
        {
            mBluetoothConnection.write( data );
        }
    }
}
