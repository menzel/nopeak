package profile;

public class Tuple<A,T>{

    private final A first;
    private final T second;
    public static final Tuple<Double, Double> EMPTY_DOUBLE_TUPLE;
    public static Tuple EMPTY_TUPLE;

    static {
        EMPTY_DOUBLE_TUPLE = new Tuple<>(null, null);
        EMPTY_TUPLE = new Tuple<>(null, null);
    }

    public Tuple(A first, T second){

        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }
}
