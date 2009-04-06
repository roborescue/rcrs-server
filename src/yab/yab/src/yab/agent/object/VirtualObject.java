// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public abstract class VirtualObject extends RCRObject {
    public VirtualObject(int id, BaseVirtualObject object,
                         DisasterSpace world) { super(id, object, world); }
}
