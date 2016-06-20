package Tools;

/**
 * Created by DELL on 2016/5/17.
 */
public class Experiment {
    public static final String DISCOVERY = "DISCOVERY";
    public static final String FORMATION = "FORMATION";
    public static final String DELAY_TCP = "DELAY_TCP";
    public static final String LOSS_RATE = "LOSS_RATE";
    public static final String BROADCAST = "BROADCAST";
    //发送100B的数据
    public static final String SENDDADA = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    //发送100B的数据
    public static final String ENDDADA = "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    public int distance = 0;
    public int devicd_num = 0;
    public boolean isGroupOwner = true;
        public static String getRecord(String type,int distance,long value,boolean role){
        return type+","+distance+","+value+","+role+","+System.currentTimeMillis();
    }
    public static String getRecord(String type,int distance,double value,boolean role){
        return type+","+distance+","+value+","+role+","+System.currentTimeMillis();
    }
    public static String getRecord(String type,int distance,String value,boolean role){
        return type+","+distance+","+value+","+role+","+System.currentTimeMillis();
    }
    public static Experiment instance = new Experiment();
}
