package cs.sysu.algorithm.matcher.measures;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.matcher.mappings.ElementMapping;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.innerstmt.*;
import cs.sysu.algorithm.matcher.measures.stmt.*;
import cs.sysu.algorithm.matcher.measures.stmt.special.ReturnAndThrowStmtSimMeasure;
import cs.sysu.algorithm.matcher.measures.stmt.textual.StmtIdenticalTokenMeasure;
import cs.sysu.algorithm.matcher.measures.stmt.textual.StmtNgramDiceMeasure;
import cs.sysu.algorithm.matcher.measures.stmt.textual.StmtTokenDiceMeasure;
import cs.sysu.algorithm.matcher.measures.token.*;
import cs.sysu.algorithm.matcher.measures.util.ElementAncestorMeasure;
import cs.sysu.algorithm.matcher.measures.util.ElementTypeMeasure;
import cs.sysu.algorithm.ml.AttrValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * APIs for calculating and using SimMeasures for
 * statements, tokens and inner-stmt elements.
 */
public class ElementSimMeasures {
    // source element
    private final ProgramElement srcEle;

    // target element
    private final ProgramElement dstEle;

    // This map stores the measure values.
    // The key is measure name and value is SimMeasure object
    private final Map<String, SimMeasure> measureMap;

    public ElementSimMeasures(ProgramElement srcE, ProgramElement dstE){
        this.srcEle = srcE;
        this.dstEle = dstE;
        this.measureMap = new HashMap<>();
        initMeasure();
    }

    private void initMeasure(){
        SimMeasure measure = new ElementTypeMeasure();
        measure.calSimMeasure(srcEle, dstEle);
        measureMap.put(SimMeasureNames.E_TYPE, measure);
    }

    private int isSameStringValueForStmt() {
        if (srcEle.isStmt()) {
            String srcValue = srcEle.getStringValue();
            String dstValue = dstEle.getStringValue();
            if ("".equals(srcValue) || "".equals(dstValue))
                return 0;
            if (srcValue.equals(dstValue))
                return 1;
        }
        return 0;
    }

    public ElementMapping getElementMapping(){
//        System.out.println("SrcEle " + srcEle + " DstEle " + dstEle);
        return new ElementMapping(srcEle, dstEle);
    }

    private void addNewMeasure(String measureName, ElementMappings eleMappings){
        SimMeasure measure = calSimMeasure(measureName, eleMappings);
        measureMap.put(measureName, measure);
    }

    public ProgramElement getSrcEle() {
        return srcEle;
    }

    public ProgramElement getDstEle() {
        return dstEle;
    }

    public ProgramElement getAnotherElement(ProgramElement element) {
        if (element.isFromSrc())
            return getDstEle();
        else
            return getSrcEle();
    }

    /**
     * Get the similarity measure given a measure name
     * If measure is not calculated, this method first calculates the measure.
     *
     * @param measureName the wanted measure name
     * @param eleMappings currently calculated element mappings
     * @return the SimMeasure object
     */
    public SimMeasure getSimMeasure(String measureName, ElementMappings eleMappings) {
        if (measureMap.containsKey(measureName)) {
            return measureMap.get(measureName);
        } else {
            addNewMeasure(measureName, eleMappings);
            return measureMap.get(measureName);
        }
    }

    private void calMeasureValue(SimMeasure measure, ElementMappings eleMappings){
        measure.setElementMappings(eleMappings);
        measure.calSimMeasure(srcEle, dstEle);
    }

    private SimMeasure calSimMeasure(String measureName, ElementMappings eleMappings){
        SimMeasure measure = getSimMeasureObjByName(measureName);
        calMeasureValue(measure, eleMappings);
        return measure;
    }

    public static SimMeasure getSimMeasureObjByName(String measureName) {
        SimMeasure measure;
        switch(measureName){
            case SimMeasureNames.E_TYPE:
                measure = new ElementTypeMeasure();
                break;
            case SimMeasureNames.SAME_STMT:
                measure = new IdenticalStmtMeasure();
                break;
            case SimMeasureNames.SAME_METHOD_BODY:
                measure = new IdenticalMethodBodyMeasure();
                break;
            case SimMeasureNames.PM:
                measure = new StmtParentMappingMeasure();
                break;
            case SimMeasureNames.NAME:
                measure = new StmtSameNameMeasure();
                break;
            case SimMeasureNames.NIT:
                measure = new StmtIdenticalTokenMeasure();
                break;
            case SimMeasureNames.JAVADOC:
                measure = new StmtJavadocMeasure();
                break;
            case SimMeasureNames.EXCHANGE:
                measure = new StmtExchangeMeasure();
                break;
            case SimMeasureNames.TYPE:
                measure = new TokenTypeMeasure();
                break;
            case SimMeasureNames.STMT:
                measure = new TokenStmtMeasure();
                break;
            case SimMeasureNames.SAME_VALUE_RENAME:
                measure = new TokenSameRenameValueMeasure();
                break;
            case SimMeasureNames.TOKEN_NEIGHBOR:
                measure = new TokenNeighborMeasure();
                break;
            case SimMeasureNames.TOKEN_LRB:
                measure = new Token_LRBMeasure();
                break;
            case SimMeasureNames.INNERSTMT:
                measure = new INNERSTMTMeasure();
                break;
            case SimMeasureNames.STRUCT:
                measure = new TokenStructureMeasure();
                break;
            case SimMeasureNames.ANCESTOR:
                measure = new ElementAncestorMeasure();
                break;
            case SimMeasureNames.TOKEN_SANDWICH:
                measure = new TokenSandwichMeasure();
                break;
            case SimMeasureNames.STMT_SANDWICH:
                measure = new StmtSandwichMeasure();
                break;
            case SimMeasureNames.MS:
                measure = new MethodSignatureMeasure();
                break;
            case SimMeasureNames.DICE:
                measure = new StmtTokenDiceMeasure();
                break;
            case SimMeasureNames.DM:
                measure = new StmtDescendantMappingMeasure();
                break;
            case SimMeasureNames.NGRAM:
                measure = new StmtNgramDiceMeasure();
                break;
            case SimMeasureNames.INNER_STMT_ELE_NAME:
                measure = new InnerStmtEleNameMappingMeasure();
                break;
            case SimMeasureNames.INNER_STMT_ELE_DICE:
                measure = new InnerStmtEleTokenDiceMeasure();
                break;
            case SimMeasureNames.INNER_STMT_SAME_STMT:
                measure = new INNERSTMTSAMESTMTMeasure();
                break;
            case SimMeasureNames.INNER_STMT_ELE_SANDWICH:
                measure = new InnerStmtEleSandwichMeasure();
                break;
            case SimMeasureNames.METHOD_INVOCATION_SAME_NAME:
                measure = new MethodInvocationSameNameMeasure();
                break;
            case SimMeasureNames.TOKEN_SCOPE:
                measure = new TokenScopeMeasure();
                break;
            case SimMeasureNames.TOKEN_NGRAM:
                measure = new TokenNgramMeasure();
                break;
            case SimMeasureNames.RETURN_STMT:
                measure = new ReturnAndThrowStmtSimMeasure();
                break;
            case SimMeasureNames.TOKEN_RENAME:
                measure = new TokenRenameMeasure();
                break;
            case SimMeasureNames.STMT_NAME_TOKEN:
                measure = new StmtNameTokenMeasure();
                break;
            default:
                throw new RuntimeException("do not support such measure now.");
        }
        return measure;
    }

    @Override
    public String toString() {
        String tmp = "" + srcEle + " => " + dstEle + " [";
        for (String measureName: measureMap.keySet()){
            tmp += measureName + ":" + measureMap.get(measureName).getValue();
            tmp += " ";
        }
        tmp += "]";
        return tmp;
    }

    /**
     * calculate all the similarity measures when building dataset
     */
    private void calAllSimMeasures(ElementMappings eleMappings){
        if (srcEle.isStmt()) {
            for (String measureName: SimMeasureConfiguration.STMT_MEASURE_CONFIGURATION)
                addNewMeasure(measureName, eleMappings);
        } else {
            for (String measureName: SimMeasureConfiguration.TOKEN_MEASURE_CONFIGURATION)
                addNewMeasure(measureName, eleMappings);
        }
    }

    /**
     * get feature vector for training and testing dataset.
     */
    public List<AttrValue> toMeasureVector(ElementMappings elementMappings){
        List<AttrValue> ret = new ArrayList<>();
        String[] measures = srcEle.isStmt() ?
                SimMeasureConfiguration.STMT_MEASURE_CONFIGURATION :
                SimMeasureConfiguration.TOKEN_MEASURE_CONFIGURATION;
        for (String measureName: measures) {
            double value = getSimMeasure(measureName, elementMappings).getValue();
            AttrValue attrValue = new AttrValue(measureName, value);
            ret.add(attrValue);
        }
        return ret;
    }

    /**
     * Compare between two measures given a measure name
     * @param measures1 first measures1
     * @param measures2 second measures
     * @param eleMappings current element mappings
     * @param measureName measure name
     * @return measures1 > measures2 ? 1 : 0
     */
    public static int doCompare(ElementSimMeasures measures1, ElementSimMeasures measures2,
                                 ElementMappings eleMappings, String measureName) {
        // If two program elements have same string value,
        // it is not necessary to use NAME, NIT, DICE and NGRAM to compare the measure
        // .. What do those 3 do here ?
        if (measures1.isSameStringValueForStmt() == 1 && measures2.isSameStringValueForStmt() == 1){
            if (measureName.equals(SimMeasureNames.NIT))
                return 0;
            if (measureName.equals(SimMeasureNames.DICE))
                return 0;
            if (measureName.equals(SimMeasureNames.NGRAM))
                return 0;
        }

        SimMeasure measure1 = measures1.getSimMeasure(measureName, eleMappings);
        SimMeasure measure2 = measures2.getSimMeasure(measureName, eleMappings);
//        System.out.println("Mea1 " + measure1.getValue() + " Mea2 " + measure2.getValue());
        return measure1.compare(measure2);
    }

    public static int doCompareWithConfigMeasures(ElementSimMeasures measures1, ElementSimMeasures measures2,
                                                  ElementMappings elementMappings, String[] measures) {
        for (String measureName: measures) {
            int cmp = doCompare(measures1, measures2, elementMappings, measureName);
            if (cmp != 0)
                return cmp;
        }
        return 0;
    }
}
