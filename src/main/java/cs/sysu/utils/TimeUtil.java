package cs.sysu.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    public final static Timestamp string2Time(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            Date parsedDate = dateFormat.parse(dateString   );
            Timestamp timestamp = new Timestamp(parsedDate.getTime());
            return timestamp;
        } catch (Exception e){
            throw new RuntimeException("time parse error!");
        }
    }
}
