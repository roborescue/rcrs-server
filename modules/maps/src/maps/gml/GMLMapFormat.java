package maps.gml;

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

import maps.MapFormat;
import maps.MapException;
import maps.Map;

import rescuecore2.log.Logger;

/**
   Abstract base class for map formats that use GML.
*/
public abstract class GMLMapFormat implements MapFormat {
    @Override
    public GMLMap read(File file) throws MapException {
        FileReader r;
        try {
            r = new FileReader(file);
        }
        catch (FileNotFoundException e) {
            throw new MapException(e);
        }
        try {
            return read(r);
        }
        catch (DocumentException e) {
            throw new MapException(e);
        }
        finally {
            try {
                r.close();
            }
            catch (IOException e) {
                Logger.warn("IOException while closing file reader", e);
            }
        }
    }

    @Override
    public void write(Map map, File file) throws MapException {
        if (map == null) {
            throw new IllegalArgumentException("Map must not be null");
        }
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (!(map instanceof GMLMap)) {
            throw new IllegalArgumentException("Map is not a GMLMap: " + map.getClass().getName());
        }
        Document doc = write((GMLMap)map);
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    if (!file.getParentFile().mkdirs()) {
                        throw new MapException("Couldn't create file " + file.getPath());
                    }
                }
                if (!file.createNewFile()) {
                    throw new MapException("Couldn't create file " + file.getPath());
                }
            }
            XMLWriter writer = new XMLWriter(new FileOutputStream(file), OutputFormat.createPrettyPrint());
            Element root = doc.getRootElement();
            for (java.util.Map.Entry<String, String> next : getNamespaces().entrySet()) {
                root.addNamespace(next.getKey(), next.getValue());
            }
            writer.write(doc);
            writer.flush();
            writer.close();
        }
        catch (IOException e) {
            throw new MapException(e);
        }
    }

    @Override
    public boolean canRead(File file) throws MapException {
        if (file.isDirectory() || !file.exists()) {
            return false;
        }
        if (!file.getName().endsWith(".gml")) {
            return false;
        }
        // Check that the XML dialect is correct by looking at the root element.
        FileReader r;
        try {
            r = new FileReader(file);
        }
        catch (FileNotFoundException e) {
            throw new MapException(e);
        }
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(r);
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                    return isCorrectRootElement(reader.getNamespaceURI(), reader.getLocalName());
                }
            }
        }
        catch (XMLStreamException e) {
            Logger.debug("Exception while reading XML stream", e);
            return false;
        }
        finally {
            try {
                r.close();
            }
            catch (IOException e) {
                Logger.warn("IOException while closing file reader", e);
            }
        }
        return false;
    }

    /**
       Read a GMLMap from a Reader.
       @param reader The Reader to read.
       @return A new GMLMap.
       @throws DocumentException If there is a problem parsing the XML.
       @throws MapException If there is a problem reading the map.
    */
    public GMLMap read(Reader reader) throws DocumentException, MapException {
        Logger.debug("Parsing GML");
        SAXReader saxReader = new SAXReader();
        Document doc = saxReader.read(reader);
        Logger.debug("Building map");
        return read(doc);
    }

    /**
       Find out if the root element is correct for this format type.
       @param uri The URI of the root element.
       @param localName The local name of the root element.
       @return True if the uri and localName are correct for this format's root element, false otherwise.
    */
    protected abstract boolean isCorrectRootElement(String uri, String localName);

    /**
       Read a Document and return a GMLMap.
       @param doc The document to read.
       @return A new GMLMap.
       @throws MapException If there is a problem reading the map.
    */
    protected abstract GMLMap read(Document doc) throws MapException;

    /**
       Turn a GMLMap into an xml document.
       @param map The map to write.
       @return A new document.
    */
    protected abstract Document write(GMLMap map);

    /**
       Get the uris and preferred prefixes for all namespaces this format cares about.
       @return A map from prefix to uri for all relevant namespaces.
    */
    protected abstract java.util.Map<String, String> getNamespaces();
}
