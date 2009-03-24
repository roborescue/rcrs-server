// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io.object;

public class BaseNode extends BaseVertex {
    public BaseNode(int id) { super(id); }

    //private boolean m_signal;
    //private int[] m_shortcutToTurn;
    //private int[] m_pocketToTurnAcross;
    //private int[] m_signalTiming;

    //public boolean signal() { return m_signal; }
    //public int[] shortcutToTurn() { return m_shortcutToTurn; }
    //public int[] pocketToTurnAcross() { return m_pocketToTurnAcross; }
    //public int[] signalTiming() { return m_signalTiming; }

    //public void setSignal(int value) { m_signal = (value != 0); }
    //public void setShortcutToTurn(int[] value) { m_shortcutToTurn = value; }
    //public void setPocketToTurnAcross(int[] value)
    //  { m_pocketToTurnAcross = value; }
    //public void setSignalTiming(int[] value) { m_signalTiming = value; }

    public void setProperty(int type, int[] value) {
        switch (type) {
        default: super.setProperty(type, value); break;
        //case PROPERTY_SIGNAL:               setSignal(value[0]);       break;
        //case PROPERTY_SHORTCUT_TO_TURN:     setShortcutToTurn(value);  break;
        //case PROPERTY_POCKET_TO_TURN_ACROSS:
        //    setPocketToTurnAcross(value);  break;
        //case PROPERTY_SIGNAL_TIMING:        setSignalTiming(value);    break;
        }
    }
}
