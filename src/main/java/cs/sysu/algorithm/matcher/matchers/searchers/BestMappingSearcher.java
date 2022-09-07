package cs.sysu.algorithm.matcher.matchers.searchers;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.matcher.mappings.ElementMapping;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.rules.ElementMatchDeterminer;
import cs.sysu.algorithm.utils.LongestCommonSubsequence;

import java.util.*;

/**
 * Search the best element pairs to map
 *
 * We have three settings:
 * 1. Consider all the target elements when searching the best element to map (one-to-one)
 * 2. Search one-to-one best mapped element pairs excluding current mappings
 * 3. Search multi-to-multi best mapped element pairs excluding current mappings
 */
public class BestMappingSearcher {
    // searcher for the candidates of a given source element
    private final CandidateSearcher candidateSearcher;

    // data structure for measures that need to be stored
    private final Map<ElementMapping, ElementSimMeasures> measuresMap;

    // global candidate, consider all the elements in the file
    private final Map<ProgramElement, Set<ProgramElement>> srcToGlobalBestDstCandidateMap;

    // local candidate, excluding the mapped elements
    private final Map<ProgramElement, Set<ProgramElement>> srcToLocalBestDstCandidateMap;

    // best source candidates for each target element excluding mapped elements
    private final Map<ProgramElement, Set<ProgramElement>> dstToLocalBestSrcCandidateMap;

    // the element mappings
    private ElementMappings eleMappings;

    // determiner that decides if two elements can be mapped
    private ElementMatchDeterminer determiner;

    public BestMappingSearcher(CandidateSearcher candidateSearcher){
        this.candidateSearcher = candidateSearcher;
        this.measuresMap = new HashMap<>();
        this.srcToGlobalBestDstCandidateMap = new HashMap<>();
        this.srcToLocalBestDstCandidateMap = new HashMap<>();
        this.dstToLocalBestSrcCandidateMap = new HashMap<>();
    }

    public void setElementMappings(ElementMappings eleMappings) {
        this.eleMappings = eleMappings;
        this.candidateSearcher.setElementMappings(eleMappings);
    }

    public void setElementMatchDeterminer(ElementMatchDeterminer determiner) {
        this.determiner = determiner;
    }

    public ElementMatchDeterminer getElementMatchDeterminer() {
        return this.determiner;
    }

    public void clearBestCandidateMaps() {
        this.srcToGlobalBestDstCandidateMap.clear();
        this.srcToLocalBestDstCandidateMap.clear();
        this.dstToLocalBestSrcCandidateMap.clear();
        this.measuresMap.clear();
    }

    public ElementSimMeasures getElementSimMeasures(ProgramElement srcEle, ProgramElement dstEle) {
        return measuresMap.get(new ElementMapping(srcEle, dstEle));
    }

    public Set<ProgramElement> getSrcStmtsToMap() {
        return candidateSearcher.getSrcStmtsToMap();
    }

    public Set<ProgramElement> getSrcTokensToMap() {
        return candidateSearcher.getSrcTokensToMap();
    }

    public Set<ProgramElement> getAllSrcStmts() {
        return candidateSearcher.getAllSrcStmts();
    }

    /**
     * Find the element pairs to map
     * @param elementsToMap data structure to store element pairs to map
     * @param excludeCurMappings whether to exclude current mappings
     */
    public void findElementPairsToMap(Map<ProgramElement, ProgramElement> elementsToMap,
                                      Set<ProgramElement> allSrcElements,
                                      boolean excludeCurMappings) {
        // First find the best target candidates for all the source elements
        // 1. consider all the target candidates
        // 2. consider candidates excluding current mappings
        for (ProgramElement srcElement: allSrcElements)
            findBestDstCandidates(srcElement);
        // Then, we find the one-to-one and multi-to-multi best mappings of elements.
        if (!excludeCurMappings) {
            Map<ProgramElement, Set<ProgramElement>> myLocal = new HashMap<>();
            findOneToOneBestElementPairsToMap(elementsToMap, srcToGlobalBestDstCandidateMap, myLocal);
        } else {
            // Find one-to-one best mappings and exclude current mappings
            findOneToOneBestElementPairsToMap(elementsToMap, srcToLocalBestDstCandidateMap, dstToLocalBestSrcCandidateMap);

            if (elementsToMap.size() > 0)
                return;

            // Find multiple-to-multiple mappings and exclude current mappings
            findBestMultiMappings(elementsToMap);
        }
    }

    /**
     * Find one-to-one mappings excluding current mappings
     * @param elementsToMap element pairs to map
     * @param allSrcElements all the source elements
     */
    public void findOneToOneElementPairsToMap(Map<ProgramElement, ProgramElement> elementsToMap,
                                              Set<ProgramElement> allSrcElements) {
        for (ProgramElement srcElement: allSrcElements)
            findBestDstCandidates(srcElement);
        findOneToOneBestElementPairsToMap(elementsToMap, srcToLocalBestDstCandidateMap, dstToLocalBestSrcCandidateMap);
    }

    private void findBestDstCandidates(ProgramElement srcElement) {
        Set<ProgramElement> candidateElements = candidateSearcher.getDstCandidateElements(srcElement);
        if (candidateElements == null || candidateElements.size() == 0)
            return;
        BestCandidateSearcher searcher = new BestCandidateSearcher(srcElement, candidateElements,
                eleMappings, determiner, candidateSearcher.getCandidateSetsAndMaps());
        Set<ProgramElement> globalBestDstCandidates = searcher.getBestGlobalCandidates();
        if (!eleMappings.isMapped(srcElement)) {
            Set<ProgramElement> localBestDstCandidates = searcher.getBestLocalCandidates();
            if (localBestDstCandidates != null)
                srcToLocalBestDstCandidateMap.put(srcElement, localBestDstCandidates);
        }
        srcToGlobalBestDstCandidateMap.put(srcElement, globalBestDstCandidates);
        measuresMap.putAll(searcher.getBestSimMeasuresMap());
    }

    private void findOneToOneBestElementPairsToMap(Map<ProgramElement, ProgramElement> elementsToMap,
                                                  Map<ProgramElement, Set<ProgramElement>> srcToBestDstCandidateMap,
                                                  Map<ProgramElement, Set<ProgramElement>> dstToBestSrcCandidateMap) {
        Map<ProgramElement, Set<ProgramElement>> dstToGlobalSrcCandidateMap = new HashMap<>();
        for (ProgramElement srcElement: srcToBestDstCandidateMap.keySet()) {
            Set<ProgramElement> dstElements = srcToBestDstCandidateMap.get(srcElement);
            for (ProgramElement dstElement: dstElements) {
                if (!dstToGlobalSrcCandidateMap.containsKey(dstElement))
                    dstToGlobalSrcCandidateMap.put(dstElement, new HashSet<>());
                dstToGlobalSrcCandidateMap.get(dstElement).add(srcElement);
            }
        }
        for (ProgramElement dstElement: dstToGlobalSrcCandidateMap.keySet()) {
            if (dstToGlobalSrcCandidateMap.get(dstElement).size() == 1) {
                ProgramElement srcElement = dstToGlobalSrcCandidateMap.get(dstElement).iterator().next();
                if (srcToBestDstCandidateMap.get(srcElement).size() == 1) {
                    if (eleMappings.getMappedElement(srcElement) != dstElement)
                        elementsToMap.put(srcElement, dstElement);
                    else {
                        Set<ProgramElement> candidates = new HashSet<>();
                        candidates.add(srcElement);
                        dstToBestSrcCandidateMap.put(dstElement, candidates);
                    }
                    continue;
                }
            }

            Set<ProgramElement> candidates = dstToGlobalSrcCandidateMap.get(dstElement);
            BestCandidateSearcher searcher = new BestCandidateSearcher(dstElement, candidates,
                    eleMappings, measuresMap, determiner, candidateSearcher.getCandidateSetsAndMaps());
            Set<ProgramElement> bestSrcCandidates = searcher.getBestGlobalCandidates();
            if (bestSrcCandidates != null) {
                dstToBestSrcCandidateMap.put(dstElement, bestSrcCandidates);
                if (bestSrcCandidates.size() == 1) {
                    ProgramElement srcElement = bestSrcCandidates.iterator().next();
                    if (srcToBestDstCandidateMap.containsKey(srcElement) && srcToBestDstCandidateMap.get(srcElement).size() == 1) {
                        if (eleMappings.getMappedElement(srcElement) != dstElement)
                            elementsToMap.put(srcElement, dstElement);
                    }
                }
            }
        }
    }

    /**
     * Mapping the multi-to-multi cases in lcs order.
     */
    private void findBestMultiMappings(Map<ProgramElement, ProgramElement> elementsToMap) {
        Set<ProgramElement> srcElements = new HashSet<>();
        Set<ProgramElement> dstElements = new HashSet<>();
        Set<ElementMapping> goodMappings = new HashSet<>();

        for (ProgramElement srcElement: srcToLocalBestDstCandidateMap.keySet()) {
            Set<ProgramElement> bestDstElements = srcToLocalBestDstCandidateMap.get(srcElement);
            for (ProgramElement dstElement: bestDstElements) {
                Set<ProgramElement> bestSrcElements = dstToLocalBestSrcCandidateMap.get(dstElement);
                if (bestSrcElements != null && bestSrcElements.contains(srcElement)) {
                    goodMappings.add(new ElementMapping(srcElement, dstElement));
                    srcElements.add(srcElement);
                    dstElements.add(dstElement);
                }
            }
        }

        if (goodMappings.size() == 0)
            return;

        List<ProgramElement> srcElementList = new ArrayList<>(srcElements);
        List<ProgramElement> dstElementList = new ArrayList<>(dstElements);
        srcElementList.sort(Comparator.comparingInt(ele -> ele.getITreeNode().getPos()));
        dstElementList.sort(Comparator.comparingInt(ele -> ele.getITreeNode().getPos()));

        getLcsEleToMap(srcElementList, dstElementList, goodMappings, elementsToMap);
    }

    /**
     * Process the multiple-to-multiple case with lcs.
     *
     * @param srcElements elements of source file in a pre-order
     * @param dstElements elements of target file in a pre-order
     * @param elementPair best mapped element pairs
     * @param elementsToMap  the element pairs that are calculated to be mapped
     */
    private static void getLcsEleToMap(List<ProgramElement> srcElements,
                                       List<ProgramElement> dstElements,
                                       Set<ElementMapping> elementPair,
                                       Map<ProgramElement, ProgramElement> elementsToMap){
        LongestCommonSubsequence<ProgramElement> lcs = new LongestCommonSubsequence<ProgramElement>(srcElements, dstElements) {
            @Override
            public boolean isEqual(ProgramElement t1, ProgramElement t2) {
                return elementPair.contains(new ElementMapping(t1, t2));
            }
        };
        List<int[]> idxes = lcs.extractIdxes();
        for (int[] idxPair: idxes){
            ProgramElement srcEle = srcElements.get(idxPair[0]);
            ProgramElement dstEle = dstElements.get(idxPair[1]);
//            System.out.println("LCS Ele To Map is " + srcEle + " " + dstEle);
            elementsToMap.put(srcEle, dstEle);
        }
    }
}
