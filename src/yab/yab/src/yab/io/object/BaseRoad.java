// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io.object;

public class BaseRoad extends BaseEdge {
    public BaseRoad(int id) { super(id); }

    //private int m_roadKind;
    //private int m_carsPassToHead;
    //private int m_carsPassToTail;
    //private int m_humansPassToHead;
    //private int m_humansPassToTail;
    private int m_width;
    private int m_block;
    private int m_repairCost;
    //private boolean m_medianStrip;
    private int m_linesToHead;
    private int m_linesToTail;
    //private int m_widthForWalkers;

    //public int roadKind() { return m_roadKind; }
    //public int carsPassToHead() { return m_carsPassToHead; }
    //public int carsPassToTail() { return m_carsPassToTail; }
    //public int humansPassToHead() { return m_humansPassToHead; }
    //public int humansPassToTail() { return m_humansPassToTail; }
    public int width() { return m_width; }
    public int block() { return m_block; }
    public int repairCost() { return m_repairCost; }
    //public boolean medianStrip() { return m_medianStrip; }
    public int linesToHead() { return m_linesToHead; }
    public int linesToTail() { return m_linesToTail; }
    //public int widthForWalkers() { return m_widthForWalkers; }

    //public void setRoadKind(int value) { m_roadKind = value; }
    //public void setCarsPassToHead(int value)   { m_carsPassToHead = value; }
    //public void setCarsPassToTail(int value)   { m_carsPassToTail = value; }
    //public void setHumansPassToHead(int value) { m_humansPassToHead = value;}
    //public void setHumansPassToTail(int value) { m_humansPassToTail = value;}
    public void setWidth(int value) { m_width = value; }
    public void setBlock(int value) { m_block = value; }
    public void setRepairCost(int value)  { m_repairCost = value; }
    //public void setMedianStrip(int value) { m_medianStrip = (value!=0); }
    public void setLinesToHead(int value) { m_linesToHead = value; }
    public void setLinesToTail(int value) { m_linesToTail = value; }
    //public void setWidthForWalkers(int value) { m_widthForWalkers = value; }

    public void setProperty(int type, int[] value) {
        switch (type) {
        default: super.setProperty(type, value); break;
        //case PROPERTY_ROAD_KIND:          setRoadKind(value[0]);       break;
        //case PROPERTY_CARS_PASS_TO_HEAD:  setCarsPassToHead(value[0]); break;
        //case PROPERTY_CARS_PASS_TO_TAIL:  setCarsPassToTail(value[0]); break;
        //casePROPERTY_HUMANS_PASS_TO_HEAD:setHumansPassToHead(value[0]);break;
        //casePROPERTY_HUMANS_PASS_TO_TAIL:setHumansPassToTail(value[0]);break;
        case PROPERTY_WIDTH:              setWidth(value[0]);           break;
        case PROPERTY_BLOCK:              setBlock(value[0]);           break;
        case PROPERTY_REPAIR_COST:        setRepairCost(value[0]);      break;
        //case PROPERTY_MEDIAN_STRIP:       setMedianStrip(value[0]);    break;
        case PROPERTY_LINES_TO_HEAD:      setLinesToHead(value[0]);     break;
        case PROPERTY_LINES_TO_TAIL:      setLinesToTail(value[0]);     break;
        //case PROPERTY_WIDTH_FOR_WALKERS: setWidthForWalkers(value[0]); break;
        }
    }
}
