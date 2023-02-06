package cs.model.main;

import cs.model.main.script.EvaluationScriptGenerator;
import cs.model.main.script.StmtComparisonScriptGenerator;

/**
 * Generate manual analysis script for the studied projects.
 */
public class GenerateEvaluationScript {
    private static final String[] projects = {
            "activemq", "junit4", "commons-io", "commons-lang","commons-math",
            "hibernate-orm", "hibernate-search", "spring-framework", "spring-roo", "netty"
    };

    private static final boolean doEvaluation = true;

    public static void main(String[] args) throws Exception {
        for (String project: projects) {
            if (doEvaluation)
                EvaluationScriptGenerator.generate(project, 20);
            else
                StmtComparisonScriptGenerator.generate(project, 10);
        }
    }
}
