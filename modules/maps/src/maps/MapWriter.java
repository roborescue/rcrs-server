package maps;

import java.io.File;

/**
   A class for writing maps.
*/
public final class MapWriter {
    private MapWriter() {
    }

    /**
       Write a map to a file.
       @param map The map to write.
       @param file The name of the file to write to.
       @param format The MapFormat to write.
       @throws MapException If there is a problem writing the map.
    */
    public static void writeMap(Map map, String file, MapFormat format) throws MapException {
        writeMap(map, new File(file), format);
    }

    /**
       Write a Map to a file.
       @param map The map to write.
       @param file The file to write to.
       @param format The MapFormat to write.
       @throws MapException If there is a problem writing the map.
    */
    public static void writeMap(Map map, File file, MapFormat format) throws MapException {
        format.write(map, file);
    }
}
