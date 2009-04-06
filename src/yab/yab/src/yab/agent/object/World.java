// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public class World extends VirtualObject {
    public World(int id, DisasterSpace world)
    { super(id, new BaseWorld(id), world); }
    private BaseWorld obj() { return (BaseWorld) object; }
}
