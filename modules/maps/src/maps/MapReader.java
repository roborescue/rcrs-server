package maps;

import maps.gml.formats.RobocupFormat;
import maps.gml.formats.OrdnanceSurveyFormat;
import maps.gml.formats.MeijoFormat;
import maps.gml.formats.GeospatialInformationAuthorityFormat;

import java.io.File;

import java.util.List;
import java.util.ArrayList;

import rescuecore2.log.Logger;

/**
   A utility class for reading maps.
*/
public final class MapReader {
    private static final List<MapFormat> ALL_FORMATS = new ArrayList<MapFormat>();

    static {
        ALL_FORMATS.add(RobocupFormat.INSTANCE);
        ALL_FORMATS.add(MeijoFormat.INSTANCE);
        ALL_FORMATS.add(OrdnanceSurveyFormat.INSTANCE);
        ALL_FORMATS.add(GeospatialInformationAuthorityFormat.INSTANCE);
    }

    private MapReader() {
    }

    /**
       Read a Map from a file and guess the format.
       @param file The name of the file to read.
       @return A Map.
       @throws MapException If there is a problem reading the map.
    */
    public static Map readMap(String file) throws MapException {
        return readMap(file, null);
    }

    /**
       Read a Map from a file using a particular format.
       @param file The name of the file to read.
       @param format The format to use. If this is null then the format will be guessed.
       @return A Map.
       @throws MapException If there is a problem reading the map.
    */
    public static Map readMap(String file, MapFormat format) throws MapException {
        return readMap(new File(file), format);
    }

    /**
       Read a Map from a file and guess the format.
       @param file The file to read.
       @return A Map.
       @throws MapException If there is a problem reading the map.
    */
    public static Map readMap(File file) throws MapException {
        return readMap(file, null);
    }

    /**
       Read a Map from a file using a particular format.
       @param file The file to read.
       @param format The format to use. If this is null then the format will be guessed.
       @return A Map.
       @throws MapException If there is a problem reading the map.
    */
    public static Map readMap(File file, MapFormat format) throws MapException {
        if (format == null) {
            format = guessFormat(file);
        }
        if (format == null) {
            throw new MapException("Unrecognised format");
        }
        Logger.debug("Reading " + format.toString() + " format");
        return format.read(file);
    }

    /**
       Guess the format for a Map.
       @param file The file to guess the format of.
       @return The most likely format or null if the file type is unrecognised.
       @throws MapException If there is a problem reading the file.
    */
    public static MapFormat guessFormat(File file) throws MapException {
        Logger.debug("Guessing format");
        for (MapFormat next : ALL_FORMATS) {
            if (next.canRead(file)) {
                return next;
            }
        }
        return null;
    }
}
