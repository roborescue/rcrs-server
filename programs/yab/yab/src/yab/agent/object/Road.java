// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.*;
import yab.io.object.*;

public class Road extends Edge {
    public Road(int id, DisasterSpace world)
    { super(id, new BaseRoad(id), world); }
    private BaseRoad obj() { return (BaseRoad) object; }

    public int width() { return obj().width(); }
    public int block() { return obj().block(); }
    public int repairCost() { return obj().repairCost(); }
    public int linesToHead() { return obj().linesToHead(); }
    public int linesToTail() { return obj().linesToTail(); }

    private float lineWidth() {
        return ((float) width()) / ((float) linesToHead() + linesToTail());
    }
    private int blockedLines() {
        return (int) Math.floor(block() / (2f * lineWidth()) + 0.5f);
    }
    public int passableLinesToHead() {
        return Util.max(0, linesToHead() - blockedLines());
    }
    public int passableLinesToTail() {
        return Util.max(0, linesToTail() - blockedLines());
    }
    public int passableLinesTo(PointObject to) {
        return to == head() ? passableLinesToHead() : passableLinesToTail();
    }
    public int passableLinesFrom(PointObject from) {
        return from == tail() ? passableLinesToHead() : passableLinesToTail();
    }
    public int passableLines() {
        return Util.min(passableLinesToHead(), passableLinesToTail());
    }
}
