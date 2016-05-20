package Tools;

/**
 * Created by DELL on 2016/5/17.
 */
public class Experiment {
    public static final String DISCOVERY = "DISCOVERY";
    public static final String FORMATION = "FORMATION";
    public static final String DELAY_TCP = "DELAY_TCP";
    public static final String LOSS_RATE = "LOSS_RATE";
    //发送100B的数据
    public static final String SENDDADA = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public int distance = 0;
    public static String getRecord(String type,int distance,long value){
        return type+","+distance+","+value+","+System.currentTimeMillis();
    }
    public static String getRecord(String type,int distance,double value){
        return type+","+distance+","+value+","+System.currentTimeMillis();
    }
    public static String getRecord(String type,int distance,String value){
        return type+","+distance+","+value+","+System.currentTimeMillis();
    }
    public static Experiment instance = new Experiment();
}
