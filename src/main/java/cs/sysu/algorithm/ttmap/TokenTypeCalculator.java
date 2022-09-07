package cs.sysu.algorithm.ttmap;

import com.github.gumtreediff.tree.ITree;
import cs.sysu.algorithm.languageutils.typechecker.JavaNodeTypeChecker;
import cs.sysu.algorithm.languageutils.typechecker.StaticNodeTypeChecker;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Type calculator for code tokens.
 *
 * Calculate the type of each token by analyzing the
 * type of parent node of the corresponding name node.
 */
public class TokenTypeCalculator {

    public static final String TYPE_NAME = "TYPE_NAME";
    public static final String VAR_NAME = "VAR_NAME";
    public static final String METHOD_NAME = "METHOD_NAME";
    public static final String VAR_DEC_NAME = "VAR_DEC_NAME";
    public static final String TYPE_DEC_NAME = "TYPE_DEC_NAME";
    public static final String METHOD_DEC_NAME = "METHOD_DEC_NAME";
    public static final String FIELD_DEC_NAME = "FIELD_DEC_NAME";

    public static final String NULL_TOKEN = "NULL_TOKEN";

    // if a token is in an annotation, this token is an annotation name
    public static final String ANNOTATION_NAME = "ANNOTATION_NAME";

    // If a token is in a qualified name, we first set it as path.
    // Then, we check if the name is variable or type
    public static final String QUALIFIED_PATH_NAME = "PATH";

    private ITree nameNode;
    private ITree typeNode;
    private String typeNodeName;
    private TokenRange token;
    private TreeTokensMap ttMap;

    private ITree bigNameNode;

    private static final JavaNodeTypeChecker typeChecker = StaticNodeTypeChecker.getConfigNodeTypeChecker();

    public TokenTypeCalculator(TokenRange token, TreeTokensMap ttMap){
        this.token = token;
        this.ttMap = ttMap;
        this.nameNode = ttMap.getTokenRangeTreeMap().get(token);
        ITree t = nameNode;

        while (typeChecker.isName(t)) {
            bigNameNode = t;
            t = t.getParent();
        }

        typeNode = t;
        typeNodeName = typeNode.getType().name;
    }

    public String getTypeOfNodeFromNameTypeMap(Map<String, Set<String>> nameTypeMap){
        String tokenStr = ttMap.getTokenByRange(token);
        Set<String> typeSet = nameTypeMap.get(tokenStr);

        if (isInImportDecOrPackageDec())
            return QUALIFIED_PATH_NAME;
        if (typeSet.contains(TYPE_DEC_NAME))
            return TYPE_NAME;
        if (typeSet.contains(TYPE_NAME))
            return TYPE_NAME;
        if (typeSet.contains(VAR_NAME) || typeSet.contains(VAR_DEC_NAME))
            return VAR_NAME;
        if (tokenStr.charAt(0) == tokenStr.toUpperCase().charAt(0))
            return TYPE_NAME;
        else
            return VAR_NAME;
    }

    public String getTypeOfNode(){
        String tokenStr = ttMap.getTokenByRange(token);
        if (tokenStr.equals("void"))
            return TYPE_NAME;
        if (typeChecker.isType(nameNode))
            return TYPE_NAME;
        if (!typeChecker.isName(nameNode))
            return nameNode.getType().name;

        if (bigNameNode != null && typeChecker.isQualifiedName(bigNameNode)) {
            List<TokenRange> ranges = ttMap.getTokenRangesOfNode(bigNameNode);
            if (!ranges.get(ranges.size() - 1).equals(token))
                return QUALIFIED_PATH_NAME;
        }

        if (isAnnotation())
            return ANNOTATION_NAME;
        if (isVar())
            return VAR_NAME;
        if (isVarDec())
            return VAR_DEC_NAME;
        if (isType())
            return TYPE_NAME;
        if (isMethod())
            return METHOD_NAME;
        if (isMethodDec())
            return METHOD_DEC_NAME;
        if (isTypeDec())
            return TYPE_DEC_NAME;
        if (isFieldDec())
            return FIELD_DEC_NAME;

        if (typeChecker.isSuperConstructorInvocation(typeNode)){
            return getTokenTypeFromSuperConstructorInvocation(typeNode);
        }

        if (typeChecker.isSuperMethodInvocation(typeNode)){
            return getTokenTypeFromSuperMethodInvocation(typeNode);
        }

        return null;
    }

    private boolean isInImportDecOrPackageDec() {
        ITree node = typeNode;
        while (node != null){
            node = node.getParent();
            if (typeChecker.isPackageDec(node) || typeChecker.isImportDec(node))
                return true;
        }
        return false;
    }

    private boolean isAnnotation(){
        if (typeChecker.isAnnotation(typeNode))
            return true;
        return false;
    }

    private boolean isType(){
        if (typeChecker.isType(typeNode))
            return true;
        if (typeChecker.isThisExpression(typeNode))
            return true;
        if (typeChecker.isArrayCreation(typeNode))
            return true;
        if (typeChecker.isImportDec(typeNode)) {
            String importStr = ttMap.getNodeContent(typeNode);
            importStr = importStr.trim();
            importStr = importStr.substring(0, importStr.length() - 1);
            importStr = importStr.trim();
            if (importStr.endsWith("*"))
                return false;
            return true;
        }
        return false;
    }

    private boolean isVarDec() {
        // Fix issue #14,
        // If the type node is VariableDeclarationFragment,
        // check whether the name is a variable declaration name.
        if (StaticNodeTypeChecker.getConfigNodeTypeChecker().isVariableDeclarationFragment(typeNode)) {
            if (isVariableDecName(typeNode))
                return true;
        }
        if (StaticNodeTypeChecker.getConfigNodeTypeChecker().isSingleVariableDeclaration(typeNode))
            return true;
        return false;
    }

    private boolean isVar(){
        if (typeChecker.isMethodInvocationArguments(typeNode))
            return true;
        if (typeChecker.isFieldAccess(typeNode))
            return true;
        if (typeChecker.isSuperFieldAccess(typeNode))
            return !isTypeOrVarForSuperFieldAccess(typeNode);
        if (typeChecker.isParenthesizedExpression(typeNode))
            return true;
        if (typeChecker.isPostfixExpression(typeNode))
            return true;
        if (typeChecker.isConditionalExpression(typeNode))
            return true;
        if (typeChecker.isPrefixExpression(typeNode))
            return true;
        if (typeChecker.isInfixExpression(typeNode))
            return true;
        if (typeChecker.isInstanceOfExpression(typeNode))
            return true;
        if (typeChecker.isCastExpression(typeNode))
            return true;
        if (typeChecker.isAssignment(typeNode))
            return true;
        if (typeChecker.isArrayAccess(typeNode))
            return true;
        if (typeChecker.isEnhancedForStatement(typeNode))
            return true;
        if (typeChecker.isConstructorInvocation(typeNode))
            return true;
        if (typeChecker.isReturnStatement(typeNode))
            return true;
        if (typeChecker.isClassInstanceCreation(typeNode))
            return true;
        // Fix issue #14,
        // If the type node is VariableDeclarationFragment,
        // check whether the name is a variable declaration name.
        if (typeChecker.isVariableDeclarationFragment(typeNode)) {
            if (!isVariableDecName(typeNode))
                return true;
        }
        return false;
    }

    private boolean isMethod(){
        return typeChecker.isMethodInvocation(typeNode) || typeChecker.isSuperMethodInvocation(typeNode);
    }

    private boolean isTypeDec(){
        return typeChecker.isTypeDec(typeNode) || typeChecker.isEnumDec(typeNode) ||
                typeChecker.isAnnotationTypeDec(typeNode);
    }

    private boolean isMethodDec(){
        return typeChecker.isMethodDec(typeNode);
    }

    private boolean isFieldDec(){
        return typeChecker.isFieldDec(typeNode);
    }

    private boolean isTypeOrVarForSuperFieldAccess(ITree superFieldAccessNode){
        List<ITree> children = superFieldAccessNode.getChildren();
        if (children.size() == 1)
            return false;
        if (children.size() == 2){
            if (children.get(0) == nameNode)
                return true;
            if (children.get(1) == nameNode)
                return false;
        }
        throw new RuntimeException("Cannot handle more children for superFieldAccess");
    }

    private String getTokenTypeFromSuperMethodInvocation(ITree superMethodInvocationNode){
        List<TokenRange> tokens = ttMap.getTokenRangesOfNode(superMethodInvocationNode);
        List<String> tokenStrings = ttMap.getTokensByRanges(tokens);
        int index = tokenStrings.indexOf("super");
        TokenRange superRange = tokens.get(index);
        if (token.second < superRange.first)
            return TYPE_NAME;
        else {
            List<ITree> children = superMethodInvocationNode.getChildren();
            for (ITree t: children){
                if (t.getPos() > superRange.second){
                    if (typeChecker.isSimpleName(t) && nameNode == t)
                        return METHOD_NAME;
                }
            }
        }
        return VAR_NAME;
    }

    private String getTokenTypeFromSuperConstructorInvocation(ITree superConsInvNode){
        List<TokenRange> tokens = ttMap.getTokenRangesOfNode(superConsInvNode);
        List<String> tokenStrings = ttMap.getTokensByRanges(tokens);
        int index = tokenStrings.indexOf("super");
        TokenRange superRange = tokens.get(index);
        if (token.second < superRange.first)
            return TYPE_NAME;
        else
            return VAR_NAME;
    }

    private boolean isVariableDecName(ITree variableDecNode) {
        for (ITree c: variableDecNode.getChildren()) {
            if (typeChecker.isSimpleName(c)) {
                if (nameNode == c)
                    return true;
                break;
            }
        }
        return false;
    }
}
