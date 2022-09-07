package cs.sysu.algorithm.element;

import com.github.gumtreediff.tree.ITree;
import cs.sysu.algorithm.languageutils.typechecker.JavaNodeTypeChecker;
import cs.sysu.algorithm.languageutils.typechecker.StaticNodeTypeChecker;

import java.util.List;

/**
 * We transform AST nodes to program elements.
 * For statement, token or inner-stmt element, we process them as program elements.
 *
 * In essence, AST nodes represent program elements.
 * When mapping AST nodes, if we leverage more attributes of program elements,
 * the generated mappings are more likely to be accurate.
 */
public interface ProgramElement {

    JavaNodeTypeChecker typeChecker = StaticNodeTypeChecker.getConfigNodeTypeChecker();

    /**
     * Set the root element.
     */
    void setRoot(boolean root);

    /**
     * Set if the element is from the source file.
     * If src is true, element is from the source file.
     * Otherwise, the element is from the target files.
     */
    void setFromSrc(boolean src);

    /**
     * Set the corresponding ITree object for the element.
     */
    void setITreeNode(ITree node);

    /**
     * Set the parent element.
     * For a statement, its parent element is the nearest ancestor statement or root element.
     * For a token, its parent element is the statement containing the token.
     */
    void setParentElement(ProgramElement parentElement);

    /**
     * Set the statement element for a given inner-stmt element or token element.
     */
    void setStmtElement(StmtElement stmtElement);

    /**
     * Set name of the element
     * Currently, we support several types of statements for calculating their names.
     */
    void setName(String name);

    /**
     * Set string value of the element
     */
    void setStringValue(String value);

    /**
     * Set start line of the element.
     */
    void setStartLine(int line);

    /**
     * For statement, the idx is the index in nearestDescendantStmts of its parent element.
     * For token, the idx is the index in tokenElements of its parent element.
     */
    void setChildIdx(int idx);

    /**
     * Get relevant ITree node of the element.
     */
    ITree getITreeNode();

    /**
     * Get ITree node type of the element.
     */
    default String getNodeType() {
        return getITreeNode().getType().name;
    }

    /**
     * Get parent element
     * For a token element, this method gets the statement of the token
     * For a inner-stmt element, this method gets the statement of the token
     */
    ProgramElement getParentElement();


    ProgramElement getStmtElement();

    /**
     * Get children statements of a statement
     */
    List<StmtElement> getNearestDescendantStmts();

    /**
     * Get token elements of a statement or inner-stmt element
     */
    List<TokenElement> getTokenElements();

    /**
     * Get inner-stmt elements of a statement or inner-stmt element
     */
    List<InnerStmtElement> getInnerStmtElements();

    /**
     * Get name of a statement
     */
    String getName();

    /**
     * Get the string value of an element,
     * for an element, its string value is defined as consecutive string of the tokens
     * in an element.
     */
    String getStringValue();

    /**
     * Whether has the same value compared to the element
     */
    boolean equalValue(ProgramElement element);

    /**
     * Whether the element is root element
     */
    boolean isRoot();

    /**
     * Whether the element is from the source file
     */
    boolean isFromSrc();

    /**
     * Whether the element is a statement element.
     */
    boolean isStmt();

    /**
     * Whether the element is a token element.
     */
    boolean isToken();

    /**
     * Whether the element is a inner-stmt element.
     */
    boolean isInnerStmtElement();

    /**
     * Whether the element is a method, field or type declaration
     */
    boolean isDeclaration();

    int getChildIdx();

    /**
     * Get ancestor statement elements.
     */
    List<ProgramElement> getAncestors();

    /**
     * Get line of the element
     * @return line number
     */
    int getStartLine();

    /**
     * An element is equal to an obj, if the obj is an element
     * that represents the same ITree node.
     *
     * @param obj the object
     * @return true if the object satisfys the condition.
     */
    boolean equals(Object obj);

    int hashCode();

    ProgramElement getLeftSibling();

    ProgramElement getRightSibling();

    void addNearestDescendantStmts(StmtElement element);

    void addTokenElement(TokenElement element);

    void addInnerStmtElement(InnerStmtElement element);

    default ProgramElementType getElementType() {
        return ProgramElementType.getElementType(this);
    }
}
