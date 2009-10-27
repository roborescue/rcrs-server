package traffic.object;

import rescuecore.RescueConstants;

public abstract class Humanoid extends MovingObject {
  public Humanoid(int id) { super(id); }

  private int m_hp;
  private int m_buriedness;

  public int hp()      { return m_hp; }
  public int buriedness() { return m_buriedness; }

  public void setHp(int value)         { m_hp = value; }
  public void setBuriedness(int value) {
	  m_buriedness = value;
  }

  public void input(String property, int[] value) {
      if ("HP".equals(property)) {
          setHp(value[0]);
      }
      else if ("BURIEDNESS".equals(property)) {
          setBuriedness(value[0]);
      }
      else {
          super.input(property, value);
      }
  }
}
