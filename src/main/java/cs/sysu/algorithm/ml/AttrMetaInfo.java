package cs.sysu.algorithm.ml;

import weka.core.Attribute;

import java.util.List;

/**
 * AttributeMetaInfo is used to create attribute for a specified type and name
 */
public class AttrMetaInfo {

    private String attrName;
    private int attrType;
    private List<String> nominalValues;
    private Attribute attr;

    /**
     * Construction for numeric or string value.
     */
    public AttrMetaInfo(String attrName, int attrType){
        this.attrName = attrName;
        this.attrType = attrType;
        this.attr = createAttr();
    }

    /**
     * Construction for nominal attribute
     */
    public AttrMetaInfo(String attrName, int attrType, List<String> nominalValues) {
        this.attrName = attrName;
        this.attrType = attrType;
        this.nominalValues = nominalValues;

        if (attrType != Attribute.NOMINAL)
            throw new RuntimeException("Unmatched attribute type: non-nominal attributes cannot have nominal values");

        this.attr = createAttr();
    }

    private Attribute createAttr(){
        switch (attrType) {
            case Attribute.NUMERIC:
                return new Attribute(attrName);
            case Attribute.NOMINAL:
                return new Attribute(attrName, nominalValues);
            case Attribute.STRING:
                return new Attribute(attrName, true);
            default:
                return null;
        }
    }

    public Attribute getAttr() {
        return attr;
    }

    public String getAttrName() {
        return attrName;
    }

    public boolean isNumeric(){
        return attrType == Attribute.NUMERIC;
    }

    public boolean isNominal(){
        return attrType == Attribute.NOMINAL;
    }

    public boolean isString() {
        return attrType == Attribute.STRING;
    }
}
