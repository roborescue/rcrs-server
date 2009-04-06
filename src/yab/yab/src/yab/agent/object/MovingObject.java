// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public abstract class MovingObject extends RealObject {
    public MovingObject(int id, BaseMovingObject object,
                        DisasterSpace world) { super(id, object, world); }
    private BaseMovingObject obj() { return (BaseMovingObject) object; }

    public int x() {
        if (!(position() instanceof Edge))
            return position().x();
        Edge e = (Edge) position();
        return e.head().x()
            + (e.tail().x() - e.head().x()) * positionExtra() / e.length();
    }
    public int y() {
        if (!(position() instanceof Edge))
            return position().y();
        Edge e = (Edge) position();
        return e.head().y()
            + (e.tail().y() - e.head().y()) * positionExtra() / e.length();
    }

    public MotionlessObject motionlessPosition() {
        return position().motionlessPosition();
    }

    public RealObject position() {
        return (RealObject) world.get(obj().position());
    }
    public int positionExtra() { return obj().positionExtra(); }
}
