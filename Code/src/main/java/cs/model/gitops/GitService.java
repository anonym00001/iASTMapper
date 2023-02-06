package cs.model.gitops;

import cs.model.utils.ExternalProcess;
import cs.model.utils.Pair;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListTagCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * APIs of git operations based on JGit.
 */

public class GitService {

    private static final String REMOTE_REFS_PREFIX = "refs/remotes/origin/";

    private String projectFolder;
    private String cloneUrl;
    private Repository repository;


    private void setProjectFolder(String projectFolder){
        this.projectFolder = projectFolder;
    }

    private void setCloneUrl(String cloneUrl){
        this.cloneUrl = cloneUrl;
    }

    public String getProjectFolder(){
        return projectFolder;
    }

    private void cloneIfNotExist() throws Exception{
        File folder = new File(projectFolder);
        assert (projectFolder != null && cloneUrl != null);
        if (!folder.exists()){
            Git.cloneRepository()
                    .setDirectory(folder)
                    .setURI(cloneUrl)
                    .setCloneAllBranches(true)
                    .call();
        }
    }

    private void openRepository() throws Exception{
        File folder = new File(projectFolder);
        assert (folder.exists());
        RepositoryBuilder builder = new RepositoryBuilder();
        repository = builder
                .setGitDir(new File(folder,".git"))
                .readEnvironment()
                .findGitDir()
                .build();
        File indexLockFile = new File(repository.getDirectory(), "index.lock");
        if (indexLockFile.exists())
            if (!indexLockFile.delete()){
                throw new RuntimeException("index.lock cannot be deleted");
            }
    }

    public Repository getRepository() {
        return repository;
    }

    public void closeRepository(){
        repository.close();
    }

    /**
     * In Windows, this function usually results in exceptions.
     */
    @Deprecated
    public void checkout(String commitId) throws Exception{
        Git git = new Git(repository);
        cleanCheckout();
        CheckoutCommand checkoutCmd = git.checkout().setName(commitId);
        checkoutCmd.call();
    }

    public void checkoutToSeparatePath(String commitId) throws Exception{
        File workTree = new File(projectFolder);
        String destinationPath = new File(workTree, "commit-checkout\\tmp-"+commitId).getAbsolutePath();
        GitUtils.createDirectory(destinationPath);
        ExternalProcess.execute(workTree, "git", "--work-tree",
                destinationPath, "checkout", commitId, "--", ".");
    }

    public Map<String, String> getAllTags() throws Exception {
        Git git = new Git(repository);
        ListTagCommand listTagCommand = git.tagList();
        List<Ref> tagRefs = listTagCommand.call();

        Map<String, String> tags = new HashMap<>();

        for (Ref r: tagRefs){
            String tagName = r.getName();
            int index = tagName.lastIndexOf('/');
            tagName = tagName.substring(index + 1);
            RevCommit commit = getCommitObjFromId(r.getObjectId().getName());
            tags.put(tagName, CommitOps.getCommitId(commit));
        }
        return tags;
    }


    public void cleanCheckout() throws Exception {
        Git git = new Git(repository);
        git.clean().setForce(true).setCleanDirectories(true).setIgnore(true).call();
        git.reset().setMode(ResetCommand.ResetType.HARD).call();
        git.close();
    }

    /**
     * Get jsonops ids of all the branches named like %branch% in a repository
     * If branch == null, retrieve all branches
     */
    public List<ObjectId> getAllBranchObjectId(String branch) throws Exception{
        List<ObjectId> currentRemoteRefs = new ArrayList<ObjectId>();
//        System.out.println("Repositiry " +  repository.getRefDatabase().getRefs());
        for (Ref ref: repository.getRefDatabase().getRefs()){
            String refName = ref.getName();
//            System.out.println("redName is " + refName);
            if (branch == null || refName.endsWith("/" + branch)) {
                currentRemoteRefs.add(ref.getObjectId());
            }
        }
        return currentRemoteRefs;
    }

    private RevWalk createReverseRevWalk(){
        RevWalk walk = new RevWalk(repository);
        walk.sort(RevSort.COMMIT_TIME_DESC, true);
        walk.sort(RevSort.REVERSE, true);
        return walk;
    }

    public RevWalk getRevWalk(ObjectId remoteRef) throws Exception{
        RevWalk walk = createReverseRevWalk();
        walk.markStart(walk.parseCommit(remoteRef));
        return walk;
    }

    public RevWalk getAllRevWalk(List<ObjectId> remoteRefs) throws Exception{
        RevWalk walk = createReverseRevWalk();
        for (ObjectId refId: remoteRefs){
            RevCommit start;
            try {
                start = walk.parseCommit(refId);
            } catch (IncorrectObjectTypeException e){
                continue;
            }
            walk.markStart(start);
        }
        // Filter all merge changes
        //walk.setRevFilter(RevFilter.NO_MERGES);
        return walk;
    }

    private TreeWalk getDiffTree(RevCommit rc, RevCommit bc) throws Exception{
        if (rc.getParentCount() == 0){
            AbstractTreeIterator oldTreeIter = new EmptyTreeIterator();
            ObjectReader reader = repository.newObjectReader();
            AbstractTreeIterator newTreeIter = new CanonicalTreeParser(null, reader, rc.getTree());
            final TreeWalk tw = new TreeWalk(repository);
            tw.setRecursive(true);
            tw.addTree(oldTreeIter);
            tw.addTree(newTreeIter);
            return tw;
        }
        ObjectId oldTree = bc.getTree();
        ObjectId newTree = rc.getTree();
        final TreeWalk tw = new TreeWalk(repository);
        tw.setRecursive(true);
        tw.addTree(oldTree);
        tw.addTree(newTree);
        return tw;
    }

    /**
     * Get a commit Obj from a commitId
     */
    public RevCommit getCommitObjFromId(String commitId){
        ObjectId commitObjId = ObjectId.fromString(commitId);
        try (RevWalk revWalk = new RevWalk(repository)) {
            return revWalk.parseCommit(commitObjId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void getFileModificationInfo(RevCommit currentCommit, RevCommit baseCommit,
                                        List<String> addFiles, Map<String, String> oldModifiedFiles) throws Exception {
        TreeWalk tw = getDiffTree(currentCommit, baseCommit);
        List<DiffEntry> diffs = DiffEntry.scan(tw);
        final RenameDetector rd = new RenameDetector(repository);
        rd.addAll(diffs);

        List<DiffEntry> diffs2 = rd.compute(tw.getObjectReader(), null);

        for (DiffEntry diff: diffs2){
            DiffEntry.ChangeType changeType = diff.getChangeType();
            String oldPath = diff.getOldPath();
            String newPath = diff.getNewPath();

            // 如果不是增加文件，也不是拷贝文件
            if (changeType != DiffEntry.ChangeType.ADD && changeType != DiffEntry.ChangeType.COPY)
                oldModifiedFiles.put(oldPath, newPath);
            else
                addFiles.add(newPath);
        }
        tw.close();
    }

    // 获取Commit全部的非新加文件
    public Map<String, String> getOldModifiedFiles(RevCommit currentCommit, RevCommit baseCommit) throws Exception{
        Map<String, String> ret = new HashMap<>();

        TreeWalk tw = getDiffTree(currentCommit, baseCommit);
        List<DiffEntry> diffs = DiffEntry.scan(tw);
        final RenameDetector rd = new RenameDetector(repository);
        rd.addAll(diffs);

        List<DiffEntry> diffs2 = rd.compute(tw.getObjectReader(), null);

        for (DiffEntry diff: diffs2){
            DiffEntry.ChangeType changeType = diff.getChangeType();
            String oldPath = diff.getOldPath();
            String newPath = diff.getNewPath();

            // 如果不是增加文件，也不是拷贝文件
            if (changeType != DiffEntry.ChangeType.ADD && changeType != DiffEntry.ChangeType.COPY)
                ret.put(oldPath, newPath);
        }
        tw.close();
        return ret;
    }

    public ByteArrayOutputStream catOperation(RevCommit commit, String path){
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        RevTree tree = commit.getTree();

        // now try to find a specific file
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(path));
            if (!treeWalk.next()) {
                throw new IllegalStateException("Did not find expected file 'README.md'");
            }

            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repository.open(objectId);

            // and then one can the loader to read the file
            loader.copyTo(output);
        } catch (Exception e){
            throw new RuntimeException("Git Cat Error: " + e.getMessage());
        }

        return output;
    }

    /**
     * Get the diff details of the current commit compared to its base commit.
     * @param currentCommit the commit needed to calculate diff
     * @param baseCommit the base commit of currentCommit
     */
    public void fileTreeDiff(RevCommit currentCommit, RevCommit baseCommit,
                             List<String> addedFiles, List<String> deletedFiles,
                             List<String> modifiedFiles, List<Pair<String, String>> renamedFiles,
                             Map<String, Integer> deletedFileChurn,
                             Map<String, Pair<Integer, Integer>> nonDeletedFileChurn) throws Exception{
        // The maps must be first initialized
        TreeWalk tw = getDiffTree(currentCommit, baseCommit);

        List<DiffEntry> diffs = DiffEntry.scan(tw);
        final RenameDetector rd = new RenameDetector(repository);
        rd.addAll(diffs);

        List<DiffEntry> diffs2 = rd.compute(tw.getObjectReader(), null);

        DiffFormatter diffFormatter = new DiffFormatter( DisabledOutputStream.INSTANCE );
        diffFormatter.setRepository(repository);
        diffFormatter.setContext( 0 );

        for (DiffEntry diff: diffs2){
            FileHeader fileHeader = diffFormatter.toFileHeader(diff);
            List<? extends HunkHeader> hunks = fileHeader.getHunks();
            int linesAdded = 0;
            int linesDeleted = 0;
            for (HunkHeader hunk: hunks) {
                EditList eList = hunk.toEditList();
                for (Edit e: eList){
                    linesDeleted += e.getLengthA();
                    linesAdded += e.getLengthB();
                }
            }

            DiffEntry.ChangeType changeType = diff.getChangeType();
            String oldPath = diff.getOldPath();
            String newPath = diff.getNewPath();

            if (changeType == DiffEntry.ChangeType.ADD){
                addedFiles.add(newPath);
                nonDeletedFileChurn.put(newPath,
                        new Pair<Integer, Integer>(linesAdded, linesDeleted));
            }

            if (changeType == DiffEntry.ChangeType.DELETE){
                deletedFiles.add(oldPath);
                deletedFileChurn.put(oldPath, linesDeleted);
            }

            if (changeType == DiffEntry.ChangeType.COPY){
                addedFiles.add(newPath);
                nonDeletedFileChurn.put(newPath,
                        new Pair<Integer, Integer>(linesAdded, linesDeleted));
            }

            if (changeType == DiffEntry.ChangeType.MODIFY){
                assert (oldPath.equals(newPath));
                modifiedFiles.add(newPath);
                nonDeletedFileChurn.put(newPath,
                        new Pair<Integer, Integer>(linesAdded, linesDeleted));
            }
            if (changeType == DiffEntry.ChangeType.RENAME){
                renamedFiles.add(new Pair<String, String>(oldPath, newPath));
                nonDeletedFileChurn.put(newPath,
                        new Pair<Integer, Integer>(linesAdded, linesDeleted));
            }
        }
        tw.close();
    }


    public Map<String, ByteArrayOutputStream> diffOperation(RevCommit curCommit, RevCommit baseCommit,
                                                            Map<String, String> oldModifiedFileMap) throws Exception{
        // The maps must be first initialized
        TreeWalk tw = getDiffTree(curCommit, baseCommit);
        List<DiffEntry> diffs = DiffEntry.scan(tw);
        final RenameDetector rd = new RenameDetector(repository);
        rd.addAll(diffs);

        List<DiffEntry> diffs2 = rd.compute(tw.getObjectReader(), null);
        DiffFormatter diffFormatter = new DiffFormatter( DisabledOutputStream.INSTANCE );
        diffFormatter.setRepository(repository);
        diffFormatter.setContext( 0 );

        Map<String, ByteArrayOutputStream> ret = new HashMap<>();

        for (DiffEntry entry: diffs2){
            // 不考虑增加文件
            DiffEntry.ChangeType changeType = entry.getChangeType();
            if (changeType == DiffEntry.ChangeType.ADD)
                continue;

            String oldPath = entry.getOldPath();
            String newPath = entry.getNewPath();

            if (!oldModifiedFileMap.get(oldPath).equals(newPath))
                continue;

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            DiffFormatter formatter = new DiffFormatter(output);
            formatter.setRepository(repository);
            formatter.format(entry);
            ret.put(oldPath, output);
        }
        return ret;
    }

    public void fileTreeDiff(RevCommit currentCommit, RevCommit baseCommit,
                             List<String> javaFilesBefore, List<String> javaFilesCurrent, Map<String, String> renamedFilesHint) throws Exception {
        if (currentCommit.getParentCount() > 0) {
            ObjectId oldTree = baseCommit.getTree();
            ObjectId newTree = currentCommit.getTree();
            final TreeWalk tw = new TreeWalk(repository);
            tw.setRecursive(true);
            tw.addTree(oldTree);
            tw.addTree(newTree);

            final RenameDetector rd = new RenameDetector(repository);
            rd.setRenameScore(80);
            rd.addAll(DiffEntry.scan(tw));

            for (DiffEntry diff : rd.compute(tw.getObjectReader(), null)) {
                DiffEntry.ChangeType changeType = diff.getChangeType();
                String oldPath = diff.getOldPath();
                String newPath = diff.getNewPath();
                if (changeType != DiffEntry.ChangeType.ADD) {
                    if (isJavafile(oldPath)) {
                        javaFilesBefore.add(oldPath);
                    }
                }
                if (changeType != DiffEntry.ChangeType.DELETE) {
                    if (isJavafile(newPath)) {
                        javaFilesCurrent.add(newPath);
                    }
                }
                if (changeType == DiffEntry.ChangeType.RENAME && diff.getScore() >= rd.getRenameScore()) {
                    if (isJavafile(oldPath) && isJavafile(newPath)) {
                        renamedFilesHint.put(oldPath, newPath);
                    }
                }
            }
        }
    }

    private boolean isJavafile(String path) {
        return path.endsWith(".java");
    }

    public static Pair<String, String> createGitRepoForFileRevision(String srcFilePath, String dstFilePath,
                                                                    String fileName,
                                                                    String repoPath) throws IOException, GitAPIException {
        GitUtils.removeAllFiles(repoPath);
        String tempPath = new File(new File(repoPath), ".git").getAbsolutePath();
        Repository localRepo = new FileRepository(tempPath);
        Git git = new Git(localRepo);
        localRepo.create();
        String filePath = new File(new File(repoPath), fileName).getAbsolutePath();
        FileUtils.copyFile(new File(srcFilePath), new File(filePath));
        git.add().addFilepattern(".").call();
        RevCommit baseCommit = git.commit().setMessage("commit 1").call();
        FileUtils.copyFile(new File(dstFilePath), new File(filePath));
        git.add().addFilepattern(".").call();
        RevCommit curCommit = git.commit().setMessage("commit 2").call();
        String baseCommitId = CommitOps.getCommitId(baseCommit);
        String curCommitId = CommitOps.getCommitId(curCommit);
        return new Pair<>(baseCommitId, curCommitId);
    }

    public static class GitServiceFactory{

        public static GitService makeGitService(String projectFolder, String cloneUrl) throws Exception{
            GitService gitService = new GitService();
            gitService.setProjectFolder(projectFolder);
            gitService.setCloneUrl(cloneUrl);
            gitService.cloneIfNotExist();
            gitService.openRepository();
            return gitService;
        }
    }
}
