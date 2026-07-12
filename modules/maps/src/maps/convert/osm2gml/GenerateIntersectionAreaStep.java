package maps.convert.osm2gml;

import maps.convert.ConvertStep;
import maps.convert.osm2gml.debug.*;

import java.util.Collection;

/**
 * This step processes the intersection graph and generates the
 * geometric polygon areas for each intersection
 */
public class GenerateIntersectionAreaStep extends ConvertStep {
    private final TemporaryMap map;

    public GenerateIntersectionAreaStep(final TemporaryMap map) {
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Generating intersection areas";
    }

    @Override
    protected void step() {
        final Collection<OSMIntersectionInfo> intersections = map.getOSMIntersectionInfo();
        final double sizeOf1m = ConvertTools.sizeOf1Metre(map.getOSMMap());

        setProgressLimit(intersections.size());

        for (final OSMIntersectionInfo next : intersections) {
            next.process(sizeOf1m);
            bumpProgress();
        }

        setStatus("Generated polygon areas for " + intersections.size() + " intersections");
        visualizeResults();
    }

    private void visualizeResults() {
        StepVisualizer.create(debug)
                .title("Generate Intersection Areas")
                .layer(LineLayer.of(map.getOSMRoadInfo())
                        .name("OSM Roads")
                        .color(DebugPalette.MAIN_STROKE))
                .layer(PointLayer.of(map.getOSMIntersectionInfo())
                        .name("OSM Intersecions")
                        .color(DebugPalette.MAIN_STROKE))
                .layer(PolygonLayer.of(map.getOSMIntersectionInfo())
                        .name("Generated Intersection Polygons")
                        .outlineColor(DebugPalette.CREATED_STROKE)
                        .fillColor(DebugPalette.CREATED_FILL))
                .show();
    }
}
