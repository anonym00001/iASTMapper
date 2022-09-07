package cs.sysu.utils;

import cs.sysu.evaluation.config.MyConfig;

import java.io.File;

public final class PathResolver {

    private final static String rootPath = MyConfig.getRootPath();

    /**
     * Get the repo path for project
     * @param projectName name of the project
     * @return repo path
     */
    public static String projectFolder(String projectName){
        return new File(new File(rootPath), "projects/" + projectName).getAbsolutePath();
    }

    /**
     * Get check out path for project and its commit
     * @param projectName the project
     * @param commitId the commit id
     * @return the path
     */
    public static String commitCheckoutFolder(String projectName, String commitId){
        String commitFolder = "commit-checkout/tmp-" + commitId;
        return new File(new File(projectFolder(projectName)), commitFolder).getAbsolutePath();
    }

    /**
     * Csv path of the running record
     */
    public static String getRunningRecordCsvPath(String project, int idx){
        File resultFolder = new File(new File(rootPath), "run-record");
        File projectResultFolder = new File(resultFolder, project);
        File csvFile = new File(projectResultFolder,"record" + idx + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getRunningRecordDirPath(String project){
        File resultFolder = new File(new File(rootPath), "run-record");
        File projectResultFolder = new File(resultFolder, project);
        return projectResultFolder.getAbsolutePath();
    }

    /**
     * Stmt Mapping analysis path
     */
    public static String getStmtMappingAnalyzeCsvPath(String project, int fileId){
        File projectFolder = new File(getStmtMappingAnalyzeDirPath(project));
        File csvFile = new File(projectFolder, "result" + fileId + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getStmtMappingAnalyzeRecordCsvPath(String project, int fileId){
        File projectFolder = new File(getStmtMappingAnalyzeDirPath(project));
        File csvFile = new File(projectFolder, "record" + fileId + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getActionStatisticCsvPath(String project, int fileId) {
        File projectFolder = new File(getStmtMappingAnalyzeDirPath(project));
        File csvFile = new File(projectFolder, "action" + fileId + ".csv");
        return csvFile.getAbsolutePath();
    }

    public static String getStmtMappingAnalyzeDirPath(String project) {
        File resultFolder = new File(new File(rootPath), "stmt-analysis");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File projectFolder = new File(resultFolder, project);
        if (!projectFolder.exists())
            projectFolder.mkdirs();
        return projectFolder.getAbsolutePath();
    }

    public static String getStmtMappingManualAnalysisCsvPath(String project){
        File resultFolder = new File(new File(rootPath), "stmt-manual-analysis");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File projectFolder = new File(resultFolder, project);
        if (!projectFolder.exists())
            projectFolder.mkdirs();
        File csvFile = new File(projectFolder, "manual.csv");
        return csvFile.getAbsolutePath();
    }

    public static String getStmtMappingManualAnalysisCsvPath(String project, String compareMethod) {
        File resultFolder = new File(new File(rootPath), "stmt-manual-analysis");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File methodFolder = new File(resultFolder, compareMethod);
        if (!methodFolder.exists())
            methodFolder.mkdirs();
        File projectFolder = new File(methodFolder, project);
        if (!projectFolder.exists())
            projectFolder.mkdirs();
        File csvFile = new File(projectFolder, "manual.csv");
        return csvFile.getAbsolutePath();
    }

    public static String getStmtMappingManualAnalysisScriptPath(String project, int idx) {
        File resultFolder = new File(new File(rootPath), "stmt-manual-analysis");
        if (!resultFolder.exists())
            resultFolder.mkdirs();
        File projectFolder = new File(resultFolder, project);
        if (!projectFolder.exists())
            projectFolder.mkdirs();
        File csvFile = new File(projectFolder, "manual" + idx + ".txt");
        return csvFile.getAbsolutePath();
    }

    public static String getStmtMappingManualAnalysisScriptPath(String project, String compareMethod, int idx) {
        File resultFolder = new File(new File(rootPath), "stmt-manual-analysis");
        if (!resultFolder.exists())
            resultFolder.mkdirs();

        File methodFolder = new File(resultFolder, compareMethod);
        if (!methodFolder.exists())
            methodFolder.mkdirs();

        File projectFolder = new File(methodFolder, project);
        if (!projectFolder.exists())
            projectFolder.mkdirs();
        File csvFile = new File(projectFolder, "manual" + idx + ".txt");
        return csvFile.getAbsolutePath();
    }
}
