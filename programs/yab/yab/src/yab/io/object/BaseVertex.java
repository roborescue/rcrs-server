// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io.object;

public abstract class BaseVertex extends BasePointObject {
    public BaseVertex(int id) { super(id); }

    private int[] m_edges;

    public int[] edges() { return m_edges; }

    public void setEdges(int[] value) { m_edges = value; }

    public void setProperty(int type, int[] value) {
        switch (type) {
        default: super.setProperty(type, value); break;
        case PROPERTY_EDGES: setEdges(value);    break;
        }
    }
}
