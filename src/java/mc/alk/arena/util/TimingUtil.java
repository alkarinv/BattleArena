package mc.alk.arena.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author alkarin
 */
public class TimingUtil {
    static List<TimingUtil> timers;

    Map<String,TimingStat> timings = new HashMap<String,TimingStat>();

    public static List<TimingUtil> getTimers() {
        return timers;
    }

    public static void resetTimers() {
        for (TimingUtil t: timers){
            t.timings.clear();
        }
    }

    public class TimingStat {
        public int count = 0;
        public long totalTime = 0;
        public long getAverage() {
            return totalTime/count;
        }
    }

    public TimingUtil(){
        if (timers == null) {
            timers = new ArrayList<TimingUtil>();
        }
        timers.add(this);
    }

    public void put(String key, TimingStat t) {
        timings.put(key, t);
    }

    public TimingStat getOrCreate(String key) {
        TimingStat t = timings.get(key);
        if (t == null){
            t = new TimingStat();
            timings.put(key,t);
        }
        return t;
    }

    public Map<String, TimingStat> getTimings() {
        return timings;
    }

}
