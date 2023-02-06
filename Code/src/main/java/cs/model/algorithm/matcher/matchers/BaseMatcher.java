package cs.model.algorithm.matcher.matchers;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.matcher.matchers.searchers.BestMappingSearcher;
import cs.model.algorithm.matcher.rules.ElementMatchDeterminer;
import cs.model.algorithm.matcher.mappings.ElementMapping;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.evaluation.config.MyConfig;

import java.util.*;

/**
 * Base Matcher for statements, tokens and inner-stmt elements
 * that uses diverse similarity measures.
 *
 * Method:
 * We have two sets of elements from two files, respectively.
 * For each element in each set, we calculate a set of candidate elements.
 * From the candidate elements, we aim to find the best-mapped element.
 */
public abstract class BaseMatcher {
    // the searcher of best mappings
    protected final BestMappingSearcher bestMappingSearcher;

    // mappings of statements, tokens and inner-stmt elements
    protected final ElementMappings elementMappings;

    // source elements that are not mapped
    protected Set<ProgramElement> srcElementsToMap;

    // Whether to process tokens
    protected boolean processToken = false;

    protected Map<ElementMapping, Integer> mappingTimeRecord;

    protected Set<ProgramElement> infiniteLoopElements;


    public String Matcher_type;

    private static int infiniteLoopTime = MyConfig.getInfiniteLoopTime();

    public BaseMatcher(ElementMappings elementMappings, BestMappingSearcher bestMappingSearcher){
        this.elementMappings = elementMappings;
        this.bestMappingSearcher = bestMappingSearcher;
        this.mappingTimeRecord = new HashMap<>();
        this.infiniteLoopElements = new HashSet<>();
    }
    public void setMatcher_type(String the_type){
        this.Matcher_type = the_type;
    }
    public void calSrcElementsToMap() {
        this.srcElementsToMap = getAllSrcElementsToMap();
    }
    public ElementMappings getElementMappings() {
        return elementMappings;
    }

    public ElementSimMeasures getElementSimMeasures(ProgramElement srcEle, ProgramElement dstEle){
        ElementSimMeasures measures = bestMappingSearcher.getElementSimMeasures(srcEle, dstEle);
        if (measures == null)
            measures = new ElementSimMeasures(srcEle, dstEle);
        return measures;
    }

    public void setProcessToken(boolean processToken) {
        this.processToken = processToken;
    }

    /**
     * Iteratively build mappings for stmt or token between
     * file before and after a revision
     */
    public boolean buildMappingsInnerLoop() {
        Map<ProgramElement, ProgramElement> elementsToMap = new HashMap<>();
        boolean findMapping = false;
        removeIllegalMappings();

        do {
            ElementMatchDeterminer determiner = new ElementMatchDeterminer(elementMappings);
            bestMappingSearcher.setElementMappings(elementMappings);
            bestMappingSearcher.setElementMatchDeterminer(determiner);
            bestMappingSearcher.clearBestCandidateMaps();

            // record if we find new mappings in this loop
            boolean findMappingInThisLoop = false;

            elementsToMap.clear();

            // First find mappings considering all the target elements
            bestMappingSearcher.findElementPairsToMap(elementsToMap, srcElementsToMap, false);
            if (elementsToMap.size() > 0) {
                // We first check if there may exist an infinite loop.
                // Then, we add the element pairs to element mappings
                for (ProgramElement srcEle : elementsToMap.keySet()) {
                    if (infiniteLoopElements.contains(srcEle))
                        continue;
                    ProgramElement dstEle = elementsToMap.get(srcEle);
                    if (infiniteLoopElements.contains(dstEle))
                        continue;
                    addElementMapping(srcEle, dstEle);
                    findMappingInThisLoop = true;
                }
            } else {
                // find mappings excluding target elements that have been mapped.
                bestMappingSearcher.findElementPairsToMap(elementsToMap, srcElementsToMap, true);

                // add the element pairs to element mappings
                for (ProgramElement srcEle: elementsToMap.keySet()) {
                    ProgramElement dstEle = elementsToMap.get(srcEle);
                    addElementMapping(srcEle, dstEle);
                    findMappingInThisLoop = true;
                }
            }
            findMapping |= findMappingInThisLoop;
            if (!findMappingInThisLoop)
                break;
        } while (elementsToMap.size() > 0);

        return findMapping;
    }

    /**
     * Find the best mapped elements and map them for inner-stmt element
     */
    public void buildMappingsForInnerStmtElements() {
        ElementMatchDeterminer determiner = new ElementMatchDeterminer(elementMappings);
        bestMappingSearcher.setElementMatchDeterminer(determiner);
        Map<ProgramElement, ProgramElement> elementsToMap = new HashMap<>();
        bestMappingSearcher.findOneToOneElementPairsToMap(elementsToMap, srcElementsToMap);
        for (ProgramElement srcEle: elementsToMap.keySet()) {
            ProgramElement dstEle = elementsToMap.get(srcEle);
            addElementMapping(srcEle, dstEle);
        }
    }

    private void removeIllegalMappings() {
        for (ProgramElement srcEle: srcElementsToMap) {
            if (elementMappings.isMapped(srcEle)) {
                ProgramElement dstEle = elementMappings.getMappedElement(srcEle);
                ElementSimMeasures measures = getElementSimMeasures(srcEle, dstEle);
                ElementMatchDeterminer determiner = new ElementMatchDeterminer(elementMappings);
                if (!determiner.determine(measures)) {
                    elementMappings.removeMapping(srcEle);
                }
            }
        }
    }

    /**
     * Add mapping between two elements
     * @param srcEle source element
     * @param dstEle destination element
     */
    protected void addElementMapping(ProgramElement srcEle, ProgramElement dstEle){
        ElementMapping mapping = new ElementMapping(srcEle, dstEle);
        if (!mappingTimeRecord.containsKey(mapping))
            mappingTimeRecord.put(mapping, 0);
        mappingTimeRecord.put(mapping, mappingTimeRecord.get(mapping) + 1);
        if (mappingTimeRecord.get(mapping) >= infiniteLoopTime) {
            infiniteLoopElements.add(srcEle);
            infiniteLoopElements.add(dstEle);
        }
        elementMappings.addMapping(srcEle, dstEle);
    }

    protected abstract Set<ProgramElement> getAllSrcElementsToMap();
}
