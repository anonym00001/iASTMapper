package at.algorithm.softwaredynamics.matchers;

import com.github.gumtreediff.matchers.Matcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by thomas on 12.12.2016.
 */
public class MatcherFactory  {

    public Class<? extends Matcher> defaultMatcherType;

    public MatcherFactory(Class<? extends Matcher> defaultMatcherType) {
        this.defaultMatcherType = defaultMatcherType;
    }

    public Matcher createMatcher() {
        return createMatcher(defaultMatcherType);
    }

    public Matcher createMatcher(Class<? extends Matcher> type)  {
        try {
            Constructor constructor = type.getConstructor();
            return (Matcher) constructor.newInstance();
        } catch (
                InstantiationException
                        | IllegalAccessException
                        | InvocationTargetException
                        | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }
}
