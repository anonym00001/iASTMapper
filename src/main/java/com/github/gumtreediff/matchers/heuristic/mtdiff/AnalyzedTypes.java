package com.github.gumtreediff.matchers.heuristic.mtdiff;

import com.github.gumtreediff.tree.Type;
import com.github.gumtreediff.tree.TypeSet;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalyzedTypes {
    private final static List<Integer> valCompareList = Arrays.asList(
            org.eclipse.jdt.core.dom.ASTNode.WILDCARD_TYPE,
            org.eclipse.jdt.core.dom.ASTNode.PREFIX_EXPRESSION,
            org.eclipse.jdt.core.dom.ASTNode.POSTFIX_EXPRESSION,
            org.eclipse.jdt.core.dom.ASTNode.INFIX_EXPRESSION,
            org.eclipse.jdt.core.dom.ASTNode.ASSIGNMENT);
    private final static List<Integer> intCompareList = Arrays.asList(34);
    private final static List<Integer> stringCompareList = Arrays.asList(
            org.eclipse.jdt.core.dom.ASTNode.SIMPLE_NAME,
            org.eclipse.jdt.core.dom.ASTNode.SIMPLE_TYPE,
            org.eclipse.jdt.core.dom.ASTNode.STRING_LITERAL,
            org.eclipse.jdt.core.dom.ASTNode.ARRAY_TYPE,
            org.eclipse.jdt.core.dom.ASTNode.PARAMETERIZED_TYPE,
            org.eclipse.jdt.core.dom.ASTNode.QUALIFIED_TYPE,
            org.eclipse.jdt.core.dom.ASTNode.QUALIFIED_NAME,
            org.eclipse.jdt.core.dom.ASTNode.NUMBER_LITERAL,
            org.eclipse.jdt.core.dom.ASTNode.CHARACTER_LITERAL,
            org.eclipse.jdt.core.dom.ASTNode.NUMBER_LITERAL);
    private final static List<Integer> boolCompareList = Arrays.asList(org.eclipse.jdt.core.dom.ASTNode.BOOLEAN_LITERAL);
    private static int identifierLabel = org.eclipse.jdt.core.dom.ASTNode.SIMPLE_NAME;
    private static int rootLabel = org.eclipse.jdt.core.dom.ASTNode.COMPILATION_UNIT;
    private static int classLabel = org.eclipse.jdt.core.dom.ASTNode.TYPE_DECLARATION;
    private static int basicTypeLabel = org.eclipse.jdt.core.dom.ASTNode.SIMPLE_TYPE;
    private static int modifierLabel = org.eclipse.jdt.core.dom.ASTNode.MODIFIER;

    private static List<Type> getLabelsForCompare(List<Integer> jdtTypeIdxes){
        List<Type> list = new ArrayList<>();
        for (int typeIdx: jdtTypeIdxes){
            list.add(TypeSet.type(ASTNode.nodeClassForType(typeIdx).getSimpleName()));
        }
        return list;
    }

    private static Type getLabelByIdx(int jdtTypeIdx){
        return TypeSet.type(ASTNode.nodeClassForType(jdtTypeIdx).getSimpleName());
    }

    public static List<Type> getLabelsForValueCompare(){
        return getLabelsForCompare(valCompareList);
    }

    public static List<Type> getLabelsForIntCompare(){
        return getLabelsForCompare(intCompareList);
    }

    public static List<Type> getLabelsForStringCompare(){
        return getLabelsForCompare(stringCompareList);
    }

    public static List<Type> getLabelsForBoolCompare(){
        return getLabelsForCompare(boolCompareList);
    }

    public static Type getIdentifierLabel(){
        return getLabelByIdx(identifierLabel);
    }

    public static Type getRootLabel(){
        return getLabelByIdx(rootLabel);
    }

    public static Type getClassLabel(){
        return getLabelByIdx(classLabel);
    }

    public static Type getBasicTypeLabel(){
        return getLabelByIdx(basicTypeLabel);
    }

    public static Type getModifierLabel(){
        return getLabelByIdx(modifierLabel);
    }
}
