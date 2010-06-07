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
        for (GMLShape shape : map.getAllShapes()) {
            ValidationError error = checkShape(shape);
            if (error != null) {
                errors.add(error);
            }
        }
        return errors;
    }

    /**
       Check if the given shape is correct.
       @param shape
       @return
     */
    private static ValidationError checkShape(GMLShape shape) {
        try {
            Geometry polygon = JTSTools.shapeToPolygon(shape);
            if (!polygon.isValid()) {
                return new ValidationError(shape.getID(), "invalid shape");
            }
            if (!polygon.contains(polygon.getCentroid())) {
                return new ValidationError(shape.getID(), "Shape doesn't contain centroid.");
            }
        }
        catch (ValidationException e) {
            return e.getError();
        }
        return null;
    }

}
