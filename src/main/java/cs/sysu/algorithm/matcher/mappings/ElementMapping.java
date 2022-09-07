package cs.sysu.algorithm.matcher.mappings;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.utils.Pair;

/**
 * A mapping of two program elements between source and target files.
 *
 * Field:
 * first is an element from the source file;
 * second is an element from the target file.
 */
public class ElementMapping extends Pair<ProgramElement, ProgramElement> {

    public ElementMapping(ProgramElement a, ProgramElement b) {
        super(a, b);
    }

    public ProgramElement getSrcEle() {
        return first;
    }

    public ProgramElement getDstEle() {
        return second;
    }
}
