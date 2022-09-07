package cs.sysu.gitops;

import cs.sysu.algorithm.utils.RangeCalculator;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Process Git diff hunks
 */
public class GitHunk {
    private static List<GitHunk> curFileHunkCache = null;

    private String commitId;
    private String oldPath;
    private String newPath;
    private String content;
    private int oldBeginPosition;
    private int oldEndPosition;
    private int newBeginPosition;
    private int newEndPosition;
    private List<GitLine> gitLines;
    private RangeCalculator srcRc;
    private RangeCalculator dstRc;

    public GitHunk(String commitId,
                   String oldPath, String newPath,
                   RangeCalculator srcRc, RangeCalculator dstRc,
                   String content, int oldBeginPosition, int oldEndPosition,
                   int newBeginPosition, int newEndPosition){
        this.commitId = commitId;
        this.oldPath = oldPath;
        this.newPath = newPath;
        this.content = content;
        this.srcRc = srcRc;
        this.dstRc = dstRc;
        this.oldBeginPosition = oldBeginPosition; // old文件的初始位置
        this.oldEndPosition = oldEndPosition;    // old文件的结束位置
        this.newBeginPosition = newBeginPosition; // new文件的初始位置
        this.newEndPosition = newEndPosition;  // old文件的结束位置
        initAllGitLines();
    }

    public String getCommitId() {
        return commitId;
    }

    public String getOldPath() {
        return oldPath;
    }

    public String getNewPath() {
        return newPath;
    }

    public String getContent() {
        return content;
    }

    public int getOldBeginPosition() {
        return oldBeginPosition;
    }

    public int getOldEndPosition() {
        return oldEndPosition;
    }

    public int getNewBeginPosition() {
        return newBeginPosition;
    }

    public int getNewEndPosition() {
        return newEndPosition;
    }

    private void initAllGitLines(){
        gitLines = new ArrayList<>();
        int oldSyncNumber = this.getOldBeginPosition();
        int newSyncNumber = this.getNewBeginPosition();
        String[] lines = RangeCalculator.getAllLines(content);
        for (String l: lines){
            String srcLineContent = srcRc.getLineContent(oldSyncNumber);
            String dstLineContent = dstRc.getLineContent(newSyncNumber);

            if (isContext(l)){
                GitLine line = new GitLine("context", oldSyncNumber, newSyncNumber);
                gitLines.add(line);
                oldSyncNumber ++;
                newSyncNumber ++;
                continue;
            }

            if (srcLineContent != null && dstLineContent != null){
                if (srcLineContent.equals(dstLineContent)){
                    if (isDeletion(l) || isAddition(l)) {
                        String tmp = l.substring(1);
                        if (!tmp.trim().equals(srcLineContent.trim())) {
                            GitLine line = new GitLine("context", oldSyncNumber, newSyncNumber);
                            gitLines.add(line);
                            oldSyncNumber++;
                            newSyncNumber++;
                            continue;
                        }
                    }
                }
            }

            if (isDeletion(l)) {
                GitLine line = new GitLine("delete", oldSyncNumber, -1);
                gitLines.add(line);
                oldSyncNumber++;
            } else if (isAddition(l)) {
                GitLine line = new GitLine("add", -1, newSyncNumber);
                gitLines.add(line);
                newSyncNumber++;
            }

            if (oldSyncNumber == oldEndPosition + 1 && newSyncNumber == newEndPosition + 1)
                break;
        }
    }

    public Set<Integer> getAllSrcLines(){
        List<Integer> ret = new ArrayList<>();
        for (GitLine gitLine: gitLines){
            if (gitLine.getSrcLineNumber() != -1)
                ret.add(gitLine.getSrcLineNumber());
        }
        return new HashSet<>(ret);
    }

    public Set<Integer> getAllDstLines(){
        List<Integer> ret = new ArrayList<>();
        for (GitLine gitLine: gitLines){
            if (gitLine.getDstLineNumber() != -1)
                ret.add(gitLine.getDstLineNumber());
        }
        return new HashSet<>(ret);
    }


    public Map<Integer, Integer> getLineMap(){
        Map<Integer, Integer> ret = new HashMap<>();
        int oldSyncNumber = this.getOldBeginPosition();
        int newSyncNumber = this.getNewBeginPosition();
        String[] lines = content.split("\\r?\\n");
        for (String l: lines){
            String srcLineContent = srcRc.getLineContent(oldSyncNumber);
            String dstLineContent = dstRc.getLineContent(newSyncNumber);

            if (srcLineContent != null && dstLineContent != null){
                if (srcLineContent.equals(dstLineContent)){
                    ret.put(oldSyncNumber, newSyncNumber);
                    oldSyncNumber ++;
                    newSyncNumber ++;
                    continue;
                }
            }
            if (isDeletion(l))
                oldSyncNumber ++;
            else if (isAddition(l))
                newSyncNumber ++;
        }
        return ret;
    }

    public Map<Integer, Integer> getBeginContextLineMap(){
        Map<Integer, Integer> ret = new HashMap<>();
        int oldSyncNumber = this.getOldBeginPosition();
        int newSyncNumber = this.getNewBeginPosition();
        String[] lines = content.split("\\r?\\n");
        for (String l: lines){
            if (isContext(l)) {
                ret.put(oldSyncNumber, newSyncNumber);
                oldSyncNumber ++;
                newSyncNumber ++;
            } else {
                break;
            }
        }
        return ret;
    }

    // 忽略空行,comment行, import 行的增加行数
    private int getNumberAdditionsIgnoreBlankAndComment(){
        return getAdditionLineNumbersIgnoreBlankAndComment(true).size();
    }

    public List<Integer> getAdditionLineNumbersIgnoreBlankAndComment(boolean ignoreBlock){
        String[] lines = content.split("\\r?\\n");
        List<Integer> lineNumbers = new ArrayList<>();
        int syncNumber = this.getNewBeginPosition();
        for (String l: lines){
            if (isAddition(l)) {
                String content = prepareContent(l);
                if (isNotNoise(content, ignoreBlock))
                    lineNumbers.add(syncNumber);
            }
            // Deletion 是原来文件行，不能被计算到
            // 新文件中
            if (!isDeletion(l))
                syncNumber ++;
        }
        return lineNumbers;
    }

    // 忽略空行和comment行的减少行数
    private int getNumberDeletionsIgnoreBlankAndComment(){
        return getDeletionLineNumbersIgnoreBlankAndComment(true).size();
    }

    public List<Integer> getDeletionLineNumbersIgnoreBlankAndComment(boolean ignoreBlock){
        String[] lines = content.split("\\r?\\n");
        List<Integer> lineNumbers = new ArrayList<>();
        int syncNumber = this.getOldBeginPosition();
        for (String l: lines){
            if (isDeletion(l)) {
                String content = prepareContent(l);
                if (isNotNoise(content, ignoreBlock))
                    lineNumbers.add(syncNumber);
            }
            // Addition 是新文件的行，
            // 不能被计算到该文件中
            if (!isAddition(l))
                syncNumber ++;
        }
        return lineNumbers;
    }

    private boolean isNotNoise(String content, boolean ignoreBlock){
        return (!isCommentOrBlankLine(content, ignoreBlock))
                && (!isImport(content))
                && (!isPackageDeclaration(content));
    }

    // 得到commit之后的文件内容
    public String getPreContent(){
        String[] lines = content.split("\\r?\\n");
        List<String> preLines = new ArrayList<>();
        for (String l: lines) {
            if (isAddition(l)) {
                continue;
            }
            preLines.add(l);
        }
        return StringUtils.join(preLines, "\n");
    }

    // 得到所有Commit之后文件的内容
    public String getNextContent(){
        String[] lines = content.split("\\r?\\n");
        List<String> preLines = new ArrayList<>();
        for (String l: lines) {
            if (isDeletion(l)) {
                continue;
            }
            preLines.add(l);
        }
        return StringUtils.join(preLines, "\n");
    }

    public List<GitLine> getGitLines() {
        return gitLines;
    }

    // 得到所有相关commit在某文件的GitHunk
    public static List<GitHunk> getGitHunks(ByteArrayOutputStream diff, String project,
                                            String oldPath, String newPath, String commitId){
        List<GitHunk> hunks = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(diff.toByteArray())));

        String currentHunkHeader = null;
        String currentHunkContent = "";

        String baseCommitId = GitUtils.getBaseCommitId(project, commitId);
        String srcFileContent;
        String dstFileContent;
        RangeCalculator srcRc;
        RangeCalculator dstRc;

        try {
            srcFileContent = GitUtils
                    .getFileContentOfCommitFile(project, baseCommitId, oldPath).toString("UTF-8");
            dstFileContent = GitUtils
                    .getFileContentOfCommitFile(project, commitId, newPath).toString("UTF-8");
            srcRc = new RangeCalculator(srcFileContent);
            dstRc = new RangeCalculator(dstFileContent);
            while (br.ready()) {
                String line = br.readLine();
                line = line.trim();
                if (isHunkHeader(line)) {
                    GitHunk gh = createGitHunk(commitId, oldPath,
                            newPath, srcRc, dstRc, currentHunkHeader, currentHunkContent);
                    if (gh != null)
                        hunks.add(gh);
                    currentHunkHeader = line;
                    currentHunkContent = "";
                } else{
                    currentHunkContent += line + "\n";
                }
            }
            br.close();
        } catch(Exception e){
            throw new RuntimeException("Cannot Analyze Diff!");
        }

        GitHunk gh = createGitHunk(commitId, oldPath, newPath, srcRc, dstRc,
                currentHunkHeader, currentHunkContent);
        if (gh != null)
            hunks.add(gh);
        return hunks;
    }

    // 根据信息创建 GitHunk
    private static GitHunk createGitHunk(String commitId,
                                         String oldPath, String newPath,
                                         RangeCalculator srcRc, RangeCalculator dstRc,
                                         String currentHunkHeader, String currentHunkContent){
        if (currentHunkHeader != null) {
            int prevStartPosition = getPrevContextStartingLineNumber(currentHunkHeader);
            int nextStartPosition = getNextContextStartingLineNumber(currentHunkHeader);
            int preRange = getPrevContextLineRange(currentHunkHeader);
            int nextRange = getNextContextLineRange(currentHunkHeader);

            return new GitHunk(commitId, oldPath, newPath,
                    srcRc, dstRc, currentHunkContent,
                    prevStartPosition, prevStartPosition + preRange - 1,
                    nextStartPosition, nextStartPosition + nextRange - 1);
        }
        return null;
    }

    // GitHunk 在commit之前文件的开始行
    private static int getPrevContextStartingLineNumber(String header) {
        String[] tokens = header.split(" ");
        String toAnalyze = tokens[1];
        String[] tokens2 = toAnalyze.split(",");
        String lineNumberStr = tokens2[0].replace("-", "");
        return Integer.parseInt(lineNumberStr);
    }

    // GitHunk 在commit之前的文件范围
    private static int getPrevContextLineRange(String header) {
        String[] tokens = header.split(" ");
        String toAnalyze = tokens[1];
        String[] tokens2 = toAnalyze.split(",");
        String lineNumberStr = tokens2[1];
        return Integer.parseInt(lineNumberStr);
    }

    // GitHunk 在commit之后文件的开始位置
    private static int getNextContextStartingLineNumber(String header) {
        String[] tokens = header.split(" ");
        String toAnalyze = tokens[2];
        String[] tokens2 = toAnalyze.split(",");
        String lineNumberStr = tokens2[0].replace("+", "");
        return Integer.parseInt(lineNumberStr);
    }

    // GitHunk 在commit之后文件范围
    private static int getNextContextLineRange(String header) {
        String[] tokens = header.split(" ");
        String toAnalyze = tokens[2];
        String[] tokens2 = toAnalyze.split(",");
        String lineNumberStr = tokens2[1];
        return Integer.parseInt(lineNumberStr);
    }

    // 该行是否是增加行
    public static boolean isAddition(String line) {
        return line.startsWith("+");
    }

    // 该行是不是减少行
    public static boolean isDeletion(String line) {
        return line.startsWith("-");
    }

    // not delete or add
    public static boolean isContext(String line){
        return !isAddition(line) && !isDeletion(line);
    }

    // Hunk Header的形式：@@ -1,2 +2,3 @@
    private static boolean isHunkHeader(String line) {
        Pattern pattern = Pattern.compile("@@\\s-\\d+,\\d+\\s\\+\\d+,\\d+\\s@@");
        Matcher matcher = pattern.matcher(line);
        return matcher.find();
    }

    private static boolean isAnnotation(String line){
        Pattern p = Pattern.compile("^\\@.*$");
        Matcher m = p.matcher(line);
        if(m.find()){
            return true;
        }
        return false;
    }

    // 去除前后 + - 号
    // 去除前后空格
    public static String prepareContent(String line){
        String content = line.trim();
        if(content.length() > 0){
            if(content.charAt(0) == '+'){
                content = content.replaceFirst("\\+","");
                content = content.trim();
            } else if (content.charAt(0) == '-'){
                content = content.replaceFirst("-","");
                content = content.trim();
            }
        }
        content = content.replaceAll("\\s+"," ");
        content = content.trim();
        return content;
    }

    // 识别当前行是否属于空行、comment行
    // 当考虑block时，
    public static boolean isCommentOrBlankLine(String line, boolean ignoreBlock){
        line = prepareContent(line);
        if (line.length() == 0)
            return true;

        if(line.equals("\\Nonewlineatendoffile")){
            return true;
        }

        boolean result = false;
        Pattern pattern = Pattern.compile("^(//)(.*)$");
        Matcher matcher = pattern.matcher(line.trim());

        result = matcher.find();
        if (result)
            return true;

        pattern = Pattern.compile("^(/\\*)(.*)$");
        matcher = pattern.matcher(line.trim());

        result = matcher.find();
        if (result)
            return true;

        pattern = Pattern.compile("^(\\*)(.*)$");
        matcher = pattern.matcher(line.trim());

        result = matcher.find();
        if (result)
            return true;

        if (ignoreBlock) {
            pattern = Pattern.compile("^(})(\\s*)$");
            matcher = pattern.matcher(line.trim());

            result = matcher.find();
            if (result)
                return true;

            pattern = Pattern.compile("^(\\{)(\\s*)$");
            matcher = pattern.matcher(line.trim());

            result = matcher.find();
            if (result)
                return true;
        }

        pattern = Pattern.compile("^;$");
        matcher = pattern.matcher(line.trim());

        result = matcher.find();
        if (result)
            return true;

        return false;
    }


    public static boolean isImport(String content) {
        boolean result = false;
        content = prepareContent(content);
        Pattern pattern = Pattern.compile("^(import)(\\s*)(.*)$");
        Matcher matcher = pattern.matcher(content);
        result = matcher.find();
        return result;
    }

    public static boolean isPackageDeclaration(String content){
        boolean result = false;
        content = prepareContent(content);
        Pattern pattern = Pattern.compile("^(package)(\\s*)(.*)$");
        Matcher matcher = pattern.matcher(content);
        result = matcher.find();
        return result;
    }

    public static boolean isLog(String content){
        boolean result = false;
        content = prepareContent(content);
        content = content.toLowerCase();
        Pattern pattern = Pattern.compile("^(system\\.out\\.print)(.*)");
        Matcher matcher = pattern.matcher(content);
        result = matcher.find();
        return result;
    }

    // 识别当前文件增加行数，忽略blank, comment
    private static int getNumAdditionsOfFileIgnoreBlankAndComment(String project,
                                                                  String commitId,
                                                                  String filePath) {
        return getAllAddedLines(project, commitId, filePath, true).size();
    }

    // 识别当前文件删除行数, 忽略blank, comment
    private static int getNumDeletionsOfFileIgnoreBlankAndComment(String project,
                                                                  String commitId,
                                                                  String filePath) {
        return getAllDeletedLines(project, commitId, filePath, true).size();
    }

    public static List<GitHunk> getGitHunksFromGit(String project, String commitId, String filePath) {
        if (curFileHunkCache != null && curFileHunkCache.size() > 0){
            if (curFileHunkCache.get(0).commitId.equals(commitId) &&
                    curFileHunkCache.get(0).oldPath.equals(filePath))
                return curFileHunkCache;
        }
        ByteArrayOutputStream diff = GitInfoRetrieval.getFileDiff(project, commitId, filePath);
        String newPath = GitInfoRetrieval.getAfterModificationPath(project, commitId, filePath);
        if (diff == null) {
            curFileHunkCache = null;
            return null;
        }

        List<GitHunk> ret = getGitHunks(diff, project, filePath, newPath, commitId);
        curFileHunkCache = ret;
        return ret;
    }

    private static Set<Integer> getAllInvolvedLinesFromGitHunks(List<GitHunk> hunks, String modifyType,
                                                                boolean ignoreBlock){
        if (hunks == null || hunks.size() == 0)
            return null;
        Set<Integer> ret = new HashSet<>();
        for (GitHunk hunk: hunks) {
            if (modifyType.equals("ADD"))
                ret.addAll(hunk.getAdditionLineNumbersIgnoreBlankAndComment(ignoreBlock));
            else
                ret.addAll(hunk.getDeletionLineNumbersIgnoreBlankAndComment(ignoreBlock));
        }
        return ret;
    }


    private static Set<Integer> getAllInvolvedLinesFromGit(String project, String commitId,
                                                           String filePath, String modifyType,
                                                           boolean ignoreBlock) {
        List<GitHunk> hunks = getGitHunksFromGit(project, commitId, filePath);
        return getAllInvolvedLinesFromGitHunks(hunks, modifyType, ignoreBlock);
    }

    // 得到一个文件全部增加行数
    public static Set<Integer> getAllAddedLines(String project, String commitId,
                                                String filePath,
                                                boolean ignoreBlock) {
        return getAllInvolvedLinesFromGit(project, commitId, filePath, "ADD", ignoreBlock);
    }

    // 得到一个文件全部delete行数
    public static Set<Integer> getAllDeletedLines(String project, String commitId,
                                                  String filePath,
                                                  boolean ignoreBlock) {
        return getAllInvolvedLinesFromGit(project, commitId, filePath, "DELETE", ignoreBlock);
    }

    public static Map<Integer, Integer> getLineMapOfChunks(String project, String commitId,
                                                           String filePath){
        List<GitHunk> hunks = getGitHunksFromGit(project, commitId, filePath);
        if (hunks == null)
            return null;
        Map<Integer, Integer> ret = new HashMap<>();
        for (GitHunk hunk: hunks){
            ret.putAll(hunk.getLineMap());
        }
        return ret;
    }

    public static List<GitLine> getGitLinesOfFile(String project, String commitId, String filePath,
                                                  int srcFileLineNumbers, int dstFileLineNumbers){
        List<GitHunk> hunks = getGitHunksFromGit(project, commitId, filePath);
        if (hunks == null)
            return null;
        List<GitLine> ret = new ArrayList<>();
        int oldSyncNumber = 1;
        int newSyncNumber = 1;
        for (GitHunk hunk: hunks){
            int oldBeginPosition = hunk.getOldBeginPosition();
            int newBeginPosition = hunk.getNewBeginPosition();
            while (oldSyncNumber < oldBeginPosition && newSyncNumber < newBeginPosition){
                ret.add(new GitLine("context", oldSyncNumber, newSyncNumber));
                oldSyncNumber ++;
                newSyncNumber ++;
            }
            ret.addAll(hunk.getGitLines());
            oldSyncNumber = hunk.getOldEndPosition() + 1;
            newSyncNumber = hunk.getNewEndPosition() + 1;
        }

        while (oldSyncNumber <= srcFileLineNumbers && newSyncNumber <= dstFileLineNumbers){
            ret.add(new GitLine("context", oldSyncNumber, newSyncNumber));
            oldSyncNumber ++;
            newSyncNumber ++;
        }

        if (oldSyncNumber != srcFileLineNumbers + 1 && newSyncNumber != dstFileLineNumbers + 1)
            throw new RuntimeException("Sync Line Number Error!");
        return ret;
    }

    public static Map<Integer, Integer> getLineMapIgnoreChunk(String project, String commitId,
                                                              String filePath,
                                                              int srcFileLineNumbers,
                                                              int dstFileLineNumbers){
        List<GitHunk> hunks = getGitHunksFromGit(project, commitId, filePath);
        if (hunks == null)
            return null;
        Map<Integer, Integer> ret = new HashMap<>();
        int oldSyncNumber = 1;
        int newSyncNumber = 1;
        for (GitHunk hunk: hunks){
            int oldBeginPosition = hunk.getOldBeginPosition();
            int newBeginPosition = hunk.getNewBeginPosition();
            while (oldSyncNumber < oldBeginPosition && newSyncNumber < newBeginPosition){
                ret.put(oldSyncNumber, newSyncNumber);
                oldSyncNumber ++;
                newSyncNumber ++;
            }
            oldSyncNumber = hunk.getOldEndPosition() + 1;
            newSyncNumber = hunk.getNewEndPosition() + 1;
        }

        while (oldSyncNumber <= srcFileLineNumbers && newSyncNumber <= dstFileLineNumbers){
            ret.put(oldSyncNumber, newSyncNumber);
            oldSyncNumber ++;
            newSyncNumber ++;
        }

        if (oldSyncNumber != srcFileLineNumbers + 1 && newSyncNumber != dstFileLineNumbers + 1)
            throw new RuntimeException("Sync Line Number Error!");
        return ret;
    }
}
