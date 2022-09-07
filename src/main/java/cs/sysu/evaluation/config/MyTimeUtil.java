package cs.sysu.evaluation.config;

import java.util.concurrent.TimeoutException;


public class MyTimeUtil {
    private static long startMatchTime = -1;
    private static final long TIME_OUT_THRESHOLD = 180000;

    public static void setStartMatchTime(){
        MyTimeUtil.startMatchTime = System.currentTimeMillis();
    }

    public static void checkCurrentTime() throws Exception {
        long curTime = System.currentTimeMillis();
        if (curTime - startMatchTime >= TIME_OUT_THRESHOLD)
            throw new TimeoutException("Match Time Out Exception: " + (curTime - startMatchTime) + "ms.");
    }
}
