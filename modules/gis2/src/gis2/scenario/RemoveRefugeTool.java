package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLShape;

/**
 * Tool for removing refuges.
 */
public class RemoveRefugeTool extends ShapeTool {
  /**
   * Construct a RemoveRefugeTool.
   *
   * @param editor The editor instance.
   */
  public RemoveRefugeTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Remove refuge";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLBuilding;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().removeRefuge(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new RemoveRefugeEdit(shape.getID()));
  }

  private class RemoveRefugeEdit extends AbstractUndoableEdit {
    private int id;

    public RemoveRefugeEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().addRefuge(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().removeRefuge(id);
      editor.updateOverlays();
    }
  }
}