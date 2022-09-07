package cs.sysu.algorithm.matcher.fastmatchers;

import cs.sysu.algorithm.element.ProgramElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Store token mapping information for each statement
 */
public class TokenMapStatistics {
    private Map<ProgramElement, Integer> srcStmtMappedTokenNumMap;
    private Map<ProgramElement, Integer> dstStmtMappedTokenNumMap;

    public TokenMapStatistics(){
        srcStmtMappedTokenNumMap = new HashMap<>();
        dstStmtMappedTokenNumMap = new HashMap<>();
    }

    /**
     * When a token is mapped, we record it for the
     * token mapping statistics of the relevant statement
     */
    public void tokenMapIncrement(ProgramElement stmt){
        if (stmt.isFromSrc()){
            if (!srcStmtMappedTokenNumMap.containsKey(stmt))
                srcStmtMappedTokenNumMap.put(stmt, 0);
            srcStmtMappedTokenNumMap.put(stmt, srcStmtMappedTokenNumMap.get(stmt) + 1);
        } else {
            if (!dstStmtMappedTokenNumMap.containsKey(stmt))
                dstStmtMappedTokenNumMap.put(stmt, 0);
            dstStmtMappedTokenNumMap.put(stmt, dstStmtMappedTokenNumMap.get(stmt) + 1);
        }
    }

    /**
     * Record that all tokens of the statement are mapped.
     * @param stmt the statement
     */
    public void recordAllTokenForStmt(ProgramElement stmt){
        if (stmt.isFromSrc())
            srcStmtMappedTokenNumMap.put(stmt, stmt.getTokenElements().size());
        else
            dstStmtMappedTokenNumMap.put(stmt, stmt.getTokenElements().size());
    }

    /**
     * Whether all tokens of a statement are mapped.
     * @param stmt statement to analyze
     * @return true if all tokens of the statement are mapped.
     */
    public boolean isAllTokenMapped(ProgramElement stmt){
        if (stmt.isFromSrc())
            return srcStmtMappedTokenNumMap.containsKey(stmt) &&
                    srcStmtMappedTokenNumMap.get(stmt) == stmt.getTokenElements().size();
        else
            return dstStmtMappedTokenNumMap.containsKey(stmt) &&
                    dstStmtMappedTokenNumMap.get(stmt) == stmt.getTokenElements().size();
    }
}
