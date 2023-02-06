package cs.model.algorithm.utils;

/**
 * Exception thrown when running GumTree, MTDiff or IJM
 */
public class GumTreeException extends RuntimeException {
    public GumTreeException(String info){
        super("gumtree ast comparison error:" + info);
    }
}
