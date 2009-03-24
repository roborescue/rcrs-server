// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io.object;

public class BaseWorld extends BaseVirtualObject {
    public BaseWorld(int id) { super(id); }

    //private int m_startTime;
    //private int m_longitude;
    //private int m_latitude;
    //private int m_windForce;
    //private int m_windDirection;

    //public int startTime() { return m_startTime; }
    //public int longitude() { return m_longitude; }
    //public int latitude() { return m_latitude; }
    //public int windForce() { return m_windForce; }
    //public int windDirection() { return m_windDirection; }

    //public void setStartTime(int value) { m_startTime = value; }
    //public void setLongitude(int value) { m_longitude = value; }
    //public void setLatitude(int value)  { m_latitude = value; }
    //public void setWindForce(int value) { m_windForce = value; }
    //public void setWindDirection(int value) { m_windDirection = value; }

    public void setProperty(int type, int[] value) {
        switch (type) {
        default: super.setProperty(type, value); break;
        //case PROPERTY_START_TIME:     setStartTime(value[0]);      break;
        //case PROPERTY_LONGITUDE:      setLongitude(value[0]);      break;
        //case PROPERTY_LATITUDE:       setLatitude(value[0]);       break;
        //case PROPERTY_WIND_FORCE:     setWindForce(value[0]);      break;
        //case PROPERTY_WIND_DIRECTION: setWindDirection(value[0]);  break;
        }
    }
}
