package com.example.android.wifidirect;

import android.app.Service;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by DELL on 2016/3/28.
 */
public class NetHelp {

    class ClientThread implements Runnable  {
        private static final int SOCKET_TIMEOUT = 5000;
        String ipaddress;
        int port;
        String data;
        public ClientThread(String ipaddress, int port, String data){
            this.ipaddress=ipaddress;
            this.port=port;
            this.data=data;
        }
        public void run(){

            Socket socket=new Socket();
            PrintWriter out;
            BufferedReader in;
            try {
                Log.d(WiFiDirectActivity.TAG, "Opening Inner client socket - ");
//				socket.bind(null);
//                socket.connect((new InetSocketAddress(ipaddress, port)), SOCKET_TIMEOUT);
                socket.connect((new InetSocketAddress(ipaddress, port)));

                Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
                in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(data);
                out.flush();
                String line;
                while(!(line=in.readLine()).equals(FileTransferService.END)){
                    Log.d(WiFiDirectActivity.TAG, "Client: Send Data Again");
                    out.println(data);
                }
                Log.d(WiFiDirectActivity.TAG, "Client: Data written");
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


    class ServerThread implements Runnable{
        int port;
        public ServerThread(int port){
            this.port=port;
        }
        public void run(){
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                Log.d(WiFiDirectActivity.TAG, "SendDataServer: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(WiFiDirectActivity.TAG, "SendDataServer: connection done");


                BufferedReader in=new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);

                String input=in.readLine();
                Log.d(WiFiDirectActivity.TAG, "SendDataServer: get a message "+input);
                out.println(FileTransferService.END);
                out.flush();
                serverSocket.close();
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
            }
        }
    }

}
