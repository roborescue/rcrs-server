package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLShape;

/**
 * Tool for removing fires.
 */
public class RemoveFireTool extends ShapeTool {
  /**
   * Construct a RemoveFireTool.
   *
   * @param editor The editor instance.
   */
  public RemoveFireTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Remove fire";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLBuilding;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().removeFire(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new RemoveFireEdit(shape.getID()));
  }

  private class RemoveFireEdit extends AbstractUndoableEdit {
    private int id;

    public RemoveFireEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().addFire(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().removeFire(id);
      editor.updateOverlays();
    }
  }
}