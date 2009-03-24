// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import java.util.*;
import yab.agent.DisasterSpace;
import yab.io.object.*;

public abstract class MotionlessObject extends RealObject {
    public MotionlessObject(int id, BaseMotionlessObject object,
                            DisasterSpace world) { super(id, object, world); }

    public MotionlessObject motionlessPosition() { return this; }
    public abstract Collection neighborhood();
}
