package gis2.scenario;

import java.awt.Graphics2D;
import java.util.Map;

import maps.gml.view.Overlay;
import maps.gml.GMLShape;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.misc.collections.LazyMap;

/**
   Overlay for viewing agents in a scenario.
*/
public class AgentOverlay implements Overlay {
    private static final int CIV_OFFSET = 0;
    private static final int FB_OFFSET = 10;
    private static final int PF_OFFSET = 20;
    private static final int AT_OFFSET = 30;

    private ScenarioEditor editor;

    /**
       Construct an AgentOverlay.
       @param editor The scenario editor.
    */
    public AgentOverlay(ScenarioEditor editor) {
        this.editor = editor;
    }

    @Override
    public void render(Graphics2D g, ScreenTransform transform) {
        // Count agents in each location
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
            int x = transform.xToScreen(shape.getCentreX());
            int y = transform.yToScreen(shape.getCentreY()) + CIV_OFFSET;
            g.drawString(count + " civs", x, y);
        }
        for (Map.Entry<Integer, Integer> next : fbs.entrySet()) {
            GMLShape shape = editor.getMap().getShape(next.getKey());
            int count = next.getValue();
            int x = transform.xToScreen(shape.getCentreX());
            int y = transform.yToScreen(shape.getCentreY()) + FB_OFFSET;
            g.drawString(count + " fbs", x, y);
        }
        for (Map.Entry<Integer, Integer> next : pfs.entrySet()) {
            GMLShape shape = editor.getMap().getShape(next.getKey());
            int count = next.getValue();
            int x = transform.xToScreen(shape.getCentreX());
            int y = transform.yToScreen(shape.getCentreY()) + PF_OFFSET;
            g.drawString(count + " pfs", x, y);
        }
        for (Map.Entry<Integer, Integer> next : ats.entrySet()) {
            GMLShape shape = editor.getMap().getShape(next.getKey());
            int count = next.getValue();
            int x = transform.xToScreen(shape.getCentreX());
            int y = transform.yToScreen(shape.getCentreY()) + AT_OFFSET;
            g.drawString(count + " ats", x, y);
        }
    }
}