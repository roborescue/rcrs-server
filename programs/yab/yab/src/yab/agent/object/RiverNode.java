// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public class RiverNode extends Vertex {
    public RiverNode(int id, DisasterSpace world)
    { super(id, new BaseRiverNode(id), world); }
    private BaseRiverNode obj() { return (BaseRiverNode) object; }
}
