package maps.gml.generator;

import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLBuilding;
import maps.gml.GMLRoad;
import maps.gml.formats.RobocupFormat;

import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.log.Logger;

import org.dom4j.Document;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

import java.io.File;
import java.io.FileOutputStream;

/**
   A tool for generating GML maps.
*/
public class GMLMapGenerator {
    private static final String OUTPUT_FILE_KEY = "generator.output";

    private Config config;

    /**
       Entry point.
       @param args Command line arguments.
    */
    public static void main(String[] args) {
        try {
            Config config = new Config();
            for (int i = 0; i < args.length; ++i) {
                config.read(new File(args[i]));
            }
            GMLMap map = new GMLMapGenerator(config).generateMap();
            Document doc = new RobocupFormat().write(map);
            String outFile = config.getValue(OUTPUT_FILE_KEY);
            Logger.debug("Writing generated map to " + outFile);
            XMLWriter writer = new XMLWriter(new FileOutputStream(new File(outFile)), OutputFormat.createPrettyPrint());
            writer.write(doc);
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
       Construct a GMLMapGenerator.
       @param config The configuration to use.
    */
    public GMLMapGenerator(Config config) {
        this.config = config;
    }

    /**
       Generate a new map.
       @return The new map.
    */
    public GMLMap generateMap() {
        GMLMap result = new GMLMap();
        new ManhattanGenerator(config).populate(result);
        return result;
    }
}