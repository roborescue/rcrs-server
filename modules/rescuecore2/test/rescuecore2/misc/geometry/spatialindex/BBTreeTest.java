package rescuecore2.misc.geometry.spatialindex;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.GeometryTools2D;

import rescuecore2.log.Logger;

public class BBTreeTest {
    private static final int NUM_POINTS = 1000;
    private static final int NUM_LINES = 1000;
    private static final int RANDOM_REGION_TEST_COUNT = 10;

    private BBTree tree;
    private List<Point2D> points;
    private List<Line2D> lines;
    private List<Indexable> all;

    @Before
    public void setup() {
        tree = new BBTree();
        points = new ArrayList<Point2D>(NUM_POINTS);
        lines = new ArrayList<Line2D>(NUM_LINES);
        all = new ArrayList<Indexable>(NUM_POINTS + NUM_LINES);
    }

    /*
    @After
    public void cleanup() {
        Logger.debug("*******************************************************");
    }
    */

    @Test
    public void testRetrieveAllPoints() {
        //        Logger.debug("testRetrieveAllPoints()");
        createPoints();
        //        tree.logTree();
        // Check that all points are retrieved if we search the whole space
        Collection<Indexable> found = tree.getItemsInRegion(0, 0, 1, 1);
        assertEquals(NUM_POINTS, found.size());
        assertThat(found, hasItems(points.toArray(new Indexable[0])));
    }

    @Test
    public void testRetrieveAllLines() {
        //        Logger.debug("testRetrieveAllLines()");
        createLines();
        //        tree.logTree();
        // Check that all lines are retrieved if we search the whole space
        Collection<Indexable> found = tree.getItemsInRegion(0, 0, 1, 1);
        assertEquals(NUM_LINES, found.size());
        assertThat(found, hasItems(lines.toArray(new Indexable[0])));
    }

    @Test
    public void testRetrieveAllPointsAndLines() {
        //        Logger.debug("testRetrieveAllPointsAndLines()");
        createPoints();
        createLines();
        //        tree.logTree();
        // Check that all points and lines are retrieved if we search the whole space
        Collection<Indexable> found = tree.getItemsInRegion(0, 0, 1, 1);
        assertEquals(NUM_POINTS + NUM_LINES, found.size());
        assertThat(found, hasItems(all.toArray(new Indexable[0])));
    }

    @Test
    public void testRetrieveSomePoints() {
        //        Logger.debug("testRetrieveSomePoints()");
        createPoints();
        //        tree.logTree();
        // Search a bunch of subspaces
        // Left half
        checkRegion(0, 0, 0.5, 1);
        // Right half
        checkRegion(0.5, 0, 1, 1);
        // Bottom half
        checkRegion(0, 0, 1, 0.5);
        // Top half
        checkRegion(0, 0.5, 1, 1);
        // Centre
        checkRegion(0.25, 0.25, 0.75, 0.75);
        // A few others
        for (int i = 0; i < RANDOM_REGION_TEST_COUNT; ++i) {
            double xMin = Math.random();
            double yMin = Math.random();
            double xMax = Math.random();
            double yMax = Math.random();
            checkRegion(Math.min(xMin, xMax), Math.min(yMin, yMax), Math.max(xMin, xMax), Math.max(yMin, yMax));
        }
    }

    @Test
    public void testRetrieveSomeLines() {
        //        Logger.debug("testRetrieveSomeLines()");
        createLines();
        //        tree.logTree();
        // Search a bunch of subspaces
        // Left half
        checkRegion(0, 0, 0.5, 1);
        // Right half
        checkRegion(0.5, 0, 1, 1);
        // Bottom half
        checkRegion(0, 0, 1, 0.5);
        // Top half
        checkRegion(0, 0.5, 1, 1);
        // Centre
        checkRegion(0.25, 0.25, 0.75, 0.75);
        // A few others
        for (int i = 0; i < RANDOM_REGION_TEST_COUNT; ++i) {
            double xMin = Math.random();
            double yMin = Math.random();
            double xMax = Math.random();
            double yMax = Math.random();
            checkRegion(Math.min(xMin, xMax), Math.min(yMin, yMax), Math.max(xMin, xMax), Math.max(yMin, yMax));
        }
    }

    @Test
    public void testRetrieveSomePointsAndLines() {
        //        Logger.debug("testRetrieveSomePointsAndLines()");
        createPoints();
        createLines();
        //        tree.logTree();
        // Search a bunch of subspaces
        // Left half
        checkRegion(0, 0, 0.5, 1);
        // Right half
        checkRegion(0.5, 0, 1, 1);
        // Bottom half
        checkRegion(0, 0, 1, 0.5);
        // Top half
        checkRegion(0, 0.5, 1, 1);
        // Centre
        checkRegion(0.25, 0.25, 0.75, 0.75);
        // A few others
        for (int i = 0; i < RANDOM_REGION_TEST_COUNT; ++i) {
            double xMin = Math.random();
            double yMin = Math.random();
            double xMax = Math.random();
            double yMax = Math.random();
            checkRegion(Math.min(xMin, xMax), Math.min(yMin, yMax), Math.max(xMin, xMax), Math.max(yMin, yMax));
        }
    }

    private void createPoints() {
        for (int i = 0; i < NUM_POINTS; ++i) {
            Point2D p = new Point2D(Math.random(), Math.random());
            points.add(p);
            tree.insert(p);
        }
        all.addAll(points);
    }

    private void createLines() {
        for (int i = 0; i < NUM_LINES; ++i) {
            Line2D l = new Line2D(new Point2D(Math.random(), Math.random()), new Point2D(Math.random(), Math.random()));
            lines.add(l);
            tree.insert(l);
        }
        all.addAll(lines);
    }

    private void checkRegion(double xMin, double yMin, double xMax, double yMax) {
        Collection<Indexable> found = tree.getItemsInRegion(xMin, yMin, xMax, yMax);
        Collection<Indexable> shouldBeFound = new ArrayList<Indexable>();
        shouldBeFound.addAll(findPoints(xMin, yMin, xMax, yMax));
        shouldBeFound.addAll(findLines(xMin, yMin, xMax, yMax));
        //        Logger.debug("Checking region " + xMin + ", " + yMin + " -> " + xMax + ", " + yMax);
        //        Logger.debug("Should find " + shouldBeFound);
        //        Logger.debug("Actually found " + found);
        assertEquals(shouldBeFound.size(), found.size());
        assertThat(found, hasItems(shouldBeFound.toArray(new Indexable[0])));
    }

    private Collection<Indexable> findPoints(double xMin, double yMin, double xMax, double yMax) {
        Collection<Indexable> result = new ArrayList<Indexable>(points.size());
        for (Point2D next : points) {
            if (next.getX() >= xMin && next.getX() <= xMax && next.getY() >= yMin && next.getY() <= yMax) {
                result.add(next);
            }
        }
        return result;
    }

    private Collection<Indexable> findLines(double xMin, double yMin, double xMax, double yMax) {
        Collection<Indexable> result = new ArrayList<Indexable>(lines.size());
        for (Line2D next : lines) {
            if (GeometryTools2D.clipToRectangle(next, xMin, yMin, xMax, yMax) != null) {
                result.add(next);
            }
        }
        return result;
    }
}