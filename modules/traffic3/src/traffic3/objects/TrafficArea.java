package traffic3.objects;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.NDC;

//import rescuecore2.log.Logger;

import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.misc.gui.ShapeDebugFrame;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Edge;
import traffic3.simulator.TrafficSimulator;

import com.infomatiq.jsi.Rectangle;

/**
 * This class wraps an Area object with some extra information.
 */
public class TrafficArea {
	// private List<TrafficAreaListener> areaListenerList = new
	// ArrayList<TrafficAreaListener>();
	private Collection<TrafficAgent> agents;
	private Collection<TrafficBlockade> blocks;

	private List<Line2D> blockingLines;
	private List<Line2D> blockadeLines;
	private List<Line2D> allBlockingLines;
	private List<Line2D> areaLines;

	private Area area;
	private Rectangle bounds;
	private Vector2D baseVector;
	private ArrayList<Line2D> openLines;
	private int[][] graph;

	/**
	 * Construct a TrafficArea.
	 *
	 * @param area
	 *            The Area to wrap.
	 */
	public TrafficArea(final Area area) {
		this.area = area;
		agents = new HashSet<TrafficAgent>();
		blocks = new HashSet<TrafficBlockade>();
		blockingLines = null;
		blockadeLines = null;
		allBlockingLines = null;
		areaLines = null;
		Rectangle2D r = area.getShape().getBounds2D();
		bounds = new Rectangle((float) r.getMinX(), (float) r.getMinY(), (float) r.getMaxX(), (float) r.getMaxY());
		/*
		 * area.addEntityListener(new EntityListener() {
		 *
		 * @Override public void propertyChanged(Entity e, Property p, Object
		 * oldValue, Object newValue) { if (p == area.getBlockadesProperty()) {
		 * blockadeLines = null; allBlockingLines = null; } if (p ==
		 * area.getEdgesProperty()) { blockingLines = null; allBlockingLines =
		 * null; } } });
		 */
	}

	/**
	 * Get the wrapped area.
	 *
	 * @return The wrapped area.
	 */
	public Area getArea() {
		return area;
	}

	/**
	 * Get the bounding rectangle.
	 *
	 * @return The bounding rectangle.
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	/**
	 * Get all lines around this area that block movement.
	 *
	 * @return All area lines that block movement.
	 */
	public List<Line2D> getBlockingLines() {
		if (blockingLines == null) {
			blockingLines = new ArrayList<Line2D>();
			for (Edge edge : area.getEdges()) {
				if (!edge.isPassable()) {
					blockingLines.add(edge.getLine());
				}
			}
		}
		return Collections.unmodifiableList(blockingLines);
	}

	public List<Line2D> getAreaLines() {
		if (areaLines == null) {
			areaLines = new ArrayList<Line2D>();
			for (Edge edge : area.getEdges()) {
				areaLines.add(edge.getLine());
			}
		}
		return Collections.unmodifiableList(areaLines);
	}

	/**
	 * Get the lines that describe blockades in this area.
	 *
	 * @return All blockade lines.
	 */
	public List<Line2D> getBlockadeLines() {
		if (blockadeLines == null) {
			blockadeLines = new ArrayList<Line2D>();
			for (TrafficBlockade block : blocks) {
				blockadeLines.addAll(block.getLines());
			}
		}
		return Collections.unmodifiableList(blockadeLines);
	}

	/**
	 * Get all lines that block movement. This includes impassable edges of the
	 * area and all blockade lines.
	 *
	 * @return All movement-blocking lines.
	 */
	public List<Line2D> getAllBlockingLines() {
		if (allBlockingLines == null) {
			allBlockingLines = new ArrayList<Line2D>();
			allBlockingLines.addAll(getBlockingLines());
			allBlockingLines.addAll(getBlockadeLines());
		}
		return Collections.unmodifiableList(allBlockingLines);
	}

	/**
	 * Find out whether this area contains a point (x, y).
	 *
	 * @param x
	 *            The X coordinate to test.
	 * @param y
	 *            The Y coordinate to test.
	 * @return True if and only if this area contains the specified point.
	 */
	public boolean contains(double x, double y) {
		return area.getShape().contains(x, y);
	}

	/**
	 * Add an agent to this area.
	 *
	 * @param agent
	 *            The agent to add.
	 */
	public void addAgent(TrafficAgent agent) {
		agents.add(agent);
	}

	/**
	 * Remove an agent from this area.
	 *
	 * @param agent
	 *            The agent to remove.
	 */
	public void removeAgent(TrafficAgent agent) {
		agents.remove(agent);
	}

	/**
	 * Get all agents in this area.
	 *
	 * @return All agents inside this area.
	 */
	public Collection<TrafficAgent> getAgents() {
		return Collections.unmodifiableCollection(agents);
	}

	/**
	 * Add a TrafficBlockade.
	 *
	 * @param block
	 *            The blockade to add.
	 */
	public void addBlockade(TrafficBlockade block) {
		blocks.add(block);
		clearBlockadeCache();
	}

	/**
	 * Remove a TrafficBlockade.
	 *
	 * @param block
	 *            The blockade to remove.
	 */
	public void removeBlockade(TrafficBlockade block) {
		blocks.remove(block);
		clearBlockadeCache();
	}

	/**
	 * Clear any cached blockade information.
	 */
	public void clearBlockadeCache() {
		blockadeLines = null;
		allBlockingLines = null;
		openLines = null;
		graph=null;
	}

	/**
	 * Get all TrafficBlockades inside this area.
	 *
	 * @return All TrafficBlockades in this area.
	 */
	public Collection<TrafficBlockade> getBlockades() {
		return Collections.unmodifiableCollection(blocks);
	}

	@Override
	public String toString() {
		return "TrafficArea (" + area + ")";
	}
	public int getNearestLineIndex(Point2D point){
		List<Line2D> oLines = getOpenLines();
		double minDst=Integer.MAX_VALUE;
		int minIndex=-1;
		/*FOR:*/for (int i = 0; i < oLines.size(); i++) {
//			Line2D line = new Line2D(point,getMidPoint(oLines.get(i).getOrigin(), oLines.get(i).getEndPoint()));
//			for (Line2D is :getAllBlockingLines()) {
//				if (GeometryTools2D.getSegmentIntersectionPoint(line, is) != null) {
//					continue FOR;
//				}
//			}
//			for (int k = 0; k < oLines.size(); k++) {
//				if(k==i)
//					continue;
//				if (GeometryTools2D.getSegmentIntersectionPoint(line, oLines.get(k)) == null) {
//					continue FOR;
//				}
//			}
			Point2D nearestPoint = GeometryTools2D.getClosestPointOnSegment(oLines.get(i), point);
			double dst = GeometryTools2D.getDistance(point, nearestPoint);
			if(dst<minDst){
				minDst=dst;
				minIndex=i;
			}
//			return i;
		}
		return minIndex;
	}

	public int[][] getGraph() {
		if (graph == null) {
			List<Line2D> oLines = getOpenLines();
			graph = new int[oLines.size()][oLines.size()];
			for (int i = 0; i < graph.length; i++) {
				FOR: for (int j = 0; j < graph.length; j++) {
					Line2D line = new Line2D(getMidPoint(oLines.get(i).getOrigin(), oLines.get(i).getEndPoint()), getMidPoint(oLines.get(j).getOrigin(), oLines.get(j)
							.getEndPoint()));
					for (Line2D is : getAllBlockingLines()) {
						if (GeometryTools2D.getSegmentIntersectionPoint(line, is) != null) {
							graph[i][j] = 100000;
							continue FOR;
						}
					}
					for (int k = 0; k < oLines.size(); k++) {
						if(k==i||k==j)
							continue;
						if (GeometryTools2D.getSegmentIntersectionPoint(line, oLines.get(k)) != null) {
							graph[i][j] = Integer.MAX_VALUE;
							continue FOR;
						}
					}
					graph[i][j] = 1;
				}
			}
		}
		return graph;
	}

	private Point2D getMidPoint(Point2D p1, Point2D p2) {
		return new Point2D((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2);
	}

	public List<Line2D> getOpenLines() {
		if (openLines == null) {
			openLines = new ArrayList<Line2D>();
			HashSet<Point2D> checkedPoint = new HashSet<Point2D>();
			for (Line2D line : getBlockadeLines()) {
				if (!checkedPoint.contains(line.getOrigin()))
					createLine(line.getOrigin(), openLines);
				if (!checkedPoint.contains(line.getEndPoint()))
					createLine(line.getEndPoint(), openLines);
				checkedPoint.add(line.getOrigin());
				checkedPoint.add(line.getEndPoint());
			}
//			createPassableEdgesLine(openLines);
//			TrafficSimulator.debug.show("Full Lines", new ShapeDebugFrame.AWTShapeInfo(getArea().getShape(), getArea() + "", Color.blue, false),
//					new ShapeDebugFrame.Line2DShapeInfo(openLines, "openLines", Color.green, false, true)
//
//			);
		}
		return Collections.unmodifiableList(openLines);
	}

	private void createPassableEdgesLine(List<Line2D> openLines) {
		for (Edge edge : getArea().getEdges()) {
			if (edge.isPassable()) {
				List<Line2D> edgeLines = new ArrayList<Line2D>();
				edgeLines.add(edge.getLine());
				// Line2D edgeLine =new Line2D(edge.getStart(),edge.getEnd());
				// ArrayList<Line2D> subtractLines=new ArrayList<Line2D>();
				// subtractLines.add(edgeLine);
				for (Line2D line : getBlockadeLines()) {
					// double distance = getDistance(edgeLine,line);
					// if(distance<1){
					List<Line2D> old = edgeLines;
					edgeLines = minus(edgeLines, line);

					// }
//					TrafficSimulator.debug.show("d", new ShapeDebugFrame.Line2DShapeInfo(edgeLines, "result", Color.green, true, true), new ShapeDebugFrame.Line2DShapeInfo(old,
//							"edgeLines", Color.black, false, false), new ShapeDebugFrame.Line2DShapeInfo(line, "blockadeLine", Color.white, false, true));
				}
				openLines.addAll(edgeLines);
			}
		}
	}

	private static List<Line2D> minus(List<Line2D> edgeLines, Line2D line) {
		List<Line2D> result = new ArrayList<Line2D>();
		for (Line2D edgeLine : edgeLines) {
			// System.out.println("Edge====");
			// System.out.println(edgeLine);
			// System.out.println(line);
			Line2D clone = new Line2D(edgeLine.getOrigin(), edgeLine.getEndPoint());
			boolean lineContaintEdgeOrigin = GeometryTools2D.contains(line, edgeLine.getOrigin());
			boolean lineContaintEdgeEnd = GeometryTools2D.contains(line, edgeLine.getEndPoint());
			boolean edgeContaintLineOrigin = GeometryTools2D.contains(edgeLine, line.getOrigin());
			boolean edgeContaintLineEnd = GeometryTools2D.contains(edgeLine, line.getEndPoint());
			if (lineContaintEdgeOrigin & lineContaintEdgeEnd)
				continue;

			if (edgeContaintLineOrigin & edgeContaintLineEnd) {
				Line2D firstLine;
				Line2D secondLine;
				double distanceoo = GeometryTools2D.getDistance(edgeLine.getOrigin(), line.getOrigin());
				double distanceoe = GeometryTools2D.getDistance(edgeLine.getOrigin(), line.getEndPoint());
				double distanceeo = GeometryTools2D.getDistance(edgeLine.getEndPoint(), line.getOrigin());
				double distanceee = GeometryTools2D.getDistance(edgeLine.getEndPoint(), line.getEndPoint());
				if (distanceoo < distanceoe) {
					firstLine = new Line2D(edgeLine.getOrigin(), line.getOrigin());
					secondLine = new Line2D(line.getEndPoint(), edgeLine.getEndPoint());
				} else {
					firstLine = new Line2D(edgeLine.getOrigin(), line.getEndPoint());
					secondLine = new Line2D(line.getOrigin(), edgeLine.getEndPoint());
				}
				// System.out.println(distanceoo);
				// System.out.println(distanceoe);
				// System.out.println(distanceeo);
				// System.out.println(distanceee);
				if (!GeometryTools2D.nearlyZero(distanceoo) && !GeometryTools2D.nearlyZero(distanceoe))
					result.add(firstLine);
				if (!GeometryTools2D.nearlyZero(distanceeo) && !GeometryTools2D.nearlyZero(distanceee))
					result.add(secondLine);
				continue;
			}
			if (lineContaintEdgeOrigin) {
				if (edgeContaintLineOrigin)
					clone.setOrigin(line.getOrigin());
				else if (edgeContaintLineEnd)
					clone.setOrigin(line.getEndPoint());
				else
					System.err.println("why?");
			}
			if (lineContaintEdgeEnd) {
				if (edgeContaintLineOrigin)
					clone.setEnd(line.getOrigin());
				else if (edgeContaintLineEnd)
					clone.setEnd(line.getEndPoint());
				else
					System.err.println("why?2");
			}

			result.add(clone);
		}
		return result;
	}

	private double getDistance(Line2D e1, Point2D p) {
		Point2D p1 = GeometryTools2D.getClosestPointOnSegment(e1, p);
		return GeometryTools2D.getDistance(p1, p);
	}

	private double getDistance(Line2D e1, Line2D e2) {
		double d1 = getDistance(e1, e2.getOrigin());
		double d2 = getDistance(e1, e2.getEndPoint());
		double d4 = getDistance(e2, e1.getOrigin());
		double d5 = getDistance(e2, e1.getEndPoint());
		d1 = Math.min(d1, d2);
		d2 = Math.min(d5, d4);
		return Math.min(d1, d2);
	}

	private Vector2D getBaseVector() {
		if (baseVector == null) {
			baseVector = new Vector2D(10, 10);
		}
		return baseVector;
	}

	private List<Line2D> getAllBlockingAndAreaLine() {
		List<Line2D> lines = new ArrayList<Line2D>(getBlockadeLines());
		lines.addAll(getAreaLines());
		return lines;
	}

	private void createLine(Point2D origin, List<Line2D> openLines) {

		Line2D newLineToUp = new Line2D(origin, getBaseVector().scale(1000));
		Line2D newLineToDown = new Line2D(origin, getBaseVector().scale(-1000));
//		TrafficSimulator.debug.show("Init", new ShapeDebugFrame.AWTShapeInfo(getArea().getShape(), getArea() + "", Color.blue, false), new ShapeDebugFrame.Line2DShapeInfo(
//				newLineToUp, "lineToUp", Color.red, false, true), new ShapeDebugFrame.Line2DShapeInfo(newLineToDown, "lineTodown", Color.green, false, true));

		for (Line2D line : getAllBlockingAndAreaLine()) {
			if (line.getOrigin().equals(origin) || line.getEndPoint().equals(origin))
				continue;
			double distance1 = GeometryTools2D.getDistance(newLineToUp.getOrigin(), newLineToUp.getEndPoint());
			// newLineToUp.getDirection().scale(distance1);
			Point2D point1 = GeometryTools2D.getSegmentIntersectionPoint(newLineToUp, line);
			if (point1 != null && !GeometryTools2D.nearlyZero(distance1))
				newLineToUp.setEnd(point1);
			double distance2 = GeometryTools2D.getDistance(newLineToDown.getOrigin(), newLineToDown.getEndPoint());
			// newLineToDown.senewLineToDown.getDirection().scale(distance2);
			Point2D point2 = GeometryTools2D.getSegmentIntersectionPoint(newLineToDown, line);
			if (point2 != null && !GeometryTools2D.nearlyZero(distance2))
				newLineToDown.setEnd(point2);
//			TrafficSimulator.debug.show(
//					"Checking",
//					// new ShapeDebugFrame.AWTShapeInfo(getArea().getShape(),
//					// getArea()+"", Color.blue, false),
//					new ShapeDebugFrame.Line2DShapeInfo(line, "checkline", Color.MAGENTA, false, true), new ShapeDebugFrame.Line2DShapeInfo(newLineToUp, "lineToUp " + distance1,
//							Color.red, false, true), new ShapeDebugFrame.Line2DShapeInfo(newLineToDown, "lineTodown " + distance2, Color.green, false, true),
//					new ShapeDebugFrame.Point2DShapeInfo(point1, "Point1" + point1, Color.red, true), new ShapeDebugFrame.Point2DShapeInfo(point2, "Point2" + point2, Color.green,
//							true));
		}
		boolean isValidLineToUp = isValidLine(newLineToUp);
		boolean isValidLineToDown = isValidLine(newLineToDown);
//		TrafficSimulator.debug.show("Final",
//		// new ShapeDebugFrame.AWTShapeInfo(getArea().getShape(), getArea()+"",
//		// Color.blue, false),
//				new ShapeDebugFrame.Line2DShapeInfo(newLineToUp, "lineToUp:" + isValidLineToUp, Color.red, isValidLineToUp, true), new ShapeDebugFrame.Line2DShapeInfo(
//						newLineToDown, "lineTodown:" + isValidLineToDown, Color.green, isValidLineToDown, true));
		if (isValidLineToUp)
			openLines.add(newLineToUp);
		if (isValidLineToDown)
			openLines.add(newLineToDown);
	}

	private boolean isValidLine(Line2D line) {
		Point midPoint = new Point((int) (line.getOrigin().getX() + line.getEndPoint().getX()) / 2, (int) (line.getOrigin().getY() + line.getEndPoint().getY()) / 2);
		if (!getArea().getShape().contains(midPoint))
			return false;
		for (TrafficBlockade blockade : getBlockades()) {
			if (blockade.getBlockade().getShape().contains(midPoint))
				return false;
		}
		return true;

	}

	@Override
	public int hashCode() {
		return area.getID().hashCode();
	}
}
