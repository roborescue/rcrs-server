// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public abstract class Humanoid extends MovingObject {
    public Humanoid(int id, BaseHumanoid object, DisasterSpace world)
    { super(id, object, world); }
    private BaseHumanoid obj() { return (BaseHumanoid) object; }

    public int hp() { return obj().hp(); }
    public int damage() { return obj().damage(); }
    public int buriedness() { return obj().buriedness(); }
}
