package viewer.object;

import rescuecore.RescueConstants;

public abstract class Humanoid extends MovingObject {
  public Humanoid(int id) { super(id); }

  //private int m_stamina;
  private int m_hp;
  private int m_damage;
  private int m_buriedness;

  //public int stamina() { return m_stamina; }
  public int hp()      { return m_hp; }
  public int damage()  { return m_damage; }
  public int buriedness() { return m_buriedness; }

  //public void setStamina(int value)    { m_stamina = value; }
  public void setHp(int value)         { m_hp = value; }
  public void setDamage(int value)     { m_damage = value; }
  public void setBuriedness(int value) { m_buriedness = value; }

  public void input(int property, int[] value) {
    switch(property) {
    default: super.input(property, value); break;
    //case PROPERTY_STAMINA:    setStamina(value[0]);     break;
    case RescueConstants.PROPERTY_HP:         setHp(value[0]);	        break;
    case RescueConstants.PROPERTY_DAMAGE:     setDamage(value[0]);      break;
    case RescueConstants.PROPERTY_BURIEDNESS: setBuriedness(value[0]);  break;
    }
  }
}
