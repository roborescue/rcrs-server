package kernel.standard;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

import kernel.KernelException;
import kernel.WorldModelCreator;

/*
import rcr.RCRDataSet;
import rcr.RCRMap;
import rcr.ScenarioGenerator;

import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
*/

/**
   A WorldModelCreator that reads OpenStreetMap files.
 */
public class OSMWorldModelCreator implements WorldModelCreator {
    private static final String MAP_NAME_KEY = "gis.osm.mapname";

    @Override
    public WorldModel<? extends Entity> buildWorldModel(Config config) throws KernelException {
        /*
        String mapName = config.getValue(MAP_NAME_KEY);
        File file = new File(mapName);
        DataSet ds = OsmReader.parseDataSet(new FileInputStream(file), new NullProgressMonitor());
        RCRDataSet dataset = new RCRDataSet(ds);
        ScenarioGenerator generator = new ScenarioGenerator(dataset);
        generator.makeScenario();
        RCRMap map = dataset.toRescueMap();
        */
        return null;
    }
}