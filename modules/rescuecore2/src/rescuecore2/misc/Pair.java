package rescuecore2.misc;

/**
   Utility class for a 2-tuple, often known as a pair.
   @param <S> The type of the first element of the pair.
   @param <T> The type of the second element of the pair.
 */
public class Pair<S, T> {
    private S first;
    private T second;

    /**
       Construct a pair object.
       @param first The first element.
       @param second The second element.
     */
    public Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    /**
       Get the first element of this pair.
       @return The first element.
     */
    public S first() {
        return first;
    }

    /**
       Get the second element of this pair.
       @return The second element.
     */
    public T second() {
        return second;
    }

    @Override
    public String toString() {
        return "<" + first + ", " + second + ">";
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (o instanceof Pair) {
            Pair<S, T> p = (Pair<S, T>)o;
            if (this.first == null && p.first != null) {
                return false;
            }
            if (this.first != null && p.first == null) {
                return false;
            }
            if (this.second == null && p.second != null) {
                return false;
            }
            if (this.second != null && p.second == null) {
                return false;
            }
            if (this.first != null && p.first != null && !this.first.equals(p.first)) {
                return false;
            }
            if (this.second != null && p.second != null && !this.second.equals(p.second)) {
                return false;
            }
            return true;
        }
        return false;
    }
}
