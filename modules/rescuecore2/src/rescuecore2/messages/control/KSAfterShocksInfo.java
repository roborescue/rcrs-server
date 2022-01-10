package rescuecore2.messages.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.components.FloatListComponent;
import rescuecore2.messages.components.IntListComponent;
import rescuecore2.scenario.Scenario;
import rescuecore2.scenario.compatibilities.CollapseSimCompatibaleScenarioV1_1;
import rescuecore2.scenario.exceptions.UncompatibleScenarioException;

/**
 *
 * The Message class that contains after shocks' information and is sent from
 * kernel to collapse simulator in early cycles
 *
 * @author Salim
 *
 */
public class KSAfterShocksInfo extends AbstractMessage {
  private IntListComponent times;
  private FloatListComponent intensities;

  /**
   * Reads the message from the inputstream and initiates its data.
   *
   * @param in
   * @throws IOException
   */
  public KSAfterShocksInfo(InputStream in) throws IOException {
    this();
    read(in);
  }

  /**
   * Creates a message from the input scenario.
   *
   * @param aftershocks
   * @throws UncompatibleScenarioException
   */
  public KSAfterShocksInfo(Scenario scenario) throws UncompatibleScenarioException {
    this();
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
    this.times.setValues(times);
    this.intensities.setValues(intensities);

  }

  /**
   * Creates a message from the input map.
   *
   * @param aftershocks
   */
  public KSAfterShocksInfo(HashMap<Integer, Float> aftershocks) {
    this();
    List<Integer> times = new ArrayList<Integer>();
    List<Float> intensities = new ArrayList<Float>();
    for (Entry<Integer, Float> e : aftershocks.entrySet()) {
      times.add(e.getKey());
      intensities.add(e.getValue());
    }
    this.times.setValues(times);
    this.intensities.setValues(intensities);

  }

  /**
   * Initiates an empty Message
   */
  protected KSAfterShocksInfo() {
    super(ControlMessageURN.KS_AFTERSHOCKS_INFO);
    intensities = new FloatListComponent(ControlMessageComponentURN.INTENSITIES);
    times = new IntListComponent(ControlMessageComponentURN.TIMES);
    addMessageComponent(times);
    addMessageComponent(intensities);
  }

  /**
   * Returns a map containing aftershocks' information. Key and value of the map
   * are time and intensity.
   *
   * @return float
   */
  public HashMap<Integer, Float> getAftershocks() {
    HashMap<Integer, Float> map = new HashMap<Integer, Float>();
    for (int i = 0; i < times.getValues().size(); i++) {
      map.put(times.getValues().get(i), intensities.getValues().get(i));
    }
    return map;
  }
}