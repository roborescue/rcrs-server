package maps.gml.editor;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

import java.util.List;

import maps.gml.GMLNode;
import maps.gml.GMLSpace;

/**
   A tool for creating spaces.
*/
public class CreateSpaceTool extends CreateShapeTool {
    /**
       Construct a CreateSpaceTool.
       @param editor The editor instance.
    */
    public CreateSpaceTool(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Create space";
    }

    @Override
    protected UndoableEdit finished(List<GMLNode> nodes) {
        GMLSpace space = editor.getMap().createSpaceFromNodes(nodes);
        return new CreateSpaceEdit(space);
    }

    private class CreateSpaceEdit extends AbstractUndoableEdit {
        private GMLSpace space;

        public CreateSpaceEdit(GMLSpace space) {
            this.space = space;
        }

        @Override
        public void undo() {
            super.undo();
            editor.getMap().removeSpace(space);
            editor.getViewer().repaint();
        }

        @Override
        public void redo() {
            super.redo();
            editor.getMap().addSpace(space);
            editor.getViewer().repaint();
        }
    }
}