package viewer.object;

import java.util.*;
import rescuecore.RescueConstants;

public class Building extends PointObject {
  public Building(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_BUILDING; }

  //private int m_floors;
  //private int m_buildingAttributes;
  //private boolean m_ignition;
  private int m_fieryness;
  private int m_brokenness;
  private int[] m_entrances;
  private HashSet m_entranceSet = null;
  //private int m_buildingCode;
  //private int m_buildingAreaGround;
  private int m_buildingAreaTotal;
  private int[] m_buildingApexes;
	private int m_importance;

  //public int floors() { return m_floors; }
  //public int buildingAttributes() { return m_buildingAttributes; }
  //public boolean ignition() { return m_ignition; }
  /** 0: no-burned
   *                  burning time rate
   *  1: burning1    (0.00 *** 0.33                   : yellow)
   *  2: burning2    (         0.33 *** 0.67          : red)
   *  3: burning3    (                  0.67 *** 1.00 : purple)
   *                  burned
   *  5: burned-out1 (*** 20%             : cyan)
   *  6: burned-out2 (    20% *** 70%     : blue)
   *  7: burned-out3 (            70% *** : gray)
   */
  public int fieryness()    { return m_fieryness; }
  public int brokenness()   { return m_brokenness; }
  public int importance()   { return m_importance; }
  public HashSet entrances() {
    if (m_entranceSet != null)
      return m_entranceSet;
    m_entranceSet = new HashSet(m_entrances.length, 1);
    for (int i = 0;  i < m_entrances.length;  i ++)
      m_entranceSet.add(WORLD.get(m_entrances[i]));
    m_entrances = null;
    return m_entranceSet;
  }
  //public int buildingCode() { return m_buildingCode; }
  //public int buildingAreaGround() { return m_buildingAreaGround; }
  public int buildingAreaTotal() { return m_buildingAreaTotal; }
  public int[] buildingApexes() { return m_buildingApexes; }

  //public void setFloors(int value) { m_floors = value; }
  //public void setBuildingAttributes(int value)
  //  { m_buildingAttributes = value; }
  //public void setIgnition(int value)   { m_ignition = (value != 0); }
  public void setFieryness(int value)  { m_fieryness = value; }
  public void setBrokenness(int value) { m_brokenness = value; }
  public void setEntrances(int[] value) {
    if (m_entranceSet == null  &&  m_entrances == null)
      m_entrances = value;
  }
  //public void setBuildingCode(int value) { m_buildingCode = value; }
  //public void setBuildingAreaGround(int value)
  //  { m_buildingAreaGround = value; }
  public void setBuildingAreaTotal(int value) { m_buildingAreaTotal=value; }
  public void setBuildingApexes(int[] value)  { m_buildingApexes = value; }
  public void setBuildingImportance(int value) { m_importance=value; }

  public void input(int property, int[] value) {
    switch(property) {
      default: super.input(property, value); break;
	//case PROPERTY_FLOORS:              setFloors(value[0]);            break;
	//case PROPERTY_BUILDING_ATTRIBUTES: setBuildingAttributes(value[0]);break;
	//case PROPERTY_IGNITION:            setIgnition(value[0]);          break;
      case RescueConstants.PROPERTY_FIERYNESS:           setFieryness(value[0]);         break;
      case RescueConstants.PROPERTY_BROKENNESS:          setBrokenness(value[0]);        break;
      case RescueConstants.PROPERTY_ENTRANCES:           setEntrances(value);            break;
	//case PROPERTY_BUILDING_CODE:       setBuildingCode(value[0]);      break;
	//case PROPERTY_BUILDING_AREA_GROUND:setBuildingAreaGround(value[0]);break;
      case RescueConstants.PROPERTY_BUILDING_AREA_TOTAL: setBuildingAreaTotal(value[0]); break;
      case RescueConstants.PROPERTY_BUILDING_APEXES:     setBuildingApexes(value);       break;
	case RescueConstants.PROPERTY_BUILDING_IMPORTANCE: setBuildingImportance(value[0]); break;
    }
  }

  public boolean isNoBurned() { return (fieryness() == 0); }
  public boolean isBurning()  { return (1 <= fieryness()  &&  fieryness() <= 3); }
  public boolean isPutOffOrBurnedOut() { return (5 <= fieryness()  &&  fieryness() <= 7); }

  public boolean isAdjacentTo(MotionlessObject obj)
    { return entrances().contains(obj); }

  public HashSet links() { return entrances(); }
}
