package maps.legacy;

import static rescuecore2.misc.EncodingTools.readInt32LE;
import static rescuecore2.misc.EncodingTools.reallySkip;

import java.io.InputStream;
import java.io.IOException;

/**
   A legacy node.
*/
public class LegacyNode extends LegacyObject {
    private int[] edges;

    @Override
    public void read(InputStream in) throws IOException {
        // CHECKSTYLE:OFF:MagicNumber
        // Skip size
        reallySkip(in, 4);
        id = readInt32LE(in);
        x = readInt32LE(in);
        y = readInt32LE(in);
        int numEdges = readInt32LE(in);
        edges = new int[numEdges];
        for (int j = 0; j < numEdges; ++j) {
            edges[j] = readInt32LE(in);
        }
        // Skip signal flag, timing, pocket to turn across, shortcut to turn
        reallySkip(in, (numEdges * 6 + 1) * 4);
        // CHECKSTYLE:ON:MagicNumber
    }

    /**
       Get the list of edges, i.e. roads and buildings adjacent to this node.
       @return The edge list.
    */
    public int[] getEdges() {
        return edges;
    }
}