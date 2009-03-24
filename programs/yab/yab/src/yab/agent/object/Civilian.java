// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public class Civilian extends Humanoid {
    public Civilian(int id, DisasterSpace world)
    { super(id, new BaseCivilian(id), world); }
    private BaseCivilian obj() {return (BaseCivilian) object; }
}
