package cs.sysu.algorithm.languageutils.typechecker;

import com.github.gumtreediff.tree.ITree;

/**
 * Interface of node type checker
 *
 * Check if an ITree node is of a specific type.
 * TODO: In the future, we make node type checker to be used for various languages.
 */
public abstract class JavaNodeTypeChecker {

    public abstract boolean isStatementNode(ITree t);

    public abstract boolean isJavaDoc(ITree t);

    public abstract boolean isTypeDec(ITree t);

    public abstract boolean isMethodDec(ITree t);

    public abstract boolean isSimpleName(ITree t);

    public abstract boolean isBlock(ITree t);

    public abstract boolean isCompilationUnit(ITree t);

    public abstract boolean isImportDec(ITree t);

    public abstract boolean isPackageDec(ITree t);

    public abstract boolean isFieldDec(ITree t);

    public abstract boolean isAnonymousClassDec(ITree t);

    public abstract boolean isIfStatement(ITree t);

    public abstract boolean isEnumDec(ITree t);

    public abstract boolean isAnnotationTypeDec(ITree t);

    public abstract boolean isInfixExpression(ITree t);

    public abstract boolean isClassInstanceCreation(ITree t);

    public abstract boolean isStringLiteral(ITree t);

    public abstract boolean isCharacterLiteral(ITree t);

    public abstract boolean isNumberLiteral(ITree t);

    public abstract boolean isInfixExpressionOperator(ITree t);

    public abstract boolean isModifier(ITree t);

    public abstract boolean isFieldAccess(ITree t);

    public abstract boolean isDimension(ITree t);

    public static String getITreeNodeTypeName(ITree node) {
        if (node == null || node.getType() == null || node.getType().name == null)
            return "";
        return node.getType().name;
    }

    public abstract boolean isStatement(String type);

    public abstract boolean isType(ITree t);

    public abstract boolean isSingleVariableDeclaration(ITree t);

    public abstract boolean isVariableDeclarationFragment(ITree t);

    public abstract boolean isMethodInvocation(ITree t);

    public abstract boolean isAssignment(ITree t);

    public abstract boolean isReturnStatement(ITree t);

    public abstract boolean isThrowStatement(ITree t);

    public abstract boolean isTryStatement(ITree t);

    public abstract boolean isQualifiedName(ITree t);

    public abstract boolean isName(ITree t);

    public abstract boolean isSuperConstructorInvocation(ITree t);

    public abstract boolean isSuperMethodInvocation(ITree t);

    public abstract boolean isAnnotation(ITree t);

    public abstract boolean isThisExpression(ITree t);

    public abstract boolean isArrayCreation(ITree t);

    public abstract boolean isMethodInvocationArguments(ITree t);

    public abstract boolean isSuperFieldAccess(ITree t);

    public abstract boolean isParenthesizedExpression(ITree t);

    public abstract boolean isPostfixExpression(ITree t);

    public abstract boolean isConditionalExpression(ITree t);

    public abstract boolean isPrefixExpression(ITree t);

    public abstract boolean isInstanceOfExpression(ITree t);

    public abstract boolean isCastExpression(ITree t);

    public abstract boolean isArrayAccess(ITree t);

    public abstract boolean isEnhancedForStatement(ITree t);

    public abstract boolean isConstructorInvocation(ITree t);

    public abstract boolean isArrayInitializer(ITree t);

    public abstract boolean isWildcardType(ITree t);

    public boolean isDeclaration(ITree t) {
        return isFieldDec(t) || isMethodDec(t) || isTypeDec(t) || isEnumDec(t) || isImportDec(t) || isPackageDec(t);
    }

    public boolean isLiteral(ITree t) {
        return t.getType().name.toLowerCase().endsWith("literal");
    }

    public boolean isDescendantOfSingleVariableDeclaration(ITree node) {
        ITree t = node;
        while (t != null) {
            if (isSingleVariableDeclaration(t))
                return true;
            t = t.getParent();
        }
        return false;
    }

    public abstract boolean isExprStmt(ITree node);

    public abstract String getTypeNameForTypeDec();

    public abstract String getTypeNameForMethodDec();

    public abstract String getTypeNameForAnnotationTypeDec();

    public abstract String getTypeNameForEnumDec();

    public abstract String getTypeNameForVarDec();

    public abstract String getTypeNameForExprStmt();

    public abstract String getTypeNameForImportDec();

    public abstract String getTypeNameForFieldDec();
}
