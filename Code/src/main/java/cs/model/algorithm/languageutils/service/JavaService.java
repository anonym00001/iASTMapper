package cs.model.algorithm.languageutils.service;

import cs.model.utils.Pair;

import java.util.Set;

public class JavaService extends BaseLanguageService implements ProgramLanguageService {

    @Override
    protected boolean isStartOfCommentBlock(String codeStr, int idx) {
        char c = codeStr.charAt(idx);
        if (idx < codeStr.length() - 1 && c == '/') {
            char nextC = codeStr.charAt(idx + 1);
            return nextC == '*';
        }
        return false;
    }

    @Override
    protected boolean isEndOfCommentBlock(String codeStr, int idx) {
        char c = codeStr.charAt(idx);
        if (idx > 0 && c == '/') {
            char lastC = codeStr.charAt(idx - 1);
            return lastC == '*';
        }
        return false;
    }

    @Override
    protected boolean isStartOfCommentLine(String codeStr, int idx) {
        char c = codeStr.charAt(idx);
        if (c == '/' && idx < codeStr.length() - 1){
            char nextC = codeStr.charAt(idx + 1);
            return nextC == '/';
        }
        return false;
    }

    @Override
    protected boolean isBlank() {
        return false;
    }

    @Override
    protected Set<Pair<Integer, Integer>> getStringLiteralRanges(String codeStr, int idx) {
        return null;
    }
}
