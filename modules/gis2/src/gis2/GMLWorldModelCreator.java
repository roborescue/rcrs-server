package gis2;

import kernel.WorldModelCreator;
import kernel.KernelException;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.GeometryTools2D;

import rescuecore2.scenario.exceptions.ScenarioException;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Edge;

import maps.MapReader;
import maps.MapException;
import maps.gml.GMLMap;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLBuilding;
import maps.gml.GMLRoad;
import maps.gml.GMLShape;
import maps.gml.GMLCoordinates;
import maps.CoordinateConversion;
import maps.ScaleConversion;

import java.util.List;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * A WorldModelCreator that reads a GML file and scenario descriptor.
 */
public class GMLWorldModelCreator implements WorldModelCreator {
	private static final String MAP_DIRECTORY_KEY = "gis.map.dir";
	private static final String MAP_FILE_KEY = "gis.map.file";
	private static final String DEFAULT_MAP_FILE = "map.gml";
	private static final String SCENARIO_FILE_KEY = "gis.map.scenario";
	private static final String DEFAULT_SCENARIO_FILE = "scenario.xml";
	private static final String MAX_FLOOR = "gis.map.max-floor";
	private static final String FLOOR_PLACEMENT_TYPE = "gis.map.floor-placement.random";
	private static final String RANDOM_FLOOR_RATE = "gis.map.floor-placement.random.floor-rate.";
	private static final String BUILDING_CODE_PLACEMENT_TYPE = "gis.map.building-code-placement.random";
	private static final String RANDOM_BUILDING_CODE_RATE = "gis.map.building-code-placement.random.code-rate.";
	private static final String MAX_BUILDING_CODE = "gis.map.max-building-code";
	
	private static final double SQ_MM_TO_SQ_M = 0.000001;

	private GisScenario scenario;
	// CHECKSTYLE:OFF:MagicNumber
	// private static final double AREA_SCALE_FACTOR = 1.0 / 1000000.0;
	// CHECKSTYLE:ON:MagicNumber
	private static final Logger LOG = Logger.getLogger(GMLWorldModelCreator.class);

	// private ShapeDebugFrame debug;

	private int nextID;
	@Override
	public String toString() {
		return "GML world model creator";
	}

	@Override
	public WorldModel<? extends Entity> buildWorldModel(Config config)
			throws KernelException {
		try {
			StandardWorldModel result = new StandardWorldModel();
			File dir = new File(config.getValue(MAP_DIRECTORY_KEY));
			File mapFile = new File(dir, config.getValue(MAP_FILE_KEY,
					DEFAULT_MAP_FILE));
			File scenarioFile = new File(dir, config.getValue(
					SCENARIO_FILE_KEY, DEFAULT_SCENARIO_FILE));
			readMapData(mapFile, result,config);
			readScenarioAndApply(scenarioFile, result, config);
			for (Entity e : result) {
				nextID = Math.max(nextID, e.getID().getValue());
			}
			++nextID;
			result.index();
			return result;
		} catch (MapException e) {
			throw new KernelException("Couldn't read GML file", e);
		} catch (DocumentException e) {
			throw new KernelException("Couldn't read scenario file", e);
		} catch (ScenarioException e) {
			throw new KernelException("Invalid scenario file", e);
		}
	}

	@Override
	public EntityID generateID() {
		return new EntityID(nextID++);
	}

	private void readMapData(File mapFile, StandardWorldModel result, Config config)
			throws MapException {
		int maxFloor = config.getIntValue(MAX_FLOOR,3);
		boolean randomfloorPlacement = config.getBooleanValue(FLOOR_PLACEMENT_TYPE,false);
		int[] floorRates = null;
		int[] floorRatesCumulative = null;
		if(randomfloorPlacement) {
			floorRates = new int[maxFloor+1];
			floorRatesCumulative = new int[maxFloor+1];
			for(int i=1;i<=maxFloor;i++){
				floorRates[i]=config.getIntValue(RANDOM_FLOOR_RATE+i);
				floorRatesCumulative[i]=floorRatesCumulative[i-1]+floorRates[i];
			}
		}

		
		int maxBuildingCode = config.getIntValue(MAX_BUILDING_CODE, 2);
		boolean randomBuildingCodePlacement = config.getBooleanValue(BUILDING_CODE_PLACEMENT_TYPE, false);
		int[] buildingCodeRates = null;
		int[] buildingCodesCumulative =  null;
		if(randomBuildingCodePlacement) {
			buildingCodeRates = new int[maxBuildingCode + 1];
			buildingCodesCumulative = new int[maxBuildingCode + 1];
			for(int i = 0; i <= maxBuildingCode; i++){
				buildingCodeRates[i] = config.getIntValue(RANDOM_BUILDING_CODE_RATE + i);
				buildingCodesCumulative[i] = (i > 0 ? buildingCodesCumulative[i - 1] : 0) + buildingCodeRates[i];
			}
		}

		
		GMLMap map = (GMLMap) MapReader.readMap(mapFile);
		CoordinateConversion conversion = getCoordinateConversion(map);
		LOG.debug("Creating entities");
		LOG.debug(map.getBuildings().size() + " buildings");
		LOG.debug(map.getRoads().size() + " roads");
		
		for (GMLBuilding next : map.getBuildings()) {
			// Create a new Building entity
			EntityID id = new EntityID(next.getID());
			Building b = new Building(id);
			List<Point2D> vertices = convertShapeToPoints(next, conversion);
			double area = GeometryTools2D.computeArea(vertices) * SQ_MM_TO_SQ_M;
			Point2D centroid = GeometryTools2D.computeCentroid(vertices);

			// LOG.debug("Building vertices: " + vertices);
			// LOG.debug("Area: " + area);
			// LOG.debug("Centroid: " + centroid);

			// Building properties
			int floors = Math.min(maxFloor, next.getFloors());
			if(randomfloorPlacement){
				int rnd=config.getRandom().nextInt(floorRatesCumulative[maxFloor])+1;
				for(int i=1;i<=maxFloor;i++){
					if(rnd<=floorRatesCumulative[i]){
						floors=i;
						break;
					}
				}
			}
			
			int code = Math.min(maxBuildingCode, next.getCode());
			if(randomBuildingCodePlacement){
				int rnd = config.getRandom().nextInt(buildingCodesCumulative[maxBuildingCode]) + 1;
				for(int i = 0; i <= maxBuildingCode; i++){
					if(rnd <= buildingCodesCumulative[i]){
						code = i;
						break;
					}
				}
			}
			
			b.setFloors(floors);
			b.setFieryness(0);
			b.setBrokenness(0);
			b.setBuildingCode(code);
			b.setBuildingAttributes(0);
			b.setGroundArea((int) Math.abs(area));
			b.setTotalArea(((int) Math.abs(area)) * b.getFloors());
			b.setImportance(next.getImportance());
			// Area properties
			b.setEdges(createEdges(next, conversion));
			b.setX((int) centroid.getX());
			b.setY((int) centroid.getY());
			result.addEntity(b);
			// LOG.debug(b.getFullDescription());
		}
		for (GMLRoad next : map.getRoads()) {
			// Create a new Road entity
			EntityID id = new EntityID(next.getID());
			Road r = new Road(id);
			List<Point2D> vertices = convertShapeToPoints(next, conversion);
			Point2D centroid = GeometryTools2D.computeCentroid(vertices);

			// LOG.debug("Road vertices: " + vertices);
			// LOG.debug("Centroid: " + centroid);

			// Road properties: None
			// Area properties
			r.setX((int) centroid.getX());
			r.setY((int) centroid.getY());
			r.setEdges(createEdges(next, conversion));
			result.addEntity(r);
			// LOG.debug(b.getFullDescription());
		}
	}

	private void readScenarioAndApply(File scenarioFile,
			StandardWorldModel result, Config config) throws DocumentException,
			ScenarioException {
		if (scenarioFile.exists()) {
			readScenario(scenarioFile);
			LOG.debug("Applying scenario");
			scenario.apply(result, config);
		}
	}

	private void readScenario(File scenarioFile) throws DocumentException,
			ScenarioException {
		if (scenarioFile.exists()) {
			SAXReader reader = new SAXReader();
			LOG.debug("Reading scenario");
			Document doc = reader.read(scenarioFile);
			scenario = new GisScenario(doc);
		}
	}

	private List<Edge> createEdges(GMLShape s, CoordinateConversion conversion) {
		// LOG.debug("Computing edges for " + s);
		List<Edge> result = new ArrayList<Edge>();
		for (GMLDirectedEdge edge : s.getEdges()) {
			GMLCoordinates start = edge.getStartCoordinates();
			GMLCoordinates end = edge.getEndCoordinates();
			Integer neighbourID = s.getNeighbour(edge);
			EntityID id = neighbourID == null ? null
					: new EntityID(neighbourID);
			// LOG.debug("Edge: " + start + " -> " + end);
			double sx = conversion.convertX(start.getX());
			double sy = conversion.convertY(start.getY());
			double ex = conversion.convertX(end.getX());
			double ey = conversion.convertY(end.getY());
			// LOG.debug(edge.getEdge() + " : " + sx + "," + sy + " -> " + ex
			// + "," + ey);
			result.add(new Edge((int) sx, (int) sy, (int) ex, (int) ey, id));
		}
		return result;
	}

	private List<Point2D> convertShapeToPoints(GMLShape shape,
			CoordinateConversion conversion) {
		List<Point2D> points = new ArrayList<Point2D>();
		for (GMLCoordinates next : shape.getCoordinates()) {
			points.add(new Point2D(conversion.convertX(next.getX()), conversion
					.convertY(next.getY())));
		}
		return points;
	}

	private CoordinateConversion getCoordinateConversion(GMLMap map) {
		return new ScaleConversion(map.getMinX(), map.getMinY(), 1000, 1000);
	}

	public GisScenario getScenario(Config config) throws DocumentException{

		if (scenario == null) {
			File dir = new File(config.getValue(MAP_DIRECTORY_KEY));
			File scenarioFile = new File(dir, config.getValue(
					SCENARIO_FILE_KEY, DEFAULT_SCENARIO_FILE));
			try {
				readScenario(scenarioFile);
			} catch (ScenarioException e) {
				e.printStackTrace();
			}
		}
		return scenario;
	}

}
