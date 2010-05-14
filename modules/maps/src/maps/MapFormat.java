package maps;

import java.io.File;

/**
   Interface for different types of map format.
*/
public interface MapFormat {
    /**
       Read a File and return a Map.
       @param file The file to read.
       @return A new Map.
       @throws MapException If there is a problem reading the map.
    */
    Map read(File file) throws MapException;

    /**
       Write a map to a file.
       @param map The map to write.
       @param file The file to write to.
       @throws MapException If there is a problem writing the map.
    */
    void write(Map map, File file) throws MapException;

    /**
       Find out if a file looks valid to this format.
       @param file The file to check.
       @return True if this format can probably read the file, false otherwise.
       @throws MapException If there is a problem reading the file.
    */
    boolean canRead(File file) throws MapException;
}
