package cs.model.evaluation.csvrecord.eval;

public class ActionAndRunningTimeRecord {
    private String project;
    private String commitId;
    private String filePath;
    private int myActionNum;
    private int gtActionNum;
    private int mtdActionNum;
    private int ijmActionNum; // ijm action may not be compared, because of different trees.

    private long gtTreeBuildTime;
    private long ijmTreeBuildTime;

    private long myPreprocessTime;

    private long myMappingTime;
    private long myActionTime;
    private long gtMappingTime;
    private long gtActionTime;
    private long mtdMappingTime;
    private long mtdActionTime;
    private long ijmMappingTime;
    private long ijmActionTime;

    public ActionAndRunningTimeRecord(String project, String commitId, String filePath) {
        this.project = project;
        this.commitId = commitId;
        this.filePath = filePath;
    }

    public void setActionNums(int myActionNum, int gtActionNum, int mtdActionNum, int ijmActionNum) {
        this.myActionNum = myActionNum;
        this.gtActionNum = gtActionNum;
        this.mtdActionNum = mtdActionNum;
        this.ijmActionNum = ijmActionNum;
    }

    public void setRunningTime(long gtTreeBuildTime, long ijmTreeBuildTime, long myProcessTime,
                               long myMappingTime, long myActionTime, long gtMappingTime, long gtActionTime,
                               long mtdMappingTime, long mtdActionTime, long ijmMappingTime, long ijmActionTime) {
        this.gtTreeBuildTime = gtTreeBuildTime;
        this.ijmTreeBuildTime = ijmTreeBuildTime;
        this.myPreprocessTime = myProcessTime;
        this.myMappingTime = myMappingTime;
        this.myActionTime = myActionTime;
        this.gtMappingTime = gtMappingTime;
        this.gtActionTime = gtActionTime;
        this.mtdMappingTime = mtdMappingTime;
        this.mtdActionTime = mtdActionTime;
        this.ijmMappingTime = ijmMappingTime;
        this.ijmActionTime = ijmActionTime;
    }

    public String[] getRecord() {
        return new String[] {
                project, commitId, filePath,
                Integer.toString(myActionNum),
                Integer.toString(gtActionNum),
                Integer.toString(mtdActionNum),
                Integer.toString(ijmActionNum),
                Long.toString(gtTreeBuildTime),
                Long.toString(ijmTreeBuildTime),
                Long.toString(myPreprocessTime),
                Long.toString(myMappingTime),
                Long.toString(myActionTime),
                Long.toString(gtMappingTime),
                Long.toString(gtActionTime),
                Long.toString(mtdMappingTime),
                Long.toString(mtdActionTime),
                Long.toString(ijmMappingTime),
                Long.toString(ijmActionTime)
        };
    }

    public static String[] getHeaders(){
        return new String[] {
                "project", "commitId", "filePath",
                "myActionNum", "gtActionNum", "mtdActionNum", "ijmActionNum",
                "gtTreeBuildTime", "ijmTreeBuildTime", "preprocessTime",
                "myMappingTime", "myActionTime",
                "gtMappingTime", "gtActionTime",
                "mtdMappingTime", "mtdActionTime",
                "ijmMappingTime", "ijmActionTime"
        };
    }
}
