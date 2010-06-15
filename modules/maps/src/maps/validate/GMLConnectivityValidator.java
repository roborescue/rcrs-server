package maps.validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import maps.gml.GMLBuilding;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLMap;
import maps.gml.GMLRoad;
import maps.gml.GMLShape;

/**
 *  Checks if a GML map is fully connected and if all individual shaped are
 *  correctly connected to each other (i.e. there are no dangling or no one-way
 *  connections).
 */
public class GMLConnectivityValidator implements MapValidator<GMLMap> {
    private GMLMap map = null;

    @Override
    public Collection<ValidationError> validate(GMLMap mmap) {
        this.map = mmap;
        List<ValidationError> errors = new LinkedList<ValidationError>();

        // Check if all shapes are connected correctly (no dangling connections,
        // etc...)
        Set<GMLShape> toBeChecked = new HashSet<GMLShape>();
        for (GMLShape shape : map.getAllShapes()) {
            errors.addAll(validateShape(shape));
            if (shape instanceof GMLBuilding || shape instanceof GMLRoad) {
                toBeChecked.add(shape);
            }
        }

        Queue<GMLShape> open = new LinkedList<GMLShape>();
        GMLShape first = toBeChecked.iterator().next();
        open.add(first);

        // check for connectivity (only simple connectivity needs to be checked,
        // as
        // we made sure that there are no one-way connections
        while (!open.isEmpty()) {
            GMLShape next = open.remove();
            toBeChecked.remove(next);
            for (GMLShape n : getNeigbours(next)) {
                if (toBeChecked.contains(n)) {
                    open.add(n);
                }
                if (!(n instanceof GMLBuilding || n instanceof GMLRoad)) {
                    String message = "Can reach non-building, non-road shape "
                            + n.getID();
                    errors.add(new ValidationError(next.getID(), message));

                }
            }
        }

        if (!toBeChecked.isEmpty()) {
            for (GMLShape unreachable : toBeChecked) {
                String message = "The map is not fully connected. Shape cannot be reached from "
                        + first.getID();
                errors.add(new ValidationError(unreachable.getID(), message));
            }
        }

        return errors;
    }

    /**
     * Check if all connections to neighbours are reflexive.
     *
     * @param shape
     * @return
     */
    private Collection<ValidationError> validateShape(GMLShape shape) {
        List<ValidationError> errors = new LinkedList<ValidationError>();

        for (GMLDirectedEdge e : shape.getEdges()) {
            if (shape.hasNeighbour(e)) {
                int nId = shape.getNeighbour(e);
                GMLShape neighbour = map.getShape(nId);

                if (neighbour == null) {
                    String message = "Connection to nonexisting id " + nId
                            + " via Edge " + e.getEdge().getID();
                    errors.add(new ValidationError(shape.getID(), message));
                }
                else if (neighbour == shape) {
                    String message = "Shape is connected to itself via Edge"
                            + e.getEdge().getID();
                    errors.add(new ValidationError(shape.getID(), message));
                }
                else {
                    GMLShape backRef = null;
                    try {
                        if (neighbour.hasNeighbour(e.getEdge())) {
                            backRef = map.getShape(neighbour.getNeighbour(e
                                    .getEdge()));
                        }
                        if (backRef != shape) {
                            String message = "Connection to " + neighbour.getID()
                                    + " via Edge " + e.getEdge().getID()
                                    + " is not reflexive.";
                            errors.add(new ValidationError(shape.getID(), message));
                        }
                    }
                    catch (IllegalArgumentException ex) {
                        String message = "Neigbour " + neighbour.getID()
                        + " does not share Edge " + e.getEdge().getID();
                        errors.add(new ValidationError(shape.getID(), message));
                    }
                }
            }
        }

        return errors;
    }

    /**
     * Get all shapes that a shape is connected to.
     * @param shape
     * @return
     */
    private Collection<GMLShape> getNeigbours(GMLShape shape) {
        Collection<GMLShape> result = new ArrayList<GMLShape>();
        for (GMLDirectedEdge edge : shape.getEdges()) {
            if (shape.hasNeighbour(edge)) {
                GMLShape n = map.getShape(shape.getNeighbour(edge));
                if (n != null) {
                    result.add(n);
                }
            }
        }

        return result;
    }

}
