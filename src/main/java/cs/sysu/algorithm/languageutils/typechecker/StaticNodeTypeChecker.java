package cs.sysu.algorithm.languageutils.typechecker;

public class StaticNodeTypeChecker {
    private static final JavaNodeTypeChecker jdtChecker = new JavaNodeTypeCheckerWithJDT();

    public static JavaNodeTypeChecker getConfigNodeTypeChecker(){
        return jdtChecker;
    }
}
