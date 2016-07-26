package protocol;

import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;

import com.example.android.wifidirect.FileTransferService;
import com.example.android.wifidirect.WiFiDirectActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 * Created by DELL on 2016/7/25.
 */
public class GroupClient {
    public static final String IP_SERVER = "192.168.49.1";
    public static int peerListFreshTime = 5*1000;//5秒更新一次全组数据
    public static int HheartBeatFreshTime = 1*1000;//1秒更新一次心跳

    public static GroupClient instance = new GroupClient();
    /** Indicates if a p2p group has been successfully formed */
    public boolean groupFormed = false;

    /** Indicates if the current device is the group owner */
    public boolean isGroupOwner = false;

    /** Group owner address */
    public InetAddress groupOwnerAddress = null;
    private boolean isInit = false;
    public void initialize(WifiP2pInfo source){
        if (source != null) {
            this.groupFormed = source.groupFormed;
            this.isGroupOwner = source.isGroupOwner;
            this.groupOwnerAddress = source.groupOwnerAddress;
        }
    }

    class Monitor implements Runnable{

        @Override
        public void run() {
            while(true){
                try{
                    SendMessageByTcp(IP_SERVER,System.currentTimeMillis()+"");
                    Thread.sleep(HheartBeatFreshTime);
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("Monitor",e.getMessage());
                }
            }
        }
    }
    public static boolean SendMessageByTcp(String ipaddress,String message){
        Socket socket=new Socket();
        PrintWriter out;
        BufferedReader in;
        boolean flag = false;
        try {
            try {
                socket.connect((new InetSocketAddress(ipaddress, GroupOwner.GroupOnwerPort)));
            }catch (Exception e){
                Log.d(WiFiDirectActivity.TAG, "final tcp in GroupOnwerPort! "+e.getMessage());
            }
            Log.d(WiFiDirectActivity.TAG, "succuss tcp in GroupOnwerPort - " + socket.isConnected());
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
    class ServerThread implements Runnable {
        int port;

        public ServerThread(int port) {
            this.port = port;
        }

        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(port);
                while (true) {
                    Log.d(WiFiDirectActivity.TAG, "GroupOwnerServer: Socket opened");
                    Socket client = serverSocket.accept();

                    //更新IP和心跳
//                    clientHeartbeat.put(client.getInetAddress().toString(),System.currentTimeMillis());

                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);

                    String input = in.readLine();
                    Log.d(WiFiDirectActivity.TAG, "SendDataServer: get a heartbeat " + input);
                    out.println(FileTransferService.END);
                    out.flush();
                    client.close();
                    //判断信息是否是UPD的同步字段，是的话就更新相关的标识
                }
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, "TMD" + e.getMessage());
            }
        }
    }
}
