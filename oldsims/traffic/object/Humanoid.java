package traffic.object;

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
}
