package com.example.android.wifidirect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Server extends Activity implements SurfaceHolder.Callback{
    private android.hardware.Camera camera;
    private SurfaceView sv;
    private SurfaceHolder sh;
    private boolean isPreview;
    private boolean isconnected=false;
    private android.hardware.Camera.PreviewCallback pc=null;
    private int count=0;
    private ServerSocket ss=null;
    private OutputStream osforserver=null;
    private Socket s=null;
    private String ipaddress;
    private int port;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        Intent intent=getIntent();
        ipaddress=intent.getExtras().getString("IPAddress");
        port=intent.getExtras().getInt("port");
        sv=(SurfaceView)findViewById(R.id.surface);
        sh=sv.getHolder();
        sh.addCallback(this);
        ServerThread st=new ServerThread();
        st.start();

    }
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event){
//        if(keyCode==KeyEvent.KEYCODE_BACK){
//            try{
//                this.s.close();
//                this.ss.close();
//                finish();
//            }catch (Exception e){e.printStackTrace();}
//            return false;
//        }else
//        return super.onKeyDown(keyCode,event);
//
//
//    }
    //创建ServerSocket和对应通信用的socket
    class ServerThread extends Thread {

        public void run(){
            try{
                ss=new ServerSocket(port);
                System.out.println("建立连接等待client连接");
                s=ss.accept();
                isconnected=true;
                System.out.println("connected");
                osforserver=s.getOutputStream();
//                osforserver.write(begin);//写入标志位代表一张图片开始

            }catch (Exception e){

                e.printStackTrace();}

        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera= android.hardware.Camera.open();
        try{
            camera.setPreviewDisplay(sh);
        }
        catch (Exception e){
            if(camera!=null){camera.release();camera=null;}

            e.printStackTrace();}
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera!=null){
            camera.setPreviewCallback(null);
            camera.stopPreview();
            isPreview =false;
            camera.release();
            camera=null;


        }
    }

    public void initCamera(){
        if(isPreview){
            camera.stopPreview();
        }
        if(camera!=null){
            final android.hardware.Camera.Parameters parameters=camera.getParameters();
            parameters.setPictureFormat(PixelFormat.JPEG);
            //    parameters.setPictureSize(480, 320);
            //    parameters.setPreviewSize(320, 320);
            camera.setDisplayOrientation(90);
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            camera.setParameters(parameters);
            pc=new android.hardware.Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
                    android.hardware.Camera.Size size =camera.getParameters().getPreviewSize();
                    try{int Plength=0;byte[] head=new byte[2]; byte[] timestamp=new byte[8];long time=0;
                        YuvImage image=new YuvImage(data,parameters.getPreviewFormat(),size.width,size.height,null);
                        if(image!=null){
                            Log.d("count", "接收到第" + count++ + "张图片");
                            ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
                            ByteArrayOutputStream temp=new ByteArrayOutputStream();


                            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, temp);
                            Plength=temp.size();

                            temp.reset();
                            head= int2byte(Plength);

                            outputStream.write(head);//写入长度
                            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, outputStream);//写入图片数据
                            Date dt=new Date();//取当前时间存到数组里面
                            time=dt.getTime();
                            timestamp=long2byte(time);//将时间转化为8个字节
                            outputStream.write(timestamp);
                            Log.d("funk", "outputStream.size=" + outputStream.size());
//                            Log.d("amount", new String(outputStream.toByteArray()));
//                            outputStream.flush();
                            MyThread th=new MyThread(outputStream);
                            th.start();
                            //  th.stop();
                        }


                    }catch (Exception e){

                        e.printStackTrace();}
                }
            };
            camera.setPreviewCallback(pc);
            camera.startPreview();
            isPreview=true;

        }



    }

    class MyThread extends Thread {
        private byte byteBuffer[] = new byte[60000];
        private ByteArrayOutputStream myoutputstream;

        public MyThread(ByteArrayOutputStream myoutputstream) {
            this.myoutputstream = myoutputstream;
            try {
                myoutputstream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                // 将图像数据通过Socket发送出去
                ByteArrayInputStream inputstream = new ByteArrayInputStream(myoutputstream.toByteArray());
                int amount;
                if(isconnected){
                    while ((amount = inputstream.read(byteBuffer)) != -1) {

                        osforserver.write(byteBuffer, 0, amount);

                        Log.d("验证：", "outputstream取到了byte" + amount);
                        myoutputstream.flush();
                    }

                    myoutputstream.close();
//                   osforserver.close();

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public byte[] int2byte(int res){
        byte[] targets=new byte[2];
        targets[0]=(byte)(res & 0xff);
        targets[1]=(byte)((res >> 8 ) & 0xff);
        return targets;


    }
   public byte[] long2byte(long res){
       byte[] byteNum = new byte[8];
       for (int ix = 0; ix < 8; ++ix) {
           int offset = 64 - (ix + 1) * 8;
           byteNum[ix] = (byte) ((res >> offset) & 0xff);
       }
       return byteNum;
   }
   }
