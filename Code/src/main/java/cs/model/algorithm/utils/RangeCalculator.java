package cs.model.algorithm.utils;

import com.github.gumtreediff.tree.ITree;
import cs.model.gitops.GitHunk;
import cs.model.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Line number calculator for a program element.
 */
public class RangeCalculator {

    private List<Integer> lineEndIndexes;
    private String[] lines;
    private String fileContent;

    public RangeCalculator(String fileContent){
        initLineEndIndexes(fileContent);
        this.fileContent = fileContent;
        buildLines();
    }

    private void buildLines(){
        List<String> tmpLines = new ArrayList<>();
        String currentLine = "";
        for (int i = 0; i < fileContent.length(); i++){
            char c = fileContent.charAt(i);
            if (c == '\n'){
                tmpLines.add(currentLine);
                currentLine = "";
            } else {
                currentLine += c;
            }
        }
        if (fileContent.endsWith("\n"))
            tmpLines.add("");
        else if (!currentLine.equals(""))
            tmpLines.add(currentLine);
        lines = tmpLines.toArray(new String[tmpLines.size()]);
    }

    public int getLineNumber(){
        return lines.length;
    }

    public String[] getLines() {
        return lines;
    }

    private void initLineEndIndexes(String fileContent){
        lineEndIndexes = new ArrayList<>();
        int the_end = 0;
        for (int i = 0; i < fileContent.length(); i++) {
            if (fileContent.charAt(i) == '\n')
                lineEndIndexes.add(i);
            the_end++;
        }
        // repair the bug add the last line index (because the last line hasn't the end character) 20220510 21:57
        lineEndIndexes.add(the_end);
    }

    /**
     * Get the line number given a character position
     * @param pos the character position
     * @return the line number of pos
     */
    public int getLineNumberOfPos(int pos){
//        System.out.println(pos + " || " + lineEndIndexes);
        int ret = findEndIndexesBetweenNode(pos, lineEndIndexes);
        if (ret == -1)
            throw new RuntimeException("Cannot find the line number of pos!");
        return ret;
    }

    /**
     * Get start and end line of an ITree node
     * @param node an ITree node
     */
    public Pair<Integer, Integer> getLineRangeOfNode(ITree node){
        if (node == null)
            return null;
        int startPos = node.getPos();
        int endPos = node.getEndPos();
        int startLine = getLineNumberOfPos(startPos);
        int endLine = getLineNumberOfPos(endPos);
        return new Pair<>(startLine, endLine);
    }

    public List<Integer> getContentLineNumbers(ITree node){
        Pair<Integer, Integer> range = getLineRangeOfNode(node);
        return getContentLineNumbersFromRange(range);
    }

    public List<Integer> getContentLineNumbersFromRange(Pair<Integer, Integer> range){
        if (range == null)
            return null;
        List<Integer> ret = new ArrayList<>();
        for (int i = range.first; i <= range.second; i++){
            String line = lines[i - 1];
            if (GitHunk.isCommentOrBlankLine(line, false))
                continue;
            ret.add(i);
        }
        return ret;
    }

    public int getStartColumnOfNode(ITree node){
        int pos = node.getPos();
        int lineNumber = findEndIndexesBetweenNode(pos, lineEndIndexes);
        if (lineNumber == 1)
            return pos + 1;
        else{
            int priorLineEndIndex = lineEndIndexes.get(lineNumber - 2);
            return pos - priorLineEndIndex;
        }
    }

    private static int findEndIndexesBetweenNode(int pos, List<Integer> endIndexes) {
        if (endIndexes.size() == 0)
            return -1;
        if (endIndexes.size() == 1) {
            if (pos > endIndexes.get(0))
                return -1;
            else
                return 1;
        }
        int medIndex = endIndexes.size() / 2;
        List<Integer> left = endIndexes.subList(0, medIndex);
        List<Integer> right = endIndexes.subList(medIndex, endIndexes.size());
        if (pos <= endIndexes.get(medIndex - 1))
            return findEndIndexesBetweenNode(pos, left);
        else
            return medIndex + findEndIndexesBetweenNode(pos, right);
    }

    public String getLineContent(int line){
        if (line <= 0 || line > lines.length)
            return null;
        return lines[line - 1];
    }

    public String getFileContent(){
        return fileContent;
    }

    public static String[] getAllLines(String content){
        return new RangeCalculator(content).getLines();
    }

}
