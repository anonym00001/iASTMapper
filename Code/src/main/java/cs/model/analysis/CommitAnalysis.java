package cs.model.analysis;

import cs.model.gitops.GitHunk;
import cs.model.gitops.GitInfoRetrieval;
import cs.model.gitops.GitUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Perform analysis for a commit.
 *
 * This class provides three ways to analyze a commit
 * 1. common use of tool: to analyze a commit using our algorithm.
 * 2. do comparison: compare mappings of statements and tokens by our algorithm with other algorithms
 * 3. do evaluation: similar to 2, but combine the results of comparison for statements and tokens
 */
public class CommitAnalysis {
    private String project;
    private String commitId;
    private String[] mapMethods;
    private Set<String> filesToAnalyze;
    private Map<String, RevisionAnalysis> resultMap;
    private Map<String, RevisionComparison> comparisonResultMap;
    private Map<String, RevisionEvaluation> evaluationMap;
    private boolean stmtOrToken;

    public CommitAnalysis(String project, String commitId) {
        this.project = project;
        this.commitId = commitId;
        this.mapMethods = null;
        this.filesToAnalyze = null;
        this.resultMap = new HashMap<>();
        this.comparisonResultMap = new HashMap<>();
        this.evaluationMap = new HashMap<>();
        this.stmtOrToken = false;
    }


    public CommitAnalysis(String project, String commitId, String[] mapMethods,
                          Set<String> filesToAnalyze, boolean stmtOrToken) {
        this.project = project;
        this.commitId = commitId;
        this.mapMethods = mapMethods;
        this.filesToAnalyze = filesToAnalyze;
        this.resultMap = new HashMap<>();
        this.comparisonResultMap = new HashMap<>();
        this.evaluationMap = new HashMap<>();
        this.stmtOrToken = stmtOrToken;
    }

    public void calResultMappings(boolean doComparison, boolean doEvaluation){
        String baseCommitId = GitUtils.getBaseCommitId(project, commitId);
        if (baseCommitId == null)
            return;
        Map<String, String> pathMap = GitInfoRetrieval.getOldModifiedFileMap(project, commitId);
        if (pathMap == null || pathMap.size() == 0)
            return;
        System.out.println("Commit is " + commitId);
        System.out.println("The size is " + pathMap.size());
        for (String oldPath: pathMap.keySet()){
            System.out.println("oldPath is " + oldPath);
            if (filesToAnalyze != null && !filesToAnalyze.contains(oldPath))
                continue;
            String newPath = pathMap.get(oldPath);
            if (newPath == null)
                continue;
            if (checkOnlyRenameOperation(project, baseCommitId, commitId, oldPath, newPath))
                continue;
            if (checkAddedOrDeletedLines(oldPath, newPath))
                continue;
            try {
                if (doComparison){
                    /*
                      Case 1:
                      Separately compare mappings of statements and tokens.
                     */
                    RevisionComparison comparison = new RevisionComparison(project, commitId, baseCommitId,
                            oldPath, newPath, stmtOrToken);
                    if (!comparison.isNoGoodResult())
                        comparisonResultMap.put(oldPath, comparison);
                } else if (doEvaluation) {
//                    System.out.println(oldPath);
                    /*
                      Case 2:
                      Compare mappings of statements and tokens collectively.
                     */
                    RevisionEvaluation comparison = new RevisionEvaluation(project, commitId, baseCommitId,
                            oldPath, newPath);
                    if (!comparison.isNoGoodResult())
                        evaluationMap.put(oldPath, comparison);
                } else {
                    /*
                      Case 3:
                      directly analyze mappings generated by our method.
                     */
                    RevisionAnalysis result = new RevisionAnalysis(project, commitId, baseCommitId,
                            oldPath, newPath);
                    if (result.getSrcFilePath() != null && result.getDstFilePath() != null)
                        resultMap.put(oldPath, result);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public Map<String, RevisionAnalysis> getRevisionAnalysisResultMap() {
        return resultMap;
    }

    public Map<String, RevisionComparison> getComparisonResultMap() {
        return comparisonResultMap;
    }

    public Map<String, RevisionEvaluation> getEvaluationMap() {
        return evaluationMap;
    }

    /**
     * If file is only renamed, not necessary to analyze it.
     */
    private boolean checkOnlyRenameOperation(String project, String baseCommitId,
                                             String commitId, String oldFilePath,
                                             String newFilePath) {
        try {
            String oldContent = GitUtils
                    .getFileContentOfCommitFile(project, baseCommitId, oldFilePath)
                    .toString("UTF-8");
            String newContent = GitUtils
                    .getFileContentOfCommitFile(project, commitId, newFilePath)
                    .toString("UTF-8");
            return oldContent.equals(newContent);
        } catch (Exception e){
            throw new RuntimeException("cannot retrieve file content");
        }
    }

    /**
     * If not add or delete code lines, not necessary to analyze it.
     */
    private boolean checkAddedOrDeletedLines(String srcFilePath, String dstFilePath){
        Set<Integer> addedLines = GitHunk.getAllAddedLines(project, commitId, srcFilePath, false);
        Set<Integer> deletedLines = GitHunk.getAllDeletedLines(project, commitId, srcFilePath, false);

        boolean nonAddedLines = addedLines == null || addedLines.size() == 0;
        boolean nonDeletedLines = deletedLines == null || deletedLines.size() == 0;

        return nonAddedLines && nonDeletedLines;
    }
}