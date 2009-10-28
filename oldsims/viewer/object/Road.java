package viewer.object;

import rescuecore.RescueConstants;

public class Road extends Edge {
    public Road(int id) { super(id); }
    public int type() { return RescueConstants.TYPE_ROAD; }

    private int m_width;
    private int m_block;
    private int m_linesToHead;
    private int m_linesToTail;

    public int width() { return m_width; }
    public int block() { return m_block; }
    public int linesToHead() { return m_linesToHead; }
    public int linesToTail() { return m_linesToTail; }

    public int aliveLinesToHead() {
        int result = linesToHead() - blockedLines();
        return ((result >= 0) ? result : 0);
    }
    public int aliveLinesToTail() {
        int result = linesToTail() - blockedLines();
        return ((result >= 0) ? result : 0);
    }

    public int blockedLines() {
        double blockWidth = ((double) block()) / 2d;
        double linesBlockedRate = blockWidth / lineWidth();
        return (int) Math.floor(linesBlockedRate + 0.5d);
    }

    public double lineWidth() { return ((double) width()) / ((double) linesToHead() + linesToTail()); }

    public int aliveLinesTo(PointObject to)
    { return (to == head()) ? aliveLinesToHead() : aliveLinesToTail(); }
    public int linesTo(PointObject to)
    { return (to == head()) ? linesToHead() : linesToTail(); }

    public void setWidth(int value) { m_width = value; }
    public void setBlock(int value) { m_block = value; }
    public void setLinesToHead(int value) { m_linesToHead = value; }
    public void setLinesToTail(int value) { m_linesToTail = value; }

    public void input(String property, int[] value) {
        if ("WIDTH".equals(property)) {
            setWidth(value[0]);
        }
        if ("BLOCK".equals(property)) {
            setBlock(value[0]);
        }
        if ("LINES_TO_HEAD".equals(property)) {
            setLinesToHead(value[0]);
        }
        if ("LINES_TO_TAIL".equals(property)) {
            setLinesToTail(value[0]);
        }
        super.input(property, value);
    }
}
