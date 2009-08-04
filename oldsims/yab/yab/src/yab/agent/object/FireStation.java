// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public class FireStation extends Building {
    public FireStation(int id, DisasterSpace world)
    { super(id, new BaseFireStation(id), world); }
    private BaseFireStation obj() { return (BaseFireStation) object; }
}
