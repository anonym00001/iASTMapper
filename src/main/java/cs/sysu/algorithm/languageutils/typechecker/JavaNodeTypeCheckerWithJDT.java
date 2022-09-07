package cs.sysu.algorithm.languageutils.typechecker;

import com.github.gumtreediff.tree.ITree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ITree Type to String.
 *
 * Check special types for each given ITree node.
 * NOTE: this class can only be used for the ITree nodes generated using JDT.
 *
 * TODO: In the future, we must make it generalize for other parser and languages.
 */
public class JavaNodeTypeCheckerWithJDT extends JavaNodeTypeChecker {

    public static final String TYPE_DEC = "TypeDeclaration";
    public static final String METHOD_DEC = "MethodDeclaration";
    public static final String ANNOTATION_TYPE_DEC = "AnnotationTypeDeclaration";
    public static final String ENUM_DEC = "EnumDeclaration";
    public static final String VAR_DEC = "VariableDeclarationStatement";
    public static final String FIELD_DEC = "FieldDeclaration";
    public static final String EXPR_STMT = "ExpressionStatement";
    public static final String IMPORT_DEC = "ImportDeclaration";

    private static final List<String> specialStmts = specialStmts();

    private static List<String> specialStmts(){
        String[] specialStmts = {
                "PackageDeclaration",
                "TypeDeclaration",
                "AnnotationTypeDeclaration",
                "AnnotationTypeMemberDeclaration",
                "EnumDeclaration",
                "EnumConstantDeclaration",
                "ImportDeclaration",
                "MethodDeclaration",
                "SwitchCase",
                "SuperConstructorInvocation",
                "ConstructorInvocation",
                "FieldDeclaration",
                "CatchClause",
                "Block",
                "Initializer"
        };
        return new ArrayList<>(Arrays.asList(specialStmts));
    }

    @Override
    public boolean isStatementNode(ITree t){
        String type = getITreeNodeTypeName(t);
        if (type.endsWith("Statement"))
            return true;
        return specialStmts.contains(type);
    }

    @Override
    public boolean isJavaDoc(ITree node){
        return getITreeNodeTypeName(node).equals("Javadoc");
    }

    @Override
    public boolean isTypeDec(ITree node){
        return getITreeNodeTypeName(node).equals("TypeDeclaration");
    }

    @Override
    public boolean isMethodDec(ITree node){
        return getITreeNodeTypeName(node).equals("MethodDeclaration");
    }

    @Override
    public boolean isSimpleName(ITree node){
        return getITreeNodeTypeName(node).equals("SimpleName");
    }

    @Override
    public boolean isBlock(ITree node){
        return getITreeNodeTypeName(node).equals("Block");
    }

    @Override
    public boolean isCompilationUnit(ITree node){
        return getITreeNodeTypeName(node).equals("CompilationUnit");
    }

    @Override
    public boolean isImportDec(ITree node){
        return getITreeNodeTypeName(node).equals("ImportDeclaration");
    }

    @Override
    public boolean isPackageDec(ITree node){
        return getITreeNodeTypeName(node).equals("PackageDeclaration");
    }

    @Override
    public boolean isFieldDec(ITree node){
        return getITreeNodeTypeName(node).equals("FieldDeclaration");
    }

    @Override
    public boolean isAnonymousClassDec(ITree node){
        return getITreeNodeTypeName(node).equals("AnonymousClassDeclaration");
    }

    @Override
    public boolean isIfStatement(ITree node){
        return getITreeNodeTypeName(node).equals("IfStatement");
    }

    @Override
    public boolean isEnumDec(ITree node){
        return getITreeNodeTypeName(node).equals("EnumDeclaration");
    }

    @Override
    public boolean isAnnotationTypeDec(ITree node){
        return getITreeNodeTypeName(node).equals("AnnotationTypeDeclaration");
    }

    @Override
    public boolean isInfixExpression(ITree node){
        return getITreeNodeTypeName(node).equals("InfixExpression");
    }

    @Override
    public boolean isClassInstanceCreation(ITree node){
        return getITreeNodeTypeName(node).equals("ClassInstanceCreation");
    }

    @Override
    public boolean isStringLiteral(ITree node){
        return getITreeNodeTypeName(node).equals("StringLiteral");
    }

    @Override
    public boolean isCharacterLiteral(ITree node){
        return getITreeNodeTypeName(node).equals("CharacterLiteral");
    }

    @Override
    public boolean isNumberLiteral(ITree node) {
        return getITreeNodeTypeName(node).equals("NumberLiteral");
    }

    @Override
    public boolean isInfixExpressionOperator(ITree node){
        return getITreeNodeTypeName(node).equals("INFIX_EXPRESSION_OPERATOR");
    }

    @Override
    public boolean isModifier(ITree node){
        return getITreeNodeTypeName(node).equals("Modifier");
    }

    @Override
    public boolean isFieldAccess(ITree node){
        return getITreeNodeTypeName(node).equals("FieldAccess");
    }

    @Override
    public boolean isDimension(ITree node) {
        return getITreeNodeTypeName(node).equals("Dimension");
    }

    @Override
    public boolean isStatement(String type){
        if (type.endsWith("Statement"))
            return true;
        return specialStmts.contains(type);
    }

    @Override
    public boolean isType(ITree t) {
        return getITreeNodeTypeName(t).endsWith("Type");
    }

    @Override
    public boolean isSingleVariableDeclaration(ITree t) {
        return getITreeNodeTypeName(t).equals("SingleVariableDeclaration");
    }

    @Override
    public boolean isVariableDeclarationFragment(ITree t) {
        return getITreeNodeTypeName(t).equals("VariableDeclarationFragment");
    }

    @Override
    public boolean isMethodInvocation(ITree t) {
        return getITreeNodeTypeName(t).equals("MethodInvocation");
    }

    @Override
    public boolean isAssignment(ITree t) {
        return getITreeNodeTypeName(t).equals("Assignment");
    }

    @Override
    public boolean isReturnStatement(ITree t) {
        return getITreeNodeTypeName(t).equals("ReturnStatement");
    }

    @Override
    public boolean isThrowStatement(ITree t) {
        return getITreeNodeTypeName(t).equals("ThrowStatement");
    }

    @Override
    public boolean isTryStatement(ITree t) {
        return getITreeNodeTypeName(t).equals("TryStatement");
    }

    @Override
    public boolean isQualifiedName(ITree t) {
        return getITreeNodeTypeName(t).equals("QualifiedName");
    }

    @Override
    public boolean isName(ITree t) {
        return getITreeNodeTypeName(t).endsWith("Name");
    }

    @Override
    public boolean isSuperConstructorInvocation(ITree t) {
        return getITreeNodeTypeName(t).equals("SuperConstructorInvocation");
    }

    @Override
    public boolean isSuperMethodInvocation(ITree t) {
        return getITreeNodeTypeName(t).equals("SuperMethodInvocation");
    }

    @Override
    public boolean isAnnotation(ITree t) {
        return getITreeNodeTypeName(t).endsWith("Annotation");
    }

    @Override
    public boolean isThisExpression(ITree t) {
        return getITreeNodeTypeName(t).equals("ThisExpression");
    }

    @Override
    public boolean isArrayCreation(ITree t) {
        return getITreeNodeTypeName(t).equals("ArrayCreation");
    }

    @Override
    public boolean isMethodInvocationArguments(ITree t) {
        return getITreeNodeTypeName(t).equals("METHOD_INVOCATION_ARGUMENTS");
    }

    @Override
    public boolean isSuperFieldAccess(ITree t) {
        return getITreeNodeTypeName(t).equals("SuperFieldAccess");
    }

    @Override
    public boolean isParenthesizedExpression(ITree t) {
        return getITreeNodeTypeName(t).equals("ParenthesizedExpression");
    }

    @Override
    public boolean isPostfixExpression(ITree t) {
        return getITreeNodeTypeName(t).equals("PostfixExpression");
    }

    @Override
    public boolean isConditionalExpression(ITree t) {
        return getITreeNodeTypeName(t).equals("ConditionalExpression");
    }

    @Override
    public boolean isPrefixExpression(ITree t) {
        return getITreeNodeTypeName(t).equals("PrefixExpression");
    }

    @Override
    public boolean isInstanceOfExpression(ITree t) {
        return getITreeNodeTypeName(t).equals("InstanceofExpression");
    }

    @Override
    public boolean isCastExpression(ITree t) {
        return getITreeNodeTypeName(t).equals("CastExpression");
    }

    @Override
    public boolean isArrayAccess(ITree t) {
        return getITreeNodeTypeName(t).equals("ArrayAccess");
    }

    @Override
    public boolean isEnhancedForStatement(ITree t) {
        return getITreeNodeTypeName(t).equals("EnhancedForStatement");
    }

    @Override
    public boolean isConstructorInvocation(ITree t) {
        return getITreeNodeTypeName(t).equals("ConstructorInvocation");
    }

    @Override
    public boolean isArrayInitializer(ITree t) {
        return t.getType().name.equals("ArrayInitializer");
    }

    @Override
    public boolean isWildcardType(ITree t) {
        return t.getType().name.equals("WildcardType");
    }

    @Override
    public boolean isExprStmt(ITree node) {
        return getITreeNodeTypeName(node).equals("ExpressionStatement");
    }

    @Override
    public String getTypeNameForTypeDec() {
        return TYPE_DEC;
    }

    @Override
    public String getTypeNameForMethodDec() {
        return METHOD_DEC;
    }

    @Override
    public String getTypeNameForAnnotationTypeDec() {
        return ANNOTATION_TYPE_DEC;
    }

    @Override
    public String getTypeNameForEnumDec() {
        return ENUM_DEC;
    }

    @Override
    public String getTypeNameForVarDec() {
        return VAR_DEC;
    }

    @Override
    public String getTypeNameForExprStmt() {
        return EXPR_STMT;
    }

    @Override
    public String getTypeNameForImportDec() {
        return IMPORT_DEC;
    }

    @Override
    public String getTypeNameForFieldDec() {
        return FIELD_DEC;
    }

}
