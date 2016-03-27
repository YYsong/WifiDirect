// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.android.wifidirect;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

	private static final int SOCKET_TIMEOUT = 5000;
	public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
	public static final String ACTION_SEND_DATA = "com.example.android.wifidirect.SEND_DATA";
	public static final String EXTRAS_FILE_PATH = "file_url";
	public static final String EXTRAS_ADDRESS = "go_host";
	public static final String EXTRAS_PORT = "go_port";
	public static final String EXTRAS_DATA = "data";
	public static final String END = "END";

	private PrintWriter out;
	private BufferedReader in;

	public FileTransferService(String name) {
		super(name);
	}

	public FileTransferService() {
		super("FileTransferService");
	}

	public static boolean copyFile(InputStream inputStream, OutputStream out) {
		byte buf[] = new byte[1024];
		int len;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				out.write(buf, 0, len);

			}
			out.close();
			inputStream.close();
		} catch (IOException e) {
			Log.d(WiFiDirectActivity.TAG, e.toString());
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

		Context context = getApplicationContext();

		if (intent.getAction().equals(ACTION_SEND_DATA)) {
			String host = intent.getExtras().getString(EXTRAS_ADDRESS);
			Socket socket = new Socket();
			int port = intent.getExtras().getInt(EXTRAS_PORT);

			try {
				Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
				socket.bind(null);
				socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

				Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
				in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
				out.println(intent.getExtras().getString(EXTRAS_DATA));
				out.flush();
				String line;
				while(!(line=in.readLine()).equals(FileTransferService.END)){
					Log.d(WiFiDirectActivity.TAG, "Client: Send Data Again");
					out.println(intent.getExtras().getString(EXTRAS_DATA));
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
}
