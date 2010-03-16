package traffic3.objects.area;

import java.awt.geom.Line2D;

public class TrafficEdge {
    private Line2D line;
    private boolean passable;

    public TrafficEdge(Line2D line, boolean passable) {
        this.line = line;
        this.passable = passable;
    }

    public Line2D getLine() {
        return line;
    }

    public boolean isPassable() {
        return passable;
    }

    @Override
    public String toString() {
        return "TrafficEdge: " + line.getX1() + ", " + line.getY1() + " -> " + line.getX2() + ", " + line.getY2() + " (" + (passable ? "passable" : "impassable") + ")";
    }
}
