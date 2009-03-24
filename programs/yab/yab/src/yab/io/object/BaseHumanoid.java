// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io.object;

public abstract class BaseHumanoid extends BaseMovingObject {
    public BaseHumanoid(int id) { super(id); }

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

    public void setProperty(int type, int[] value) {
        switch (type) {
        default: super.setProperty(type, value); break;
        //case PROPERTY_STAMINA:    setStamina(value[0]);     break;
        case PROPERTY_HP:         setHp(value[0]);          break;
        case PROPERTY_DAMAGE:     setDamage(value[0]);      break;
        case PROPERTY_BURIEDNESS: setBuriedness(value[0]);  break;
        }
    }
}
