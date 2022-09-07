package cs.sysu.algorithm.element;

import com.github.gumtreediff.tree.ITree;

import java.util.*;

/**
 * Base class of program element.
 */
public abstract class AbstractElement implements ProgramElement {

    // if the element is root
    protected boolean isRoot = false;

    // source file or target file
    protected boolean isFromSrc = true;

    // tree node of the element
    protected ITree node = null;

    // name of a declaration, variable, method invocation or other elements
    protected String name = null;

    // nearest descendant statements
    protected List<StmtElement> nearestDescendantStmts = null;

    // token elements of a statement or inner-stmt element
    protected List<TokenElement> tokenElements = null;

    // inner-stmt elements in a statement or inner-stmt element
    protected List<InnerStmtElement> innerStmtElements = null;

    // the statement of a token or inner-stmt element
    protected StmtElement stmtElement = null;

    // string value of a statement, token or inner-stmt element
    protected String value = "";

    // Nearest ancestor element
    protected ProgramElement parentElement;

    // type list of a method, used as cache
    protected List<MethodParameterType> methodTypeList = null;

    // method of the element
    protected ProgramElement methodElement = null;

    // For statement, the childIdx is the index in nearestDescendantStmts
    // For token, the childIdx is the index in tokenElements
    protected int childIdx;

    // line of the element
    protected int startLine = -1;

    // ancestor elements of statements, tokens and inner-stmt elements
    protected List<ProgramElement> ancestors = null;

    @Override
    public void setRoot(boolean root) {
        isRoot = root;
    }

    @Override
    public void setFromSrc(boolean fromSrc) {
        isFromSrc = fromSrc;
    }

    @Override
    public void setITreeNode(ITree node) {
        this.node = node;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setParentElement(ProgramElement parentElement) {
        this.parentElement = parentElement;
    }

    @Override
    public void setStmtElement(StmtElement stmtElement) {
        this.stmtElement = stmtElement;
    }

    @Override
    public void setStringValue(String value) {
        this.value = value.trim();
    }

    @Override
    public ITree getITreeNode() {
        return node;
    }

    @Override
    public boolean isFromSrc() {
        return isFromSrc;
    }

    @Override
    public ProgramElement getParentElement() {
        return parentElement;
    }

    @Override
    public StmtElement getStmtElement() {
        return stmtElement;
    }

    @Override
    public List<StmtElement> getNearestDescendantStmts() {
        return nearestDescendantStmts;
    }

    @Override
    public List<TokenElement> getTokenElements() {
        return tokenElements;
    }

    @Override
    public List<InnerStmtElement> getInnerStmtElements() {
        return innerStmtElements;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isRoot() {
        return isRoot;
    }

    @Override
    public String getStringValue() {
        return value;
    }

    @Override
    public void setChildIdx(int childIdx) {
        this.childIdx = childIdx;
    }

    @Override
    public int getChildIdx() {
        return childIdx;
    }

    @Override
    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    @Override
    public int getStartLine() {
        return startLine;
    }

    @Override
    public List<ProgramElement> getAncestors(){
        if (this.ancestors != null)
            return ancestors;
        if (this.isRoot()){
            this.ancestors = new ArrayList<>();
            return ancestors;
        }
        ProgramElement parent = getParentElement();
        List<ProgramElement> parentAncestors = parent.getAncestors();
        this.ancestors = new ArrayList<>(parentAncestors);
        this.ancestors.add(parent);
        return this.ancestors;
    }

    void initNearestDescendantStmts() {
        nearestDescendantStmts = new ArrayList<>();
    }

    void initTokenElements(){
        tokenElements = new ArrayList<>();
    }

    void initInnerStmtElements() {
        innerStmtElements = new ArrayList<>();
    }

    @Override
    public void addNearestDescendantStmts(StmtElement element) {
        nearestDescendantStmts.add(element);
    }

    @Override
    public void addTokenElement(TokenElement element) {
        tokenElements.add(element);
    }

    @Override
    public void addInnerStmtElement(InnerStmtElement element) {
        innerStmtElements.add(element);
    }
}
