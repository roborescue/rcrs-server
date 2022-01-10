package firesimulator.gui;

import java.util.HashMap;

/**
 *
 * Created by Alireza Kandeh on March 2018
 */

public class GUILayerFactory {

  private static GUILayerFactory _instance = null;

  private HashMap<String, GUILayer> layers = new HashMap<String, GUILayer>();

  public static GUILayerFactory getInstance() {
    if (_instance == null) {
      _instance = new GUILayerFactory();
    }
    return _instance;
  }

  public void addLayer(String name, Class c) {
    try {
      layers.put(name, (GUILayer) c.getDeclaredConstructor().newInstance());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public GUILayer getLayer(String name) {
    return layers.get(name);
  }
}