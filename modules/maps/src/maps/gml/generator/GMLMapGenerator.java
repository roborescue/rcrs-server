package maps.gml.generator;

import maps.gml.GMLMap;
import maps.gml.GMLException;
import maps.gml.MapWriter;
import maps.gml.formats.RobocupFormat;

import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.log.Logger;

import java.io.File;
import java.io.IOException;

/**
   A tool for generating GML maps.
*/
public class GMLMapGenerator {
    private static final String OUTPUT_FILE_KEY = "generator.output";

    private Config config;

    /**
       Construct a GMLMapGenerator.
       @param config The configuration to use.
    */
    public GMLMapGenerator(Config config) {
        this.config = config;
    }

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
            String outFile = config.getValue(OUTPUT_FILE_KEY);
            Logger.debug("Writing generated map to " + outFile);
            MapWriter.writeGMLMap(map, outFile, RobocupFormat.INSTANCE);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (GMLException e) {
            e.printStackTrace();
        }
        catch (ConfigException e) {
            e.printStackTrace();
        }
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