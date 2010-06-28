package maps.gml.editor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

import maps.gml.GMLBuilding;
import maps.gml.GMLEdge;
import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.GMLObject;
import maps.gml.GMLRoad;
import maps.gml.GMLSpace;
import maps.gml.view.DecoratorOverlay;
import maps.gml.view.EdgeDecorator;
import maps.gml.view.FilledShapeDecorator;
import maps.gml.view.LineEdgeDecorator;
import maps.gml.view.NodeDecorator;
import maps.gml.view.SquareNodeDecorator;
import maps.validate.GMLMapValidator;
import maps.validate.MapValidator;
import maps.validate.ValidationError;

import rescuecore2.log.Logger;

/**
 * Check the map for errors and highlight them on the map.
 *
 */
public class ValidateFunction extends AbstractFunction {
    private static final Color HIGHLIGHT_COLOUR = Color.RED;
    private static final int NODE_SIZE = 5;

    private DecoratorOverlay overlay = new DecoratorOverlay();

    private NodeDecorator nodeHighlight;
    private EdgeDecorator edgeHighlight;
    private FilledShapeDecorator shapeHighlight;

    /**
     * Create a new ValidateFunction.
     * @param editor The editor.
     */
    public ValidateFunction(GMLEditor editor) {
        super(editor);
        nodeHighlight = new SquareNodeDecorator(HIGHLIGHT_COLOUR, NODE_SIZE);
        edgeHighlight = new LineEdgeDecorator(HIGHLIGHT_COLOUR);
        shapeHighlight = new FilledShapeDecorator(HIGHLIGHT_COLOUR,
                HIGHLIGHT_COLOUR, HIGHLIGHT_COLOUR);
    }

    @Override
    public void execute() {
        overlay.clearAllDecorators();

        Collection<ValidationError> allErrors = new ArrayList<ValidationError>();
        for (MapValidator<GMLMap> validator : GMLMapValidator
                .getDefaultValidators()) {
            Logger.info("Validating " + validator);
            Collection<ValidationError> errors = validator.validate(editor
                    .getMap());
            allErrors.addAll(errors);

            for (ValidationError e : errors) {
                System.out.println(e);
                addDecorator(e.getId());
            }
        }
        editor.getInspector().setErrors(allErrors);

        editor.getViewer().removeOverlay(overlay);
        editor.getViewer().addOverlay(overlay);
        editor.getViewer().repaint();

    }

    /**
     * Add a new error decorator for the object with the given id.
     * @param id
     */
    private void addDecorator(int id) {
        GMLObject obj = editor.getMap().getObject(id);
        if (obj == null) {
            return;
        }
        if (obj instanceof GMLBuilding) {
            overlay.setBuildingDecorator(shapeHighlight, (GMLBuilding) obj);
        }
        else if (obj instanceof GMLRoad) {
            overlay.setRoadDecorator(shapeHighlight, (GMLRoad) obj);
        }
        else if (obj instanceof GMLSpace) {
            overlay.setSpaceDecorator(shapeHighlight, (GMLSpace) obj);
        }
        else if (obj instanceof GMLEdge) {
            overlay.setEdgeDecorator(edgeHighlight, (GMLEdge) obj);
        }
        else if (obj instanceof GMLNode) {
            overlay.setNodeDecorator(nodeHighlight, (GMLNode) obj);
        }

    }

    @Override
    public String getName() {
        return "Validate map";
    }

}
