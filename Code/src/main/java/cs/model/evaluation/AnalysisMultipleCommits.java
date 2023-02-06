package cs.model.evaluation;

import cs.model.algorithm.actions.StmtTokenAction;
import cs.model.algorithm.actions.TreeEditAction;
import cs.model.algorithm.element.ProgramElement;
import cs.model.analysis.CommitAnalysis;
import cs.model.analysis.RevisionEvaluation;
import cs.model.gitops.CommitOps;
import cs.model.gitops.GitUtils;
import cs.model.utils.CsvOperationsUtil;
import cs.model.utils.Pair;
import cs.model.utils.PathResolver;
import cs.model.utils.TimeUtil;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

public class AnalysisMultipleCommits {

    private static final Timestamp afterDate = TimeUtil.string2Time("1990-01-01 00:00:00.000");
    private static final Timestamp beforeDate = TimeUtil.string2Time("2019-01-01 00:00:00.000");
    private static final String[] methods = {"gt", "mtdiff", "ijm"};

    private static String project;
    private static Map<Pair<String, String>, List<String[]>> methodRecordMap = new HashMap<>();


    public static void init(String project){
        AnalysisMultipleCommits.project = project;
        methodRecordMap = new HashMap<>();
        for (int i = 0; i < methods.length - 1; i ++){
            String method1 = methods[i];
            for (int j = i + 1; j < methods.length; j++){
                String method2 = methods[j];
                if (method1.equals(method2))
                    continue;
                methodRecordMap.put(new Pair<>(method1, method2), new ArrayList<>());
            }
        }
    }


    public static void run() throws IOException {
        List<String> commitIds = GitUtils.getAllCommitIds(project);
        System.out.println("The length " + commitIds.toArray().length);
        long startTime=System.currentTimeMillis();
        int idx = 0;
        int commitId_length  = commitIds.size();
        for (String commitId: commitIds){
            RevCommit tmp = GitUtils.getCommitObjById(project, commitId);
            if (CommitOps.getCommitTime(tmp) < afterDate.getTime() / 1000)
                continue;
            if (CommitOps.getCommitTime(tmp) > beforeDate.getTime() / 1000)
                continue;
            try{
                Set<String> filesToAnalyze = null;
//                EvalASTMatchForCommit eval = new EvalASTMatchForCommit(project, commitId, methods, filesToAnalyze);

                CommitAnalysis mappingResult = new CommitAnalysis(project, commitId,
                        null, filesToAnalyze, true);
                mappingResult.calResultMappings(false, true);
                Map<String, RevisionEvaluation> evaluationMap = mappingResult.getEvaluationMap();
                for (String filePath: evaluationMap.keySet()) {
                    System.out.println(filePath);
                    RevisionEvaluation comparision = evaluationMap.get(filePath);
                    comparision.generateStmtAndTokenRecords();

                    System.out.println(comparision.getRecords().size());
                    System.out.println(comparision.getEditActionRecord());
                }
                System.out.println("Nums : " + idx + "/" + commitId_length);


            } catch (Exception e){
                System.out.println("HELLO: " + commitId);
                e.printStackTrace();
            }
            idx++;
        }
        long endTime=System.currentTimeMillis(); //获取结束时间
        System.out.println("All Time： "+(endTime-startTime) / (commitId_length + 1) +"ms");
    }
}
