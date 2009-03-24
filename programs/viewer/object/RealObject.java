package viewer.object;

import java.util.*;
import rescuecore.InputBuffer;

public abstract class RealObject extends RescueObject {
  public RealObject(int id) { super(id); }

  public abstract int x();
  public abstract int y();

  public abstract MotionlessObject motionlessPosition();

  // ------------------------------------------------------------------- action
  private int m_lastActingTime = -1;
  private int m_actionType;
  private byte[] m_actionData;

  public int actionType() {
    return (m_lastActingTime == WORLD.time()) ? m_actionType : -1;
  }
  public InputBuffer actionData() {
    return (m_lastActingTime == WORLD.time())
      ? new InputBuffer(m_actionData)
      : null;
  }

  public void setAction(int command, byte[] actionData) {
    m_lastActingTime = WORLD.time();
    m_actionType = command;
    m_actionData = actionData;
  }

  private int m_lastCommunicatingTime = -1;

  private final ArrayList m_communicationDataList = new ArrayList();
  public ArrayList communicationDataList() {
    if (m_lastCommunicatingTime != WORLD.time())
      m_communicationDataList.clear();
    return m_communicationDataList;
  }

  public class Communication {
    public final int command;
    private final byte[] m_data;

    public Communication(int command, byte[] communicationData) {
      this.command = command;
      m_data = communicationData;
    }

    public InputBuffer communicationData() {
		return new InputBuffer(m_data);
    }
  }

  public void setCommunication(int command, byte[] communicationData) {
    m_lastCommunicatingTime = WORLD.time();
    communicationDataList().add(new Communication(command, communicationData));
  }
}
