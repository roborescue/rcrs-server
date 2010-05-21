package rescuecore2.misc;

import java.io.InputStream;
import java.io.IOException;

/**
   An InputStream that keeps track of how many bytes have been read.
*/
public class CountingInputStream extends InputStream {
    private int count;
    private InputStream downstream;

    /**
       Construct a CountingInputStream that reads from another stream.
       @param downstream The downstream InputStream to read from.
    */
    public CountingInputStream(InputStream downstream) {
        this.downstream = downstream;
        count = 0;
    }

    @Override
    public int read() throws IOException {
        int result = downstream.read();
        ++count;
        return result;
    }

    @Override
    public String toString() {
        return downstream + " at position " + count;
    }

    /**
       Get the number of bytes read so far.
       @return The number of bytes read.
    */
    public int getByteCount() {
        return count;
    }
}