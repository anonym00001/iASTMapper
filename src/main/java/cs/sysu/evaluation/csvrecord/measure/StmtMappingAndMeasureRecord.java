package cs.sysu.evaluation.csvrecord.measure;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasureConfiguration;
import cs.sysu.algorithm.ml.AttrMetaInfo;
import cs.sysu.algorithm.ml.AttrValue;
import weka.core.Attribute;

import java.util.*;

/**
 * Record representing mappings at statement level and the measure values for the statement.
 */
public class StmtMappingAndMeasureRecord {
    private String project;
    private String commitId;
    private String filePath;
    private String stmtType;
    private int srcStartPos;
    private int dstStartPos;
    private int srcStartLine;
    private int dstStartLine;
    private int srcTokenNum;
    private int dstTokenNum;
    private Map<String, Double> measureValueMap;

    private static String[] consideredMeasures = SimMeasureConfiguration.STMT_MEASURE_CONFIGURATION;


    public StmtMappingAndMeasureRecord(String project, String commitId, String filePath){
        this.project = project;
        this.commitId = commitId;
        this.filePath = filePath;
    }

    public StmtMappingAndMeasureRecord(String[] record) {
        this.project = record[0];
        this.commitId = record[1];
        this.filePath = record[2];
        this.stmtType = record[3];
        this.srcStartPos = Integer.parseInt(record[4]);
        this.dstStartPos = Integer.parseInt(record[5]);
        this.srcStartLine = Integer.parseInt(record[6]);
        this.dstStartLine = Integer.parseInt(record[7]);
        this.srcTokenNum = Integer.parseInt(record[8]);
        this.dstTokenNum = Integer.parseInt(record[9]);
        this.measureValueMap = new HashMap<>();
        int i = 0;
        for (String measureName: consideredMeasures) {
            measureValueMap.put(measureName, Double.parseDouble(record[10 + i]));
            i ++;
        }
    }

    public List<AttrValue> toAttrValue() {
        List<AttrValue> attrValues = new ArrayList<>();
        for (String measureName: consideredMeasures) {
            Double value = measureValueMap.get(measureName);
            AttrValue attrV = new AttrValue(measureName, value);
            attrValues.add(attrV);
        }

        AttrValue attrV = new AttrValue("accurate", "A");
        attrValues.add(attrV);

        return attrValues;
    }

    public double getMeasureValue(String measureName){
        return measureValueMap.get(measureName);
    }

    public boolean stmtHasNoToken(){
        return srcTokenNum + dstTokenNum == 0;
    }

    public void setStmtInfo(StmtElement srcEle, StmtElement dstEle) {
        this.stmtType = srcEle.getNodeType();
        this.srcStartPos = srcEle.getITreeNode().getPos();
        this.dstStartPos = dstEle.getITreeNode().getPos();
        this.srcStartLine = srcEle.getStartLine();
        this.dstStartLine = dstEle.getStartLine();
        this.srcTokenNum = srcEle.getNameAndLiteralNum();
        this.dstTokenNum = dstEle.getNameAndLiteralNum();
    }

    public void setMeasures(ProgramElement srcEle, ProgramElement dstEle, ElementMappings eleMappings) {
        this.measureValueMap = new HashMap<>();
        ElementSimMeasures measures = new ElementSimMeasures(srcEle, dstEle);
        for (String measureName: consideredMeasures) {
             double value = measures.getSimMeasure(measureName, eleMappings).getValue();
             this.measureValueMap.put(measureName, value);
        }
    }

    public String[] toRecord() {
        String[] record = {
                project, commitId, filePath, stmtType,
                Integer.toString(srcStartPos), Integer.toString(dstStartPos),
                Integer.toString(srcStartLine), Integer.toString(dstStartLine),
                Integer.toString(srcTokenNum), Integer.toString(dstTokenNum)
        };
        List<String> recordList = new ArrayList<>(Arrays.asList(record));
        for (String measureName: consideredMeasures) {
            recordList.add(Double.toString(measureValueMap.get(measureName)));
        }
        return recordList.toArray(new String[recordList.size()]);
    }

    public static String[] getHeaders() {
        String[] headers = {
                "project", "commitId", "filePath", "stmtType", "srcStartPos", "dstStartPos",
                "srcStartLine", "dstStartLine", "srcTokenNum", "dstTokenNum",
        };
        List<String> headerList = new ArrayList<>(Arrays.asList(headers));
        for (String measure: consideredMeasures)
            headerList.add(measure);
        return headerList.toArray(new String[headerList.size()]);
    }

    public static List<AttrMetaInfo> getAttrMetaInfos() {
        List<AttrMetaInfo> ret = new ArrayList<>();
        for (String measureName: consideredMeasures) {
            AttrMetaInfo info = new AttrMetaInfo(measureName, Attribute.NUMERIC);
            ret.add(info);
        }

        List<String> nominalValues = new ArrayList<>();
        nominalValues.add("A");
        nominalValues.add("I");
        AttrMetaInfo info = new AttrMetaInfo("accurate", Attribute.NOMINAL, nominalValues);
        ret.add(info);

        return ret;
    }

    public static List<List<AttrValue>> getAttrValuesFromCsv(List<String[]> records){
        List<List<AttrValue>> ret = new ArrayList<>();
        if (records != null) {
            for (String[] record : records) {
                StmtMappingAndMeasureRecord resultRecord = new StmtMappingAndMeasureRecord(record);
                ret.add(resultRecord.toAttrValue());
            }
        }
        return ret;
    }
}
