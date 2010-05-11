package maps.gml.editor;

import javax.swing.undo.UndoableEdit;
import javax.swing.undo.AbstractUndoableEdit;

import java.util.List;

import maps.gml.GMLNode;
import maps.gml.GMLRoad;

/**
   A tool for creating roads.
*/
public class CreateRoadTool extends CreateShapeTool {
    /**
       Construct a CreateRoadTool.
       @param editor The editor instance.
    */
    public CreateRoadTool(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Create road";
    }

    @Override
    protected UndoableEdit finished(List<GMLNode> nodes) {
        GMLRoad road = editor.getMap().createRoadFromNodes(nodes);
        return new CreateRoadEdit(road);
    }

    private class CreateRoadEdit extends AbstractUndoableEdit {
        private GMLRoad road;

        public CreateRoadEdit(GMLRoad road) {
            this.road = road;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getMap().removeRoad(road);
            editor.getViewer().repaint();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getMap().addRoad(road);
            editor.getViewer().repaint();
        }
    }
}