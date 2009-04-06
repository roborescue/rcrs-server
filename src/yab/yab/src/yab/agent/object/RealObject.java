// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.*;
import yab.io.object.*;

public abstract class RealObject extends RCRObject {
    public RealObject(int id, BaseRealObject object,
                      DisasterSpace world) { super(id, object, world); }

    public abstract int x();
    public abstract int y();
    public abstract MotionlessObject motionlessPosition();

    public boolean hasBeenSeen() {
        return time() >= Constants.TIME_STARTING_ACTION;
    }

    public int distance(RealObject to) { return Util.distance(this, to); }
    public int direction(RealObject to) { return Util.direction(this, to); }
}
