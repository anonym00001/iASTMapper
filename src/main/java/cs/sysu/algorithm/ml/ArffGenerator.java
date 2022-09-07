package cs.sysu.algorithm.ml;

import org.apache.commons.io.FileUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator class for weka arff files
 */
public class ArffGenerator {
    private ArrayList<Attribute> attributes;
    private Instances instances;
    private Map<String, Attribute> attrNameInfoMap;

    public ArffGenerator(String arffName, List<AttrMetaInfo> attrInfos){
        this.attributes = new ArrayList<>();
        attrNameInfoMap = new HashMap<>();
        for (AttrMetaInfo attrInfo: attrInfos) {
            this.attributes.add(attrInfo.getAttr());
            attrNameInfoMap.put(attrInfo.getAttrName(), attrInfo.getAttr());
        }
        this.instances = new Instances(arffName, attributes, 0);
    }

    private Attribute getAttribute(String attrName){
        return attrNameInfoMap.get(attrName);
    }

    public void addInstance(List<AttrValue> attrValues){
        Instance newInst = new DenseInstance(attrValues.size());
        for (AttrValue value: attrValues) {
            Attribute attr = getAttribute(value.getAttrName());
            if (attr.isNumeric())
                newInst.setValue(attr, (double) value.getValue());
            else
                newInst.setValue(attr, (String) value.getValue());
        }
        this.instances.add(newInst);
    }

    public Instances getInstances() {
        return instances;
    }

    public void writeArff(String filePath) throws IOException {
        FileUtils.writeStringToFile(new File(filePath), this.instances.toString(), "UTF-8");
    }
}
