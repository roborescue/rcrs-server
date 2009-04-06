// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public class PoliceForce extends Humanoid {
    public PoliceForce(int id, DisasterSpace world)
    { super(id, new BasePoliceForce(id), world); }
    private BasePoliceForce obj() { return (BasePoliceForce) object; }
}
