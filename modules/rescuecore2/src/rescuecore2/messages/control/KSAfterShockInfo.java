package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.control.ControlMessageProto.KSAfterShockInfoProto;
import rescuecore2.scenario.Scenario;
import rescuecore2.scenario.compatibilities.CollapseSimCompatibaleScenarioV1_1;
import rescuecore2.scenario.exceptions.UncompatibleScenarioException;

/**
 *
 * The Message class that contains after shocks' information and is sent from
 * kernel to collapse simulator in early cycles
 *
 */
public class KSAfterShockInfo extends AbstractMessage {

  private List<Integer> times;
  private List<Float> intensities;

  /**
   * Reads the message from the input stream and initiates its data.
   *
   * @param in
   * @throws IOException
   */
  public KSAfterShockInfo(InputStream in) throws IOException {
    super(ControlMessageURN.KS_AFTERSHOCK_INFO.toString());
    this.read(in);
  }

  /**
   * Creates a message from the input scenario.
   *
   * @param aftershocks
   * @throws UncompatibleScenarioException
   */
  public KSAfterShockInfo(Scenario scenario) throws UncompatibleScenarioException {
    super(ControlMessageURN.KS_AFTERSHOCK_INFO.toString());
    HashMap<Integer, Float> aftershocks = null;
    if (scenario instanceof CollapseSimCompatibaleScenarioV1_1) {
      aftershocks = ((CollapseSimCompatibaleScenarioV1_1) scenario).getAftershocks();
    } else {
      UncompatibleScenarioException e = new UncompatibleScenarioException();
      throw e;
    }
    List<Integer> times = new ArrayList<Integer>();
    List<Float> intensities = new ArrayList<Float>();
    for (Entry<Integer, Float> e : aftershocks.entrySet()) {
      times.add(e.getKey());
      intensities.add(e.getValue());
    }
    this.times = times;
    this.intensities = intensities;

  }

  /**
   * Creates a message from the input map.
   *
   * @param aftershocks
   */
  public KSAfterShockInfo(HashMap<Integer, Float> aftershocks) {
    super(ControlMessageURN.KS_AFTERSHOCK_INFO.toString());
    List<Integer> times = new ArrayList<Integer>();
    List<Float> intensities = new ArrayList<Float>();
    for (Entry<Integer, Float> e : aftershocks.entrySet()) {
      times.add(e.getKey());
      intensities.add(e.getValue());
    }
    this.times = times;
    this.intensities = intensities;

  }

  /**
   * Returns a map containing aftershocks' information. Key and value of the map
   * are time and intensity.
   *
   * @return After shocks
   */
  public HashMap<Integer, Float> getAftershocks() {
    HashMap<Integer, Float> map = new HashMap<Integer, Float>();
    for (int i = 0; i < this.times.size(); i++) {
      map.put(this.times.get(i), this.intensities.get(i));
    }
    return map;
  }

  @Override
  public void write(OutputStream out) throws IOException {
    KSAfterShockInfoProto ksAfterShockInfo = KSAfterShockInfoProto.newBuilder().addAllTimes(this.times)
        .addAllIntensities(this.intensities).build();

    ksAfterShockInfo.writeTo(out);
  }

  @Override
  public void read(InputStream in) throws IOException {
    KSAfterShockInfoProto ksAfterShockInfo = KSAfterShockInfoProto.parseFrom(in);

    this.times = new ArrayList<Integer>(ksAfterShockInfo.getTimesList());
    this.intensities = new ArrayList<Float>(ksAfterShockInfo.getIntensitiesList());
  }
}