package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLShape;

/**
   Tool for placing refuges.
*/
public class PlaceRefugeTool extends ShapeTool {
    /**
       Construct a PlaceRefugeTool.
       @param editor The editor instance.
    */
    public PlaceRefugeTool(ScenarioEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Place refuge";
    }

    @Override
    protected boolean shouldHighlight(GMLShape shape) {
        return shape instanceof GMLBuilding;
    }

    @Override
    protected void processClick(GMLShape shape) {
        editor.getScenario().addRefuge(shape.getID());
        editor.setChanged();
        editor.updateOverlays();
        editor.addEdit(new AddRefugeEdit(shape.getID()));
    }

    private class AddRefugeEdit extends AbstractUndoableEdit {
        private int id;

        public AddRefugeEdit(int id) {
            this.id = id;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getScenario().removeRefuge(id);
            editor.updateOverlays();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getScenario().addRefuge(id);
            editor.updateOverlays();
        }
    }
}