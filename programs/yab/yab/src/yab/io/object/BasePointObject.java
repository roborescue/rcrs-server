// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io.object;

public abstract class BasePointObject extends BaseMotionlessObject {
    public BasePointObject(int id) { super(id); }

    private int m_x;
    private int m_y;
  
    public int x() { return m_x; }
    public int y() { return m_y; }

    public void setX(int value) { m_x = value; }
    public void setY(int value) { m_y = value; }

    public void setProperty(int type, int[] value) {
        switch (type) {
        default: super.setProperty(type, value); break;
        case PROPERTY_X: setX(value[0]);  break;
        case PROPERTY_Y: setY(value[0]);  break;
        }
    }
}
