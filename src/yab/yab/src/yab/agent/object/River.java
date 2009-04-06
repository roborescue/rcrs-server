// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public class River extends Edge {
    public River(int id, DisasterSpace world)
    { super(id, new BaseRiver(id), world); }
    private BaseRiver obj() { return (BaseRiver) object; }
}
