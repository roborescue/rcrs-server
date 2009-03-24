// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent.object;

import yab.agent.DisasterSpace;
import yab.io.object.*;

public class Node extends Vertex {
    public Node(int id, DisasterSpace world)
    { super(id, new BaseNode(id), world); }
    private BaseNode obj() { return (BaseNode) object; }
}
