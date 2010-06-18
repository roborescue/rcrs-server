package maps.validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import maps.gml.GMLMap;
import maps.gml.GMLShape;

import com.vividsolutions.jts.geom.Geometry;

/**
   Validate the correctness of basic shape properties.
   @author goebelbe
 */
public class GMLShapeValidator implements MapValidator<GMLMap> {

    @Override
    public Collection<ValidationError> validate(GMLMap map) {
        List<ValidationError> errors = new ArrayList<ValidationError>();
        List<Geometry> polygons = new ArrayList<Geometry>();
        List<GMLShape> shapes = new ArrayList<GMLShape>(map.getAllShapes());

        for (GMLShape shape : shapes) {
            try {
                Geometry polygon = checkShape(shape);
                polygons.add(polygon);
            }
            catch (ValidationException e) {
                errors.add(e.getError());
                polygons.add(null);
            }
        }

        for (int i = 0; i < polygons.size(); i++) {
            Geometry s1 = polygons.get(i);
            if (s1 == null) {
                continue;
            }
            for (int j = i + 1; j < polygons.size(); j++) {
                Geometry s2 = polygons.get(j);
                if (s2 != null && s1.intersects(s2) && !s1.touches(s2)) {
                    int s1Id = shapes.get(i).getID();
                    int s2Id = shapes.get(j).getID();
                    String message = " Shape overlaps with shape " + s2Id;
                    errors.add(new ValidationError(s1Id, message));
                }
            }
        }
        return errors;
    }

    /**
       Check if the given shape is correct.
       @param shape
       @return
     */
    private static Geometry checkShape(GMLShape shape) throws ValidationException {
        Geometry polygon = JTSTools.shapeToPolygon(shape);
        if (!polygon.isValid()) {
            throw new ValidationException(shape.getID(), "invalid shape");
        }
        if (!polygon.contains(polygon.getCentroid())) {
            throw new ValidationException(shape.getID(), "Shape doesn't contain centroid.");
        }
        return polygon;
    }

}
