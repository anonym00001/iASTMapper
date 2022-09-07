package cs.sysu.evaluation.csvrecord.running;

/**
 * Record of the processed commits.
 *
 * To avoid re-analyze the commits that have been analyzed.
 */
public class RunningRecord {
    private String commitId;

    public RunningRecord(String commitId){
        this.commitId= commitId;
    }

    public String[] toRecord(){
        String[] record = {
                commitId
        };
        return record;
    }

    public static String[] getHeaders(){
        String[] headers = {"commitId"};
        return headers;
    }
}
