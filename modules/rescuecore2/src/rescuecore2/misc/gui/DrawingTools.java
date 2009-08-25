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

    /**
       Get the coordinates for arrow heads along a line.
       @param startX The start of the line (X).
       @param startY The start of the line (Y).
       @param endX The end of the line (X).
       @param endY The end of the line (Y).
       @param angle The angle of the arrow barbs.
       @param length The length of the arrow barbs.
       @return A pair of coordinate pairs for the ends of the arrow barbs.
     */
    public static Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getArrowHeads(int startX, int startY, int endX, int endY, double angle, double length) {
        double dx = endX - startX;
        double dy = endY - startY;
        double vectorX = ((Math.cos(angle) * dx) - (Math.sin(angle) * dy));
        double vectorY = ((Math.sin(angle) * dx) + (Math.cos(angle) * dy));
        // Normalise the vector
        double vLength = Math.hypot(vectorX, vectorY);
        vectorX /= vLength;
        vectorY /= vLength;
        // Now calculate end points
        int leftX = endX + (int)(vectorX * length);
        int leftY = endY + (int)(vectorY * length);
        int rightX = endX - (int)(vectorY * length);
        int rightY = endY + (int)(vectorX * length);
        Pair<Integer, Integer> left = new Pair<Integer, Integer>(leftX, leftY);
        Pair<Integer, Integer> right = new Pair<Integer, Integer>(rightX, rightY);
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
       @param g The graphics object to draw on.
     */
    public static void drawArrowHeads(int startX, int startY, int endX, int endY, double angle, double length, Graphics g) {
        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> barbs = getArrowHeads(startX, startY, endX, endY, angle, length);
        int leftX = barbs.first().first();
        int leftY = barbs.first().second();
        int rightX = barbs.second().first();
        int rightY = barbs.second().second();
        g.drawLine(leftX, leftY, endX, endY);
        g.drawLine(rightX, rightY, endX, endY);
    }
}