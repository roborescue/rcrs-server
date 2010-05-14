package maps.legacy;

import static rescuecore2.misc.EncodingTools.readInt32LE;
import static rescuecore2.misc.EncodingTools.reallySkip;

import java.io.InputStream;
import java.io.IOException;

/**
   A legacy road.
*/
public class LegacyRoad extends LegacyObject {
    private int head;
    private int tail;
    private int length;
    private int width;

    @Override
    public void read(InputStream in) throws IOException {
        // CHECKSTYLE:OFF:MagicNumber
        // Skip size
        reallySkip(in, 4);
        id = readInt32LE(in);
        head = readInt32LE(in);
        tail = readInt32LE(in);
        length = readInt32LE(in);
        // Skip roadkind, cars/humans to head/tail - 5 * 4 bytes
        reallySkip(in, 20);
        width = readInt32LE(in);
        // Skip block, repaircost, median, lines to head/tail, width for walkers - 6 * 4 bytes
        reallySkip(in, 24);
        // CHECKSTYLE:ON:MagicNumber
    }

    /**
       Get the ID of the head node.
       @return The head node id.
    */
    public int getHead() {
        return head;
    }

    /**
       Get the ID of the tail node.
       @return The tail node id.
    */
    public int getTail() {
        return tail;
    }

    /**
       Get the length of this road in mm.
       @return The length.
    */
    public int getLength() {
        return length;
    }

    /**
       Get the width of this road in mm.
       @return The width.
    */
    public int getWidth() {
        return width;
    }
}