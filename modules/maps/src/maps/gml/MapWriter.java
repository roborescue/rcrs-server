package maps.gml;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Map;

/**
   A class for writing GML maps.
*/
public final class MapWriter {
    private MapWriter() {
    }

    /**
       Write a GMLMap to a file.
       @param map The map to write.
       @param file The name of the file to write to.
       @param format The MapFormat to write.
       @throws IOException If there is a problem writing the map.
       @throws GMLException If there is a problem writing the map.
    */
    public static void writeGMLMap(GMLMap map, String file, MapFormat format) throws IOException, GMLException {
        writeGMLMap(map, new FileOutputStream(new File(file)), format);
    }

    /**
       Write a GMLMap to a file.
       @param map The map to write.
       @param file The file to write to.
       @param format The MapFormat to write.
       @throws IOException If there is a problem writing the map.
       @throws GMLException If there is a problem writing the map.
    */
    public static void writeGMLMap(GMLMap map, File file, MapFormat format) throws IOException, GMLException {
        writeGMLMap(map, new FileOutputStream(file), format);
    }

    /**
       Write a GMLMap to an OutputStream.
       @param map The map to write.
       @param stream The stream to write to.
       @param format The MapFormat to write.
       @throws IOException If there is a problem writing the map.
       @throws GMLException If there is a problem writing the map.
    */
    public static void writeGMLMap(GMLMap map, OutputStream stream, MapFormat format) throws IOException, GMLException {
        Document doc = format.write(map);
        XMLWriter writer = new XMLWriter(stream, OutputFormat.createPrettyPrint());
        Element root = doc.getRootElement();
        for (Map.Entry<String, String> next : format.getNamespaces().entrySet()) {
            root.addNamespace(next.getKey(), next.getValue());
        }
        writer.write(doc);
        writer.flush();
        writer.close();
    }
}
