package cs.model.main.analysis;

import cs.model.analysis.CommitAnalysis;
import cs.model.analysis.RevisionAnalysis;
import cs.model.evaluation.csvrecord.measure.StmtMappingAndMeasureRecord;
import cs.model.gitops.CommitOps;
import cs.model.gitops.GitUtils;
import cs.model.evaluation.csvrecord.running.RunningBlueMapRecord;
import cs.model.evaluation.csvrecord.running.RunningRecord;
import cs.model.utils.CsvOperationsUtil;
import cs.model.utils.FileOperations;
import cs.model.utils.PathResolver;
import cs.model.utils.TimeUtil;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;


/**
 * Only run our method and record all the statement mapping results.
 * Also present the measure values for each record.
 */
public class StmtMappingSingleAnalysis {
    private static final Timestamp afterDate = TimeUtil.string2Time("1990-01-01 00:00:00.000");
    private static final Timestamp beforeDate = TimeUtil.string2Time("2019-01-01 00:00:00.000");

    private static String[] header = StmtMappingAndMeasureRecord.getHeaders();
    private static List<String[]> records = new ArrayList<>();
    private static List<String[]> runningRecords = new ArrayList<>();
    private static Map<String, Set<String>> runningBlueMap;
    private static Set<String> processedCommits;
    private static int fileId = 1;
    private static String project;

    public static void doAnalysis(String project){
        StmtMappingSingleAnalysis.project = project;
        StmtMappingSingleAnalysis.fileId = 1;
        records = new ArrayList<>();
        runningRecords = new ArrayList<>();
        runningBlueMap = getRunningBlueMap();
        processedCommits = getProcessedCommits();
        run();
    }

    private static void run(){
        List<String> commitIds = GitUtils.getAllCommitIds(project);
        int idx = 0;
        for (String commitId: commitIds){
            if (processedCommits.contains(commitId)) {
                System.out.println(commitId);
                continue;
            }
            if (!runningBlueMap.containsKey(commitId))
                continue;

            RevCommit curRev = GitUtils.getCommitObjById(project, commitId);
            if (CommitOps.getCommitTime(curRev) < afterDate.getTime() / 1000)
                continue;
            if (CommitOps.getCommitTime(curRev) > beforeDate.getTime() / 1000)
                continue;
            System.out.println(commitId);
            try {
                Set<String> filesToAnalyze = runningBlueMap.get(commitId);
                CommitAnalysis commitResult = new CommitAnalysis(project, commitId,
                        null, filesToAnalyze, true);
                commitResult.calResultMappings(false, false);
                Map<String, RevisionAnalysis> resultMap = commitResult.getRevisionAnalysisResultMap();

                String[] runningRecord = new RunningRecord(commitId).toRecord();
                for (String filePath: resultMap.keySet()){
                    RevisionAnalysis result = resultMap.get(filePath);
                    List<String[]> tmpRecords = result.getMappingRecords();
                    records.addAll(tmpRecords);
                }
                runningRecords.add(runningRecord);

                idx ++;
                if (idx % 100 == 0)
                    refreshRecords();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        try {
            refreshRecords();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void refreshRecords() throws IOException {
        if (records.size() > 0){
            String csvPath = PathResolver.getStmtMappingAnalyzeCsvPath(project, fileId);
            CsvOperationsUtil.writeCSV(csvPath, header, records);

            String[] runningRecordHeader = RunningRecord.getHeaders();
            String csvPath2 = PathResolver.getStmtMappingAnalyzeRecordCsvPath(project, fileId);
            CsvOperationsUtil.writeCSV(csvPath2, runningRecordHeader, runningRecords);

            fileId ++;
            records.clear();
            runningRecords.clear();
        }
    }

    private static Map<String, Set<String>> getRunningBlueMap() {
        Map<String, Set<String>> ret = new HashMap<>();
        String recordDir = PathResolver.getRunningRecordDirPath(project);
        try {
            List<String> csvFilePaths = FileOperations.listFilesInDir(recordDir);
            for (String csvPath: csvFilePaths) {
                Map<String, Set<String>> tmpMap = getRunningRecordMap(csvPath);
                ret.putAll(tmpMap);
            }
            return ret;
        } catch (Exception e) {
            throw new RuntimeException("read csv error");
        }
    }

    private static Set<String> getProcessedCommits(){
        Set<String> ret = new HashSet<>();
        String analyzeDir = PathResolver.getStmtMappingAnalyzeDirPath(project);
        try {
            List<String> csvFilePaths = FileOperations.listFilesInDir(analyzeDir);
            for (String csvPath: csvFilePaths){
                String path = new File(csvPath).getName();
                if (path.startsWith("record")) {
                    Set<String> tmpSet = getProcessedCommits(csvPath);
                    ret.addAll(tmpSet);
                }
            }
            return ret;
        } catch (Exception e) {
            throw new RuntimeException("read csv error");
        }
    }

    private static Map<String, Set<String>> getRunningRecordMap(String resultCsvPath) throws Exception {
        Map<String, Set<String>> ret = new HashMap<>();
        String[] headers = RunningBlueMapRecord.getHeaders();
        List<String[]> records = CsvOperationsUtil.getCSVData(resultCsvPath, headers);
        if (records != null) {
            for (String[] record : records) {
                String commitId = record[0];
                String filePath = record[1];
                if (!ret.containsKey(commitId))
                    ret.put(commitId, new HashSet<>());
                ret.get(commitId).add(filePath);
            }
        }
        return ret;
    }

    private static Set<String> getProcessedCommits(String resultCsvPath) throws Exception {
        String[] headers = RunningRecord.getHeaders();
        List<String[]> records = CsvOperationsUtil.getCSVData(resultCsvPath, headers);
        Set<String> ret = new HashSet<>();
        if (records != null){
            for (String[] record: records){
                String commitId = record[0];
                ret.add(commitId);
            }
        }
        return ret;
    }

    public static List<String[]> getRecords(String project) throws Exception {
        String stmtResultDir = PathResolver.getStmtMappingAnalyzeDirPath(project);
        System.out.println(stmtResultDir);
        List<String> files = FileOperations.listFilesInDir(stmtResultDir);
        List<String[]> allRecords = new ArrayList<>();
        for (String file: files){
            File obj = new File(file);
            if (!obj.getName().startsWith("result"))
                continue;
            List<String[]> tmpRecords = CsvOperationsUtil.getCSVData(file, header);
            if (tmpRecords != null)
                allRecords.addAll(tmpRecords);
        }
        return allRecords;
    }
}
