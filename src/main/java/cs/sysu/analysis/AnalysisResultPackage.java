package cs.sysu.analysis;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.MappingStore;
import cs.sysu.algorithm.actions.TreeEditAction;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.utils.GumTreeUtil;
import cs.sysu.algorithm.utils.RangeCalculator;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResultPackage {
    private ElementMappings eleMappings;
    private MappingStore ms;
    private List<TreeEditAction> treeEditActions;
    private long actionGenerationTime;

    public AnalysisResultPackage(ElementMappings eleMappings, MappingStore ms,
                                 RangeCalculator srcRc, RangeCalculator dstRc) {
        this.eleMappings = eleMappings;
        this.ms = ms;
        long time1 = System.currentTimeMillis();
        List<Action> actionList = GumTreeUtil.getEditActions(ms);
        long time2 = System.currentTimeMillis();
        actionGenerationTime = time2 - time1;
        treeEditActions = new ArrayList<>();

        if (actionList != null) {
            for (Action a: actionList) {
                TreeEditAction ea = new TreeEditAction(a, ms, srcRc, dstRc);
                if (ea.isJavadocRelated())
                    continue;
                treeEditActions.add(ea);
            }
        }
    }

    public ElementMappings getEleMappings() {
        return eleMappings;
    }

    public MappingStore getMs() {
        return ms;
    }

    public List<TreeEditAction> getTreeEditActions() {
        return treeEditActions;
    }

    public long getActionGenerationTime() {
        return actionGenerationTime;
    }
}
