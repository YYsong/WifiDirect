package Tools;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.android.wifidirect.WiFiDirectActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;

/**
 * Created by DELL on 2016/5/16.
 */
public class WriteFile {
    public static final String filePath = "/sdcard/WifiDirectDemo/";
    //常量，为编码格式
    public static final String ENCODING = "UTF-8";
    //定义文件的名称
    public static final String fileName = "data.txt";
    private FileOutputStream out = null;
    private BufferedWriter writer = null;
    private Context context = null;

    public WriteFile(Activity context){
        try {
            this.context=context;
            this.out = context.openFileOutput(WriteFile.fileName, Context.MODE_APPEND);
            this.writer = new BufferedWriter(new OutputStreamWriter(out));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean write(String input){

        try{
            writer.write(input+"\r\n");
            writer.flush();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public void write(String strcontent, String filePath, String fileName) {
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        String strContent = strcontent;
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write((strContent+"\r\n").getBytes());
            Toast.makeText(this.context, "保存成功", Toast.LENGTH_LONG).show();
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }
}
