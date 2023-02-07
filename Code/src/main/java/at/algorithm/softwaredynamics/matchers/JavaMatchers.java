package at.algorithm.softwaredynamics.matchers;


import com.github.gumtreediff.matchers.CompositeMatchers.CompositeMatcher;
import com.github.gumtreediff.matchers.heuristic.gt.GreedySubtreeMatcher;
import com.github.gumtreediff.matchers.optimizations.IdenticalSubtreeMatcherThetaA;
import com.github.gumtreediff.tree.ITree;
import cs.model.algorithm.languageutils.typechecker.JavaNodeTypeChecker;
import cs.model.algorithm.languageutils.typechecker.StaticNodeTypeChecker;

/**
 * Created by thomas on 28.02.2017.
 */
public class JavaMatchers {

    private static JavaNodeTypeChecker typeChecker = StaticNodeTypeChecker.getConfigNodeTypeChecker();

    @Deprecated
    public static class IterativeJavaMatcher extends CompositeMatcher {
        public IterativeJavaMatcher() {
            super(
                    new IdenticalSubtreeMatcherThetaA(),
                    new IdenticalImportsMatcher(),
                    new JavaInnerTypeDeclarationMatcher(),
                    new JavaInnerEnumDeclarationMatcher(),
                    new JavaMethodMatcher(),
                    new JavaFieldDeclarationMatcher(),
                    new JavaClassDeclarationMatcher()
            );
        }
    }

    @Deprecated
    public static class IterativeJavaMatcher_V1 extends CompositeMatcher {
        public IterativeJavaMatcher_V1() {
            super(
                    new IdenticalSubtreeMatcherThetaA(),
                    new IdenticalImportsMatcher(),
                    new JavaInnerTypeDeclarationMatcher_V1(),
                    new JavaInnerEnumDeclarationMatcher(),
                    new JavaMethodMatcher_V1(),
                    new JavaFieldDeclarationMatcher(),
                    new JavaClassDeclarationMatcher()
            );
        }
    }

    public static class IterativeJavaMatcher_V2 extends CompositeMatcher {
        public IterativeJavaMatcher_V2() {
            super(
                    new IdenticalSubtreeMatcherThetaA(),
                    new IdenticalImportsMatcher(),
                    new JavaInnerTypeDeclarationMatcher_V2(),
                    new JavaInnerEnumDeclarationMatcher(),
                    new JavaMethodMatcher_V2(),
                    new JavaFieldDeclarationMatcher(),
                    new JavaClassDeclarationMatcher()
            );
        }
    }

    public static class LabelAwareClassicGumTree extends CompositeMatcher {
        public LabelAwareClassicGumTree() {
            super(
                    new IdenticalSubtreeMatcherThetaA(),
                    new GreedySubtreeMatcher(),
                    new LabelAwareBottomUpMatcher()
            );
        }
    }

    public static class PartialInnerMatcher extends CompositeMatcher {
        public PartialInnerMatcher() {
            super(
                    new IdenticalSubtreeMatcherThetaA(),
                    new LabelAwareBottomUpMatcher()
            );
        }
    }

    public static class JavaClassDeclarationMatcher extends PartialMatcher {
        public JavaClassDeclarationMatcher() {
            super(new PartialMatcherConfiguration(
                    t -> t.getMetrics().depth > 1 &&
                            (
                                    typeChecker.isFieldDec(t) || typeChecker.isMethodDec(t)
                                    || typeChecker.isTypeDec(t) || typeChecker.isEnumDec(t)
                            ),
                    PartialInnerMatcher.class
            ));
        }
    }

    public static class JavaFieldDeclarationMatcher extends PartialMatcher {
        public JavaFieldDeclarationMatcher() {
            super(new PartialMatcherConfiguration(
                    t -> !t.isRoot()
                            && typeChecker.isTypeDec(t.getParent())
                            && typeChecker.isFieldDec(t),
                    PartialInnerMatcher.class,
                    LabelAwareClassicGumTree.class,
                    typeChecker::isFieldDec
            ));
        }
    }

    @Deprecated
    public static class JavaMethodMatcher extends PartialMatcher {
        public JavaMethodMatcher() {
            super(new PartialMatcherConfiguration(
                    t ->    // sub nodes of types that are no methods
                            !t.isRoot()
                                    && typeChecker.isTypeDec(t.getParent())
                                    && !typeChecker.isMethodDec(t)
                                    // method bodies
                                    || !t.isRoot()
                                    && typeChecker.isMethodDec(t.getParent())
                                    && typeChecker.isBlock(t)
                    ,
                    LabelAwareClassicGumTree.class,
                    LabelAwareClassicGumTree.class,
                    typeChecker::isMethodDec
            ));
        }
    }

    @Deprecated
    public static class SignatureBasedJavaMethodMatcher extends PartialMatcher {
        public SignatureBasedJavaMethodMatcher() {
            super(new PartialMatcherConfiguration(
                    t ->    // sub nodes of types that are no methods
                            !t.isRoot()
                                    && typeChecker.isTypeDec(t.getParent())
                                    && !typeChecker.isMethodDec(t)
                                    // method bodies
                                    || !t.isRoot()
                                    && typeChecker.isMethodDec(t.getParent())
                                    && typeChecker.isBlock(t)
                    ,
                    LabelAwareClassicGumTree.class,
                    LabelAwareClassicGumTree.class,
                    typeChecker::isMethodDec
            ));
        }
    }

    public static class SignatureBasedJavaMethodMatcher_V1 extends PartialMatcher {
        public SignatureBasedJavaMethodMatcher_V1() {
            super(new PartialMatcherConfiguration(
                    t ->    // sub nodes of types that are no methods
                            !t.isRoot()
                                    && typeChecker.isTypeDec(t.getParent())
                                    && !typeChecker.isMethodDec(t)
                                    // modifiers in methods
                                    || !t.isRoot()
                                    && typeChecker.isModifier(t)
                                    && t.getParents().stream().anyMatch(typeChecker::isMethodDec)
                                    // method bodies
                                    || !t.isRoot()
                                    && typeChecker.isMethodDec(t.getParent())
                                    && typeChecker.isBlock(t)
                    ,
                    LabelAwareClassicGumTree.class,
                    LabelAwareClassicGumTree.class,
                    typeChecker::isMethodDec
            ));
        }
    }

    @Deprecated
    public static class JavaMethodMatcher_V1 extends CompositeMatcher {

        public JavaMethodMatcher_V1() {
            super(
                    new StructureBasedJavaMethodMatcher(),
                    new SignatureBasedJavaMethodMatcher()
            );
        }
    }

    public static class JavaMethodMatcher_V2 extends CompositeMatcher {
        public JavaMethodMatcher_V2() {
            super(
                    new SignatureBasedJavaMethodMatcher_V1(),
                    new StructureBasedJavaMethodMatcher()
            );
        }
    }

    public static class StructureBasedJavaMethodMatcher extends PartialMatcher {
        public StructureBasedJavaMethodMatcher() {
            super(new PartialMatcherConfiguration(
                    t ->    // sub nodes of types that are no methods
                            !t.isRoot()
                                    && typeChecker.isTypeDec(t.getParent())
                                    && !typeChecker.isMethodDec(t)
                    ,
                    LabelAwareClassicGumTree.class,
                    LabelAwareClassicGumTree.class,
                    typeChecker::isMethodDec
            ));
        }
    }

    @Deprecated
    public static class JavaInnerTypeDeclarationMatcher extends PartialMatcher {
        public JavaInnerTypeDeclarationMatcher() {
            super(new PartialMatcherConfiguration(
                    t -> !t.isRoot()
                            && typeChecker.isTypeDec(t.getParent())
                            && !typeChecker.isTypeDec(t),
                    PartialInnerMatcher.class,
                    IterativeJavaMatcher.class,
                    t -> typeChecker.isTypeDec(t)
                            && t.getParent() != null
                            && typeChecker.isTypeDec(t.getParent())
            ));
        }
    }

    @Deprecated
    public static class JavaInnerTypeDeclarationMatcher_V1 extends PartialMatcher {
        public JavaInnerTypeDeclarationMatcher_V1() {
            super(new PartialMatcherConfiguration(
                    t -> {
                        if (t.isRoot())
                            return false;

                        ITree grandParent = t.getParent().getParent(); // could be null

                        return grandParent != null
                                && typeChecker.isTypeDec(grandParent)
                                && grandParent.getParent() != null
                                && typeChecker.isTypeDec(grandParent.getParent())
                                ||
                                !typeChecker.isTypeDec(t)
                                        && (grandParent == null ||
                                        !typeChecker.isTypeDec(grandParent));
                    }
                    ,
                    PartialInnerMatcher.class,
                    IterativeJavaMatcher_V1.class,
                    t -> typeChecker.isTypeDec(t)
                            && t.getParent() != null && typeChecker.isTypeDec(t.getParent())
            ));
        }
    }

    public static class JavaInnerTypeDeclarationMatcher_V2 extends PartialMatcher {
        public JavaInnerTypeDeclarationMatcher_V2() {
            super(new PartialMatcherConfiguration(
                    t -> {
                        if (t.isRoot())
                            return false;

                        ITree grandParent = t.getParent().getParent(); // could be null

                        return grandParent != null
                                && typeChecker.isTypeDec(grandParent)
                                && grandParent.getParent() != null
                                && typeChecker.isTypeDec(grandParent.getParent())
                                ||
                                !typeChecker.isTypeDec(t)
                                        && (grandParent == null || !typeChecker.isTypeDec(grandParent));
                    }
                    ,
                    PartialInnerMatcher.class,
                    IterativeJavaMatcher_V2.class,
                    t -> typeChecker.isTypeDec(t)
                            && t.getParent() != null
                            && typeChecker.isTypeDec(t.getParent())
            ));
        }
    }

    public static class JavaInnerEnumDeclarationMatcher extends PartialMatcher {
        public JavaInnerEnumDeclarationMatcher() {
            super(new PartialMatcherConfiguration(
                    t -> !t.isRoot()
                            && typeChecker.isTypeDec(t.getParent())
                            && !typeChecker.isEnumDec(t),

                    PartialInnerMatcher.class,
                    LabelAwareClassicGumTree.class,
                    t -> typeChecker.isEnumDec(t)
                            && t.getParent() != null
                            && typeChecker.isTypeDec(t.getParent())
            ));
        }
    }
}
