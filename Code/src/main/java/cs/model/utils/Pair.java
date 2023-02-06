package cs.model.utils;

import java.io.Serializable;

public class Pair<T1, T2> implements Serializable {

    public final T1 first;

    public final T2 second;

    public Pair(T1 a, T2 b) {
        this.first = a;
        this.second = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;
        return first.equals(pair.first) && second.equals(pair.second);
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", first.toString(), second.toString());
    }

    @Override
    public int hashCode(){
        int hash = 17;
        hash = hash * 31 + first.hashCode();
        hash  = hash * 31 + second.hashCode();
        return hash;
    }
}