package utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class StopTimer {

    private static final String FINISHED_POINT = "FINISHED_POINT";
    private final String module;
    private final TreeMap<Long, String> stopTimes = new TreeMap<Long, String>();
    private boolean stop = false;

    public StopTimer(String module) {
        this.module = module;
    }

    public void start() {
        stopTimes.clear();
        stop = false;
    }

    public synchronized void newPoint(String name) {
        long time = System.currentTimeMillis();
        if (stopTimes.isEmpty()) {
            stopTimes.put(time, name);
            return;
        }
        Long lastTime = stopTimes.lastKey();
        if (time <= lastTime) {
            time = lastTime + 1;
        }
        stopTimes.put(time, name);
    }

    public void stop() {
        newPoint(FINISHED_POINT);
        stop = true;
    }

    public synchronized List<String> getResults() {
        LinkedHashMap<String, Long> timeResults = getTimeResults();
        List<String> lines = new ArrayList<String>();
        long overall = 0;
        for (Entry<String, Long> timeResult : timeResults.entrySet()) {
            lines.add(toDisplayString(timeResult));
            overall += timeResult.getValue();
        }
        // add overall
        lines.add(module + " " + "Overall: "
            + getTimeString(overall));
        return lines;
    }

    public synchronized LinkedHashMap<String, Long> getTimeResults() {
        LinkedHashMap<String, Long> results = new LinkedHashMap<String, Long>();
        Iterator<Entry<Long, String>> iterator = stopTimes.entrySet().iterator();
        Entry<Long, String> firstEntry = iterator.next();
        Entry<Long, String> lastEntry = firstEntry;
        while (iterator.hasNext()) {
            lastEntry = iterator.next();
            results.put(firstEntry.getValue(),
                getExecutionTime(firstEntry, lastEntry.getKey()));
            firstEntry = lastEntry;
        }
        /* if counting stopped already, the last entry is the stop point, then no need to calculate it*/
        if (!stop) {
            // print the last entry
            long currentTimeMillis = System.currentTimeMillis();
            results.put(lastEntry.getValue(), getExecutionTime(lastEntry, currentTimeMillis));
        }
        return results;
    }

    private String toDisplayString(Entry<String, Long> timeResult) {
        return module + " - " + timeResult.getKey()
            + ": " + getTimeString(timeResult.getValue());
    }

    private long getExecutionTime(Entry<Long, String> entry, long endTime) {
        return endTime - entry.getKey();
    }

    private String getTimeString(long diff) {
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        long diffSec = timeUnit.toSeconds(diff);
        long diffMin = timeUnit.toMinutes(diff);
        StringBuilder sb = new StringBuilder();
        sb.append(diff).append(" ms");
        if (diffMin >= 1) {
            sb.append("(").append(diffMin).append("m").append(diffSec - (60 * diffMin)).append("s")
                .append(")");
        } else if (diffSec > 1) {
            sb.append("(").append(diffSec).append("s").append(")");
        }
        return sb.toString();
    }

    public String getResultString() {
        return StringUtils.newLineJoin(getResults());
    }
}
