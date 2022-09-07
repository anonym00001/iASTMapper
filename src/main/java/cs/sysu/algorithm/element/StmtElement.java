package cs.sysu.algorithm.element;

import com.github.gumtreediff.tree.ITree;
import cs.sysu.algorithm.languageutils.typechecker.StaticNodeTypeChecker;
import cs.sysu.algorithm.ttmap.TokenRange;

import java.util.*;

/**
 * Class of statement element.
 *
 * A statement in an AST is represented as a statement element.
 * It is composed by multiple token elements including names, literals and other types of tokens.
 * A statement can have descendant statement elements.
 */
public class StmtElement extends AbstractElement implements ProgramElement {

    // value token element map
    protected Map<String, Set<TokenElement>> valueTokenElementMap = null;

    // token frequency map
    protected Map<String, Integer> tokenFreqMap = null;

    // Whether only consider name and literal when considering the string similarity
    protected boolean onlyNameAndLiteral = false;

    // number of names and literals in the statement
    protected int nameAndLiteralNum = -1;

    // range token element map
    protected Map<TokenRange, TokenElement> rangeElementMap = null;

    // the n for the n-grams
    protected int ngramSize = -1;
    protected Set<String> ngrams = null;

    // descendant statements excluding blocks
    protected Set<ProgramElement> nonBlockDescendants = null;

    // name token
    protected TokenElement nameToken = null;

    // tree inner-stmt element map
    protected Map<ITree, InnerStmtElement> treeInnerStmtEleMap;

    // all inner-stmt elements
    protected List<InnerStmtElement> innerStmtElements = null;

    public StmtElement() {
        initNearestDescendantStmts();
        initInnerStmtElements();
        treeInnerStmtEleMap = new HashMap<>();
        innerStmtElements = new ArrayList<>();
    }

    public ITree getJavadoc() {
        if (node.getChildren().size() > 0) {
            ITree t = node.getChild(0);
            if (typeChecker.isJavaDoc(t))
                return t;
        }
        return null;
    }

    InnerStmtElement addInnerStmtElement(ITree t) {
        InnerStmtElement element = new InnerStmtElement(this, t);
        treeInnerStmtEleMap.put(t, element);
        innerStmtElements.add(element);
        return element;
    }

    /**
     * Get value and token map for the stmt
     * @param onlyNameAndLiteral set true when only consider names and literals
     * @return value and token map
     */
    public Map<String, Set<TokenElement>> getValueTokenElementMap(boolean onlyNameAndLiteral) {
        if (valueTokenElementMap == null || (this.onlyNameAndLiteral != onlyNameAndLiteral)){
            valueTokenElementMap = new HashMap<>();
            for (TokenElement tokenEle: this.tokenElements){
//                System.out.println("The token is " + tokenEle + " " + tokenEle.isName() + " " + tokenEle.isLiteral() + " " + tokenEle.tokenType);
                if (onlyNameAndLiteral && (!tokenEle.isName() && !tokenEle.isLiteral()))
                    continue;
                String val = tokenEle.getStringValue();
                if (!valueTokenElementMap.containsKey(val))
                    valueTokenElementMap.put(val, new HashSet<>());
                valueTokenElementMap.get(val).add(tokenEle);
            }
        }
        this.onlyNameAndLiteral = onlyNameAndLiteral;
        return valueTokenElementMap;
    }

    public Map<String, Integer> getTokenNumMap(boolean onlyNameAndLiteral) {
        if (tokenFreqMap == null || (this.onlyNameAndLiteral != onlyNameAndLiteral)){
            Map<String, Set<TokenElement>> valEleMap = getValueTokenElementMap(onlyNameAndLiteral);
            tokenFreqMap = new HashMap<>();
            for (String key: valEleMap.keySet()){
//                System.out.println("The key is " + key);
                tokenFreqMap.put(key, valEleMap.get(key).size());
            }
        }
        return tokenFreqMap;
    }

    public int getNameAndLiteralNum() {
        if (nameAndLiteralNum != -1)
            return nameAndLiteralNum;
        int ret = 0;
        Map<String, Integer> tokenNumMap = getTokenNumMap(true);
        if (tokenNumMap != null) {
            for (String val : tokenNumMap.keySet()) {
                int tokenNum = tokenNumMap.get(val);
//                System.out.println("Token is " + val + " " + tokenNum);
                ret += tokenNum;
            }
        }
        this.nameAndLiteralNum = ret;
        return ret;
    }

    @Override
    public boolean equalValue(ProgramElement element) {
        if (element == this)
            return true;
        if (element == null)
            return false;
        if (!(element instanceof StmtElement))
            return false;
        StmtElement objEle = (StmtElement) element;

        if (objEle.getTokenElements().size() != this.getTokenElements().size())
            return false;

        for (int i = 0; i < objEle.getTokenElements().size(); i++){
            if (!objEle.getTokenElements().get(i).equalValue(this.getTokenElements().get(i)))
                return false;
        }
        return true;
    }

    @Override
    public boolean isStmt() {
        return true;
    }

    @Override
    public boolean isToken() {
        return false;
    }

    @Override
    public boolean isInnerStmtElement() {
        return false;
    }

    @Override
    public boolean isDeclaration() {
        return typeChecker.isDeclaration(this.node);
    }

    public boolean isMethodDec(){
        return typeChecker.isMethodDec(this.getITreeNode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StmtElement))
            return false;
        ProgramElement element = (ProgramElement) obj;
        if (this.node == element.getITreeNode())
            return true;

        if (this.isFromSrc() != element.isFromSrc())
            return false;
        if (this.node.getType() != element.getITreeNode().getType() ||
                this.node.getPos() != element.getITreeNode().getPos())
            return false;
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 17;
        hash = hash * 31 + Boolean.hashCode(isFromSrc());
        hash = hash * 31 + this.node.getType().hashCode();
        hash = hash * 31 + Integer.hashCode(this.node.getPos());
        return hash;
    }

    @Override
    public ProgramElement getLeftSibling() {
        if (getChildIdx() == 0)
            return null;
        return getParentElement().getNearestDescendantStmts().get(getChildIdx() - 1);
    }

    @Override
    public ProgramElement getRightSibling() {
        int size = getParentElement().getNearestDescendantStmts().size();
        if (getChildIdx() < size - 1)
            return getParentElement().getNearestDescendantStmts().get(getChildIdx() + 1);
        return null;
    }

    @Override
    public String toString() {
        String tmpStr = " (LINE:+";
        if (isFromSrc())
            tmpStr = " (LINE:-";
        String ret = this.node.getType().name + tmpStr + startLine + ")";
        return ret;
    }

    /**
     * Get type list of method
     */
    public List<MethodParameterType> getMethodTypeList() {
        if (isMethodDec()){
            if (methodTypeList == null)
                methodTypeList = MethodParameterType.getMethodSignatureTypeList(this);
            return methodTypeList;
        }
        return null;
    }

    /**
     * Get token range and element map
     */
    public Map<TokenRange, TokenElement> getRangeElementMap() {
        if (rangeElementMap == null && tokenElements != null){
            rangeElementMap = new HashMap<>();
            for (TokenElement element: tokenElements){
                rangeElementMap.put(element.getTokenRange(), element);
            }
        }
        return rangeElementMap;
    }

    /**
     * Get ngrams of names and literals
     */
    public Set<String> getNgramsOfNameAndLiteral(int ngramSize, NGramCalculator calculator) {
        if (this.ngramSize == ngramSize && ngrams != null)
            return ngrams;
        ngrams = new HashSet<>();
        this.ngramSize = ngramSize;
        Set<String> nameAndLiterals = getTokenNumMap(true).keySet();
        for (String string: nameAndLiterals) {
            ngrams.addAll(calculator.calculateNGrams(string, ngramSize, true));
        }
        return ngrams;
    }

    /**
     * Get all descendant statements except blocks.
     */
    public Set<ProgramElement> getAllNonBlockDescendantStmts() {
        if (nonBlockDescendants != null)
            return nonBlockDescendants;
        nonBlockDescendants = new HashSet<>();
        List<ProgramElement> stmts = ElementTreeUtils.getAllStmtsPreOrder(this);
        for (ProgramElement stmtEle: stmts) {
            if (stmtEle == this || typeChecker.isBlock(stmtEle.getITreeNode()))
                continue;
            nonBlockDescendants.add(stmtEle);
        }
        return nonBlockDescendants;
    }

    /**
     * Get the token representing the name of the statement
     */
    public TokenElement getNameToken() {
        if (getName() == null)
            return null;
        if (nameToken != null)
            return nameToken;
        String name = getName();
        for (TokenElement tokenEle: getTokenElements()) {
            if (tokenEle.getStringValue().equals(name)) {
                nameToken = tokenEle;
                break;
            }
        }
        return nameToken;
    }

    /**
     * Get method of the element.
     */
    public ProgramElement getMethodOfElement() {
        if (methodElement != null)
            return methodElement;

        ProgramElement tmp = this;
        while (tmp != null) {
            if (tmp instanceof StmtElement) {
                if (((StmtElement) tmp).isMethodDec()) {
                    methodElement = tmp;
                    return methodElement;
                }
            }
            tmp = tmp.getParentElement();
        }
        return null;
    }

    public void calTokensOfInnerStmtElements() {
//        for (TokenElement tokenEle: getTokenElements())
//            System.out.println("The token Ele is " + tokenEle);
        for (TokenElement tokenEle: getTokenElements()) {
//            System.out.println("The token Ele is " + tokenEle);
            tokenEle.createInnerStmtElementsWithToken(treeInnerStmtEleMap);
        }
    }

    public Set<InnerStmtElement> getAllInnerStmtElements() {
        return new HashSet<>(treeInnerStmtEleMap.values());
    }
}
