package rescuecore2.misc.geometry.spatialindex;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import rescuecore2.log.Logger;

/**
   A spatial index that creates a (probably) unbalanced tree of bounding boxes. This is almost certainly not as efficient as an RTree or similar, but it is much easier to implement.
*/
public class BBTree extends AbstractSpatialIndex {
    private static final int DEFAULT_MAX_CHILDREN = 3;

    private static final int DEFAULT_TIMING_POINTS = 50000;
    private static final int DEFAULT_TIMING_LINES = 50000;
    private static final int DEFAULT_TIMING_REGIONS = 1000;

    private Node root;
    private int maxChildren;

    /**
       Construct a BBTree with a default maximum number of children per branch.
    */
    public BBTree() {
        this(DEFAULT_MAX_CHILDREN);
    }

    /**
       Construct a BBTree with a given maximum number of children per branch.
       @param maxChildren The maximum number of children per branch.
    */
    public BBTree(int maxChildren) {
        this.maxChildren = maxChildren;
        root = new Branch();
    }

    /**
       Conduct a timing test.
       @param args Command line arguments: [-p points] [-l lines] [-r regions]
    */
    public static void main(String[] args) {
        int points = DEFAULT_TIMING_POINTS;
        int lines = DEFAULT_TIMING_LINES;
        int regions = DEFAULT_TIMING_REGIONS;
        // CHECKSTYLE:OFF:ModifiedControlVariable
        for (int i = 0; i < args.length; ++i) {
            if ("-p".equalsIgnoreCase(args[i])) {
                points = Integer.parseInt(args[++i]);
            }
            else if ("-l".equalsIgnoreCase(args[i])) {
                lines = Integer.parseInt(args[++i]);
            }
            else if ("-r".equalsIgnoreCase(args[i])) {
                regions = Integer.parseInt(args[++i]);
            }
            else {
                System.out.println("Unrecognised option: " + args[i]);
            }
        }
        // CHECKSTYLE:ON:ModifiedControlVariable
        BBTree tree = new BBTree();
        long start = System.currentTimeMillis();
        for (int i = 0; i < points; ++i) {
            rescuecore2.misc.geometry.Point2D p = new rescuecore2.misc.geometry.Point2D(Math.random(), Math.random());
            tree.insert(p);
        }
        for (int i = 0; i < lines; ++i) {
            rescuecore2.misc.geometry.Point2D p1 = new rescuecore2.misc.geometry.Point2D(Math.random(), Math.random());
            rescuecore2.misc.geometry.Point2D p2 = new rescuecore2.misc.geometry.Point2D(Math.random(), Math.random());
            rescuecore2.misc.geometry.Line2D l = new rescuecore2.misc.geometry.Line2D(p1, p2);
            tree.insert(l);
        }
        tree.logTree();
        long fill = System.currentTimeMillis();
        for (int i = 0; i < regions; ++i) {
            double xMin = Math.random();
            double yMin = Math.random();
            double xMax = Math.random();
            double yMax = Math.random();
            tree.getItemsInRegion(Math.min(xMin, xMax), Math.min(yMin, yMax), Math.max(xMin, xMax), Math.max(yMin, yMax));
        }
        long end = System.currentTimeMillis();
        long fillTime = fill - start;
        long fetchTime = end - fill;
        double fillAverage = ((double)fillTime) / (double)(points + lines);
        double fetchAverage = ((double)fetchTime) / (double)regions;
        System.out.println("Time to populate tree with " + points + " points and " + lines + " lines: " + fillTime + "ms (average " + fillAverage + "ms)");
        System.out.println("Time to read " + regions + " regions:  " + fetchTime + "ms (average " + fetchAverage + "ms)");
    }

    @Override
    public void insert(Indexable i) {
        //        Logger.debug("Inserting " + i);
        //        Logger.debug("Tree before insert");
        //        logTree();
        Leaf newLeaf = new Leaf(i);
        Node insertPoint = findInsertionPoint(root, i.getBoundingRegion());
        //        Logger.debug("Insertion point: " + insertPoint);
        if (insertPoint instanceof Leaf) {
            Branch b = new Branch();
            if (insertPoint.parent != null) {
                insertPoint.parent.insert(b);
                insertPoint.parent.remove(insertPoint);
            }
            b.insert(insertPoint);
            b.insert(newLeaf);
        }
        else {
            Branch b = (Branch)insertPoint;
            b.insert(newLeaf);
        }
        newLeaf.recomputeBounds();
        //        Logger.debug("Tree after insert");
        //        logTree();
    }

    @Override
    public Collection<Indexable> getItemsInRegion(Region region) {
        //        Logger.debug("Getting items in region " + region);
        Collection<Indexable> result = new ArrayList<Indexable>();
        if (root != null) {
            Stack<Node> open = new Stack<Node>();
            open.push(root);
            while (!open.isEmpty()) {
                Node next = open.pop();
                //                Logger.debug("Next node: " + next);
                if (next.bounds.intersects(region)) {
                    if (next instanceof Branch) {
                        //                        Logger.debug("Adding children");
                        open.addAll(((Branch)next).children);
                    }
                    else if (next instanceof Leaf) {
                        Leaf l = (Leaf)next;
                        if (region.intersects(l.entry.getBoundingRegion())) {
                            //                            Logger.debug("Leaf intersects region");
                            result.add(l.entry);
                        }
                        /*
                        else {
                            Logger.debug("Leaf does not intersect region");
                        }
                        */
                    }
                }
                /*
                else {
                    Logger.debug("No intersection");
                }
                */
            }
        }
        return result;
    }

    /**
       Write this tree to the logger.
    */
    public void logTree() {
        Logger.debug("BBTree");
        Logger.debug("Max children per node: " + maxChildren);
        Logger.debug("Tree depth: " + root.getDepth());
        root.log("  ");
    }

    private Node findInsertionPoint(Node parent, Region newRegion) {
        //        Logger.debug("Choosing insertion point: current parent = " + parent + ", new region = " + newRegion);
        if (parent instanceof Leaf) {
            //            Logger.debug("Parent is a leaf");
            return parent;
        }
        Branch b = (Branch)parent;
        if (b.children.size() < maxChildren) {
            //            Logger.debug("Parent can fit the child");
            return b;
        }
        Node best = findLeastAreaEnlargement(b.children, newRegion);
        //        Logger.debug("Best child: " + best);
        return findInsertionPoint(best, newRegion);
    }

    private Node findLeastAreaEnlargement(Collection<Node> nodes, Region newRegion) {
        //        Logger.debug("Finding least area enlargement for " + newRegion);
        Node best = null;
        double bestDiff = 0;
        for (Node next : nodes) {
            //            Logger.debug("Next node: " + next);
            double diff = computeAreaEnlargement(next, newRegion);
            if (best == null || diff < bestDiff) {
                best = next;
                bestDiff = diff;
            }
        }
        //        Logger.debug("Best: " + best);
        return best;
    }

    private double computeAreaEnlargement(Node node, Region newRegion) {
        double oldArea = node.bounds instanceof RectangleRegion ? ((RectangleRegion)node.bounds).getArea() : 0;
        double newArea = cover(node.bounds, newRegion).getArea();
        //        Logger.debug("Old area: " + oldArea);
        //        Logger.debug("New area: " + newArea);
        //        Logger.debug("Increase: " + (newArea - oldArea));
        return newArea - oldArea;
    }

    private RectangleRegion cover(Region... regions) {
        return cover(Arrays.asList(regions));
    }

    private RectangleRegion cover(List<? extends Region> regions) {
        if (regions.isEmpty()) {
            throw new IllegalArgumentException("Cannot cover zero regions");
        }
        double xMin = Double.POSITIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        double xMax = Double.NEGATIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        for (Region next : regions) {
            // CHECKSTYLE:OFF:EmptyBlock
            if (next == null || next instanceof NullRegion) {
                // Ignore
            }
            else {
                xMin = Math.min(xMin, next.getXMin());
                xMax = Math.max(xMax, next.getXMax());
                yMin = Math.min(yMin, next.getYMin());
                yMax = Math.max(yMax, next.getYMax());
            }
        }
        if (Double.isInfinite(xMin)) {
            return null;
        }
        return new RectangleRegion(xMin, yMin, xMax, yMax);
    }

    private abstract class Node {
        Region bounds;
        Branch parent;

        Node() {
            bounds = null;
            parent = null;
        }

        abstract void recomputeBounds();

        abstract void log(String prefix);

        abstract int getDepth();
    }

    private class Branch extends Node {
        List<Node> children;

        Branch() {
            this.children = new ArrayList<Node>(maxChildren);
        }

        void insert(Node child) {
            children.add(child);
            child.parent = this;
            bounds = cover(bounds, child.bounds);
        }

        void remove(Node child) {
            children.remove(child);
            child.parent = null;
        }

        @Override
        public String toString() {
            return "Branch [" + bounds + "] (" + children.size() + " children) {depth " + getDepth() + "}";
        }

        @Override
        void log(String prefix) {
            Logger.debug(prefix + this);
            String newPrefix = prefix + "  ";
            for (Node next : children) {
                next.log(newPrefix);
            }
        }

        @Override
        void recomputeBounds() {
            List<Region> childBounds = new ArrayList<Region>(children.size());
            for (Node next : children) {
                childBounds.add(next.bounds);
            }
            bounds = cover(childBounds);
            if (parent != null) {
                parent.recomputeBounds();
            }
        }

        @Override
        int getDepth() {
            int max = 0;
            for (Node next : children) {
                max = Math.max(max, next.getDepth());
            }
            return max + 1;
        }
    }

    private class Leaf extends Node {
        Indexable entry;

        Leaf(Indexable entry) {
            this.entry = entry;
            bounds = entry.getBoundingRegion();
        }

        @Override
        public String toString() {
            return "Leaf [" + bounds + "] (" + entry + ")";
        }

        @Override
        void log(String prefix) {
            Logger.debug(prefix + this.toString());
        }

        @Override
        void recomputeBounds() {
            if (parent != null) {
                parent.recomputeBounds();
            }
        }

        @Override
        int getDepth() {
            return 1;
        }
    }
}