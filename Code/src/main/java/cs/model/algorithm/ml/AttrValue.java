package cs.model.algorithm.ml;

import cs.model.utils.Pair;

/**
 * Attr value object: to store the value of each attribute
 */
public class AttrValue extends Pair<String, Object> {

    public AttrValue(String a, Object b) {
        super(a, b);
    }

    public String getAttrName() {
        return first;
    }

    public Object getValue() {
        return second;
    }
}
