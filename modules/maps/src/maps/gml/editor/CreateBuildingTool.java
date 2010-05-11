package maps.gml.editor;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

import java.util.List;

import maps.gml.GMLNode;
import maps.gml.GMLBuilding;

/**
   A tool for creating buildings.
*/
public class CreateBuildingTool extends CreateShapeTool {
    /**
       Construct a CreateBuildingTool.
       @param editor The editor instance.
    */
    public CreateBuildingTool(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Create building";
    }

    @Override
    protected UndoableEdit finished(List<GMLNode> nodes) {
        GMLBuilding building = editor.getMap().createBuildingFromNodes(nodes);
        return new CreateBuildingEdit(building);
    }

    private class CreateBuildingEdit extends AbstractUndoableEdit {
        private GMLBuilding building;

        public CreateBuildingEdit(GMLBuilding building) {
            this.building = building;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getMap().removeBuilding(building);
            editor.getViewer().repaint();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getMap().addBuilding(building);
            editor.getViewer().repaint();
        }
    }
}