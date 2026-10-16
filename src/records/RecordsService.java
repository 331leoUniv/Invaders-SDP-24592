package records;
import java.util.LinkedHashMap;
import java.util.Map;
/** One local profile's completed runs, deduplicated by run ID. */
public final class RecordsService {
    private final Map<String, RunRecord> records = new LinkedHashMap<String, RunRecord>();
    public synchronized boolean completeRun(final RunRecord record) {
        if (record == null || this.records.containsKey(record.getRunId())) return false;
        this.records.put(record.getRunId(),record); return true;
    }
    public synchronized int getRecordCount() { return this.records.size(); }

    public synchronized int getHighestScore() {
        int result = 0;
        if (!this.records.isEmpty())
            result = Integer.MIN_VALUE;
        for (RunRecord record : this.records.values())
            result = Math.max(result, record.getScore());
        return result;
    }

    public synchronized int getMostEnemiesDefeated() {
        int result = 0;
        for (RunRecord record : this.records.values())
            result = Math.max(result, record.getEnemiesDefeated());
        return result;
    }

    public synchronized long getLongestSurvivalMillis() {
        long result = 0;
        for (RunRecord record : this.records.values())
            result = Math.max(result, record.getActiveDurationMillis());
        return result;
    }
}
