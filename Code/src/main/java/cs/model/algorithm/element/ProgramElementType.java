package cs.model.algorithm.element;

import java.util.*;

public class ProgramElementType {

    private String nodeType = "";
    private String tokenType = "";
    private int specialTokenType = -1;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgramElementType that = (ProgramElementType) o;
        return specialTokenType == that.specialTokenType &&
                nodeType.equals(that.nodeType) &&
                tokenType.equals(that.tokenType);
    }
    @Override
    public int hashCode() {
        return Objects.hash(nodeType, tokenType, specialTokenType);
    }
    public String getNodeType(){ return nodeType;}
    public String getTokenType(){ return tokenType;}
    public static ProgramElementType getElementType(ProgramElement element) {
        ProgramElementType type = new ProgramElementType();
        type.nodeType = element.getNodeType();
//        System.out.println("Ele " + element + " nodeType is " + element.getNodeType());
        if (element.isToken()) {
            TokenElement tokenEle = (TokenElement) element;
            if (tokenEle.isName())
                type.nodeType = "Name";
            type.tokenType = tokenEle.getTokenType();
            String tokenValue = element.getStringValue();
            if (specialTokenTypeMap.containsKey(tokenValue))
                type.specialTokenType = specialTokenTypeMap.get(tokenValue);
        }
//        System.out.println("Ele " + element + " nodeType is " + type.nodeType + " " + type.tokenType);
        return type;
    }

    private static final String[][] specialTokens = {
            {"protected", "public", "private"},
            {"final"},
            {"static"},
            {"+", "-"},
            {"*", "/"},
            {">>", "<<"},
            {"==", ">=", "<=", ">", "<", "!="},
            {"&&", "||"},
            {"&", "|"}
    };

    private static final Set<String> allSpecialTokens = getSpecialTokens();
    private static final Map<String, Integer> specialTokenTypeMap = getSpecialTokenTypeMap();

    private static Set<String> getSpecialTokens(){
        Set<String> allSpecialTokens = new HashSet<>();
        for (String[] list: specialTokens) {
            allSpecialTokens.addAll(Arrays.asList(list));
        }
        return allSpecialTokens;
    }

    private static Map<String, Integer> getSpecialTokenTypeMap() {
        Map<String, Integer> ret = new HashMap<>();
        int type = 0;
        for (String[] list: specialTokens) {
            for (String str: list) {
                ret.put(str, type);
            }
            type ++;
        }
        return ret;
    }
}
