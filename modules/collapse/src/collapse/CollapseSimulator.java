package collapse;

import rescuecore2.config.Config;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.KSAfterShocksInfo;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModelListener;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.log.Logger;
import rescuecore2.GUIComponent;

import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardEntityConstants;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.StandardWorldModel;

import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.random.ContinuousUniformGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.Maths;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.awt.geom.Path2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;

import javax.swing.JComponent;

/**
 * A simple collapse simulator.
 */
public class CollapseSimulator extends StandardSimulator implements
		GUIComponent {
	private static final String CONFIG_PREFIX = "collapse.";
	private static final String DESTROYED_SUFFIX = ".p-destroyed";
	private static final String SEVERE_SUFFIX = ".p-severe";
	private static final String MODERATE_SUFFIX = ".p-moderate";
	private static final String SLIGHT_SUFFIX = ".p-slight";
	private static final String NONE_SUFFIX = ".p-none";

	private static final String DESTROYED_MEAN_SUFFIX = "destroyed.mean";
	private static final String DESTROYED_SD_SUFFIX = "destroyed.sd";
	private static final String SEVERE_MEAN_SUFFIX = "severe.mean";
	private static final String SEVERE_SD_SUFFIX = "severe.sd";
	private static final String MODERATE_MEAN_SUFFIX = "moderate.mean";
	private static final String MODERATE_SD_SUFFIX = "moderate.sd";
	private static final String SLIGHT_MEAN_SUFFIX = "slight.mean";
	private static final String SLIGHT_SD_SUFFIX = "slight.sd";

	private static final String BLOCK_KEY = "collapse.create-road-blockages";

	private static final String FLOOR_HEIGHT_KEY = "collapse.floor-height";
	private static final String WALL_COLLAPSE_EXTENT_MIN_KEY = "collapse.wall-extent.min";
	private static final String WALL_COLLAPSE_EXTENT_MAX_KEY = "collapse.wall-extent.max";

	/* Aftershock Requirment 2013 */
	private static final String RANDOM_AFTERSHOCK = "collapse.aftershock.random";

	/* Aftershock Requirment 2013 */
	// enum IntensityType {
	// ON_DAMAGE, ON_DISTANCE;
	// public String toString() {
	// if (this.equals(ON_DAMAGE))
	// return "damage";
	// else
	// return "distance";
	// };
	//
	// }
	enum CollapsePolicy {
		MERGE_BLOCKADES, DONT_MERGE_BLOCKADES;
		public String toString() {
			if (this.equals(MERGE_BLOCKADES))
				return "merge";
			else
				return "dont_merge";
		};
	}

	CollapsePolicy policy = CollapsePolicy.DONT_MERGE_BLOCKADES;

	private static final int MAX_COLLAPSE = 100;

	private static final double REPAIR_COST_FACTOR = 0.000001; // Converts
																// square mm to
																// square m.

	public static final String NAME = "Collapse Simulator v1.1";

	private static final List<EntityID> EMPTY_ID_LIST = new ArrayList<EntityID>(
			0);

	private NumberGenerator<Double> destroyed;
	private NumberGenerator<Double> severe;
	private NumberGenerator<Double> moderate;
	private NumberGenerator<Double> slight;

	private boolean block;

	private double floorHeight;
	private NumberGenerator<Double> extent;

	private Map<StandardEntityConstants.BuildingCode, CollapseStats> stats;

	private CollapseSimulatorGUI gui;
	private Collection<Building> buildingCache;
	private Collection<Road> roadCache;

	public CollapseSimulator() {
	}

	/**
	 * This method instantiate a CollapseWorldModel instance.
	 */
	@Override
	protected StandardWorldModel createWorldModel() {
		return new CollapseWorldModel();/* Aftershocks Requirement:2013 */
	}

	@Override
	public JComponent getGUIComponent() {
		if (gui == null) {
			gui = new CollapseSimulatorGUI();
		}
		return gui;
	}

	@Override
	public String getGUIComponentName() {
		return "Collapse simulator";
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected void postConnect() {
		super.postConnect();
		stats = new EnumMap<StandardEntityConstants.BuildingCode, CollapseStats>(
				StandardEntityConstants.BuildingCode.class);
		for (StandardEntityConstants.BuildingCode code : StandardEntityConstants.BuildingCode
				.values()) {
			stats.put(code, new CollapseStats(code, config));
		}
		slight = new GaussianGenerator(config.getFloatValue(CONFIG_PREFIX
				+ SLIGHT_MEAN_SUFFIX), config.getFloatValue(CONFIG_PREFIX
				+ SLIGHT_SD_SUFFIX), config.getRandom());
		moderate = new GaussianGenerator(config.getFloatValue(CONFIG_PREFIX
				+ MODERATE_MEAN_SUFFIX), config.getFloatValue(CONFIG_PREFIX
				+ MODERATE_SD_SUFFIX), config.getRandom());
		severe = new GaussianGenerator(config.getFloatValue(CONFIG_PREFIX
				+ SEVERE_MEAN_SUFFIX), config.getFloatValue(CONFIG_PREFIX
				+ SEVERE_SD_SUFFIX), config.getRandom());
		destroyed = new GaussianGenerator(config.getFloatValue(CONFIG_PREFIX
				+ DESTROYED_MEAN_SUFFIX), config.getFloatValue(CONFIG_PREFIX
				+ DESTROYED_SD_SUFFIX), config.getRandom());
		block = config.getBooleanValue(BLOCK_KEY);
		floorHeight = config.getFloatValue(FLOOR_HEIGHT_KEY) * 1000;
		extent = new ContinuousUniformGenerator(
				config.getFloatValue(WALL_COLLAPSE_EXTENT_MIN_KEY),
				config.getFloatValue(WALL_COLLAPSE_EXTENT_MAX_KEY),
				config.getRandom());
		buildingCache = new HashSet<Building>();
		roadCache = new HashSet<Road>();
		for (StandardEntity next : model) {
			if (next instanceof Building) {
				buildingCache.add((Building) next);
			}
			if (next instanceof Road) {
				roadCache.add((Road) next);
			}
		}
		model.addWorldModelListener(new WorldModelListener<StandardEntity>() {
			@Override
			public void entityAdded(WorldModel<? extends StandardEntity> model,
					StandardEntity e) {
				if (e instanceof Building) {
					buildingCache.add((Building) e);
				}
				if (e instanceof Road) {
					roadCache.add((Road) e);
				}
			}

			@Override
			public void entityRemoved(
					WorldModel<? extends StandardEntity> model, StandardEntity e) {
				if (e instanceof Building) {
					buildingCache.remove((Building) e);
				}
				if (e instanceof Road) {
					roadCache.remove((Road) e);
				}
			}
		});
	}

	@Override
	protected void processCommands(KSCommands c, ChangeSet changes) {
		long start = System.currentTimeMillis();
		int time = c.getTime();
		Logger.info("Timestep " + time);
		if (gui != null) {
			gui.timestep(time);
		}
		Collection<Building> collapsed = doCollapse(changes, time);
		Map<Road, Collection<java.awt.geom.Area>> newBlock = doBlock(collapsed,
				time);
		// Create blockade objects
		Map<Road, Collection<Blockade>> blockades = createBlockadeObjects(newBlock);
		for (Map.Entry<Road, Collection<Blockade>> entry : blockades.entrySet()) {
			Road r = entry.getKey();
			List<EntityID> existing = r.getBlockades();
			List<EntityID> ids = new ArrayList<EntityID>();
			if (existing != null) {
				ids.addAll(existing);
			}
			for (Blockade b : entry.getValue()) {
				ids.add(b.getID());
			}
			r.setBlockades(ids);
			changes.addAll(entry.getValue());
			changes.addChange(r, r.getBlockadesProperty());
		}
		// If any roads have undefined blockades then set the blockades property
		// to the empty list
		for (Road next : roadCache) {
			if (!next.isBlockadesDefined()) {
				next.setBlockades(EMPTY_ID_LIST);
				changes.addChange(next, next.getBlockadesProperty());
			}
		}
		long end = System.currentTimeMillis();
		Logger.info("Timestep " + time + " took " + (end - start) + " ms");
	}

	private Collection<Building> doCollapse(ChangeSet changes, int time) {
		Collection<Building> result = new HashSet<Building>();
		if (gui != null) {
			gui.startCollapse(buildingCache.size());
		}
		if (time == 1) {
			result.addAll(doEarthquakeCollapse(changes));
		}
		/* Aftershocks Requirement:2013 */
		if (time != 1 && model().aftershockHappens(time)) {
			result.addAll(doEarthquakeCollapse(changes));
		}
		if (gui != null) {
			gui.endCollapse();
		}
		if (gui != null) {
			gui.startFire(buildingCache.size());
		}
		// result.addAll(doFireCollapse(changes));
		if (gui != null) {
			gui.endFire();
		}
		return result;
	}

	static long t1;
	static long t2;
	static long t3;

	private Map<Road, Collection<java.awt.geom.Area>> doBlock(
			Collection<Building> collapsed, int time) {
		Map<Road, Collection<java.awt.geom.Area>> result = new LazyMap<Road, Collection<java.awt.geom.Area>>() {
			@Override
			public Collection<java.awt.geom.Area> createValue() {
				return new ArrayList<java.awt.geom.Area>();
			}
		};
		if (!block) {
			return result;
		}
		if (gui != null) {
			gui.startBlock(collapsed.size());
		}
		for (Building b : collapsed) {
			createBlockages(b, result, time);
			if (gui != null) {
				gui.bumpBlock();
			}
		}
		if (gui != null) {
			gui.endBlock();
		}
		return result;
	}

	private Collection<Building> doEarthquakeCollapse(ChangeSet changes) {
		Map<StandardEntityConstants.BuildingCode, Map<CollapseDegree, Integer>> count = new EnumMap<StandardEntityConstants.BuildingCode, Map<CollapseDegree, Integer>>(
				StandardEntityConstants.BuildingCode.class);
		Map<StandardEntityConstants.BuildingCode, Integer> total = new EnumMap<StandardEntityConstants.BuildingCode, Integer>(
				StandardEntityConstants.BuildingCode.class);

		for (StandardEntityConstants.BuildingCode code : StandardEntityConstants.BuildingCode
				.values()) {
			Map<CollapseDegree, Integer> next = new EnumMap<CollapseDegree, Integer>(
					CollapseDegree.class);
			for (CollapseDegree cd : CollapseDegree.values()) {
				next.put(cd, 0);
			}
			count.put(code, next);
			total.put(code, 0);
		}

		Logger.debug("Collapsing buildings");
		Collection<Building> result = new HashSet<Building>();
		for (Building b : buildingCache) {
			StandardEntityConstants.BuildingCode code = b.getBuildingCodeEnum();
			int damage = code == null ? 0 : stats.get(code).damage();
			damage = Maths.restrictRange(damage, 0, MAX_COLLAPSE);
			int lastDamage = b.getBrokenness();
			/*
			 * Aftershock Requirement 2013: this ignores the new damage of it is
			 * less than the previous
			 */
			if (damage < lastDamage) {
				damage = lastDamage;
			}
			/* Aftershock Requirement 2013 */
			b.setBrokenness(damage);
			changes.addChange(b, b.getBrokennessProperty());

			CollapseDegree degree = CollapseDegree.get(damage);
			count.get(code).put(degree, count.get(code).get(degree) + 1);
			total.put(code, total.get(code) + 1);

			if (damage > 0) {
				result.add(b);
			}
			if (gui != null) {
				gui.bumpCollapse();
			}
		}
		Logger.info("Finished collapsing buildings: ");
		for (StandardEntityConstants.BuildingCode code : StandardEntityConstants.BuildingCode
				.values()) {
			Logger.info("Building code " + code + ": " + total.get(code)
					+ " buildings");
			Map<CollapseDegree, Integer> data = count.get(code);
			for (Map.Entry<CollapseDegree, Integer> entry : data.entrySet()) {
				Logger.info("  " + entry.getValue() + " "
						+ entry.getKey().toString().toLowerCase());
			}
		}
		return result;
	}

	private Collection<Building> doFireCollapse(ChangeSet changes) {
		Logger.debug("Checking fire damage");
		Collection<Building> result = new HashSet<Building>();
		for (Building b : buildingCache) {
			/*
			 * Aftershock Requirement @ 2103: it is used in doBlock to check
			 * whether apply the aftershock intensity or not
			 */
			model().getCSBuiding(b).setHasFireDamage(false);

			if (!b.isFierynessDefined()) {
				if (gui != null) {
					gui.bumpFire();
				}
				continue;
			}
			int minDamage = 0;
			switch (b.getFierynessEnum()) {
			case HEATING:
				minDamage = slight.nextValue().intValue();
				break;
			case BURNING:
				minDamage = moderate.nextValue().intValue();
				break;
			case INFERNO:
				minDamage = severe.nextValue().intValue();
				break;
			case BURNT_OUT:
				minDamage = destroyed.nextValue().intValue();
				break;
			default:
				break;
			}
			/* Aftershock Requirement @ 2103 */
			if (minDamage != 0) {
				// indicates that the building has fire damage
				model().getCSBuiding(b).setHasFireDamage(true);
			}
			minDamage = Maths.restrictRange(minDamage, 0, MAX_COLLAPSE);
			int damage = b.isBrokennessDefined() ? b.getBrokenness() : 0;
			if (damage < minDamage) {
				Logger.info(b + " damaged by fire. New brokenness: "
						+ minDamage);
				b.setBrokenness(minDamage);
				changes.addChange(b, b.getBrokennessProperty());
				result.add(b);
			}
			if (gui != null) {
				gui.bumpFire();
			}
		}
		Logger.debug("Finished checking fire damage");
		return result;
	}

	private Map<Road, Collection<Blockade>> createBlockadeObjects(
			Map<Road, Collection<java.awt.geom.Area>> blocks) {
		Map<Road, Collection<Blockade>> result = new LazyMap<Road, Collection<Blockade>>() {
			@Override
			public Collection<Blockade> createValue() {
				return new ArrayList<Blockade>();
			}
		};
		int count = 0;
		for (Collection<java.awt.geom.Area> c : blocks.values()) {
			count += c.size();
		}
		try {
			if (count != 0) {
				List<EntityID> newIDs = requestNewEntityIDs(count);
				Iterator<EntityID> it = newIDs.iterator();
				Logger.debug("Creating new blockade objects");
				for (Map.Entry<Road, Collection<java.awt.geom.Area>> entry : blocks
						.entrySet()) {
					Road r = entry.getKey();
					for (java.awt.geom.Area area : entry.getValue()) {
						EntityID id = it.next();
						Blockade blockade = makeBlockade(id, area, r.getID());
						if (blockade != null) {
							result.get(r).add(blockade);
						}
					}
				}
			}
		} catch (InterruptedException e) {
			Logger.error("Interrupted while requesting IDs");
		}
		return result;
	}

	private void createBlockages(Building b,
			Map<Road, Collection<java.awt.geom.Area>> roadBlockages, int time) {
		long t = System.currentTimeMillis();
		Logger.debug("Creating blockages for " + b);
		/*
		 * Aftershock's Requirement@2103: this checks the ratio of the
		 * building's floors that is possible to collapsed
		 */
		double remainingfloors = model().getCSBuiding(b)
				.getRemainingToCollapse(floorHeight);
		double currentExtent = extent.nextValue();
		double damage = b.getBrokenness();
		/*
		 * Aftershock Requirement 2013: applies the intensity of the aftershock.
		 * This only applies if there is an aftershock and time is >1 and the
		 * building doesn't have any fire damage. In case of any fire damage the
		 * building creates the max possible collapse.
		 */
		if (time != 1 && model().aftershockHappens(time)
				&& !model().getCSBuiding(b).hasFireDamage()) {
			float intensity = model().aftershockIntensity(time);
			damage = (int) ((float) damage * Math.min(1, intensity));
		}
		/* Aftershock's Requirement: 2013 */
		double d = remainingfloors * (damage / (double) MAX_COLLAPSE)
				* currentExtent;
		t1 += System.currentTimeMillis() - t;
		t = System.currentTimeMillis();
		/* Aftershock's Requirement: 2013 */
		model().getCSBuiding(b).increaseCollapseRatio(
				(float) (d / model().getCSBuiding(b).getTotalCollapse(
						floorHeight)));
		// Place some blockages on surrounding roads
		List<java.awt.geom.Area> wallAreas = new ArrayList<java.awt.geom.Area>();
		// Project each wall out and build a list of wall areas
		for (Edge edge : b.getEdges()) {
			projectWall(edge, wallAreas, d);
		}
		java.awt.geom.Area fullArea = new java.awt.geom.Area();
		for (java.awt.geom.Area wallArea : wallAreas) {
			fullArea.add(wallArea);
		}

		/*
		 * new ShapeDebugFrame().show("Collapsed building", new
		 * ShapeDebugFrame.AWTShapeInfo(b.getShape(), "Original building area",
		 * Color.RED, true), new ShapeDebugFrame.AWTShapeInfo(fullArea,
		 * "Expanded building area (d = " + d + ")", Color.BLACK, false) );
		 */
		// Find existing blockade areas
		java.awt.geom.Area existing = new java.awt.geom.Area();
		if (policy.equals(CollapsePolicy.MERGE_BLOCKADES)) {
			for (StandardEntity e : model
					.getEntitiesOfType(StandardEntityURN.BLOCKADE)) {
				Blockade blockade = (Blockade) e;
				existing.add(blockadeToArea(blockade));
			}
		}
		t2 += System.currentTimeMillis() - t;
		// Intersect wall areas with roads
		t = System.currentTimeMillis();
		Map<Road, Collection<java.awt.geom.Area>> blockadesForRoads = createRoadBlockades(
				fullArea, existing);
		t3 += System.currentTimeMillis() - t;
		// Add to roadBlockages
		for (Map.Entry<Road, Collection<java.awt.geom.Area>> entry : blockadesForRoads
				.entrySet()) {
			Road r = entry.getKey();
			Collection<java.awt.geom.Area> c = entry.getValue();
			roadBlockages.get(r).addAll(c);
		}
	}

	private void projectWall(Edge edge,
			Collection<java.awt.geom.Area> areaList, double d) {
		Line2D wallLine = new Line2D(edge.getStartX(), edge.getStartY(),
				edge.getEndX() - edge.getStartX(), edge.getEndY()
						- edge.getStartY());
		Vector2D wallDirection = wallLine.getDirection();
		Vector2D offset = wallDirection.getNormal().normalised().scale(-d);
		Path2D path = new Path2D.Double();
                
		Point2D right = wallLine.getOrigin();
		Point2D left = wallLine.getEndPoint();
                
		Point2D first = left.plus(offset.scale(-1));
		Point2D second = right.plus(offset.scale(-1));
		Point2D third = right.plus(offset);
		Point2D fourth = left.plus(offset);
                
		path.moveTo(first.getX(), first.getY());
		path.lineTo(second.getX(), second.getY());
		path.lineTo(third.getX(), third.getY());
		path.lineTo(fourth.getX(), fourth.getY());
		path.closePath();
                
		java.awt.geom.Area wallArea = new java.awt.geom.Area(path);
		areaList.add(wallArea);
		// Also add circles at each corner
		double radius = offset.getLength();
		Ellipse2D ellipse1 = new Ellipse2D.Double(right.getX() - radius,
				right.getY() - radius, radius * 2, radius * 2);
		Ellipse2D ellipse2 = new Ellipse2D.Double(left.getX() - radius,
				left.getY() - radius, radius * 2, radius * 2);
		areaList.add(new java.awt.geom.Area(ellipse1));
		areaList.add(new java.awt.geom.Area(ellipse2));
		// Logger.info("Edge from " + wallLine + " expanded to " + first + ", "
		// + second + ", " + third + ", " + fourth);
		// debug.show("Collapsed building",
		// new ShapeDebugFrame.AWTShapeInfo(buildingArea,
		// "Original building area", Color.RED, true),
		// new ShapeDebugFrame.Line2DShapeInfo(wallLine, "Wall edge",
		// Color.WHITE, true, true),
		// new ShapeDebugFrame.AWTShapeInfo(wallArea, "Wall area (d = " + d +
		// ")", Color.GREEN, false),
		// new ShapeDebugFrame.AWTShapeInfo(ellipse1, "Ellipse 1", Color.BLUE,
		// false),
		// new ShapeDebugFrame.AWTShapeInfo(ellipse2, "Ellipse 2", Color.ORANGE,
		// false)
		// );
	}

	private Map<Road, Collection<java.awt.geom.Area>> createRoadBlockades(
			java.awt.geom.Area buildingArea, java.awt.geom.Area existing) {
		Map<Road, Collection<java.awt.geom.Area>> result = new HashMap<Road, Collection<java.awt.geom.Area>>();
		java.awt.Rectangle rectangle = buildingArea.getBounds();
		Collection<StandardEntity> roads = model.getObjectsInRectangle(
				(int) rectangle.getMinX(), (int) rectangle.getMinY(),
				(int) rectangle.getMaxX(), (int) rectangle.getMaxY());
		for (StandardEntity e : roads) {
			if (!(e instanceof Road)) {
				continue;
			}
			Road r = (Road) e;
			java.awt.geom.Area roadArea = areaToGeomArea(r);
			java.awt.geom.Area intersection = new java.awt.geom.Area(roadArea);
			intersection.intersect(buildingArea);
			intersection.subtract(existing);
			if (intersection.isEmpty()) {
				continue;
			}
			existing.add(intersection);
			List<java.awt.geom.Area> blockadeAreas = fix(intersection);
			result.put(r, blockadeAreas);
			// debug.show("Road blockage",
			// new ShapeDebugFrame.AWTShapeInfo(buildingArea, "Building area",
			// Color.BLACK, false),
			// new ShapeDebugFrame.AWTShapeInfo(roadArea, "Road area",
			// Color.BLUE, false),
			// new ShapeDebugFrame.AWTShapeInfo(intersection, "Intersection",
			// Color.GREEN, true)
			// );
		}
		return result;
	}

	private java.awt.geom.Area areaToGeomArea(
			rescuecore2.standard.entities.Area area) {
		Path2D result = new Path2D.Double();
		Iterator<Edge> it = area.getEdges().iterator();
		Edge e = it.next();
		result.moveTo(e.getStartX(), e.getStartY());
		result.lineTo(e.getEndX(), e.getEndY());
		while (it.hasNext()) {
			e = it.next();
			result.lineTo(e.getEndX(), e.getEndY());
		}
		return new java.awt.geom.Area(result);
	}

	private List<java.awt.geom.Area> fix(java.awt.geom.Area area) {
		List<java.awt.geom.Area> result = new ArrayList<java.awt.geom.Area>();
		if (area.isSingular()) {
			result.add(area);
			return result;
		}
		PathIterator it = area.getPathIterator(null);
		Path2D current = null;
		// CHECKSTYLE:OFF:MagicNumber
		double[] d = new double[6];
		while (!it.isDone()) {
			switch (it.currentSegment(d)) {
			case PathIterator.SEG_MOVETO:
				if (current != null) {
					result.add(new java.awt.geom.Area(current));
				}
				current = new Path2D.Double();
				current.moveTo(d[0], d[1]);
				break;
			case PathIterator.SEG_LINETO:
				current.lineTo(d[0], d[1]);
				break;
			case PathIterator.SEG_QUADTO:
				current.quadTo(d[0], d[1], d[2], d[3]);
				break;
			case PathIterator.SEG_CUBICTO:
				current.curveTo(d[0], d[1], d[2], d[3], d[4], d[5]);
				break;
			case PathIterator.SEG_CLOSE:
				current.closePath();
				break;
			default:
				throw new RuntimeException(
						"Unexpected result from PathIterator.currentSegment: "
								+ it.currentSegment(d));
			}
			it.next();
		}
		// CHECKSTYLE:ON:MagicNumber
		if (current != null) {
			result.add(new java.awt.geom.Area(current));
		}
		return result;
	}

	private Blockade makeBlockade(EntityID id, java.awt.geom.Area area,
			EntityID roadID) {
		if (area.isEmpty()) {
			return null;
		}
		Blockade result = new Blockade(id);
		int[] apexes = getApexes(area);
		List<Point2D> points = GeometryTools2D.vertexArrayToPoints(apexes);
		if (points.size() < 2) {
			return null;
		}
		int cost = (int) (GeometryTools2D.computeArea(points) * REPAIR_COST_FACTOR);
		if (cost == 0) {
			return null;
		}
		Point2D centroid = GeometryTools2D.computeCentroid(points);
		result.setApexes(apexes);
		result.setPosition(roadID);
		result.setX((int) centroid.getX());
		result.setY((int) centroid.getY());
		result.setRepairCost((int) cost);
		return result;
	}

	private int[] getApexes(java.awt.geom.Area area) {
		// Logger.debug("getApexes");
		List<Integer> apexes = new ArrayList<Integer>();
		// CHECKSTYLE:OFF:MagicNumber
		PathIterator it = area.getPathIterator(null, 100);
		double[] d = new double[6];
		int moveX = 0;
		int moveY = 0;
		int lastX = 0;
		int lastY = 0;
		boolean finished = false;
		while (!finished && !it.isDone()) {
			int x = 0;
			int y = 0;
			switch (it.currentSegment(d)) {
			case PathIterator.SEG_MOVETO:
				x = (int) d[0];
				y = (int) d[1];
				moveX = x;
				moveY = y;
				// Logger.debug("Move to " + x + ", " + y);
				break;
			case PathIterator.SEG_LINETO:
				x = (int) d[0];
				y = (int) d[1];
				// Logger.debug("Line to " + x + ", " + y);
				if (x == moveX && y == moveY) {
					finished = true;
				}
				break;
			case PathIterator.SEG_QUADTO:
				x = (int) d[2];
				y = (int) d[3];
				// Logger.debug("Quad to " + x + ", " + y);
				if (x == moveX && y == moveY) {
					finished = true;
				}
				break;
			case PathIterator.SEG_CUBICTO:
				x = (int) d[4];
				y = (int) d[5];
				// Logger.debug("Cubic to " + x + ", " + y);
				if (x == moveX && y == moveY) {
					finished = true;
				}
				break;
			case PathIterator.SEG_CLOSE:
				// Logger.debug("Close");
				finished = true;
				break;
			default:
				throw new RuntimeException(
						"Unexpected result from PathIterator.currentSegment: "
								+ it.currentSegment(d));
			}
			// Logger.debug(x + ", " + y);
			if (!finished && (x != lastX || y != lastY)) {
				apexes.add(x);
				apexes.add(y);
			}
			lastX = x;
			lastY = y;
			it.next();
		}
		// CHECKSTYLE:ON:MagicNumber
		int[] result = new int[apexes.size()];
		int i = 0;
		for (Integer next : apexes) {
			result[i++] = next;
		}
		return result;
	}

	private java.awt.geom.Area blockadeToArea(Blockade b) {
		Path2D result = new Path2D.Double();
		int[] apexes = b.getApexes();
		result.moveTo(apexes[0], apexes[1]);
		for (int i = 2; i < apexes.length; i += 2) {
			result.lineTo(apexes[i], apexes[i + 1]);
		}
		result.closePath();
		return new java.awt.geom.Area(result);
	}

	private class CollapseStats {
		private double pDestroyed;
		private double pSevere;
		private double pModerate;
		private double pSlight;

		CollapseStats(StandardEntityConstants.BuildingCode code, Config config) {
			String s = CONFIG_PREFIX + code.toString().toLowerCase();
			pDestroyed = config.getFloatValue(s + DESTROYED_SUFFIX);
			pSevere = pDestroyed + config.getFloatValue(s + SEVERE_SUFFIX);
			pModerate = pSevere + config.getFloatValue(s + MODERATE_SUFFIX);
			pSlight = pModerate + config.getFloatValue(s + SLIGHT_SUFFIX);
		}

		int damage() {
			double d = random.nextDouble();
			if (d < pDestroyed) {
				return destroyed.nextValue().intValue();
			}
			if (d < pSevere) {
				return severe.nextValue().intValue();
			}
			if (d < pModerate) {
				return moderate.nextValue().intValue();
			}
			if (d < pSlight) {
				return slight.nextValue().intValue();
			}
			return 0;
		}
	}

	private enum CollapseDegree {
		NONE(0), SLIGHT(25), MODERATE(50), SEVERE(75), DESTROYED(100);

		private int max;

		private CollapseDegree(int max) {
			this.max = max;
		}

		public static CollapseDegree get(int d) {
			for (CollapseDegree next : values()) {
				if (d <= next.max) {
					return next;
				}
			}
			throw new IllegalArgumentException(
					"Don't know what to do with a damage value of " + d);
		}
	}

	// /* Aftershocks Requirement:2013 */
	// /**
	// * Handles KAAftershocksInfo and calls the parent processMessage.
	// */
	// @Override
	// protected void processMessage(Message msg) {
	//
	// }

	/* Aftershocks Requirement:2013 */
	public CollapseWorldModel model() {
		return (CollapseWorldModel) model;
	}

	@Override
	protected void processMessage(Message msg) {
		if (msg instanceof KSAfterShocksInfo) {
			model().updateAftershocks((KSAfterShocksInfo) msg);
		} else {
			super.processMessage(msg);
		}
	}

	public static void main(String[] args) {
	}
}
