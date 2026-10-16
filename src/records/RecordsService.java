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
}
