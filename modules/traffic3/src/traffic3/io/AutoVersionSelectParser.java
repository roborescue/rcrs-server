package traffic3.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JComponent;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerException;

import org.util.xml.parse.ElementParser;
import org.util.xml.parse.policy.ParserPolicy;
import org.util.xml.element.TagElement;
import org.util.xml.element.Element;
import org.util.xml.parse.XMLParseException;

/**
 *
 */
public class AutoVersionSelectParser {

    private Parser[] parserList;
    private boolean guiConfirm;
    private JComponent rootFrame;
    private Parser outputParser;

    /**
     * Constructor.
     * @param pl parsers
     * @param gc enable gui confirm
     */
    public AutoVersionSelectParser(Parser[] pl, boolean gc) {
        assert pl != null : "parser list is null.";
        parserList = Arrays.copyOf(pl, pl.length);
        guiConfirm = gc;
    }

    /*
    public Parser[] getSelectedParserList(File file) throws Exception {
    ArrayList<Parser> supported_parser_list = new ArrayList<Parser>();
    for(Parser parser : parserList) {
        InputStream input = new FileInputStream(file);
        if(parser.isSupported(input))
        supported_parser_list.add(parser);
    }
    return supported_parser_list.toArray(new Parser[0]);
    }
    */

    /**
     * get selected parser list.
     * this method list up supported parsers for the file
     * @param file file
     * @return supported parser list
     * @throws IOException io
     * @throws XMLParseException parse
     */
    public Parser[] getSelectedParserList(File file) throws IOException, XMLParseException {

        List<Parser> supportedParserList = new ArrayList<Parser>();
        InputStream input = new FileInputStream(file);
        try {
        ElementParser p = new ElementParser(input);

        final String[] version = new String[]{null};
        ParserPolicy policy = new ParserPolicy() {
                public Element allowElement(Element element) {
                    if (element.isTagElement()) {
                        TagElement tag = (TagElement)element;
                        if ("rcrs:version".equals(tag.getKey().toLowerCase())) {
                            version[0] = tag.getValue();
                        }
                        return null;
                    }
                    else {
                        return element;
                    }
                }
                public boolean checkEndTag() {
                    return true;
                }
                public boolean finished() {
                    return version[0] != null;
                }
                public boolean forceEmptyTag(String key) {
                    return false;
                }
                public ParserPolicy getInnerPolicy(Element element) {
                    return this;
                }
                public String selectEncoding(String ltk) {
                    return null;
                }
                public boolean throwExceptionIfDocumentHasError() {
                    return true;
                }
            };
        p.setPolicy(policy);
        p.parse();

        String v = version[0];
        for (Parser parser : parserList) {
            if (parser.getVersion().equals(v)) {
                supportedParserList.add(parser);
            }
        }
        }
        finally {
            input.close();
        }
        return supportedParserList.toArray(new Parser[0]);
    }

    /**
     * select parser for a file.
     * if there are multiple supported parser and gui confirming is available then this method show gui and user select with gui.
     * @param file file
     * @return parser selected parser
     * @throws Exception exception
     */
    public Parser selectParser(File file) throws FileNotFoundException, IOException, XMLParseException, ParserNotFoundException {
        Parser[] supportedParserList = getSelectedParserList(file);
        Parser selectedParser = null;
        if (supportedParserList.length == 0) {
            throw new ParserNotFoundException("file: " + file);
        }
        if (supportedParserList.length > 2 && guiConfirm) {
            String message = "";
            String title = "";
            int type = JOptionPane.INFORMATION_MESSAGE;
            Icon icon = null;
            Object[] choice = supportedParserList;
            Object selection = JOptionPane.showInputDialog(rootFrame, message, title, type, icon, choice, choice[0]);
            selectedParser = (Parser)selection;
        }
        else {
            selectedParser = supportedParserList[0];
        }
        return selectedParser;
    }

    public Parser selectParser(URL url) throws FileNotFoundException, IOException, XMLParseException, ParserNotFoundException {
        Parser selectedParser = null;
        String message = "";
        String title = "";
        int type = JOptionPane.INFORMATION_MESSAGE;
        Icon icon = null;
        Object[] choice = parserList;
        Object selection = JOptionPane.showInputDialog(rootFrame, message, title, type, icon, choice, choice[0]);
        selectedParser = (Parser)selection;
        return selectedParser;
    }

    /**
     * set output parser.
     * @param op output parser
     */
    public void setOutputParser(Parser op) {
        outputParser = op;
    }

    /**
     * input.
     * @param wm world manager
     * @param file file
     * @throws Exception exception
     */
    public void input(WorldManager wm, File file) throws FileNotFoundException, IOException, XMLParseException, ParserNotFoundException, WorldManagerException {
        Parser parser = selectParser(file);
        System.out.println(parser);
        FileInputStream in = new FileInputStream(file);
        try {
            parser.input(wm, in);
        }
        finally {
            in.close();
        }
    }

    /**
     * input.
     * @param wm world manager
     * @param url url
     * @throws Exception exception
     */
    public void input(WorldManager wm, URL url) throws FileNotFoundException, IOException, XMLParseException, ParserNotFoundException, WorldManagerException {
        Parser parser = selectParser(url);
        System.out.println(parser);
        parser.input(wm, url.openStream());
    }

    /**
     * output.
     * @param wm world manager
     * @param file file
     * @throws Exception exception
     */
    public void output(WorldManager wm, File file) throws Exception {
        Parser op;
        if (outputParser != null) {
            op = outputParser;
        }
        else if (guiConfirm) {
            throw new Exception("not supported yet");
        }
        else {
            op = parserList[0];
        }
        FileOutputStream out = new FileOutputStream(file);
        try {
            op.output(wm, out);
            out.flush();
        }
        finally {
            out.close();
        }
    }

}
