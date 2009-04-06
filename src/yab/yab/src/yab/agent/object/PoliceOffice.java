// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public class PoliceOffice extends Building {
    public PoliceOffice(int id, DisasterSpace world)
    { super(id, new BasePoliceOffice(id), world); }
    private BasePoliceOffice obj() { return (BasePoliceOffice) object; }
}
