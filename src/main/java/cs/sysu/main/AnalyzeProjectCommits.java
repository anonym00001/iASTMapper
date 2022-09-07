package cs.sysu.main;

import cs.sysu.main.analysis.StmtMappingCompareAnalysis;
import cs.sysu.main.analysis.StmtMappingSingleAnalysis;

/**
 * Perform analysis on all the studied projects
 */
public class AnalyzeProjectCommits {

    // analyzed projects
    private static final String[] projects = {
            "activemq", "commons-io","commons-math", "commons-lang", "junit4",
            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"
    };

    private static final boolean doCompare = true;
    private static final boolean doEvaluation = true;

    public static void main(String[] args){
        for (String project: projects){
            if (doCompare) {
                StmtMappingCompareAnalysis.doAnalysis(project, doEvaluation);
            } else {
                StmtMappingSingleAnalysis.doAnalysis(project);
            }
        }
    }
}
