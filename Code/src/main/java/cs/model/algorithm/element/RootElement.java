package cs.model.algorithm.element;

/**
 * The root element in an element tree.
 */
public class RootElement extends AbstractElement implements ProgramElement {

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    public boolean equalValue(ProgramElement element) {
        return false;
    }

    @Override
    public boolean isStmt() {
        return typeChecker.isStatementNode(this.node);
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RootElement))
            return false;
        ProgramElement element = (ProgramElement) obj;
        if (this.node == element.getITreeNode())
            return true;
        if (this.isFromSrc() != element.isFromSrc())
            return false;
        if (this.node.getType() != element.getITreeNode().getType())
            return false;
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 17;
        hash = hash * 31 + Boolean.hashCode(isFromSrc);
        return hash;
    }

    @Override
    public ProgramElement getLeftSibling() {
        return null;
    }

    @Override
    public ProgramElement getRightSibling() {
        return null;
    }
}
