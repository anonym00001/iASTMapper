package cs.model.gitops;

public class GitLine {
    private String type;
    private int srcLineNumber;
    private int dstLineNumber;

    public GitLine(String type, int srcLineNumber, int dstLineNumber){
        this.type = type;
        this.srcLineNumber = srcLineNumber;
        this.dstLineNumber = dstLineNumber;
    }

    public String getType() {
        return type;
    }

    public int getSrcLineNumber() {
        return srcLineNumber;
    }

    public int getDstLineNumber() {
        return dstLineNumber;
    }

    @Override
    public String toString() {
        return "GitLine{" +
                "type='" + type + '\'' +
                ", srcLineNumber=" + srcLineNumber +
                ", dstLineNumber=" + dstLineNumber +
                '}';
    }
}
