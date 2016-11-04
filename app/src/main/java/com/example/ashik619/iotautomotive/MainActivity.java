package com.example.ashik619.iotautomotive;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter mBluetoothAdapter;
    int REQUEST_ENABLE_BT = 1;
    TextView textview1;
    AlertDialog.Builder pairADB;
    AlertDialog pairADBObject;
    private static final UUID CONNUUID = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
    BluetoothDevice selectedDevice;
    String selectedDeviceName;
    BluetoothSocket bs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textview1 = (TextView) findViewById(R.id.textView1);
        pairADB = new AlertDialog.Builder(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        enablebt();
    }

    void enablebt() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            selectPairedDevice();
        }
    }

    void selectPairedDevice() {
        //textview1.append("\nBluetooth is enabled...");
        // Listing paired devices
        //textview1.append("\nPaired Devices are:");
        final Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        ArrayList<String> pairlist = new ArrayList<String>();
        for (BluetoothDevice device : devices) {
            //textview1.append("\n  Device: " + device.getName() + ", " + device);
            pairlist.add(device.getName());
        }
        final String[] pairarray = pairlist.toArray(new String[0]);
        pairADB.setItems(pairarray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                selectedDeviceName = pairarray[item];
                for (BluetoothDevice device : devices) {
                    if (selectedDeviceName.equals(device.getName()))
                        selectedDevice = device;
                }
                textview1.append("\n" + selectedDevice.getName());
                //connectDevice(selectedDevice);
                //ConnectThread ct = new ConnectThread(selectedDevice, mBluetoothAdapter);
                //ct.start();


            }
        });
        pairADBObject = pairADB.create();
    }
    public void server(View v) {
        AcceptThread at = new AcceptThread();
        at.start();
    }
    public void client(View v) {
        ConnectThread at = new ConnectThread(selectedDevice);
        at.start();
    }

    public void selectpair(View v) {
        pairADBObject.show();
    }
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("blutooth", CONNUUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    showToast("no client");
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    //manageConnectedSocket(socket);
                    try{
                        mmServerSocket.close();
                        showToast("connected done");
                        break;
                    }catch (Exception e2){}

                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(CONNUUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out

                try {
                    mmSocket =(BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                    Thread.sleep(200);
                    mmSocket.connect();
                } catch (Exception e){
                    showToast("unable to connect");
                    throw new RuntimeException(e);
                }
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}