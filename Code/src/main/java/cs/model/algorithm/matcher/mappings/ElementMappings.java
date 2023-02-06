package cs.model.algorithm.matcher.mappings;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.model.algorithm.element.InnerStmtElement;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.TokenElement;
import cs.model.algorithm.matcher.rename.RenameStatistics;

import java.util.*;

/**
 * Data structure that stores mappings of program elements between
 * source file and target file.
 */
public class ElementMappings implements Iterable<ElementMapping> {
    private Map<ProgramElement, ProgramElement> srcToDst;
    private Map<ProgramElement, ProgramElement> dstToSrc;

    private RenameStatistics renameStatistics;

    public ElementMappings(){
        srcToDst = new HashMap<>();
        dstToSrc = new HashMap<>();
        renameStatistics = new RenameStatistics();
    }

    public Map<ProgramElement,ProgramElement> getSrcToDst(){
        return this.srcToDst;
    }

    public Set<ProgramElement> getMappedSrcElements(){
        return new HashSet<>(srcToDst.keySet());
    }

    public void addMapping(ProgramElement srcEle, ProgramElement dstEle){
        removeMapping(srcEle);
        removeMapping(dstEle);
        srcToDst.put(srcEle, dstEle);
        dstToSrc.put(dstEle, srcEle);
        if (srcEle.isToken()) {
            renameStatistics.addRenameByNewTokenMapping((TokenElement) srcEle, (TokenElement) dstEle);
            InnerStmtElement srcInnerEle = ((TokenElement) srcEle).getInnerStmtEleOfToken();
            InnerStmtElement dstInnerEle = ((TokenElement) dstEle).getInnerStmtEleOfToken();
            if (srcInnerEle != null && dstInnerEle != null) {
                srcToDst.put(srcInnerEle, dstInnerEle);
                dstToSrc.put(dstInnerEle, srcInnerEle);
            }
        }
    }

    public void removeMapping(ProgramElement element){
        if (isMapped(element)){
            ProgramElement srcEle;
            ProgramElement dstEle;
            if (element.isFromSrc()){
                srcEle = element;
                dstEle = getDstForSrc(element);
            } else {
                srcEle = getSrcForDst(element);
                dstEle = element;
            }
            srcToDst.remove(srcEle);
            dstToSrc.remove(dstEle);
            if (srcEle.isToken()) {
                renameStatistics.removeRenameByTokenMapping((TokenElement) srcEle, (TokenElement) dstEle);
                InnerStmtElement srcInnerEle = ((TokenElement) srcEle).getInnerStmtEleOfToken();
                InnerStmtElement dstInnerEle = ((TokenElement) dstEle).getInnerStmtEleOfToken();
                if (srcInnerEle != null && dstInnerEle != null) {
                    srcToDst.remove(srcInnerEle);
                    dstToSrc.remove(dstInnerEle);
                }
            }
        }
    }

    public ProgramElement getSrcForDst(ProgramElement dstEle){
        return dstToSrc.get(dstEle);
    }

    public ProgramElement getDstForSrc(ProgramElement srcEle){
        return srcToDst.get(srcEle);
    }

    private boolean isSrcMapped(ProgramElement srcEle){
        return srcToDst.containsKey(srcEle);
    }

    private boolean isDstMapped(ProgramElement dstEle){
        return dstToSrc.containsKey(dstEle);
    }

    public ProgramElement getMappedElement(ProgramElement element){
        if (element.isFromSrc())
            return getDstForSrc(element);
        else
            return getSrcForDst(element);
    }

    public boolean isMapped(ProgramElement ele){
        if (ele.isFromSrc())
            return isSrcMapped(ele);
        else
            return isDstMapped(ele);
    }

    public Set<ElementMapping> asSet() {
        return new AbstractSet<ElementMapping>() {

            @Override
            public Iterator<ElementMapping> iterator() {
                Iterator<ProgramElement> it = srcToDst.keySet().iterator();
                return new Iterator<ElementMapping>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public ElementMapping next() {
                        ProgramElement src = it.next();
                        if (src == null) return null;
                        return new ElementMapping(src, srcToDst.get(src));
                    }
                };
            }

            @Override
            public int size() {
                return srcToDst.keySet().size();
            }
        };
    }


    @Override
    public Iterator<ElementMapping> iterator() {
        return asSet().iterator();
    }

    @Override
    public String toString(){
        StringBuilder b = new StringBuilder();
        for (ElementMapping m: this){
            b.append(m.toString()).append('\n');
        }
        return b.toString();
    }

    public ElementMappings copy() {
        ElementMappings mappings = new ElementMappings();
        mappings.srcToDst = new HashMap<>(srcToDst);
        mappings.dstToSrc = new HashMap<>(dstToSrc);
        return mappings;
    }

    public RenameStatistics getRenameStatistics() {
        return renameStatistics;
    }

    public boolean isTokenRenamed(ProgramElement srcElement, ProgramElement dstElement){
        TokenElement srcToken = (TokenElement) srcElement;
        TokenElement dstToken = (TokenElement) dstElement;
        String srcStr = srcToken.getStringValue();
        String dstStr = dstToken.getStringValue();
//        System.out.println("String value is " + srcStr + " " + dstStr);
        if (srcStr.equals(dstStr))
            return false;
        Set<String> otherNames = getRenameStatistics()
                .getDstNameForSrcNameWithoutCurTokenPairs(srcStr, this, srcToken, dstToken);
        if (otherNames != null)
            return otherNames.contains(dstStr);
        return false;
    }

    public MappingStore toMappingStore(ITree srcRoot, ITree dstRoot) {
        MappingStore ms = new MappingStore(srcRoot, dstRoot);
        for (ProgramElement src: srcToDst.keySet()) {
            ITree srcT = src.getITreeNode();
            ITree dstT = srcToDst.get(src).getITreeNode();
            ms.addMapping(srcT, dstT);
        }
        return ms;
    }

}
