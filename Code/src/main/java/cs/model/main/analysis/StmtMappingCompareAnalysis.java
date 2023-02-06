package cs.model.main.analysis;

import cs.model.analysis.CommitAnalysis;
import cs.model.analysis.RevisionEvaluation;
import cs.model.analysis.RevisionComparison;
import cs.model.evaluation.csvrecord.compare.StmtComparisonRecord;
import cs.model.evaluation.csvrecord.eval.AutoEvaluationRecord;
import cs.model.evaluation.csvrecord.eval.ActionAndRunningTimeRecord;
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
 *
 */
public class StmtMappingCompareAnalysis {

    // start date to analyze the project data
    private static final Timestamp afterDate = TimeUtil.string2Time("1990-01-01 00:00:00.000");
    // end date to analyze the project data
    private static final Timestamp beforeDate = TimeUtil.string2Time("2019-01-01 00:00:00.000");

    private static String[] csvHeader;
    private static String[] runningRecordCsvHeader = RunningRecord.getHeaders();
    private static String[] editActionCsvHeader;
    private static List<String[]> records = new ArrayList<>();
    private static List<String[]> runningRecords = new ArrayList<>();
    private static List<String[]> actionStatisticRecords = new ArrayList<>();
    private static Map<String, Set<String>> runningBlueMap;
    private static Set<String> processedCommits;
    private static int fileId = 1;
    private static String project;
    private static boolean doEval = false;

    public static void doAnalysis(String project, boolean doEvaluation){
        StmtMappingCompareAnalysis.doEval = doEvaluation;
        StmtMappingCompareAnalysis.project = project;
        StmtMappingCompareAnalysis.fileId = 1;

        if (doEval) {
            csvHeader = AutoEvaluationRecord.getHeaders();
        } else {
            csvHeader = StmtComparisonRecord.getHeaders();
        }

        editActionCsvHeader = ActionAndRunningTimeRecord.getHeaders();

        System.out.println("123");
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
            // analyzed commits will not be analyzed again
            if (processedCommits.contains(commitId)) {
                System.out.println(commitId);
                idx ++;
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
                commitResult.calResultMappings(!doEval, doEval);
                String[] runningRecord = new RunningRecord(commitId).toRecord();

                if (doEval) {
                    Map<String, RevisionEvaluation> evaluationMap = commitResult.getEvaluationMap();
                    for (String filePath: evaluationMap.keySet()) {
                        RevisionEvaluation evaluation = evaluationMap.get(filePath);
                        evaluation.generateStmtAndTokenRecords();
                        List<String[]> tmpRecords = evaluation.getRecords();
                        records.addAll(tmpRecords);
                        actionStatisticRecords.add(evaluation.getEditActionRecord());
                    }
                } else {
                    Map<String, RevisionComparison> comparisonMap = commitResult.getComparisonResultMap();
                    for (String filePath : comparisonMap.keySet()) {
                        RevisionComparison comparison = comparisonMap.get(filePath);
                        List<String[]> tmpRecords = comparison.getRecords();
                        records.addAll(tmpRecords);
                    }
                }
                runningRecords.add(runningRecord);

                idx ++;
                System.out.println(idx);
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
            CsvOperationsUtil.writeCSV(csvPath, csvHeader, records);

            String csvPath2 = PathResolver.getStmtMappingAnalyzeRecordCsvPath(project, fileId);
            CsvOperationsUtil.writeCSV(csvPath2, runningRecordCsvHeader, runningRecords);

            String csvPath3 = PathResolver.getActionStatisticCsvPath(project, fileId);
            CsvOperationsUtil.writeCSV(csvPath3, editActionCsvHeader, actionStatisticRecords);

            fileId ++;
            records.clear();
            runningRecords.clear();
            actionStatisticRecords.clear();
        }
    }

    private static Map<String, Set<String>> getRunningBlueMap() {
        Map<String, Set<String>> ret = new HashMap<>();
        String recordDir = PathResolver.getRunningRecordDirPath(project);
        System.out.println("recordDir is " + recordDir);
        try {
            List<String> csvFilePaths = FileOperations.listFilesInDir(recordDir);
            for (String csvPath: csvFilePaths) {
                Map<String, Set<String>> tmpMap = getRunningBlueMap(csvPath);
                ret.putAll(tmpMap);
            }
            return ret;
        } catch (Exception e) {
            throw new RuntimeException("read blue map csv error");
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

    private static Map<String, Set<String>> getRunningBlueMap(String resultCsvPath) throws Exception {
        Map<String, Set<String>> ret = new HashMap<>();
        String[] blueMapHeader = RunningBlueMapRecord.getHeaders();
        List<String[]> records = CsvOperationsUtil.getCSVData(resultCsvPath, blueMapHeader);
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

    public static List<String[]> getRecords(String project, boolean doEval) throws Exception {
        if (doEval) {
            csvHeader = AutoEvaluationRecord.getHeaders();
        } else {
            csvHeader = StmtComparisonRecord.getHeaders();
        }
        String stmtResultDir = PathResolver.getStmtMappingAnalyzeDirPath(project);
        System.out.println(stmtResultDir);
        List<String> files = FileOperations.listFilesInDir(stmtResultDir);
        List<String[]> allRecords = new ArrayList<>();
        for (String file: files){
            File obj = new File(file);
            if (!obj.getName().startsWith("result"))
                continue;
            List<String[]> tmpRecords = CsvOperationsUtil.getCSVData(file, csvHeader);
            if (tmpRecords != null)
                allRecords.addAll(tmpRecords);
        }
        return allRecords;
    }
}
