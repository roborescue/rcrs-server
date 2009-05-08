package rescuecore2.misc;

/**
   Utility class for a 2-tuple, often known as a pair.
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
}