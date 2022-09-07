package cs.sysu.algorithm.element;

import com.github.gumtreediff.tree.ITree;
import cs.sysu.algorithm.languageutils.typechecker.JavaNodeTypeChecker;
import cs.sysu.algorithm.languageutils.typechecker.StaticNodeTypeChecker;
import cs.sysu.algorithm.ttmap.TokenRange;
import cs.sysu.algorithm.ttmap.TreeTokensMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Elements to calculate the name
 * 1. All declarations
 * 2. Variable Declaration
 * 3. Method Invocation
 * 4. Assignment
 */
public class ElementNameCalculator {
    private ProgramElement element;
    private List<String> names;
    private TreeTokensMap ttMap;

    private static final JavaNodeTypeChecker typeChecker = StaticNodeTypeChecker.getConfigNodeTypeChecker();

    public ElementNameCalculator(ProgramElement element, TreeTokensMap ttMap){
        this.element = element;
        this.names = new ArrayList<>();
        this.ttMap = ttMap;
        String typeName = element.getNodeType();

        if (isTypeOrMethodDeclaration(typeName)) {
            String decName = getNameForTypeOrMethodDeclaration();
            if (decName != null)
                this.names.add(decName);
        } else if (isVarOrFieldDeclaration(typeName)) {
            this.names = getNamesForVariableOrFieldDeclaration();
        } else if (typeName.equals(typeChecker.getTypeNameForExprStmt())) {
            String stmtName = getExpressionStatementName();
            if (stmtName != null)
                this.names.add(stmtName);
        } else if (typeName.equals(typeChecker.getTypeNameForImportDec())){
            String name = getNameForImportDeclaration();
            if (name != null)
                this.names.add(name);
        }
    }

    private boolean isTypeOrMethodDeclaration(String typeName){
        if (typeName.equals(typeChecker.getTypeNameForTypeDec()))
            return true;
        if (typeName.equals(typeChecker.getTypeNameForEnumDec()))
            return true;
        if (typeName.equals(typeChecker.getTypeNameForAnnotationTypeDec()))
            return true;
        if (typeName.equals(typeChecker.getTypeNameForMethodDec()))
            return true;
        return false;
    }

    private boolean isVarOrFieldDeclaration(String typeName) {
        return typeName.equals(typeChecker.getTypeNameForVarDec()) ||
                typeName.equals(typeChecker.getTypeNameForFieldDec());
    }

    public String getName() {
        if (names.size() == 1)
            return names.get(0);
        return null;
    }

    private String getNameForTypeOrMethodDeclaration(){
        ITree node = element.getITreeNode();
        if (!(node.getLabel().equals(ITree.NO_LABEL)))
            return node.getLabel();
        for (ITree c: node.getChildren()){
            if (typeChecker.isSimpleName(c))
                return c.getLabel();
        }
        return null;
    }

    // Currently, we consider the first variable or field name
    private List<String> getNamesForVariableOrFieldDeclaration(){
        List<String> names = new ArrayList<>();
        ITree node = element.getITreeNode();
        for (ITree c: node.getChildren()){
            if (typeChecker.isVariableDeclarationFragment(c)){
                String variableName = getVariableName(c);
                if (variableName != null)
                    names.add(variableName);
            }
        }
        return names;
    }

    private String getVariableName(ITree variableDecFragment){
        if (!variableDecFragment.getLabel().equals(ITree.NO_LABEL))
            return variableDecFragment.getLabel();
        for (ITree c: variableDecFragment.getChildren()){
            if (typeChecker.isSimpleName(c))
                return c.getLabel();
        }
        return null;
    }

    private String getExpressionStatementName(){
        ITree node = element.getITreeNode();
        if (node.getChildren().size() == 1){
            ITree c = node.getChild(0);
            // Avoid considering the name of method invocation
             if (typeChecker.isMethodInvocation(c))
                 return getNameForMethodInvocation(c);
            if (typeChecker.isAssignment(c))
                return getNameForAssignment(c);
        }
        return null;
    }

    private String getNameForAssignment(ITree assignment) {
        if (!assignment.getLabel().equals(ITree.NO_LABEL))
            return assignment.getLabel();
        for (ITree c: assignment.getChildren()) {
            if (typeChecker.isSimpleName(c))
                return c.getLabel();
            if (typeChecker.isFieldAccess(c))
                return getNameOfFieldAccess(c);
        }
        return null;
    }

    private String getNameOfFieldAccess(ITree access) {
        int size = access.getChildren().size();
        return access.getChildren().get(size - 1).getLabel();
    }

    private String getNameForMethodInvocation(ITree methodInvocation){
        if (!methodInvocation.getLabel().equals(ITree.NO_LABEL))
            return methodInvocation.getLabel();
        for (ITree c: methodInvocation.getChildren()){
            if (typeChecker.isSimpleName(c))
                return c.getLabel();
        }
        return null;
    }

    private String getNameForImportDeclaration() {
        ITree importDec = element.getITreeNode();
        if (importDec.isLeaf())
            return importDec.getLabel();
        List<TokenRange> tokens = ttMap.getTokenRangesOfNode(importDec);
        TokenRange nameRange = tokens.get(tokens.size() - 1);
        String name = ttMap.getTokenByRange(nameRange);
        if (!name.equals("*"))
            return name;
        return null;
    }
}
