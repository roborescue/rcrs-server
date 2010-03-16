package maps.gml;

import org.dom4j.Document;

/**
   Interface for different types of GML map format.
*/
public interface MapFormat {
    /**
       Read a Document and return a GMLMap.
       @param doc The document to read.
       @return A new GMLMap.
    */
    GMLMap read(Document doc);

    /**
       Turn a GMLMap into an xml document.
       @param map The map to write.
       @return A new document.
    */
    Document write(GMLMap map);

    /**
       Find out if a document looks valid to this format.
       @param doc The document to check.
       @return True if this format can probably parse the document, false otherwise.
    */
    boolean looksValid(Document doc);
}
