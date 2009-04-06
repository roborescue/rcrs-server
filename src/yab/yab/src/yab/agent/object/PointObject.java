// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public abstract class PointObject extends MotionlessObject {
    public PointObject(int id, BasePointObject object,
                       DisasterSpace world) { super(id, object, world); }
    private BasePointObject obj() { return (BasePointObject) object; }

    public int x() { return obj().x(); }
    public int y() { return obj().y(); }
}
