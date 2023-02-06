package cs.model.algorithm.element;

import com.github.gumtreediff.tree.ITree;
import cs.model.algorithm.ttmap.TokenRange;
import cs.model.algorithm.ttmap.TokenTypeCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class of token element.
 *
 * A token in an AST is represented as a token element.
 * A token is related to a range including a start
 * character position and end character position.
 */
public class TokenElement extends InnerStmtElement implements ProgramElement {

    protected TokenRange tokenRange;         // range of token
    protected String tokenType;              // type of token

    // When printing tokens, we make sure that
    // the column of "line" and "index" are consistent.
    // By doing so, it is more convenient to visualize
    // the mappings of tokens.
    private static final int positionIdx = 30;

    // cache the ancestor inner-stmt elements
    private List<InnerStmtElement> innerStmtElementsWithToken = null;

    // cache the ancestor multi-token elements
    private List<InnerStmtElement> multiTokenElementsWithToken = null;

    // cache the nearest multi-token element
    private InnerStmtElement nearestMultiTokenElement = null;

    // cache inner-stmt element represented by the token
    private InnerStmtElement innerStmtEleOfToken = null;

    @Override
    public boolean isRoot() {
        return isRoot;
    }

    @Override
    public boolean equalValue(ProgramElement element) {
        if (element == this)
            return true;
        if (element == null)
            return false;
        if (!(element instanceof TokenElement))
            return false;
        TokenElement objEle = (TokenElement) element;
        return objEle.getStringValue().equals(this.getStringValue());
    }

    @Override
    public boolean isStmt() {
        return false;
    }

    @Override
    public boolean isToken() {
        return true;
    }

    @Override
    public boolean isInnerStmtElement() {
        return false;
    }

    @Override
    public boolean isDeclaration() {
        return false;
    }

    private String repeat(char c, int number){
        String ret = "";
        for (int i = 0; i < number; i++)
            ret += c;
        return ret;
    }

    @Override
    public String toString() {
        String tmpStr = "(LINE:+";
        if (isFromSrc())
            tmpStr = "(LINE:-";

        if (this.value.length() > positionIdx)
            tmpStr = " " + tmpStr;
        else
            tmpStr = repeat(' ', positionIdx - this.value.length()) + tmpStr;

        return this.value + tmpStr + startLine + ", INDEX:" + childIdx + ")";
    }

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof TokenElement))
            return false;
        TokenElement element = (TokenElement) obj;
        if (this.isFromSrc() != element.isFromSrc())
            return false;
        return this.getTokenRange().equals(element.getTokenRange());
    }

    @Override
    public int hashCode(){
        int hash = 17;
        hash = hash * 31 + Boolean.hashCode(isFromSrc());
        hash = hash * 31 + this.getTokenRange().hashCode();
        return hash;
    }

    @Override
    public ProgramElement getLeftSibling() {
        if (getChildIdx() == 0)
            return null;
        return getStmtElement().getTokenElements().get(getChildIdx() - 1);
    }

    @Override
    public ProgramElement getRightSibling() {
        int size = getStmtElement().getTokenElements().size();
        if (getChildIdx() < size - 1) {
            return getStmtElement().getTokenElements().get(getChildIdx() + 1);
        }
        return null;
    }

    public void setTokenRange(TokenRange tokenRange) {
        this.tokenRange = tokenRange;
    }

    public TokenRange getTokenRange() {
        return tokenRange;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getTokenType() {
        return tokenType;
    }

    public boolean isName() {
        return this.getTokenType().endsWith("NAME");
    }

    public boolean isLiteral() {
        return typeChecker.isLiteral(this.getITreeNode());
    }

    public boolean isVarName() {
        return this.getTokenType().equals(TokenTypeCalculator.VAR_NAME) ||
                this.getTokenType().equals(TokenTypeCalculator.VAR_DEC_NAME);
    }

    private static boolean isStatementNode(ITree t) {
        return typeChecker.isStatementNode(t);
    }

    public InnerStmtElement getNearestMultiTokenInnerStmtElement() {
        if (nearestMultiTokenElement != null)
            return nearestMultiTokenElement;
        if (innerStmtElementsWithToken == null)
            getStmtElement().calTokensOfInnerStmtElements();

        for (InnerStmtElement ele: innerStmtElementsWithToken) {
            if (ele.getTokenElements().size() > 1) {
                nearestMultiTokenElement = ele;
                break;
            }
        }
        if (nearestMultiTokenElement == null)
            nearestMultiTokenElement = new InnerStmtElement();
        return nearestMultiTokenElement;
    }

    /**
     * create ancestor multi-token inner-statement elements for the token
     */
    void createInnerStmtElementsWithToken(Map<ITree, InnerStmtElement> nodeElementMap) {
        if (innerStmtElementsWithToken == null) {
            innerStmtElementsWithToken = new ArrayList<>();
            ITree tmpNode = this.node;
            if (tmpNode != null) {
                while (tmpNode != null && !isStatementNode(tmpNode)) {
                    InnerStmtElement ele = nodeElementMap.get(tmpNode);
//                    System.out.println("The inner stmt is " + ele);
                    ele.addTokenElement(this);
                    innerStmtElementsWithToken.add(ele);
                    tmpNode = tmpNode.getParent();
                }
            }
        }
    }

    public List<InnerStmtElement> getInnerStmtElementsWithToken() {
        if (innerStmtElementsWithToken == null)
            getStmtElement().calTokensOfInnerStmtElements();
        return innerStmtElementsWithToken;
    }

    public List<InnerStmtElement> getMultiTokenElementsWithToken() {
        if (multiTokenElementsWithToken != null)
            return multiTokenElementsWithToken;
        multiTokenElementsWithToken = new ArrayList<>();
        List<InnerStmtElement> tmpElements = getInnerStmtElementsWithToken();
        for (InnerStmtElement element: tmpElements) {
            if (element.getTokenElements().size() > 0)
                multiTokenElementsWithToken.add(element);
        }
        return multiTokenElementsWithToken;
    }

    public void setInnerStmtEleOfToken(InnerStmtElement innerStmtEleOfToken) {
        this.innerStmtEleOfToken = innerStmtEleOfToken;
    }

    public InnerStmtElement getInnerStmtEleOfToken() {
        return innerStmtEleOfToken;
    }
}
