/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wifidirect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.wifidirect.DeviceListFragment.DeviceActionListener;

import Tools.Experiment;
import Tools.WriteFile;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

	public static final String IP_SERVER = "192.168.49.1";
	public static int PORT = 8988;
	public static Boolean SendEnd = false;

	private static boolean server_running = false;
	private static boolean wifip2p_server_running = false;

	protected static final int CHOOSE_FILE_RESULT_CODE = 20;
	protected static final int SENDDATA_OK = 21;
	private View mContentView = null;
	private WifiP2pDevice device;
	private WifiP2pInfo info;
	ProgressDialog progressDialog = null;

//	private DeviceDetailFragment fragment=null;


	private TextView NetworkNameView ;
	private TextView NetworkPasswdView ;
	private int intent=-1;

	private Boolean getP2pNetworkInfo=false;
	private WifiP2pGroup wifiP2PGroup=null;
	public long timestamp=0;
//	private String p2pGroupNetworkName=null;
//	private String p2pGroupNetworkPasswd=null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//		fragment= (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_list);

		mContentView = inflater.inflate(R.layout.device_detail, null);
		NetworkNameView=(TextView)mContentView.findViewById(R.id.network_name);
		NetworkPasswdView=(TextView) mContentView.findViewById(R.id.network_passwd);

		mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				WifiP2pConfig config = new WifiP2pConfig();
				config.deviceAddress = device.deviceAddress;
				config.wps.setup = WpsInfo.PBC;

				if(intent!=-1){
					config.groupOwnerIntent=intent;
				}

				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
						"Connecting to :" + device.deviceAddress, true, true
						//                        new DialogInterface.OnCancelListener() {
						//
						//                            @Override
						//                            public void onCancel(DialogInterface dialog) {
						//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
						//                            }
						//                        }
				);
				Utils.setuptimep=System.currentTimeMillis();
				((DeviceActionListener) getActivity()).connect(config);

			}
		});

		mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						((DeviceActionListener) getActivity()).disconnect();
					}
				});


		//点击发送数据按钮，打开一个intent，先把
//		mContentView.findViewById(R.id.btn_send_data).setOnClickListener(
//				new View.OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						// Allow user to pick an image from Gallery or other
//						// registered apps
////						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
////						intent.setType("image/*");
////						startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
//						Intent intent = new Intent();
//						startActivityForResult(intent,SENDDATA_OK);
//					}
//				});

		mContentView.findViewById(R.id.btn_get_wifi).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Get P2p Network infomation
				Log.d("GetInfo", "Start!");
				new GetP2pGroupInfoTask().executeOnExecutor(Executors.newCachedThreadPool());
			}
		});

		mContentView.findViewById(R.id.btn_intent).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String intentString = ((EditText) mContentView.findViewById(R.id.Group_intent)).getText().toString();

				int intentNum = -1;
				try {
					intentNum = Integer.parseInt(intentString);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (intentNum >= 0 && intentNum <= 14) {
					intent = intentNum;
					((TextView) mContentView.findViewById(R.id.intent_value)).setText("" + intent);
				}
			}
		});

		return mContentView;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		String localIP = Utils.getLocalIPAddress();
		// Trick to find the ip in the file /proc/net/arp
		String client_mac_fixed = new String(device.deviceAddress).replace("99", "19");
		String clientIP = Utils.getIPFromMac(client_mac_fixed);

		// User has picked an image. Transfer it to group owner i.e peer using
		// FileTransferService.
		Uri uri = data.getData();
		TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
		statusText.setText("Sending: " + uri);
		Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);
		Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
		serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
		serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());

		if(localIP.equals(IP_SERVER)){
			serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, clientIP);
		}else{
			serviceIntent.putExtra(FileTransferService.EXTRAS_ADDRESS, IP_SERVER);
		}

		serviceIntent.putExtra(FileTransferService.EXTRAS_PORT, PORT);
		getActivity().startService(serviceIntent);
//		switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
//			case SENDDATA_OK:
//				Bundle b=data.getExtras(); //data为B中回传的Intent
//				String str=b.getString("resullt");//str即为回传的值
//				//告知用户
//				Toast.makeText(getActivity(), "Send data result:"+str,
//						Toast.LENGTH_SHORT).show();
//				break;
//			default:
//				break;
//		}
	}

	@Override
	public void onConnectionInfoAvailable(final WifiP2pInfo info) {

		long setuptime = System.currentTimeMillis()-Utils.setuptimep;
		Utils.setuptimep=0;
		Tools.WriteFile wf = new Tools.WriteFile(getActivity());
		String record = Experiment.getRecord(Experiment.FORMATION, Experiment.instance.distance, setuptime);
		wf.write(record, WriteFile.filePath, WriteFile.fileName);
		Log.d(Experiment.FORMATION,""+setuptime);

		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		//Get P2p Info
		this.info = info;
		this.getView().setVisibility(View.VISIBLE);

		// The owner IP is now known.
		TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
		view.setText(getResources().getString(R.string.group_owner_text) + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)));

		// InetAddress from WifiP2pInfo struct.
		view = (TextView) mContentView.findViewById(R.id.device_info);
		view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());


		view = (TextView) mContentView.findViewById(R.id.server_status);
		mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);

		if (!server_running){
			new ServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
			server_running = true;
		}
		if (!wifip2p_server_running){
//			new WifiServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
			new Thread(new ServerThread(SendData.TCPPORT)).start();
			new Thread(new DatagramServerThread(SendData.UPDPORT)).start();
			wifip2p_server_running = true;
			view.setText("TCP and UDP server is runnint? " +wifip2p_server_running );
		}



		// hide the connect button
		mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
	}


	class GetP2pGroupInfoTask extends AsyncTask<Void,Void,Boolean>{

		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			Toast.makeText(getActivity(),"Getting Info",Toast.LENGTH_SHORT).show();
			progressDialog = ProgressDialog.show(getActivity(), "Getting Info",
					"Connecting to Network" , true, true
					//                        new DialogInterface.OnCancelListener() {
					//
					//                            @Override
					//                            public void onCancel(DialogInterface dialog) {
					//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
					//                            }
					//                        }
			);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				WifiP2pManager.GroupInfoListener listener=new WifiP2pManager.GroupInfoListener() {
					@Override
					public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
						wifiP2PGroup=wifiP2pGroup;
						getP2pNetworkInfo=true;
					}
				};
				WifiP2pManager manager=((WiFiDirectActivity) getActivity()).getP2pManager();
				WifiP2pManager.Channel channel=((WiFiDirectActivity) getActivity()).getP2pChannel();
				manager.requestGroupInfo(channel, listener);
				Log.d("GetInfo","Inbackground");
				while(!getP2pNetworkInfo||wifiP2PGroup==null){
					Log.d("GetInfo","Getting");
				}
				if(wifiP2PGroup.getNetworkName()!=null)Log.d("WifiName", wifiP2PGroup.getNetworkName());
				if(wifiP2PGroup.getPassphrase()!=null)Log.d("WifiPass", wifiP2PGroup.getPassphrase());
				if(wifiP2PGroup.getClientList().size()!=0){
					Log.d("wifi clients","They are:");
					for(WifiP2pDevice d :wifiP2PGroup.getClientList()){
						Log.d("Wifi",d.deviceName);
						Log.d("Wifi",d.deviceAddress);
					}
				}
			}catch (Exception e){
				e.printStackTrace();
				return false;
			}
			return true;
		}
		@Override
		protected void onPostExecute(Boolean result){
			if(result){
				Log.d("GetInfo", "onPostExecute");
				progressDialog.dismiss();

				if(wifiP2PGroup.getNetworkName()!=null)NetworkNameView.setText("Network name : " + wifiP2PGroup.getNetworkName());
				if(wifiP2PGroup.getPassphrase()!=null)NetworkPasswdView.setText("Network passwd : " + wifiP2PGroup.getPassphrase());

			}else{
				Log.e("Erorr","Failed to get p2p network info");
				Toast.makeText(getActivity(),"Faile to get Info",Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Updates the UI with device data
	 * 
	 * @param device the device to be displayed
	 */
	public void showDetails(WifiP2pDevice device) {
		//Get device object
		this.device = device;
		this.getView().setVisibility(View.VISIBLE);
		TextView view = (TextView) mContentView.findViewById(R.id.device_address);
		view.setText(device.deviceAddress);
		view = (TextView) mContentView.findViewById(R.id.device_info);
		view.setText(device.toString());

	}

	/**
	 * Clears the UI fields after a disconnect or direct mode disable operation.
	 */
	public void resetViews() {
		mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
		TextView view = (TextView) mContentView.findViewById(R.id.device_address);
		view.setText(R.string.empty);
		view = (TextView) mContentView.findViewById(R.id.device_info);
		view.setText(R.string.empty);
		view = (TextView) mContentView.findViewById(R.id.group_owner);
		view.setText(R.string.empty);
		view = (TextView) mContentView.findViewById(R.id.status_text);
		view.setText(R.string.empty);
		mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
		this.getView().setVisibility(View.GONE);
	}

	/**
	 * A simple server socket that accepts connection and writes some data on
	 * the stream.
	 * 当wifi-direct连接成功后（见onConnectionInfoAvailable方法），启动这个异步线程，开启server socket，接收传送过来的图片，结束
	 */
	public static class ServerAsyncTask extends AsyncTask<Void, Void, String> {

		private final Context context;
		private final TextView statusText;

		/**
		 * @param context
		 * @param statusText
		 */
		public ServerAsyncTask(Context context, View statusText) {
			this.context = context;
			this.statusText = (TextView) statusText;
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				ServerSocket serverSocket = new ServerSocket(PORT);
				Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
				Socket client = serverSocket.accept();
				Log.d(WiFiDirectActivity.TAG, "Server: connection done");
				final File f = new File(Environment.getExternalStorageDirectory() + "/"
						+ context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
						+ ".jpg");

				File dirs = new File(f.getParent());
				if (!dirs.exists())
					dirs.mkdirs();
				f.createNewFile();

				Log.d(WiFiDirectActivity.TAG, "server: copying files " + f.toString());
				InputStream inputstream = client.getInputStream();
				FileTransferService.copyFile(inputstream, new FileOutputStream(f));
				serverSocket.close();
				server_running = false;
				return f.getAbsolutePath();
			} catch (IOException e) {
				Log.e(WiFiDirectActivity.TAG, e.getMessage());
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				statusText.setText("File copied - " + result);
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse("file://" + result), "image/*");
				context.startActivity(intent);
			}

		}

		/*
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			statusText.setText("Opening a server socket");
		}

	}

	public static class WifiServerAsyncTask extends AsyncTask<Void, Void, String> {

		private final Context context;
		private final TextView statusText;
		private String input;
		/**
		 * @param context
		 * @param statusText
		 */
		public WifiServerAsyncTask(Context context, View statusText) {
			this.context = context;
			this.statusText = (TextView) statusText;
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				ServerSocket serverSocket = new ServerSocket(8888);
				Log.d(WiFiDirectActivity.TAG, "SendDataServer: Socket opened");
				Socket client = serverSocket.accept();
				Log.d(WiFiDirectActivity.TAG, "SendDataServer: connection done");


				BufferedReader in=new BufferedReader(new InputStreamReader(client.getInputStream()));
				PrintWriter out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);

				input=in.readLine();
				Log.d(WiFiDirectActivity.TAG, "SendDataServer: get a message " + input);
				out.println(FileTransferService.END);
				out.flush();
				serverSocket.close();
				server_running = false;
			} catch (IOException e) {
				Log.e(WiFiDirectActivity.TAG, e.getMessage());
			}
			return input;
		}

		/*
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			statusText.setText("SendDataServer: get a message "+input);
		}

		/*
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			statusText.setText("Opening a server socket");
		}

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
					Log.d(WiFiDirectActivity.TAG, "SendDataServer: Socket opened");
					Socket client = serverSocket.accept();
					Log.d(WiFiDirectActivity.TAG, "SendDataServer: connection done");


					BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
					PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);

					String input = in.readLine();
					Log.d(WiFiDirectActivity.TAG, "SendDataServer: get a message " + input);
					if(input.equals(SendData.UDPEND)){
						DeviceDetailFragment.SendEnd = true;
					}
					out.println(FileTransferService.END);
					out.flush();
					client.close();
				}
			} catch (IOException e) {
				Log.e(WiFiDirectActivity.TAG, "TMD" + e.getMessage());
			}
		}
	}

	class DatagramServerThread implements Runnable{
		int port;
		double recNum = 0.0;
		public DatagramServerThread(int port){
			this.port=port;
		}
		public void run(){
			DatagramSocket serverSocket=null;
			while(true){
				try {
					serverSocket = new DatagramSocket(port);
					Log.d(WiFiDirectActivity.TAG, "DatagramPacket: Socket opened");
					byte[] recvBuf = new byte[100];
					DatagramPacket recvPacket = new DatagramPacket(recvBuf,recvBuf.length);
					Log.d(WiFiDirectActivity.TAG, "DatagramPacket: connection done");
					int i=0;
					while(i==0){
						serverSocket.receive(recvPacket);
						i = recvPacket.getLength();
						if(i>0){
							String receiveStr = new String(recvBuf,0,recvPacket.getLength());
							Log.d(WiFiDirectActivity.TAG, "get udp package: "+receiveStr);
							recNum++;
							i=0;
						}
						if(DeviceDetailFragment.SendEnd){
							Tools.WriteFile wf = new Tools.WriteFile(getActivity());
							String record = Experiment.getRecord(Experiment.LOSS_RATE, Experiment.instance.distance,recNum/SendData.SENDTIME);
							wf.write(record, WriteFile.filePath, WriteFile.fileName);
							recNum=0.0;
							DeviceDetailFragment.SendEnd = false;
						}
					}
				} catch (IOException e) {
					Log.e(WiFiDirectActivity.TAG, "TMD"+e.getMessage());
				}
			}
		}
	}
}
