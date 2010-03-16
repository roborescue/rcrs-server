package rescuecore2.misc.gui;

import rescuecore2.misc.Pair;

import java.awt.Graphics;

/**
   A bunch of useful functions for drawing things.
 */
public final class DrawingTools {
    /** A default angle for arrow barbs. */
    public static final double DEFAULT_ARROW_ANGLE = Math.toRadians(135);
    /** A default length (in pixels) for arrow barbs. */
    public static final double DEFAULT_ARROW_LENGTH = 5;
    /** A default distance along the line for arrow barbs. */
    public static final double DEFAULT_ARROW_DISTANCE = 0.5;

    private DrawingTools() {}

    /**
       Get the coordinates for arrow heads along a line.
       @param startX The start of the line (X).
       @param startY The start of the line (Y).
       @param endX The end of the line (X).
       @param endY The end of the line (Y).
       @param angle The angle of the arrow barbs.
       @param length The length of the arrow barbs.
       @param d The distance along the line to place the barbs. This must be between zero and one.
       @return A pair of coordinate pairs for the ends of the arrow barbs.
     */
    public static Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getArrowHeads(int startX, int startY, int endX, int endY, double angle, double length, double d) {
        double dx = endX - startX;
        double dy = endY - startY;
        double headX = startX + (d * dx);
        double headY = startY + (d * dy);
        double vectorX = ((Math.cos(angle) * dx) - (Math.sin(angle) * dy));
        double vectorY = ((Math.sin(angle) * dx) + (Math.cos(angle) * dy));
        // Normalise the vector
        double vLength = Math.hypot(vectorX, vectorY);
        vectorX /= vLength;
        vectorY /= vLength;
        // Now calculate end points
        double leftX = headX + (vectorX * length);
        double leftY = headY + (vectorY * length);
        double rightX = headX - (vectorY * length);
        double rightY = headY + (vectorX * length);
        Pair<Integer, Integer> left = new Pair<Integer, Integer>((int)leftX, (int)leftY);
        Pair<Integer, Integer> right = new Pair<Integer, Integer>((int)rightX, (int)rightY);
        return new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(left, right);
    }

    /**
       Draw an arrowhead on a line.
       @param startX The start of the line (X).
       @param startY The start of the line (Y).
       @param endX The end of the line (X).
       @param endY The end of the line (Y).
       @param angle The angle of the arrow barbs.
       @param length The length of the arrow barbs.
       @param d The distance along the line to draw the barbs. This must be between zero and one.
       @param g The graphics object to draw on.
     */
    public static void drawArrowHeads(int startX, int startY, int endX, int endY, double angle, double length, double d, Graphics g) {
        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> barbs = getArrowHeads(startX, startY, endX, endY, angle, length, d);
        int leftX = barbs.first().first();
        int leftY = barbs.first().second();
        int rightX = barbs.second().first();
        int rightY = barbs.second().second();
        double dx = endX - startX;
        double dy = endY - startY;
        int headX = (int)(startX + (d * dx));
        int headY = (int)(startY + (d * dy));
        g.drawLine(leftX, leftY, headX, headY);
        g.drawLine(rightX, rightY, headX, headY);
    }

    /**
       Draw an arrowhead on a line with some default options.
       @param startX The start of the line (X).
       @param startY The start of the line (Y).
       @param endX The end of the line (X).
       @param endY The end of the line (Y).
       @param g The graphics object to draw on.
     */
    public static void drawArrowHeads(int startX, int startY, int endX, int endY, Graphics g) {
        drawArrowHeads(startX, startY, endX, endY, DEFAULT_ARROW_ANGLE, DEFAULT_ARROW_LENGTH, DEFAULT_ARROW_DISTANCE, g);
    }
}
