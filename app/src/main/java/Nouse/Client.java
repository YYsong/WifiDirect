package Nouse;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.example.android.wifidirect.R;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import Tools.write;
//暂时没用
public class Client extends Activity {
    private boolean isserver=false;
    private boolean isconnected=false;
    public java.net.Socket s;
    private SurfaceView sv;
    private SurfaceHolder sh;
    private MyHandler mhandler;
    private String ipaddress;
    private int port;
    private TextView tv=null;
    public boolean haspainted=false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        Intent intent=getIntent();
        ipaddress=intent.getExtras().getString("IPAddress");
        System.out.println("IPAddress="+ipaddress);
        port=intent.getExtras().getInt("port");
        sv=(SurfaceView)findViewById(R.id.surface);
        sh=sv.getHolder();
        sh.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        tv=(TextView)findViewById(R.id.info);
        mhandler=new MyHandler();
        ClientThread ct=new ClientThread();
        ct.start();

//        ClientThread ct=new ClientThread();
//        ct.start();
    }
    class MyHandler extends android.os.Handler{
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            Bundle b=msg.getData();
            Bitmap bitmap=b.getParcelable("pic");
            long stime=b.getLong("time");
            long currenttime=new Date().getTime();

            if(bitmap!=null){
                float delay=(currenttime-stime);
                write w=new write();
//                Log.d("苦尽甘来", "苦尽甘来,这张图片是"+stime+"发的，当前时间是"+currenttime+"，时延是"+delay+"毫秒");
                tv.setText("苦尽甘来,这张图片是"+stime+"发的，当前时间是"+currenttime+"，时延是"+delay+"毫秒");

                w.write(String.valueOf(delay)+" ","/sdcard/wifi/","delay.txt");
                Canvas canvas = sh.lockCanvas();
                if (canvas == null || sh == null) {
                    if (sh == null) System.out.println("sh==null");
                    if (canvas == null) System.out.println("canvas==null");
                    return;
                }
//                System.out.println("canvas已锁定");
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.FILL);
//                System.out.println("bitmap大小="+bitmap.getByteCount());
                canvas.drawBitmap(bitmap, 0, 0, paint);
                sh.unlockCanvasAndPost(canvas);
//                bitmap=null;
            }
        }

    }


    class ClientThread extends Thread {
        public void run(){

            try {
                System.out.println("开始连接");
                s=new Socket();
                s.connect(new InetSocketAddress(ipaddress, port));
                System.out.println("连接成功");
                DrawThread st=new DrawThread(sh);
                st.start();
//            is=s.getInputStream();
            }catch (Exception e){e.printStackTrace();}


        }


    }
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        ClientThread ct=new ClientThread();
//        ct.start();
//        DrawThread st=new DrawThread();
//        st.start();
//
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//
//    }

    class DrawThread extends Thread {
        public SurfaceHolder sh;
        public DrawThread(SurfaceHolder surfaceHolder){
            this.sh=surfaceHolder;

        }
        public void run(){
            System.out.println("DrawThread线程启动");
//            try {
//                s.connect(new InetSocketAddress("192.168.1.22", 8199));
//                System.out.println("连接成功");
//
//            }catch (Exception e){e.printStackTrace();}


//            Canvas canvas=sh.lockCanvas();
//            if(canvas==null||sh==null){
//                if(sh==null) System.out.println("sh==null");
//                if(canvas==null)System.out.println("canvas==null");
//                return;}

            try {

                InputStream is=s.getInputStream();
                LinkedList<Byte> pool=new LinkedList<Byte>();//数据按字节处理，因为有头字符
                int Plength=0;//传递过来这张图片的大小，int型
                byte[]head=new byte[2];//2位的head代表图片长度的字节数组
                byte[]received=new byte[2];
                byte[]buffer =new byte[60000];int len=0;
                if(is!=null){
                    System.out.println("接收线程没有问题");}
//                    ByteArrayOutputStream outputStream =new ByteArrayOutputStream();
                while((len=is.read(buffer))!=-1){
                    System.out.println("这次发来的字节有"+len+"个，当前pool里有"+pool.size()+"个字节");
                    for(int i=0;i<len;i++){pool.add(buffer[i]);buffer[i]=0;}//pool完整的接收到新的数据
                    if(pool.size()<2)continue;//不够一个头文件,继续接收
                    head[0] = pool.get(0);
                    head[1] = pool.get(1);//获取数据大小
                    Plength=getUnsignedIntNum(head,0,2);
                    System.out.println("下一张图片的字节数是"+Plength+"，当前pool中有"+pool.size()+"个字节");
                    if (pool.size() < Plength+2+8) {
                        Log.e("不够一个包长", "继续接收，图片字节需求是" + Plength + "，当前pool中有" + pool.size() + "个字节");// half package
                        continue;
                    }

                    else if(Plength+2+8==pool.size()){// full package，含有2位尺寸信息和对应的信息
                        Log.e("刚好一个包长", "带走");
                        received = new byte[Plength+8];
                        pool.remove(0);pool.remove(0);
                        for (int i = 0; i < Plength+8; i++) {
                            received[i] = pool.get(0);
                            pool.remove(0);
                        }
                        Send(received);
                    }
                    else {
                        Log.e("不只一个包", "一个一个拿,当前pool里有" + pool.size() + "个字节");
                        // more than one
                        while (Plength +2+8<= pool.size()) {//每次满足尺寸+数据消息的封装包字节数就发走一个
                            received = new byte[Plength+8];
                            pool.remove(0);pool.remove(0);
                            for (int i = 0; i < Plength+8; i++) {
                                received[i] = pool.get(0);
                                pool.remove(0);
                            }
                            Send(received);
                            Log.e("发出一个包", "长度是" + Plength + "剩下的字节数是" + pool.size());
                            if (pool.size() < 2)
                                break;
                            head[0] = pool.get(0);
                            head[1] = pool.get(1);
                            Plength = getUnsignedIntNum(head, 0, 2);
                            System.out.println("下一张图片的尺寸是"+Plength);
                        }
                    }

//                       System.out.println("inputstream值为" + isnull + "读取到的数据大小是（inputstream的长度）" + len);
                    // outputStream.write(buffer, 0, len);
                }
//                    System.out.println("outputStream值为"+outputStream.size()+"||||"+outputStream.toByteArray().length);
//                       byte data[]=outputStream.toByteArray();
//                      Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//
//                       if (bitmap != null) {
////                           System.out.println("生成了一张图片");
////                        Paint paint = new Paint();
////                        paint.setAntiAlias(true);
////                        paint.setStyle(Paint.Style.FILL);
////                        canvas.drawBitmap(bitmap, 0, 0, paint);
//                           Message msg=new Message();
//                           Bundle b=new Bundle();
//                           b.putParcelable("pic",bitmap);
//                           msg.setData(b);
//                           mhandler.sendMessage(msg);
//////
//                       }
//                       outputStream.reset();



//            System.out.println("没有数据了");
//                    outputStream.close();
//                    buffer=null;

            } catch (Exception e) {
                e.printStackTrace();
            }



        }


    }

    public static int getUnsignedIntNum(byte[] data, int indexStart, int length) {
        if (length > 3) {
            // int为4个字节，故待转字节数不能超过4；
            // 此外，为4时也可能得到错误结果，如当待转字节为01,00,00,80(表示uint32)，转为int为0x80000001为负，错误
            try {
                throw new Exception(
                        "error in getUnsignedIntNum:length is bigger than 3");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int res = 0;
        for (int i = 0; i < length; i++) {
            res |= (data[indexStart + i] & 0xff) << (8 * i);
        }
        return res;
    }
    public void Send(byte[] received){
        int picsize=received.length-8;

        Bitmap bitmap = BitmapFactory.decodeByteArray(received, 0, received.length - 8);
        byte [] timestamp=new byte[8];
        for(int i=0;i<8;i++){
            timestamp[i]=received[picsize+i];

        }
        long time=byte2long(timestamp);//提取出时间戳
        if (bitmap != null) {
//                           System.out.println("生成了一张图片");
//                        Paint paint = new Paint();
//                        paint.setAntiAlias(true);
//                        paint.setStyle(Paint.Style.FILL);
//                        canvas.drawBitmap(bitmap, 0, 0, paint);
            Message msg=new Message();
            Bundle b=new Bundle();
            b.putParcelable("pic", bitmap);
            b.putLong("time", time);
            msg.setData(b);
            String date=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(time));
            String cdate=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(new Date().getTime()));
            System.out.println("收到图片的时间是"+date+"当前时间是"+cdate);
            mhandler.sendMessage(msg);
////
//这里应该有一个对于bitmap的释放，但是bitmap的释放不能在绘图之前(绘图和处理是异步的，如何有顺序的操作)，一种解决办法是直接把handler的功能去掉，
        }
        else{
            System.out.println("不能生成一张完整的图片");bitmap.recycle();}



    }
public long byte2long(byte[] res){
    long num = 0;
    for (int ix = 0; ix < 8; ++ix) {
        num <<= 8;
        num |= (res[ix] & 0xff);
    }
    return num;
}


}
