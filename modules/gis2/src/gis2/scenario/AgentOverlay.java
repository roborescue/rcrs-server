package gis2.scenario;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Map;

import maps.gml.GMLShape;
import maps.gml.view.Overlay;

import rescuecore2.misc.collections.LazyMap;
import rescuecore2.misc.gui.ScreenTransform;

/**
 * Overlay for viewing agents in a scenario.
 */
public class AgentOverlay implements Overlay {
  private static final int SIZE = 11;
  private static final Color CIVILIAN_COLOUR = Color.GREEN;
  private static final Color FIRE_BRIGADE_COLOUR = Color.RED;
  private static final Color POLICE_FORCE_COLOUR = Color.BLUE;
  private static final Color AMBULANCE_TEAM_COLOUR = Color.WHITE;
  private static final int OFFSET = 7;
  private ScenarioEditor editor;

  /**
   * Construct an AgentOverlay.
   *
   * @param editor The scenario editor.
   */
  public AgentOverlay(ScenarioEditor editor) {
    this.editor = editor;
  }

  @Override
  public void render(Graphics2D g, ScreenTransform transform) {
    // Count agents in each location
    g.setFont(new Font(g.getFont().getName(), Font.BOLD, g.getFont().getSize()));
    Map<Integer, Integer> civs = new LazyMap<Integer, Integer>() {
      @Override
      public Integer createValue() {
        return 0;
      }
    };
    Map<Integer, Integer> fbs = new LazyMap<Integer, Integer>() {
      @Override
      public Integer createValue() {
        return 0;
      }
    };
    Map<Integer, Integer> pfs = new LazyMap<Integer, Integer>() {
      @Override
      public Integer createValue() {
        return 0;
      }
    };
    Map<Integer, Integer> ats = new LazyMap<Integer, Integer>() {
      @Override
      public Integer createValue() {
        return 0;
      }
    };
    for (int next : editor.getScenario().getCivilians()) {
      civs.put(next, civs.get(next) + 1);
    }
    for (int next : editor.getScenario().getFireBrigades()) {
      fbs.put(next, fbs.get(next) + 1);
    }
    for (int next : editor.getScenario().getPoliceForces()) {
      pfs.put(next, pfs.get(next) + 1);
    }
    for (int next : editor.getScenario().getAmbulanceTeams()) {
      ats.put(next, ats.get(next) + 1);
    }
    // Now draw them
    for (Map.Entry<Integer, Integer> next : civs.entrySet()) {
      GMLShape shape = editor.getMap().getShape(next.getKey());
      int count = next.getValue();
      // int x = transform.xToScreen(shape.getCentreX());
      // int y = transform.yToScreen(shape.getCentreY()) + CIV_OFFSET;
      int x = transform.xToScreen(shape.getCentreX()) + OFFSET;
      int y = transform.yToScreen(shape.getCentreY());
      // g.drawString(count + " civs", x, y);
      paint(g, x, y, CIVILIAN_COLOUR);
      g.drawString(count + "", x, y);

    }
    for (Map.Entry<Integer, Integer> next : fbs.entrySet()) {
      GMLShape shape = editor.getMap().getShape(next.getKey());
      int count = next.getValue();
      // int x = transform.xToScreen(shape.getCentreX());
      // int y = transform.yToScreen(shape.getCentreY()) + FB_OFFSET;
      int x = transform.xToScreen(shape.getCentreX());
      int y = transform.yToScreen(shape.getCentreY()) - OFFSET;
      // g.drawString(count + " fbs", x, y);
      paint(g, x, y, FIRE_BRIGADE_COLOUR);
      g.drawString(count + "", x, y);
    }
    for (Map.Entry<Integer, Integer> next : pfs.entrySet()) {
      GMLShape shape = editor.getMap().getShape(next.getKey());
      int count = next.getValue();
      // int x = transform.xToScreen(shape.getCentreX());
      // int y = transform.yToScreen(shape.getCentreY()) + PF_OFFSET;
      int x = transform.xToScreen(shape.getCentreX());
      int y = transform.yToScreen(shape.getCentreY()) + OFFSET;
      // g.drawString(count + " pfs", x, y);
      paint(g, x, y, POLICE_FORCE_COLOUR);
      g.drawString(count + "", x, y);
    }
    for (Map.Entry<Integer, Integer> next : ats.entrySet()) {
      GMLShape shape = editor.getMap().getShape(next.getKey());
      int count = next.getValue();
      // int x = transform.xToScreen(shape.getCentreX());
      // int y = transform.yToScreen(shape.getCentreY()) + AT_OFFSET;
      int x = transform.xToScreen(shape.getCentreX()) - OFFSET;
      int y = transform.yToScreen(shape.getCentreY());
      // g.drawString(count + " ats", x, y);
      paint(g, x, y, AMBULANCE_TEAM_COLOUR);
      g.drawString(count + "", x, y);

    }
  }

  public void paint(Graphics2D g, int x, int y, Color color) {
    Shape shape = new Ellipse2D.Double(x - SIZE / 4, y - SIZE, SIZE, SIZE);
    g.setColor(color);
    g.fill(shape);
    g.draw(shape);
    g.setColor(Color.black);
  }

}