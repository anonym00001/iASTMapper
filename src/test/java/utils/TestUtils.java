package utils;

import cs.sysu.algorithm.actions.StmtTokenAction;
import cs.sysu.algorithm.actions.TreeEditAction;
import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.analysis.CommitAnalysis;
import cs.sysu.analysis.RevisionAnalysis;
import cs.sysu.analysis.RevisionComparison;
import cs.sysu.analysis.RevisionEvaluation;
import cs.sysu.baseline.BaselineMatcher;
import cs.sysu.gitops.GitInfoRetrieval;
import cs.sysu.gitops.GitUtils;
import cs.sysu.utils.Pair;

import java.io.*;
import java.util.*;

public class TestUtils {
    public static void testiASTMapper(String project, String commitId, String file) throws IOException {
        Set<String> filePaths = null;
        filePaths = new HashSet<>();
        filePaths.add(file);
        CommitAnalysis mappingResult = new CommitAnalysis(project, commitId,
                null, filePaths, false);
        long time1 = System.currentTimeMillis();
        mappingResult.calResultMappings(false, false);
        Map<String, RevisionAnalysis> resultMap = mappingResult.getRevisionAnalysisResultMap();

        for (String filePath: resultMap.keySet()){
            System.out.println(filePath);

            RevisionAnalysis m = resultMap.get(filePath);

            List<StmtTokenAction> actionList = m.generateActions();
            List<TreeEditAction> treeEditActions = m.generateEditActions();

            for (StmtTokenAction action: actionList)
                System.out.println(action);

            for (TreeEditAction action: treeEditActions) {
                System.out.println(action);
            }

            System.out.println("action num: " + treeEditActions.size());
        }
        long time2 = System.currentTimeMillis();
        System.out.println(time2 - time1);
    }

    public static void testBaseline(String project, String commitId, String file, String method) throws Exception {
        long time1 = System.currentTimeMillis();
        Pair<String, String> contentPair = getSrcAndDstFileContent(project, commitId, file);
        if (contentPair == null)
            return;
        String srcFileContent = contentPair.first;
        String dstFileContent = contentPair.second;

        BaselineMatcher matcher = new BaselineMatcher(srcFileContent, dstFileContent);
        List<StmtTokenAction> actions = matcher.generateStmtTokenEditActionsForMethod(method);
        List<TreeEditAction> treeEditActions = matcher.getTreeEditActions();

        for (StmtTokenAction action: actions)
            System.out.println(action);

        for (TreeEditAction action: treeEditActions)
            System.out.println(action);

        long time2 = System.currentTimeMillis();
        System.out.println(time2 - time1);
        System.out.println("action num: " + treeEditActions.size());
    }

    private static Pair<String, String> getSrcAndDstFileContent(String project,
                                                                String commitId,
                                                                String srcFilePath) throws UnsupportedEncodingException {
        String baseCommitId = GitUtils.getBaseCommitId(project, commitId);
        if (baseCommitId == null)
            return null;
        Map<String, String> pathMap = GitInfoRetrieval.getOldModifiedFileMap(project, commitId);
        if (pathMap == null || pathMap.size() == 0)
            return null;
        String dstPath = pathMap.get(srcFilePath);

        ByteArrayOutputStream srcFileStream = GitUtils.getFileContentOfCommitFile(project, baseCommitId, srcFilePath);
        String srcFileContent = srcFileStream.toString("UTF-8");

        ByteArrayOutputStream dstFileStream = GitUtils.getFileContentOfCommitFile(project, commitId, dstPath);
        String dstFileContent = dstFileStream.toString("UTF-8");

        return new Pair<>(srcFileContent, dstFileContent);
    }
}
