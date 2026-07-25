package maps.convert.osm2gml;

import maps.convert.ConvertStep;
import maps.convert.osm2gml.debug.DebugPalette;
import maps.convert.osm2gml.debug.PolygonLayer;
import maps.convert.osm2gml.debug.StepVisualizer;

import java.awt.geom.Area;
import java.util.*;
import java.util.List;

import static maps.convert.osm2gml.ConvertTools.areaToTemporaryPassableShapes;

/**
 * This step cleans up overlaps between buildings and roads by subtracting
 * the building shapes from the road shapes.
 */
public class CleanBuildingOverlapsStep extends ConvertStep {
    private final TemporaryMap map;

    public CleanBuildingOverlapsStep(TemporaryMap map) {
        this.map = map;
    }


    @Override
    public String getDescription() {
        return "Cleaning overlaps between buildings and passable areas";
    }

    @Override
    protected void step() {
        Collection<TemporaryBuilding> buildings = map.getBuildings();
        Collection<TemporaryObject> initialPassableShapes = new ArrayList<>(map.getAllPassableShapes());

        if (buildings.isEmpty() || initialPassableShapes.isEmpty()) {
            setStatus("No buildings or passable-areas to process.");
            return;
        }

        int cleanedCount = 0;
        List<TemporaryObject> newObjects = new ArrayList<>();
        List<TemporaryObject> objectsToRemove = new ArrayList<>();

        for (TemporaryObject shape : initialPassableShapes) {
            Area roadArea = new Area(shape.getShape());
            boolean modified = false;
            for (TemporaryBuilding building : buildings) {
                // Quick check using bounds for performance
                if (!shape.getBounds().intersects(building.getBounds())) {
                    continue;
                }

                Area buildingArea = new Area(building.getShape());
                Area intersection = new Area(roadArea);
                intersection.intersect(buildingArea);

                if (!intersection.isEmpty()) {
                    // Subtract the building's shape from the road
                    roadArea.subtract(buildingArea);
                    modified = true;
                }
            }
            if (modified) {
                objectsToRemove.add(shape);
                newObjects.addAll(areaToTemporaryPassableShapes(roadArea, shape, map));
                cleanedCount++;
            }
        }

        // Apply the changes to the map
        for (TemporaryObject object : objectsToRemove) {
            map.removeTemporaryObject(object);
        }
        for (TemporaryObject object : newObjects) {
            map.addTemporaryObject(object);
        }

        // After making major geometric changes, we must resynchronize
        // the map's entire low-level state with the new high-level objects.
        map.resynchronizeStateFromObjects();

        setStatus("Cleaned " + cleanedCount + " passable areas that overlapped with buildings.");
        visualizeResults(objectsToRemove, newObjects);
    }

    private void visualizeResults(List<TemporaryObject> removed, List<TemporaryObject> created) {
        StepVisualizer.create(debug)
                .title("Clean Building Overlaps")
                .layer(PolygonLayer.of(removed)
                        .name("Removed Roads/Intersections")
                        .fillColor(DebugPalette.CORAL_FILL)
                        .outlineColor(DebugPalette.CORAL_STROKE))
                .layer(PolygonLayer.of(created)
                        .name("Created Roads/Intersections")
                        .fillColor(DebugPalette.MOSS_FILL)
                        .outlineColor(DebugPalette.MOSS_STROKE))
                .backgroundLayer(PolygonLayer.of(map.getAllObjects())
                        .name("Objects")
                        .fillColor(DebugPalette.SLATE_FILL)
                        .outlineColor(DebugPalette.SLATE_STROKE))
                .show();
    }
}
