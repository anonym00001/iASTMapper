package cs.sysu.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CodeStringSplitUtils {
    public static List<String> splitCamelCase(String s){
        return new LinkedList<String>(Arrays.asList(s.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")));
    }

    public static List<String> splitSnakeCase(String s) {
        return new LinkedList<>(Arrays.asList(s.split("_")));
    }
}
