// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public class AmbulanceTeam extends Humanoid {
    public AmbulanceTeam(int id, DisasterSpace world)
    { super(id, new BaseAmbulanceTeam(id), world); }
    private BaseAmbulanceTeam obj() { return (BaseAmbulanceTeam) object; }
}
