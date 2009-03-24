package traffic.object;

import rescuecore.RescueConstants;

public class FireBrigade extends Humanoid {
  public FireBrigade(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_FIRE_BRIGADE; }

  //private int m_waterQuantity;
  //private int m_stretchedLength;

  //public int waterQuantity()   { return m_waterQuantity; }
  //public int stretchedLength() { return m_stretchedLength; }
  //public void setWaterQuantity(int value)   { m_waterQuantity = value; }
  //public void setStretchedLength(int value) { m_stretchedLength = value; }

  public void input(int property, int[] value) {
    switch(property) {
    default: super.input(property, value); break;
    //case PROPERTY_WATER_QUANTITY:   setWaterQuantity(value[0]);    break;
    //case PROPERTY_STRETCHED_LENGTH: setStretchedLength(value[0]);  break;
    }
  }
}
