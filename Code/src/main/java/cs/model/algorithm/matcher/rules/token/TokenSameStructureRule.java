package cs.model.algorithm.matcher.rules.token;

import com.github.gumtreediff.tree.ITree;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.TokenElement;
import cs.model.algorithm.languageutils.typechecker.JavaNodeTypeChecker;
import cs.model.algorithm.languageutils.typechecker.StaticNodeTypeChecker;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.algorithm.matcher.measures.SimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasureNames;
import cs.model.algorithm.matcher.measures.token.TokenStructureMeasure;
import cs.model.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.model.algorithm.matcher.rules.ElementMatchRule;

import java.util.Map;

/**
 * Mapping rule for token.
 *
 * Tokens in the same multi-token structures can be mapped.
 */
public class TokenSameStructureRule extends AbstractElementMatchRule implements ElementMatchRule {

    private static final JavaNodeTypeChecker typeChecker = StaticNodeTypeChecker.getConfigNodeTypeChecker();
    public  TokenSameStructureRule(){
        super();
    }

    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        SimMeasure measure1 = measures.getSimMeasure(SimMeasureNames.STRUCT, eleMappings);
        if (measure1.getValue() > 1.0) {
            ProgramElement srcElement = measures.getSrcEle();
            ProgramElement dstElement = measures.getDstEle();

            ProgramElement srcParentEle = srcElement.getStmtElement();
            ProgramElement dstParentEle = dstElement.getStmtElement();
            if (srcParentEle.isDeclaration() && dstParentEle.isDeclaration() &&
                    eleMappings.getMappedElement(srcParentEle) != dstParentEle)
                return false;

            ITree srcNode = ((TokenStructureMeasure) measure1).getSrcMultiTokenNode();
            ITree dstNode = ((TokenStructureMeasure) measure1).getDstMultiTokenNode();
            if (srcNode != null && dstNode != null) {
                // Fix issue #20, tokens in parameters of different method declarations
                // cannot be determined to be mapped.
                if (typeChecker.isDescendantOfSingleVariableDeclaration(srcNode))
                    return false;
                if (typeChecker.isDescendantOfSingleVariableDeclaration(dstNode))
                    return false;
                // Fix issue #42, do not consider that type can be mapped between different statements
                if (typeChecker.isType(srcNode))
                    return false;
            }
            return true;
        }
        return false;
    }
}
