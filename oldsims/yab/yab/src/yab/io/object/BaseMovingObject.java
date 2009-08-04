// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io.object;

public abstract class BaseMovingObject extends BaseRealObject {
    public BaseMovingObject(int id) { super(id); }

    private int m_position;
    private int m_positionExtra;
    //private int m_direction;
    //private int[] m_positionHistory;

    public int position() { return m_position; }
    public int positionExtra() { return m_positionExtra; }
    //public int direction() { return m_direction; }
    //public int[] positionHistory() { return m_positionHistory; }

    public void setPosition(int value) { m_position = value; }
    public void setPositionExtra(int value) { m_positionExtra = value; }
    //public void setDirection(int value) { m_direction = value; }
    //public void setPositionHistory(int[] value) {
    //    m_positionHistory = value;
    //}

    public void setProperty(int type, int[] value) {
        switch (type) {
        default: super.setProperty(type, value); break;
        case PROPERTY_POSITION:         setPosition(value[0]);      break;
        case PROPERTY_POSITION_EXTRA:   setPositionExtra(value[0]); break;
        //case PROPERTY_DIRECTION:        setDirection(value[0]);     break;
        //case PROPERTY_POSITION_HISTORY: setPositionHistory(value);  break;
        }
    }
}
