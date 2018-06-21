package coldash.easyreminders.util;

import java.util.List;

public class Zip {
    public interface PairFunction<X, Y> {
        boolean apply(X x, Y y);
    }

    public interface TripleFunction<X, Y, Z> {
        boolean apply(X x, Y y, Z z);
    }

    public static class Pair<X, Y> {
        List<X> first;
        List<Y> second;

        public Pair(List<X> first, List<Y> second) {
            this.first = first;
            this.second = second;
        }

        public void forEach(PairFunction<X, Y> fn){
            for(int i = 0; i < first.size(); i++)
                if(!fn.apply(first.get(i), second.get(i)))
                    break;
        }
    }

    public static class Triple<X, Y, Z> {
        List<X> first;
        List<Y> second;
        List<Z> third;

        public Triple(List<X> first, List<Y> second, List<Z> third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public void forEach(TripleFunction<X, Y, Z> fn){
            for(int i = 0; i < first.size(); i++)
                if(!fn.apply(first.get(i), second.get(i), third.get(i)))
                    break;
        }
    }

    public static <X, Y> Pair<X, Y> zip(List<X> x, List<Y> y){
        if(x.size() != y.size())
            throw new IllegalArgumentException(String.format("Sizes must match. Got %d and %d",
                                                                x.size(), y.size()));

        return new Pair<>(x, y);
    }

    public static <X, Y, Z> Triple<X, Y, Z> zip(List<X> x, List<Y> y, List<Z> z){
        if(x.size() != y.size() || x.size() != z.size())
            throw new IllegalArgumentException(String.format("Sizes must match. Got %d, %d and %d",
                    x.size(), y.size(), z.size()));

        return new Triple<>(x, y, z);
    }
}
