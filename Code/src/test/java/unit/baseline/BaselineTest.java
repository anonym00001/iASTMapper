package unit.baseline;

import utils.TestUtils;

import java.util.HashSet;
import java.util.Set;

public class BaselineTest {
    public static void main(String[] args) throws Exception {
        test1();
    }
    // baseline Test
    private static void test1() throws Exception {
        String project = "commons-lang";
        String commitId = "6fdd16815acb4d46bf82f94236e09291aba0ff5b";
        String file = "src/java/org/apache/commons/lang/exception/ExceptionUtils.java";

        // optional methods (gt,mtd,ijm)
        TestUtils.testBaseline(project, commitId, file, "gt");
    }
    // The function for testing our iASTMapper
    private static void test2() throws Exception {
        String project = "junit4";
        String commitId = "890b7b977e42360aa8975c8535fc66bfd8d8cb3e";
        String file = "src/org/junit/internal/runners/TestIntrospector.java";

        TestUtils.testiASTMapper(project, commitId, file);
    }

}
