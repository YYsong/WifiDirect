package com.example.android.wifidirect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import Tools.Experiment;
import Tools.WriteFile;

public class SendData extends Activity {
    private String TAG = "===Client===";
    private String TAG1 = "===Send===";

    public static int TCPPORT=8888;
    public static int UPDPORT=6666;
    public static double SENDTIME=100.00;
    public static String UDPEND="UDPEND";
    //UI
    private Context ctx=null;
    private TextView senddatacontent = null;
    private Button btnsend=null;
    private Button btntcp=null;
    private Button btnudp=null;
    private EditText serverIP=null;
    private EditText edtsendms=null;
    private Context self= null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);

        senddatacontent = (TextView) findViewById(R.id.send_data_content);
        btnsend = (Button) findViewById(R.id.send_data_send);
        serverIP= (EditText) findViewById(R.id.send_data_ip);
        edtsendms = (EditText) findViewById(R.id.send_data_content);
        btntcp = (Button) findViewById(R.id.send_data_exp);
        btnudp = (Button) findViewById(R.id.send_udp_exp);

        this.self=this;
        ctx=SendData.this;
//        String localIP = Utils.getLocalIPAddress();
        // Trick to find the ip in the file /proc/net/arp
//        String client_mac_fixed = new String(device.deviceAddress).replace("99", "19");
//        String clientIP = Utils.getIPFromMac(client_mac_fixed);

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.

        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(WiFiDirectActivity.TAG, "Sending----------- " + edtsendms.getText().toString());
//                Intent serviceIntent = new Intent(ctx, FileTransferService.class);
//                serviceIntent.setAction(FileTransferService.ACTION_SEND_DATA);
//                serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, serverIP.getText().toString());
//                serviceIntent.putExtra(FileTransferService.EXTRAS_PORT, PORT);
//                serviceIntent.putExtra(FileTransferService.EXTRAS_DATA, edtsendms.getText().toString());
//                ctx.startService(serviceIntent);
                new Thread(new ClientThread(serverIP.getText().toString(),SendData.TCPPORT,edtsendms.getText().toString())).start();
            }
        });

        btntcp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(WiFiDirectActivity.TAG, "Sending---TCP-------- " + edtsendms.getText().toString());
                new Thread(new ClientThread(serverIP.getText().toString(),SendData.TCPPORT,Experiment.SENDDADA)).start();
            }
        });
        btnudp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(WiFiDirectActivity.TAG, "Sending--UDP--------- " + edtsendms.getText().toString());
                new Thread(new UdpClientThread(serverIP.getText().toString(),SendData.UPDPORT,Experiment.SENDDADA)).start();
            }
        });

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

    class ClientThread implements Runnable  {
        private static final int SOCKET_TIMEOUT = 5000;
        String ipaddress;
        int port;
        String data;
        Context activity;
        public ClientThread(String ipaddress, int port, String data){
            this.ipaddress=ipaddress;
            this.port=port;
            this.data=data;
//            this.activity = activity;
        }
        public void run(){

            Socket socket=new Socket();
            PrintWriter out;
            BufferedReader in;
            try {
                Log.d(WiFiDirectActivity.TAG, "Opening Inner client socket - ");
//				socket.bind(null);
//                socket.connect((new InetSocketAddress(ipaddress, port)), SOCKET_TIMEOUT);
                try {
                    socket.connect((new InetSocketAddress(ipaddress, port)));
                }catch (Exception e){
                    Log.d(WiFiDirectActivity.TAG, "tmd "+e.getMessage());
                }
                Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
                in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(data);
                long starttime= System.currentTimeMillis();
                out.flush();
                String line;
                while(!(line=in.readLine()).equals(FileTransferService.END)){
                    Log.d(WiFiDirectActivity.TAG, "Client: Send Data Again");
                    out.println(data);
                }
                long endtime= System.currentTimeMillis();
                long relaytime = endtime- starttime;

                Tools.WriteFile wf = new Tools.WriteFile(SendData.this);
                String record = Experiment.getRecord(Experiment.DELAY_TCP, Experiment.instance.distance, relaytime);
                wf.write(record, WriteFile.filePath, WriteFile.fileName);

                Log.d(WiFiDirectActivity.TAG, "relaytime is " + relaytime);
                Log.d(WiFiDirectActivity.TAG, "Client: Data written");
//                Toast.makeText(activity, "TCP Test Success!",Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    class UdpClientThread implements Runnable  {
        private static final int SOCKET_TIMEOUT = 5000;
        String ipaddress;
        int port;
        String data;
        private Context activity;
        private DatagramSocket dataSocket;
        private DatagramPacket dataPacket;
        private byte sendDataByte[];
        private String sendStr;

        public UdpClientThread(String ipaddress, int port, String data){
            this.ipaddress=ipaddress;
            this.port=port;
            this.data=data;
//            this.activity=activity;
        }
        public void run(){
            int i=0;
            if(dataSocket == null){
                try{
                    dataSocket = new DatagramSocket();
                }catch (Exception e) {
                    Log.d(WiFiDirectActivity.TAG, "udp client socket start faild! " + e.getMessage());
                    dataSocket.close();
                }
            }else{
                Log.d(WiFiDirectActivity.TAG, "udp client socket already is! " );
            }

                sendDataByte = new byte[100];
                sendStr= data;
                sendDataByte = sendStr.getBytes();
            try{
                dataPacket = new DatagramPacket(sendDataByte,sendDataByte.length,InetAddress.getByName(ipaddress),port);
            }catch (Exception e) {
                Log.d(WiFiDirectActivity.TAG, "udp client packet start faild! " + e.getMessage());
                dataSocket.close();
            }
            while(i<SendData.SENDTIME){
                try {
                    dataSocket.send(dataPacket);
                    i++;
                    Log.d(WiFiDirectActivity.TAG, "udp send in " + i+" .data is "+dataPacket.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Socket socket=new Socket();
            PrintWriter out;
            BufferedReader in;
            try {
                try {
                    socket.connect((new InetSocketAddress(ipaddress, SendData.TCPPORT)));
                }catch (Exception e){
                    Log.d(WiFiDirectActivity.TAG, "final tcp in udp test error! "+e.getMessage());
                }
                Log.d(WiFiDirectActivity.TAG, "final tcp in udp test Client socket - " + socket.isConnected());
                in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(SendData.UDPEND);
                out.flush();
                String line;
                while(!(line=in.readLine()).equals(FileTransferService.END)){
                    Log.d(WiFiDirectActivity.TAG, "final tcp in udp test Client: Send Data Again");
                    out.println(SendData.UDPEND);
                }
                Log.d(WiFiDirectActivity.TAG, "final tcp in udp test Client: Data UDPEND");
//                Toast.makeText(activity, "UDP Test Success!",Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
