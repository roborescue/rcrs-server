package viewer.object;

import rescuecore.RescueConstants;

public class World extends VirtualObject {
  public World(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_WORLD; }

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

  public void input(int property, int[] value) {
    switch(property) {
    default: super.input(property, value); break;
    //case PROPERTY_START_TIME:     setStartTime(value[0]);      break;
    //case PROPERTY_LONGITUDE:      setLongitude(value[0]);      break;
    //case PROPERTY_LATITUDE:       setLatitude(value[0]);       break;
    //case PROPERTY_WIND_FORCE:     setWindForce(value[0]);      break;
    //case PROPERTY_WIND_DIRECTION: setWindDirection(value[0]);  break;
    }
  }
}
