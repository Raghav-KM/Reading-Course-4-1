package com.example.android.bluetoothmessagingapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button listen, send, listDevices;
    ListView listView;
    TextView msg_box, status;
    EditText writeMsg;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;

    SendReceive sendReceive;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        findViewByIdes();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            } else {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, 0);
                Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_SHORT).show();
            }
        }

        implementListeners();
    }

    private void implementListeners() {

        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                    } else {
                        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
                        String[] strings = new String[bt.size()];
                        btArray = new BluetoothDevice[bt.size()];
                        int index = 0;

                        if (bt.size() > 0) {
                            for (BluetoothDevice device : bt) {
                                btArray[index] = device;
                                strings[index] = device.getName();
                                index++;
                            }
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);
                            listView.setAdapter(arrayAdapter);
                        }
                    }
                }
            }
        });

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServerClass serverClass = new ServerClass();
                serverClass.start();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClientClass clientClass = new ClientClass(btArray[i]);
                clientClass.start();

                status.setText("Connecting");
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string = String.valueOf(writeMsg.getText());
                sendReceive.write(string.getBytes());
            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private void findViewByIdes() {
        listen = (Button) findViewById(R.id.listen);
        send = (Button) findViewById(R.id.send);
        listView = (ListView) findViewById(R.id.listview);
        msg_box = (TextView) findViewById(R.id.msg);
        status = (TextView) findViewById(R.id.status);
        writeMsg = (EditText) findViewById(R.id.writemsg);
        listDevices = (Button) findViewById(R.id.listDevices);
    }

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                    } else {
                        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket = null;

            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();

                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1) {
            device = device1;

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                    } else {
                        socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                    } else {
                        socket.connect();
                    }
                }
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}