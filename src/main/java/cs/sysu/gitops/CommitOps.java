package cs.sysu.gitops;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * APIs to process RevCommit object in Jgit.
 */
public class CommitOps {

    public static String getCommitId(RevCommit rc){
        return rc.getId().getName();
    }

    public static String getCommitMessage(RevCommit rc){
        return rc.getFullMessage();
    }

    public static PersonIdent getCommitterIdentity(RevCommit rc){
        return rc.getCommitterIdent();
    }

    public static String getCommitter(RevCommit rc){
        return rc.getCommitterIdent().getName();
    }

    public static String getCommitterEmail(RevCommit rc){
        return rc.getCommitterIdent().getEmailAddress();
    }

    public static int getCommitTime(RevCommit rc){
        return rc.getCommitTime();
    }

    public static int getParentCount(RevCommit rc){
        return rc.getParentCount();
    }

    public static RevCommit[] getParents(RevCommit rc){
        return rc.getParents();
    }

    public static boolean isMerge(RevCommit rc) {
        return rc.getParentCount() > 1;
    }

    public static boolean isChild(RevCommit child, RevCommit parent){
        if (child.getParentCount() > 0){
            for (RevCommit p: child.getParents()){
                String pId = getCommitId(p);
                String parentId = getCommitId(parent);
                if (pId.equals(parentId))
                    return true;
            }

        }
        return false;
    }

    public static List<String> getParentCommitIds(RevCommit rc){
        List<String> parentIds = new ArrayList<>();
        RevCommit[] parents = rc.getParents();
        if (rc.getParentCount() == 0)
            return null;
        else{
            for(RevCommit commit: parents)
                parentIds.add(commit.getId().getName());
            return parentIds;
        }
    }
}
