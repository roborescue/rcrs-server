// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io.object;

public abstract class BaseEdge extends BaseMotionlessObject {
    public BaseEdge(int id) { super(id); }

    private int m_head;
    private int m_tail;
    private int m_length;

    //public PointObject head() { return (PointObject) WORLD.get(m_head); }
    //public PointObject tail() { return (PointObject) WORLD.get(m_tail); }
    public int head() { return m_head; }
    public int tail() { return m_tail; }
    public int length() { return m_length; }

    public void setHead(int value)   { m_head = value; }
    public void setTail(int value)   { m_tail = value; }
    public void setLength(int value) { m_length = value; }

    public void setProperty(int type, int[] value) {
        switch (type) {
        default: super.setProperty(type, value);    break;
        case PROPERTY_HEAD:   setHead(value[0]);    break;
        case PROPERTY_TAIL:   setTail(value[0]);    break;
        case PROPERTY_LENGTH: setLength(value[0]);  break;
        }
    }
}
