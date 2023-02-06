package cs.model.algorithm.languageutils.service;

import cs.model.utils.Pair;

import java.util.Set;

public abstract class BaseLanguageService implements ProgramLanguageService {

    @Override
    public String removeCommentAndBlank(String codeStr) {
        for (int i = 0; i < codeStr.length(); i++) {

        }
        return null;
    }

    protected abstract boolean isStartOfCommentBlock(String codeStr, int idx);

    protected abstract boolean isEndOfCommentBlock(String codeStr, int idx);

    protected abstract boolean isStartOfCommentLine(String codeStr, int idx);

    protected abstract boolean isBlank();

    protected abstract Set<Pair<Integer, Integer>> getStringLiteralRanges(String codeStr, int idx);

    private int findEndOfCommentBlock(String codeStr, int idx) {
        return -1;
    }

    private int findNextLine(String codeStr, int idx) {
        return -1;
    }
}
