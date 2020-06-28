package maps.gml.view;

import maps.gml.GMLShape;
import maps.gml.GMLRoad;
import maps.gml.GMLBuilding;
import maps.gml.GMLSpace;
import maps.gml.GMLCoordinates;

import rescuecore2.misc.gui.ScreenTransform;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Polygon;
import java.util.List;

/**
   This class knows how to decorate buildings, roads and spaces.
*/
public class FilledShapeDecorator implements BuildingDecorator, RoadDecorator, SpaceDecorator {
    private Color buildingColour;
    private Color roadColour;
    private Color spaceColour;

    /**
       Construct a FilledShapeDecorator.
       @param buildingColour The colour of buildings.
       @param roadColour The colour of roads.
       @param spaceColour The colour of spaces.
    */
    public FilledShapeDecorator(Color buildingColour, Color roadColour, Color spaceColour) {
        this.buildingColour = buildingColour;
        this.roadColour = roadColour;
        this.spaceColour = spaceColour;
    }

    @Override
    public void decorate(GMLBuilding building, Graphics2D g, ScreenTransform transform) {
        if (buildingColour == null) {
            return;
        }
        g.setColor(buildingColour);
        draw(building, g, transform);
    }

    @Override
    public void decorate(GMLRoad road, Graphics2D g, ScreenTransform transform) {
        if (roadColour == null) {
            return;
        }
        g.setColor(roadColour);
        draw(road, g, transform);
    }

    @Override
    public void decorate(GMLSpace space, Graphics2D g, ScreenTransform transform) {
        if (spaceColour == null) {
            return;
        }
        g.setColor(spaceColour);
        draw(space, g, transform);
    }

    private void draw(GMLShape shape, Graphics2D g, ScreenTransform transform) {
        List<GMLCoordinates> coords = shape.getUnderlyingCoordinates();
        int n = coords.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        int i = 0;
        for (GMLCoordinates next : coords) {
            xs[i] = transform.xToScreen(next.getX());
            ys[i] = transform.yToScreen(next.getY());
            ++i;
        }
        g.fill(new Polygon(xs, ys, n));
    }

    public Color getBuildingColour() {
        return buildingColour;
    }
}