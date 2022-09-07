package cs.sysu.algorithm.ttmap;

import com.github.gumtreediff.tree.ITree;
import cs.sysu.utils.Pair;

import java.util.Map;

/**
 * Range of a token
 *
 * Field:
 * first is start position;
 * second is end position.
 */
public class TokenRange extends Pair<Integer, Integer> {
    public TokenRange(Integer a, Integer b) {
        super(a, b);
    }

    public TokenRange(Pair<Integer, Integer> range){
        super(range.first, range.second);
    }

    /**
     * Return the position info for a token
     * @param ttmap a tree token map
     * @return a string like "line:1, index:1"
     */
    public String toPositionString(TreeTokensMap ttmap){
        ITree stmt = ttmap.getStmtOfTokenRange(this);
        Map<TokenRange, Integer> tokenIndexMap = ttmap.getTokenRangeIndexMapOfNode(stmt);
        int index = tokenIndexMap.get(this);
        int line = ttmap.getLineRangeOfStmt(stmt).first;
        return "Line:" + line + ", Index:" + index;
    }

    /**
     * Get the token value
     * @param ttMap a tree token map
     * @return string value of the token
     */
    public String toString(TreeTokensMap ttMap){
        return ttMap.getTokenByRange(this);
    }

    public String toString(TreeTokensMap ttmap, boolean withStmtInfo){
        String value = ttmap.getTokenByRange(this);
        if (withStmtInfo)
            value += "(" + toPositionString(ttmap) + ")";
        return value;
    }
}
