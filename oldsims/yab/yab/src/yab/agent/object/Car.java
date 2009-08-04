// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public class Car extends Humanoid {
    public Car(int id, DisasterSpace world)
    { super(id, new BaseCar(id), world); }
    private BaseCar obj() { return (BaseCar) object; }
}
