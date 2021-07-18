package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLRoad;
import maps.gml.GMLShape;

/**
 * Tool for removing refuges.
 */
public class RemoveHydrantTool extends ShapeTool {
  /**
   * Construct a RemoveHydrantTool.
   *
   * @param editor The editor instance.
   */
  public RemoveHydrantTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Remove hydrant";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLRoad;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().removeHydrant(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new RemoveHydrantEdit(shape.getID()));
  }

  private class RemoveHydrantEdit extends AbstractUndoableEdit {
    private int id;

    public RemoveHydrantEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().addHydrant(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().removeHydrant(id);
      editor.updateOverlays();
    }
  }
}