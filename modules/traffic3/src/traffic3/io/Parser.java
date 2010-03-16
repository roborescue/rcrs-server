package traffic3.io;

import java.io.InputStream;
import java.io.OutputStream;
import traffic3.manager.WorldManager;
import org.util.xml.parse.XMLParseException;
import java.io.IOException;
import traffic3.manager.WorldManagerException;

/**
 *
 */
public interface Parser {

    /**
     * get version of this parser.
     * @return version
     */
    String getVersion();

    /**
     * get description of this parser.
     * @return description
     */
    String getDescription();

    /**
     * return whether this parser can input this stream.
     * @param in input
     * @return isSupported
     */
    boolean isSupported(InputStream in);

    /**
     * input from inputstream for worldmanager.
     * @param worldManager world manager
     * @param in input
     * @throws UnsupportedOperatonException a parser can be unsupport this operation
     * @throws XMLParserException xml parser
     * @throws IOException io
     * @throws WorldManagerException WorldManager reject to append some object
     */
    void input(WorldManager worldManager, InputStream in) throws UnsupportedOperationException, XMLParseException, IOException, WorldManagerException;

    /**
     * output to outputstream for worldmanager.
     * @param worldManager world manager
     * @param out output
     * @throws UnsupportedOperatonException a parser can be unsupport this operation
     * @throws XMLParserException xml parser
     * @throws IOException io
     * @throws WorldManagerException WorldManager reject to append some object
     */
    void output(WorldManager worldManager, OutputStream out) throws UnsupportedOperationException, IOException;
}
