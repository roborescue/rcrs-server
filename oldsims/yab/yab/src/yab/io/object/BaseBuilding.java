// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io.object;

public class BaseBuilding extends BasePointObject {
    public BaseBuilding(int id) { super(id); }

    private int m_floors;
    private int m_buildingAttributes;
    private boolean m_ignition;
    private int m_fieryness;
	private int m_heat;
    private int m_brokenness;
    //private int[] m_entrances;
    private int m_entrance;
    private int m_buildingCode;
    private int m_buildingAreaGround;
    private int m_buildingAreaTotal;
    private int[] m_buildingApexes;

    public int floors() { return m_floors; }
    public int buildingAttributes() { return m_buildingAttributes; }
    public boolean ignition() { return m_ignition; }
    public int fieryness()    { return m_fieryness; }
	public int heat()	{ return m_heat;}
    public int brokenness()   { return m_brokenness; }
    //public int[] entrances() { return m_entrances; }
    /** The entrances property may consist of more than one entrances
     *  in the specification, but a building has one entrance informally.
     */
    public int entrance() { return m_entrance; }
    public int buildingCode() { return m_buildingCode; }
    public int buildingAreaGround() { return m_buildingAreaGround; }
    public int buildingAreaTotal() { return m_buildingAreaTotal; }
    public int[] buildingApexes() { return m_buildingApexes; }

    public void setFloors(int value) { m_floors = value; }
    public void setBuildingAttributes(int value)
      { m_buildingAttributes = value; }
    public void setIgnition(int value)   { m_ignition = (value != 0); }
    public void setFieryness(int value)  { m_fieryness = value; }
	public void setHeat (int value) { m_heat = value;}
    public void setBrokenness(int value) { m_brokenness = value; }
    //public void setEntrances(int[] value) { m_entrances = value; }
    public void setEntrance(int value) { m_entrance = value; }
    public void setBuildingCode(int value) { m_buildingCode = value; }
    public void setBuildingAreaGround(int value) {
        m_buildingAreaGround = value;
    }
    public void setBuildingAreaTotal(int value) { m_buildingAreaTotal=value; }
    public void setBuildingApexes(int[] value)  { m_buildingApexes = value; }

    public void setProperty(int type, int[] value) {
        switch (type) {
        default: super.setProperty(type, value); break;
        case PROPERTY_FLOORS:              setFloors(value[0]);        break;
        case PROPERTY_BUILDING_ATTRIBUTES: setBuildingAttributes(value[0]); break;
        case PROPERTY_IGNITION:            setIgnition(value[0]);      break;
        case PROPERTY_FIERYNESS:           setFieryness(value[0]);     break;
        case PROPERTY_BROKENNESS:          setBrokenness(value[0]);    break;
        //case PROPERTY_ENTRANCES:           setEntrances(value);        break;
        case PROPERTY_ENTRANCES:           setEntrance(value[0]);      break;
        case PROPERTY_BUILDING_CODE:       setBuildingCode(value[0]);  break;
        case PROPERTY_BUILDING_AREA_GROUND:setBuildingAreaGround(value[0]);  break;
        case PROPERTY_BUILDING_AREA_TOTAL: setBuildingAreaTotal(value[0]);   break;
        case PROPERTY_BUILDING_APEXES:     setBuildingApexes(value);   break;
	case PROPERTY_HEAT:		    setHeat (value[0]); break;
        }
    }
}
