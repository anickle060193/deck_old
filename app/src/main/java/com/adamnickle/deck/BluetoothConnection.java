package com.adamnickle.deck;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adamnickle.deck.spi.ConnectionInterface;
import com.adamnickle.deck.spi.BluetoothConnectionListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;


public class BluetoothConnection
{
    private static final String TAG = BluetoothConnection.class.getSimpleName();



    public BluetoothConnection()
    {

    }


}
