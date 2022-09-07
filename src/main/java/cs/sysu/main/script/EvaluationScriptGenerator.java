package cs.sysu.main.script;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.analysis.CommitAnalysis;
import cs.sysu.analysis.RevisionEvaluation;
import cs.sysu.evaluation.csvrecord.eval.ManualEvaluationRecord;
import cs.sysu.evaluation.utils.MappingMethodNames;
import cs.sysu.evaluation.csvrecord.eval.AutoEvaluationRecord;
import cs.sysu.main.analysis.StmtMappingCompareAnalysis;
import cs.sysu.utils.CsvOperationsUtil;
import cs.sysu.utils.FileRevision;
import cs.sysu.utils.PathResolver;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;

/**
 * Generate script for manual analysis
 */
public class EvaluationScriptGenerator {
    private static String project;
    private static int sampleNum = 20;
    private static Map<FileRevision, List<AutoEvaluationRecord>> revisionRecordMap;

    private static List<AutoEvaluationRecord> gtInconsistentRecords;
    private static List<AutoEvaluationRecord> mtdInconsistentRecords;
    private static List<AutoEvaluationRecord> ijmInconsistentRecords;
    private static Map<FileRevision, List<AutoEvaluationRecord>> gtInconsistentRevRecordMap;
    private static Map<FileRevision, List<AutoEvaluationRecord>> mtdInconsistentRevRecordMap;
    private static Map<FileRevision, List<AutoEvaluationRecord>> ijmInconsistentRevRecordMap;

    public static void generate(String project, int sampleNum) throws Exception {
        EvaluationScriptGenerator.project = project;
        EvaluationScriptGenerator.sampleNum = sampleNum;
        revisionRecordMap = new HashMap<>();

        gtInconsistentRecords = new ArrayList<>();
        mtdInconsistentRecords = new ArrayList<>();
        ijmInconsistentRecords = new ArrayList<>();
        gtInconsistentRevRecordMap = new HashMap<>();
        mtdInconsistentRevRecordMap = new HashMap<>();
        ijmInconsistentRevRecordMap = new HashMap<>();
        run();
    }

    private static void run() throws Exception {
        List<String[]> csvRecords = StmtMappingCompareAnalysis.getRecords(project, true);
        prepareRecords(csvRecords);

        Collections.shuffle(gtInconsistentRecords, new Random(1));
        Collections.shuffle(mtdInconsistentRecords, new Random(1));
        Collections.shuffle(ijmInconsistentRecords, new Random(1));

        List<AutoEvaluationRecord> sampleGtRecords = gtInconsistentRecords.subList(0, sampleNum * 10);
        List<AutoEvaluationRecord> sampleMtdRecords = mtdInconsistentRecords.subList(0, sampleNum * 10);
        List<AutoEvaluationRecord> sampleIjmRecords = ijmInconsistentRecords.subList(0, sampleNum * 10);

        doMappingAndGenerateCsvScript(sampleGtRecords, MappingMethodNames.GT);
        doMappingAndGenerateCsvScript(sampleMtdRecords, MappingMethodNames.MTDIFF);
        doMappingAndGenerateCsvScript(sampleIjmRecords, MappingMethodNames.IJM);
    }

    private static void prepareRecords(List<String[]> csvRecords) {
        for (String[] record: csvRecords) {
            AutoEvaluationRecord comparisonRecord = AutoEvaluationRecord.getObjFromCsvRecord(record);
            FileRevision fr = comparisonRecord.getFileRevision();

            if (!revisionRecordMap.containsKey(fr))
                revisionRecordMap.put(fr, new ArrayList<>());
            revisionRecordMap.get(fr).add(comparisonRecord);

            if (comparisonRecord.isGtSrcInconsistent() || comparisonRecord.isGtDstInconsistent()) {
                gtInconsistentRecords.add(comparisonRecord);
                if (!gtInconsistentRevRecordMap.containsKey(fr))
                    gtInconsistentRevRecordMap.put(fr, new ArrayList<>());
                gtInconsistentRevRecordMap.get(fr).add(comparisonRecord);
            }

            if (comparisonRecord.isMtdSrcInconsistent() || comparisonRecord.isMtdDstInconsistent()) {
                mtdInconsistentRecords.add(comparisonRecord);
                if (!mtdInconsistentRevRecordMap.containsKey(fr))
                    mtdInconsistentRevRecordMap.put(fr, new ArrayList<>());
                mtdInconsistentRevRecordMap.get(fr).add(comparisonRecord);
            }

            if (comparisonRecord.isIjmSrcInconsistent() || comparisonRecord.isIjmDstInconsistent()) {
                ijmInconsistentRecords.add(comparisonRecord);
                if (!ijmInconsistentRevRecordMap.containsKey(fr))
                    ijmInconsistentRevRecordMap.put(fr, new ArrayList<>());
                ijmInconsistentRevRecordMap.get(fr).add(comparisonRecord);
            }
        }
    }

    private static void doMappingAndGenerateCsvScript(List<AutoEvaluationRecord> sampleStmtPairRecords, String compareMethod) throws Exception {
        int idx = 1;
        List<String[]> allRecords = new ArrayList<>();

        for (AutoEvaluationRecord record: sampleStmtPairRecords){
            if (revisionRecordMap.get(record.getFileRevision()).size() > 100)
                continue;
            if (record.getStmtType().equals("ImportDeclaration"))
                continue;
            if (record.getFileRevision().first.equals("37cacf15bdb2314b7d3872d726e84ab8ba20a4b1"))
                continue;

            boolean isSrc = record.getRandomSrcOrDst(compareMethod);
            int startPos = record.getStartPos(isSrc);
            doAnalysis(record.getFileRevision(), isSrc, startPos, compareMethod, allRecords, idx);

            idx ++;

            if (idx == sampleNum + 1)
                break;
        }

        String[] header = ManualEvaluationRecord.getHeaders();
        String csvPath = PathResolver.getStmtMappingManualAnalysisCsvPath(project, compareMethod);
        CsvOperationsUtil.writeCSV(csvPath, header, allRecords);
    }

    private static void doAnalysis(FileRevision fr, boolean isSrc, int startPos, String compareMethod,
                                   List<String[]> allRecords, int idx) throws Exception {
        String commitId = fr.first;
        String filePath = fr.second;
        Set<String> filesToAnalyze = new HashSet<>();
        filesToAnalyze.add(filePath);

        CommitAnalysis commitResult = new CommitAnalysis(project, commitId, null,
                filesToAnalyze, true);
        commitResult.calResultMappings(false, true);

        Map<String, RevisionEvaluation> comparisonMap = commitResult.getEvaluationMap();
        RevisionEvaluation comparison = comparisonMap.get(filePath);
        ProgramElement stmtEle = comparison.getStmtElement(isSrc, startPos);
        comparison.generateManualScriptAndRecord(stmtEle, compareMethod);
        List<String[]> tmpRecords = comparison.getRecords();

        String script = comparison.getScript();
        allRecords.addAll(tmpRecords);
        writeScript(script, compareMethod, idx);
    }

    private static void writeScript(String script, String compareMethod, int idx) throws Exception {
        String scriptPath = PathResolver.getStmtMappingManualAnalysisScriptPath(project, compareMethod, idx);
        FileUtils.writeStringToFile(new File(scriptPath), script, "UTF-8");
    }

}
