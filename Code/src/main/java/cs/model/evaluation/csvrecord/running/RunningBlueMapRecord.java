package cs.model.evaluation.csvrecord.running;

/**
 * Record for
 */
public class RunningBlueMapRecord {
    private String commitId;
    private String filePath;

    public RunningBlueMapRecord(String commitId, String filePath){
        this.commitId = commitId;
        this.filePath = filePath;
    }

    public static RunningBlueMapRecord fromCsvRecord(String[] record){
        return new RunningBlueMapRecord(record[0], record[1]);
    }

    public String getCommitId() {
        return commitId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String[] toCsvRecord(){
        String[] record = {
                commitId,
                filePath,
        };
        return record;
    }

    public static String[] getHeaders(){
        String[] headers = {"commitId", "filePath"};
        return headers;
    }
}
