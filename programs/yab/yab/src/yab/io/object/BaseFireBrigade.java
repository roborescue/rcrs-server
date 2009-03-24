// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io.object;

public class BaseFireBrigade extends BaseHumanoid {
    public BaseFireBrigade(int id) { super(id); }

    //private int m_waterQuantity;
    //private int m_stretchedLength;

    //public int waterQuantity()   { return m_waterQuantity; }
    //public int stretchedLength() { return m_stretchedLength; }

    //public void setWaterQuantity(int value)   { m_waterQuantity = value; }
    //public void setStretchedLength(int value) { m_stretchedLength = value; }

    public void setProperty(int type, int[] value) {
        switch (type) {
        default: super.setProperty(type, value); break;
        //case PROPERTY_WATER_QUANTITY:   setWaterQuantity(value[0]);   break;
        //case PROPERTY_STRETCHED_LENGTH: setStretchedLength(value[0]); break;
        }
    }
}
