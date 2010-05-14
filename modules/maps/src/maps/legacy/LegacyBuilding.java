package maps.legacy;

import static rescuecore2.misc.EncodingTools.readInt32LE;
import static rescuecore2.misc.EncodingTools.reallySkip;

import java.io.InputStream;
import java.io.IOException;

/**
   A legacy building.
*/
public class LegacyBuilding extends LegacyObject {
    private int floors;
    private int code;
    private int[] entrances;
    private int[] apexes;

    @Override
    public void read(InputStream in) throws IOException {
        // CHECKSTYLE:OFF:MagicNumber
        // Skip size
        reallySkip(in, 4);
        id = readInt32LE(in);
        x = readInt32LE(in);
        y = readInt32LE(in);
        floors = readInt32LE(in);
        // Skip attributes, ignition, fieryness, brokenness - 4 * 4 bytes
        reallySkip(in, 16);
        int numEntrances = readInt32LE(in);
        entrances = new int[numEntrances];
        for (int j = 0; j < numEntrances; ++j) {
            entrances[j] = readInt32LE(in);
        }
        // Skip shapeID, ground area, total read - 3 * 4 bytes
        reallySkip(in, 12);
        code = readInt32LE(in);
        int numApexes = readInt32LE(in);
        apexes = new int[numApexes * 2];
        for (int j = 0; j < numApexes; ++j) {
            // Apexes
            apexes[j * 2] = readInt32LE(in);
            apexes[j * 2 + 1] = readInt32LE(in);
        }
        // CHECKSTYLE:ON:MagicNumber
    }

    /**
       Get the number of floors in this building.
       @return The number of floors.
    */
    public int getFloors() {
        return floors;
    }

    /**
       Get the building code.
       @return The building code.
    */
    public int getCode() {
        return code;
    }

    /**
       Get the list of entrance nodes.
       @return The entrances.
    */
    public int[] getEntrances() {
        return entrances;
    }

    /**
       Get the list of apexes.
       @return The apex list.
    */
    public int[] getApexes() {
        return apexes;
    }
}