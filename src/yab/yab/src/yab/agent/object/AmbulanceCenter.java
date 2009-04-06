// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;
import yab.io.object.*;

public class AmbulanceCenter extends Building {
    public AmbulanceCenter(int id, DisasterSpace world)
    { super(id, new BaseAmbulanceCenter(id), world); }
    private BaseAmbulanceCenter obj() { return (BaseAmbulanceCenter) object; }
}
