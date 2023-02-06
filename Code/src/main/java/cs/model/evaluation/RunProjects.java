package cs.model.evaluation;

import java.io.IOException;


public class RunProjects {
    private static final String[] projects = {
            "activemq", "junit4", "commons-io", "commons-lang","commons-math",
            "hibernate-orm", "hibernate-search", "netty", "spring-framework", "spring-roo"
    };
    public static void main(String[] args) throws IOException {
        for (String project: projects){
            System.out.println("Projects: " + project);
            AnalysisMultipleCommits.init(project);
            AnalysisMultipleCommits.run();
        }
    }
}
