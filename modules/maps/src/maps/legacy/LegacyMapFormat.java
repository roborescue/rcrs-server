package maps.legacy;

import maps.Map;
import maps.MapFormat;
import maps.MapException;

import java.io.File;
import java.io.IOException;

/**
   MapFormat for legacy maps.
*/
public final class LegacyMapFormat implements MapFormat {
    /** Singleton instance. */
    public static final LegacyMapFormat INSTANCE = new LegacyMapFormat();

    private LegacyMapFormat() {}

    @Override
    public LegacyMap read(File file) throws MapException {
        try {
            return new LegacyMap(file);
        }
        catch (IOException e) {
            throw new MapException(e);
        }
    }

    @Override
    public void write(Map map, File file) throws MapException {
        throw new RuntimeException("LegacyMapFormat.write not implemented");
    }

    @Override
    public boolean canRead(File file) throws MapException {
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }
        // Look for road.bin, node.bin and building.bin files
        File road = new File(file, "road.bin");
        File node = new File(file, "node.bin");
        File building = new File(file, "building.bin");
        return road.exists() && node.exists() && building.exists();
    }
}
