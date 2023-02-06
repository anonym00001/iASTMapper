package cs.model.gitops;

import cs.model.evaluation.config.MyConfig;
import cs.model.utils.PathResolver;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * APIs for GitService
 */
public class GitUtils {

    private static GitProjectInfoRetrieval projectInfoRetrieval = null;

    public static boolean isJavaFile(String filePath){
        return filePath != null && (filePath.toLowerCase().endsWith(".java") ||
                filePath.toLowerCase().endsWith(".java.t"));
    }

    public static String getGitRelativePath(String filePath, String projectPath){
        return getGitRelativePath(new File(filePath), new File(projectPath));
    }

    private static String getGitRelativePath(File fileObj, File projectDir){
        String filePath = fileObj.getAbsolutePath();
        String projectPath = projectDir.getAbsolutePath();
        assert (filePath.startsWith(projectPath));
        String subPath = filePath.substring(projectPath.length() + 1);
        return subPath.replace(File.separator, "/");
    }

    public static void checkoutCommitToSeparatePath(String project, String commitId) throws Exception{
        String projectFolder = PathResolver.projectFolder(project);
        String cloneUrl = MyConfig.getCloneUrl(project);
        String commitCheckoutFolder = PathResolver.commitCheckoutFolder(project, commitId);
        if (new File(commitCheckoutFolder).exists())
            return;
        GitService gitService = GitService.GitServiceFactory.makeGitService(projectFolder, cloneUrl);
        gitService.checkoutToSeparatePath(commitId);
        gitService.closeRepository();
    }

    /**
     * Check if the file is likely to be a testing file
     *
     * @param gitRelativePath the relative path in git
     * @return whether the file is a testing file
     */
    public static boolean isLikelyTestFromRelativePath(String gitRelativePath){
        // 1. If configuration says: do not remove test
        //    just return false.
        boolean removeTest = MyConfig.ifRemoveTestFiles();
        if (!removeTest)
            return false;

        if (gitRelativePath.endsWith("Test.java"))
            return true;

        // 2. check whether path contains "test" or "tests"
        String[] subPaths = StringUtils.split(gitRelativePath, '/');
        for (String sp: subPaths){
            String tempSP = sp.toLowerCase();
            if (tempSP.equals("test") || tempSP.equals("tests"))
                return true;
            /**
             * We consider example files are similar to tests.
             */
            if (tempSP.equals("example") || tempSP.equals("examples"))
                return true;

            if (tempSP.startsWith("test") || tempSP.startsWith("example"))
                return true;

            if (tempSP.contains(".test") || tempSP.contains(".example"))
                return true;

            if (tempSP.toLowerCase().endsWith("-test"))
                return true;
        }
        return false;
    }

    public static void removeAllFiles(String directoryPath) {
        File f = new File(directoryPath);
        if (f.exists()){
            try {
                FileUtils.deleteDirectory(f);
            } catch (Exception e){
                // do nothing
            }
        }
    }

    public static void createDirectory(String directoryPath) throws Exception{
        File f = new File(directoryPath);
        if (f.exists())
            removeAllFiles(directoryPath);
        f.mkdirs();
    }

    public static List<String> getAllFilePaths(String dirFolder){
        List<String> allFilePaths = new ArrayList<>();
        File folder = new File(dirFolder);
        assert (folder.exists() || folder.isDirectory());
        File[] subFiles = folder.listFiles();

        if (subFiles == null)
            return allFilePaths;
        for (File f: subFiles){
            if (f.isHidden())
                continue;

            if (f.isFile()){
                if (isJavaFile(f.getAbsolutePath()))
                    allFilePaths.add(f.getAbsolutePath());
            }
            else {
                allFilePaths.addAll(getAllFilePaths(f.getAbsolutePath()));
            }
        }
        return allFilePaths;
    }

    public static List<String> getAllJavaFilePaths(String dirFolder){
        List<String> allFiles = getAllFilePaths(dirFolder);
        List<String> ret = new ArrayList<>();
        for (String f: allFiles){
            if (!isJavaFile(f))
                continue;
            String relativePath = getGitRelativePath(f, dirFolder);
            if (isLikelyTestFromRelativePath(relativePath))
                continue;
            ret.add(f);
        }
        return ret;
    }

    public static RevWalk getRevWalkForAllBranches(GitService gitService) throws Exception{
        List<ObjectId> branches = gitService.getAllBranchObjectId(null);
//        System.out.println("Branches " + branches.size());
        return gitService.getAllRevWalk(branches);
    }

    public static <E> List<E> toList(Iterable<E> iterable) {
        if (iterable instanceof List) {
            return (List<E>) iterable;
        }
        ArrayList<E> list = new ArrayList<E>();
        if (iterable != null) {
            for (E e : iterable) {
                list.add(e);
            }
        }
        return list;
    }

    private static void initProjectInfoRetrieval(String project){
        if (projectInfoRetrieval == null)
            projectInfoRetrieval = new GitProjectInfoRetrieval(project);
        else if (!projectInfoRetrieval.checkCurrentProject(project)) {
            projectInfoRetrieval.close();
            projectInfoRetrieval = new GitProjectInfoRetrieval(project);
        }
    }

    public static List<String> getAllCommitIds(String project){
        initProjectInfoRetrieval(project);
        return projectInfoRetrieval.getAllCommitIds();
    }

    public static RevCommit getCommitObjById(String project, String commitId){
        initProjectInfoRetrieval(project);
        return projectInfoRetrieval.getCommitObjById(commitId);
    }

    /**
     * Get the information of added, deleted and changed files.
     *
     * @param project the analyzed project
     * @param commitId current commit id
     * @param addedFiles the list to store the added files
     * @param oldModifyMap the map to store old file to new file map
     * @param fileDiffMap  the map to store file to git diff map
     * @param getDiff whether get the git diff results
     * @throws Exception
     */
    public static void getModificationInfo(String project, String commitId,
                                           List<String> addedFiles, Map<String, String> oldModifyMap,
                                           Map<String, ByteArrayOutputStream> fileDiffMap,
                                           boolean getDiff) throws Exception{
        initProjectInfoRetrieval(project);
        GitService gitService = projectInfoRetrieval.getGitService();
        RevCommit curCommit = GitUtils.getCommitObjById(project, commitId);
        RevCommit baseCommit;
        if (curCommit.getParentCount() == 1)
            baseCommit = curCommit.getParent(0);
        else
            baseCommit = null;
        if (baseCommit != null) {
            gitService.getFileModificationInfo(curCommit, baseCommit, addedFiles, oldModifyMap);
            if (getDiff) {
                Map<String, ByteArrayOutputStream> tmp = gitService.diffOperation(curCommit, baseCommit, oldModifyMap);
                fileDiffMap.putAll(tmp);
            }
        }
    }

    public static GitService getGitService(String project){
        initProjectInfoRetrieval(project);
        return projectInfoRetrieval.getGitService();
    }

    public static GitService getRawGitService(String project) {
        String projectFolder = PathResolver.projectFolder(project);
        String cloneUrl = MyConfig.getCloneUrl(project);
//        System.out.println("ProjectPath is " + projectFolder + " " + cloneUrl);
        try {
            return GitService.GitServiceFactory.makeGitService(projectFolder, cloneUrl);
        } catch (Exception e){
            throw new RuntimeException("Git Init Error: " + e.getMessage());
        }
    }

    public static ByteArrayOutputStream getFileContentOfCommitFile(String project,
                                                                   String commitId,
                                                                   String filePath){
        initProjectInfoRetrieval(project);
        return projectInfoRetrieval.getFileContentOfCommitFile(commitId, filePath);
    }

    public static String getBaseCommitId(String project, String commitId) {
        initProjectInfoRetrieval(project);
        return projectInfoRetrieval.getBaseCommitId(commitId);
    }

    public static boolean checkNonJavaAndTestFiles(String filePath){
        return GitUtils.isJavaFile(filePath) && (!GitUtils.isLikelyTestFromRelativePath(filePath));
    }
}
