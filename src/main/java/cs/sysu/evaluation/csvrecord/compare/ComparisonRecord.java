package cs.sysu.evaluation.csvrecord.compare;


import cs.sysu.algorithm.matcher.mappings.ElementMappings;

/**
 * Record to compare the mapping results of our method and baselines.
 * Base class of StmtComparisonRecord and TokenComparisonRecord.
 */
public abstract class ComparisonRecord {

    // whether the mapping is inconsistent with the results of gumtree
    protected boolean gtInconsistent;

    // whether the mapping is inconsistent with the results of mtdiff
    protected boolean mtdInconsistent;

    // whether the mapping is inconsistent with the results of ijm
    protected boolean ijmInconsistent;

    public ComparisonRecord(boolean gtInconsistent, boolean mtdInconsistent, boolean ijmInconsistent){
        this.gtInconsistent = gtInconsistent;
        this.mtdInconsistent = mtdInconsistent;
        this.ijmInconsistent = ijmInconsistent;
    }

    public abstract String getScript();

    public abstract String[] toRecord(ElementMappings eleMappings);
}
