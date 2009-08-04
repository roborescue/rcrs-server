package traffic.object;

import rescuecore.RescueConstants;

public class AmbulanceTeam extends Humanoid {
  public AmbulanceTeam(int id) { super(id); }
  public int type() { return RescueConstants.TYPE_AMBULANCE_TEAM; }

  private MovingObject m_loadingMvObj = null;
  private boolean m_doLoad   = false;
  private boolean m_doUnload = false;
  public void setLoad(MovingObject mv) { m_doLoad = true;   m_loadingMvObj = mv;   }
  public void resetLoad()              { m_doLoad = false;  m_loadingMvObj = null; }
  public void load()                   { m_doLoad = false;                         }
  public void setUnload() { m_doUnload = true;                          }
  public void unload()    { m_doUnload = false;  m_loadingMvObj = null; }
  public boolean isLoading() { return m_loadingMvObj instanceof MovingObject; }
  public MovingObject loadingMvObj() { return m_loadingMvObj; }
  public boolean doLoad()   { return m_doLoad; }
  public boolean doUnload() { return m_doUnload; }
}
