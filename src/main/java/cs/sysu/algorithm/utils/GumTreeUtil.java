package cs.sysu.algorithm.utils;

import at.aau.softwaredynamics.gen.OptimizedJdtTreeGenerator;
import at.aau.softwaredynamics.matchers.JavaMatchers;
import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.gen.jdt.cd.CdJdtTreeGenerator;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import cs.sysu.algorithm.languageutils.typechecker.StaticNodeTypeChecker;

import java.io.*;
import java.util.List;

/**
 * APIs that use GumTree, MTDiff, IJM and other methods to build ITree, create mappings and generate actions.
 */
public class GumTreeUtil {

    private static ITree getITreeRoot(Reader reader, String matcherId){
        Run.initGenerators();
        ITree t = null;
        try {
            if (matcherId.equals("change-distiller")) {
                t = new CdJdtTreeGenerator().generate(reader).getRoot();
            } else if (matcherId.equals("ijm")) {
                t = new OptimizedJdtTreeGenerator().generate(reader).getRoot();
            } else {
                t = new JdtTreeGenerator().generate(reader).getRoot();
            }
        } catch(Exception e) {
            throw new GumTreeException(e.getMessage());
        }
        return t;
    }

    /**
     * Calculate mappings of AST nodes using GumTree
     * @param src root of source ITree
     * @param dst root of target ITree
     * @return the generated mappings
     */
    public static MappingStore getTreeMappings(ITree src, ITree dst) {
        return getTreeMappings(src, dst, null);
    }

    /**
     * Calculate mappings of AST nodes using GumTree given mappings of AST nodes
     * @param src root of source ITree
     * @param dst root of target ITree
     * @param ms  the given mappings
     * @return the generated mappings
     */
    public static MappingStore getTreeMappings(ITree src, ITree dst, MappingStore ms){
        return getTreeMappings(src, dst, ms, "gumtree");
    }

    /**
     * Calculate mappings of AST nodes using a specified method
     * @param src root of source ITree
     * @param dst root of target ITree
     * @param ms a given mappings
     * @param matcherId the matcher name, e.g., "gumtree"
     * @return the generated mappings
     */
    public static MappingStore getTreeMappings(ITree src, ITree dst, MappingStore ms, String matcherId){

        Matcher m = Matchers.getInstance().getMatcher(matcherId);
        if (matcherId != null && matcherId.equals("ijm"))
            m = new JavaMatchers.IterativeJavaMatcher_V2();
        MappingStore ms2;
        try {
            if (ms == null) {
                ms2 = m.match(src, dst);
            } else {
                MappingStore tmp = new MappingStore(ms);
                ms2 = m.match(src, dst, tmp);
            }
            return ms2;
        } catch (Exception | OutOfMemoryError e){
            e.printStackTrace();
            throw new GumTreeException(e.getMessage());
        }
    }

    /**
     * Build an ITree based on bytestream using a specified method
     * @param stream the bytestream
     * @param matcherId the matcher name
     * @return the generated ITree
     */
    public static ITree getITreeRoot(ByteArrayOutputStream stream, String matcherId) {
        Reader reader = new InputStreamReader(new ByteArrayInputStream(stream.toByteArray()));
        return getITreeRoot(reader, matcherId);
    }

    /**
     * Build an ITree based on given file content using a specified method
     * @param fileContent the file content
     * @param matcherId the matcher name
     * @return the generated ITree
     */
    public static ITree getITreeRoot(String fileContent, String matcherId){
        Reader reader = new StringReader(fileContent);
        return getITreeRoot(reader, matcherId);
    }

    /**
     * Calculate a list of edit actions that are implemented by GumTree
     * based on a given mappings of ITree nodes.
     *
     * @param ms mappings of ITree nodes
     */
    public static List<Action> getEditActions(MappingStore ms){
        if (ms == null)
            return null;
        EditScriptGenerator g = new ChawatheScriptGenerator();
        return g.computeActions(ms).asList();
    }

    public static boolean isDirectElementOfNode(ITree t, ITree node){
        if (t == node)
            return false;
        ITree temp = t;
        while (temp != null && temp != node){
            if (StaticNodeTypeChecker.getConfigNodeTypeChecker().isStatementNode(temp))
                break;
            temp = temp.getParent();
        }
        return temp == node;
    }
}
