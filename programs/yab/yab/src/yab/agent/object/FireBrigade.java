// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public class FireBrigade extends Humanoid {
    public FireBrigade(int id, DisasterSpace world)
    { super(id, new BaseFireBrigade(id), world); }
    private BaseFireBrigade obj() { return (BaseFireBrigade) object; }

    //public int waterQuantity() { return obj().waterQuantity(); }
}
