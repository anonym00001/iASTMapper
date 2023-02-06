package cs.model.gitops;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * Retrieve the commit information of a project.
 */
public class GitProjectInfoRetrieval {
    private GitService gitService;
    private String currentProjectName;
    private List<String> commitIds;
    private Map<String, RevCommit> commitObjMap;

    public GitProjectInfoRetrieval(String project){
        this.gitService = GitUtils.getRawGitService(project);
        this.currentProjectName = project;
        this.commitIds = new ArrayList<>();
        this.commitObjMap = new HashMap<>();
        init();
    }

    private void init() {
        try {
            RevWalk revWalk = GitUtils.getRevWalkForAllBranches(gitService);
            Iterator<RevCommit> iter = revWalk.iterator();
            while (iter.hasNext()) {
                RevCommit tmp = iter.next();
                String commitId = CommitOps.getCommitId(tmp);
                commitIds.add(commitId);
                commitObjMap.put(commitId, tmp);
            }

        } catch (Exception e){
            throw new RuntimeException("Git Error");
        }
    }

    public GitService getGitService() {
        return gitService;
    }

    public String getCurrentProjectName() {
        return currentProjectName;
    }

    public List<String> getAllCommitIds() {
        return commitIds;
    }

    public Map<String, RevCommit> getCommitObjMap() {
        return commitObjMap;
    }

    public boolean checkCurrentProject(String project){
        return project.equals(currentProjectName);
    }

    public RevCommit getCommitObjById(String commitId){
        return commitObjMap.get(commitId);
    }

    public ByteArrayOutputStream getFileContentOfCommitFile(String commitId, String filePath){
        RevCommit curCommit = getCommitObjById(commitId);
        return gitService.catOperation(curCommit, filePath);
    }

    public String getBaseCommitId(String commitId){
//        System.out.println("the commitId is " + commitId);
        RevCommit curCommit = getCommitObjById(commitId);
        if (curCommit.getParentCount() == 1)
            return CommitOps.getCommitId(curCommit.getParent(0));
        return null;
    }

    public Repository getRepository(){
        return gitService.getRepository();
    }

    public void close(){
        gitService.closeRepository();
    }
}
