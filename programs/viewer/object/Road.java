package viewer.object;

import rescuecore.RescueConstants;

public class Road extends Edge {
  public Road(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_ROAD; }

  //private int m_roadKind;
  //private int m_carsPassToHead;
  //private int m_carsPassToTail;
  //private int m_humansPassToHead;
  //private int m_humansPassToTail;
  private int m_width;
  private int m_block;
  //private int m_repairCost;
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
  //public int repairCost() { return m_repairCost; }
  //public boolean medianStrip() { return m_medianStrip; }
  public int linesToHead() { return m_linesToHead; }
  public int linesToTail() { return m_linesToTail; }
  //public int widthForWalkers() { return m_widthForWalkers; }

  public int aliveLinesToHead() {
    int result = linesToHead() - blockedLines();
    return ((result >= 0) ? result : 0);
  }
  public int aliveLinesToTail() {
    int result = linesToTail() - blockedLines();
    return ((result >= 0) ? result : 0);
  }

  public int blockedLines() {
    double blockWidth = ((double) block()) / 2d;
    double linesBlockedRate = blockWidth / lineWidth();
    return (int) Math.floor(linesBlockedRate + 0.5d);
  }

  public double lineWidth() { return ((double) width()) / ((double) linesToHead() + linesToTail()); }

  public int aliveLinesTo(PointObject to)
    { return (to == head()) ? aliveLinesToHead() : aliveLinesToTail(); }
  public int linesTo(PointObject to)
    { return (to == head()) ? linesToHead() : linesToTail(); }

  //public void setRoadKind(int value) { m_roadKind = value; }
  //public void setCarsPassToHead(int value)   { m_carsPassToHead = value; }
  //public void setCarsPassToTail(int value)   { m_carsPassToTail = value; }
  //public void setHumansPassToHead(int value) { m_humansPassToHead = value; }
  //public void setHumansPassToTail(int value) { m_humansPassToTail = value; }
  public void setWidth(int value) { m_width = value; }
  public void setBlock(int value) { m_block = value; }
  //public void setRepairCost(int value)  { m_repairCost = value; }
  //public void setMedianStrip(int value) { m_medianStrip = (value!=0); }
  public void setLinesToHead(int value) { m_linesToHead = value; }
  public void setLinesToTail(int value) { m_linesToTail = value; }
  //public void setWidthForWalkers(int value) { m_widthForWalkers = value; }

  public void input(int property, int[] value) {
    switch(property) {
    default: super.input(property, value); break;
    //case PROPERTY_ROAD_KIND:          setRoadKind(value[0]);        break;
    //case PROPERTY_CARS_PASS_TO_HEAD:  setCarsPassToHead(value[0]);  break;
    //case PROPERTY_CARS_PASS_TO_TAIL:  setCarsPassToTail(value[0]);  break;
    //case PROPERTY_HUMANS_PASS_TO_HEAD:setHumansPassToHead(value[0]);break;
    //case PROPERTY_HUMANS_PASS_TO_TAIL:setHumansPassToTail(value[0]);break;
    case RescueConstants.PROPERTY_WIDTH:              setWidth(value[0]);           break;
    case RescueConstants.PROPERTY_BLOCK:              setBlock(value[0]);           break;
    //case PROPERTY_REPAIR_COST:        setRepairCost(value[0]);      break;
    //case PROPERTY_MEDIAN_STRIP:       setMedianStrip(value[0]);     break;
    case RescueConstants.PROPERTY_LINES_TO_HEAD:      setLinesToHead(value[0]);     break;
    case RescueConstants.PROPERTY_LINES_TO_TAIL:      setLinesToTail(value[0]);     break;
    //case PROPERTY_WIDTH_FOR_WALKERS:  setWidthForWalkers(value[0]); break;
    }
  }
}
