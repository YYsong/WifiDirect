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

import net.fec.openrq.ArrayDataEncoder;
import net.fec.openrq.EncodingPacket;
import net.fec.openrq.encoder.SourceBlockEncoder;

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
    public static int BROADCAST_PORT=5555;
    public static int FOUNTAIN_PORT=7777;
    public static double SENDTIME=100.00;
    public static volatile String UDPEND="UDPEND";
    public static volatile String UDPSTART="UDPSTART";
    //UI
    private Context ctx=null;
    private TextView senddatacontent = null;
    private Button btnsend=null;
    private Button btntcp=null;
    private Button btnudp=null;
    private Button btnbroadcast=null;
    private Button btnraptorR=null;
    private Button btremind=null;
    private EditText serverIP=null;
    private EditText edtsendms=null;
    private Context self= null;
//    private UdpClientThread udpClientThread= null;

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
        btnbroadcast = (Button) findViewById(R.id.send_broadcast_exp);
        btnraptorR = (Button) findViewById(R.id.send_raptorR_exp);
//        btremind = (Button) findViewById(R.id.remind);


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
                Log.d(WiFiDirectActivity.TAG, "Sending--UDP--------- ");
                new Thread(new UdpClientThread(serverIP.getText().toString(),SendData.UPDPORT,Experiment.SENDDADA)).start();
//                udpClientThread.addSendTime();
                Toast.makeText(SendData.this, "Sending--UDP-!",Toast.LENGTH_SHORT).show();
            }
        });
//        btremind.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //初始化UDP线程
//                udpClientThread = new UdpClientThread(serverIP.getText().toString(),SendData.UPDPORT,Experiment.SENDDADA);
//                new Thread(udpClientThread).start();
//            }
//        });
        btnbroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(WiFiDirectActivity.TAG,"Broadcasting");
                new Thread(new BroadCastClientThread("192.168.49.255",SendData.BROADCAST_PORT,Experiment.SENDDADA)).start();
                Toast.makeText(SendData.this, "Sending--broadcast-!",Toast.LENGTH_SHORT).show();
            }
        });
        btnraptorR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(WiFiDirectActivity.TAG,"Broadcasting");
                new Thread(new RaptorBroadcastClientThread("192.168.49.255",SendData.FOUNTAIN_PORT,FountainCode.input)).start();
                Toast.makeText(SendData.this, "Sending--broadcast-!",Toast.LENGTH_SHORT).show();
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
                String record = Experiment.getRecord(Experiment.DELAY_TCP, Experiment.instance.distance, relaytime,Experiment.instance.isGroupOwner);
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
        private int sendTimes = 0;

        public UdpClientThread(String ipaddress, int port, String data){
            this.ipaddress=ipaddress;
            this.port=port;
            this.data=data;
//            this.activity=activity;
        }
        public boolean TestUDP(){
            int i=0;
            if(dataSocket == null){
                try{
                    dataSocket = new DatagramSocket();
                    dataSocket.setSendBufferSize(20000);
                    dataSocket.setReceiveBufferSize(20000);
                }catch (Exception e) {
                    Log.d(WiFiDirectActivity.TAG, "udp client socket start faild! " + e.getMessage());
                    dataSocket.close();
                    return false;
                }
            }else{
                Log.d(WiFiDirectActivity.TAG, "udp client socket already is! " );
                return false;
            }

            sendDataByte = new byte[100];
            sendStr= data;
            sendDataByte = sendStr.getBytes();
            try{
                dataPacket = new DatagramPacket(sendDataByte,sendDataByte.length,InetAddress.getByName(ipaddress),port);
            }catch (Exception e) {
                Log.d(WiFiDirectActivity.TAG, "udp client packet start faild! " + e.getMessage());
                dataSocket.close();
                return false;
            }
            boolean sendstart = false;
            while(!sendstart){
                sendstart = SendMessageByTcp(ipaddress,SendData.UDPSTART);
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
            boolean sendend = false;
            while(!sendend){
                sendend = SendMessageByTcp(ipaddress,SendData.UDPEND);
            }
            return true;
        }
        public void run(){
//            while(sendTimes>0){
//                boolean result = false;
//                while(!result){
//                    result = TestUDP();
//                }
//                sendTimes--;
//            }

            TestUDP();
        }
    }

    class BroadCastClientThread implements Runnable  {
        private static final int SOCKET_TIMEOUT = 5000;
        String ipaddress = "192.168.49.255";
        int port;
        String data;
        private Context activity;
        private DatagramSocket dataSocket;
        private DatagramPacket dataPacket;
        private DatagramPacket dataEndPacket;
        private byte sendDataByte[];
        private String sendStr;
        private int sendTimes = 0;

        public BroadCastClientThread(String ipaddress, int port, String data){
            this.ipaddress=ipaddress;
            this.port=port;
            this.data=data;
//            this.activity=activity;
        }
        public boolean SendUDP(String senddata){
            int i=0;
            if(dataSocket == null){
                try{
                    dataSocket = new DatagramSocket();
                    dataSocket.setSendBufferSize(20000);
                    dataSocket.setReceiveBufferSize(20000);
                }catch (Exception e) {
                    Log.d(WiFiDirectActivity.TAG, "udp client socket start faild! " + e.getMessage());
                    dataSocket.close();
                    return false;
                }
            }else{
                Log.d(WiFiDirectActivity.TAG, "udp client socket already is! " );

                return false;
            }
            //send data
            sendDataByte = new byte[100];
            sendStr= senddata;
            sendDataByte = sendStr.getBytes();
            String end=Experiment.instance.ENDDADA;
            try{
                dataPacket = new DatagramPacket(sendDataByte,sendDataByte.length,InetAddress.getByName(ipaddress),port);
                dataEndPacket = new DatagramPacket(end.getBytes(),end.getBytes().length,InetAddress.getByName(ipaddress),port);
            }catch (Exception e) {
                Log.d(WiFiDirectActivity.TAG, "udp client packet start faild! " + e.getMessage());
                dataSocket.close();
                return false;
            }
            while(i<SendData.SENDTIME){
                try {
                    dataSocket.send(dataPacket);
                    i++;
                    Log.d(WiFiDirectActivity.TAG, "udp send in " + i+" .data is "+sendStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //send end
            i=0;
            while(i<SendData.SENDTIME){
                try {
                    dataSocket.send(dataEndPacket);
                    i++;
                    Log.d(WiFiDirectActivity.TAG, "udp send in " + i+" .data is "+dataEndPacket.getData().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        public void run(){
            boolean result = SendUDP(this.data);
        }
    }
    class RaptorBroadcastClientThread implements Runnable  {
        private static final int SOCKET_TIMEOUT = 5000;
        String ipaddress = "192.168.49.255";
        int port;
        String data;
        private Context activity;
        private DatagramSocket dataSocket;
        private DatagramPacket dataPacket;
        private byte sendDataByte[];
        private String sendStr;
        private int sendTimes = 0;

        public RaptorBroadcastClientThread(String ipaddress, int port, String data){
            this.ipaddress=ipaddress;
            this.port=port;
            this.data=data;
//            this.activity=activity;
        }
        public boolean SendUDP(String senddata){
            int i=0;
            if(dataSocket == null){
                try{
                    dataSocket = new DatagramSocket();
                    dataSocket.setSendBufferSize(20000);
                    dataSocket.setReceiveBufferSize(20000);
                }catch (Exception e) {
                    Log.d(WiFiDirectActivity.TAG, "udp client socket start faild! " + e.getMessage());
                    dataSocket.close();
                    return false;
                }
            }else{
                Log.d(WiFiDirectActivity.TAG, "udp client socket already is! " );

                return false;
            }
            try{
                ArrayDataEncoder arrayDataEncoder = FountainCode.getEncoder(data.getBytes("UTF-8"));
                if(arrayDataEncoder!=null){
                    for(SourceBlockEncoder sourceBlockEncoder: arrayDataEncoder.sourceBlockIterable()){
                        for(EncodingPacket encodingPacket: sourceBlockEncoder.sourcePacketsIterable()){
                            sendDataByte=encodingPacket.asArray();
                            dataSocket.send(new DatagramPacket(sendDataByte, sendDataByte.length, InetAddress.getByName(ipaddress), port));
                            Log.d(WiFiDirectActivity.TAG, "udp send fountain packet .data length is " + sendDataByte.length);
                        }
                        for(EncodingPacket encodingPacket: sourceBlockEncoder.repairPacketsIterable(sourceBlockEncoder.numberOfSourceSymbols())){
                            sendDataByte=encodingPacket.asArray();
                            dataSocket.send(new DatagramPacket(sendDataByte,sendDataByte.length,InetAddress.getByName(ipaddress),port));
                            Log.d(WiFiDirectActivity.TAG, "udp send fountain packet  .data length is " + sendDataByte.length);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.d(WiFiDirectActivity.TAG, "udp client packet start faild! " + e.getMessage());
                dataSocket.close();
                return false;
            }
            dataSocket.close();
            return true;
        }
        public void run(){
            boolean result = SendUDP(this.data);
        }
    }

    public static boolean SendMessageByTcp(String ipaddress,String message){
        Socket socket=new Socket();
        PrintWriter out;
        BufferedReader in;
        boolean flag = false;
        try {
            try {
                socket.connect((new InetSocketAddress(ipaddress, SendData.TCPPORT)));
            }catch (Exception e){
                Log.d(WiFiDirectActivity.TAG, "final tcp in udp test error! "+e.getMessage());
            }
            Log.d(WiFiDirectActivity.TAG, "final tcp in udp test Client socket - " + socket.isConnected());
            in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(message);
            out.flush();
            String line;
            while(!(line=in.readLine()).equals(FileTransferService.END)){
                Log.d(WiFiDirectActivity.TAG, "final tcp in udp test Client: Send Data Again");
                out.println(message);
            }
            Log.d(WiFiDirectActivity.TAG, "final tcp in udp test Client: Data UDPEND");
            flag=true;
//                Toast.makeText(activity, "UDP Test Success!",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(WiFiDirectActivity.TAG, e.getMessage());
            flag = false;
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
        return flag;
    }
}
