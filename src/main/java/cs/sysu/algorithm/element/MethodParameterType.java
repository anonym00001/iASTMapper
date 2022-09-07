package cs.sysu.algorithm.element;

import com.github.gumtreediff.tree.ITree;
import cs.sysu.algorithm.languageutils.typechecker.StaticNodeTypeChecker;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.ttmap.TokenTypeCalculator;

import java.util.*;

/**
 * A type that appears in a method signature.
 *
 * For a token, this class finds the type containing the token and appears in the signature.
 * For example, if we have a method: void foo(String[] stringList)
 * The element type of "String" is String[]
 */
public class MethodParameterType {
    private List<ProgramElement> tokens;   // tokens of the type
    private String nodeType;               // node type of the parameter type

    public MethodParameterType(ProgramElement tokenEle){
        tokens = new LinkedList<>();
        ITree node = tokenEle.getITreeNode();
        ITree lastNode = node;
        while (!ProgramElement.typeChecker.isStatementNode(node)) {
            if (!ProgramElement.typeChecker.isSimpleName(node) && !ProgramElement.typeChecker.isType(node))
                break;
            lastNode = node;
            node = node.getParent();
        }
        this.nodeType = lastNode.getType().name;
        tokens.add(tokenEle);
        int i = tokenEle.getChildIdx() + 1;
        List<TokenElement> tokenElements = tokenEle.getParentElement().getTokenElements();
        while (i < tokenElements.size()) {
            if (lastNode.getEndPos() > tokenElements.get(i).getTokenRange().second)
                tokens.add(tokenElements.get(i));
            i ++;
        }
    }

    public List<ProgramElement> getTokens() {
        return tokens;
    }

    public boolean equalsWithRename(MethodParameterType that, ElementMappings elementMappings) {
        if (!nodeType.equals(that.nodeType))
            return false;
//        System.out.println("Node type is " + nodeType + " " + that.nodeType);
        if (this.tokens.size() != that.tokens.size())
            return false;
        for (int i = 0; i < this.tokens.size(); i++) {
//            System.out.println(i + " The tolen is " + this.tokens.get(i));
//            TokenElement srcToken = (TokenElement) this.tokens.get(i);
//            String srcStr = srcToken.getStringValue();
//            System.out.println(" The tolen value is " + srcStr);
            if (this.tokens.get(i).equalValue(that.tokens.get(i)))
                continue;
            if (elementMappings.isTokenRenamed(this.tokens.get(i), that.tokens.get(i)))
                continue;
            return false;
        }
        return true;
    }

    public static boolean isIdenticalMethodParameterTypeList(List<MethodParameterType> typeList1,
                                                             List<MethodParameterType> typeList2,
                                                             ElementMappings elementMappings) {
        if (typeList1.size() != typeList2.size())
            return false;
        for (int i = 0; i < typeList1.size(); i++) {
            MethodParameterType type1 = typeList1.get(i);
            MethodParameterType type2 = typeList2.get(i);
//            System.out.println("Methode type "+ type1 +" " + type2);
            if (!type1.equalsWithRename(type2, elementMappings))
                return false;
        }
        return true;
    }

    /**
     * Get the type list of a method
     * @return a list of element types
     */
    public static List<MethodParameterType> getMethodSignatureTypeList(ProgramElement methodEle){
        List<TokenElement> tokens = methodEle.getTokenElements();
        Set<ProgramElement> addedTokens = new HashSet<>();
        List<MethodParameterType> typeList = new ArrayList<>();
        for (TokenElement tokenEle: tokens) {
            if (tokenEle.getStringValue().equals("throws"))
                break;
            if (addedTokens.contains(tokenEle))
                continue;
            if (tokenEle.getTokenType().equals(TokenTypeCalculator.TYPE_NAME)){
                MethodParameterType type = new MethodParameterType(tokenEle);
                addedTokens.addAll(type.getTokens());
                typeList.add(type);
            }
        }
        return typeList;
    }
}
