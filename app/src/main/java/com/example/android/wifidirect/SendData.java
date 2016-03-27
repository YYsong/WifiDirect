package com.example.android.wifidirect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SendData extends Activity {
    private String TAG = "===Client===";
    private String TAG1 = "===Send===";

    public static int PORT=8888;

    //UI
    private Context ctx=null;
    private TextView senddatacontent = null;
    private Button btnsend=null;
    private EditText serverIP=null;
    private EditText edtsendms=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);

        senddatacontent = (TextView) findViewById(R.id.send_data_content);
        btnsend = (Button) findViewById(R.id.send_data_send);
        serverIP= (EditText) findViewById(R.id.send_data_ip);
        edtsendms = (EditText) findViewById(R.id.send_data_content);

        ctx=SendData.this;
//        String localIP = Utils.getLocalIPAddress();
        // Trick to find the ip in the file /proc/net/arp
//        String client_mac_fixed = new String(device.deviceAddress).replace("99", "19");
//        String clientIP = Utils.getIPFromMac(client_mac_fixed);

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        Log.d(WiFiDirectActivity.TAG, "Sending----------- " + edtsendms.getText().toString());
        Intent serviceIntent = new Intent(ctx, FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_DATA);
        serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, serverIP.getText().toString());
        serviceIntent.putExtra(FileTransferService.EXTRAS_PORT, PORT);
        serviceIntent.putExtra(FileTransferService.EXTRAS_DATA,edtsendms.getText().toString() );
        ctx.startService(serviceIntent);
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "start onStart~~~");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "start onRestart~~~");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "start onResume~~~");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "start onPause~~~");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "start onStop~~~");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "start onDestroy~~~");

    }



}
