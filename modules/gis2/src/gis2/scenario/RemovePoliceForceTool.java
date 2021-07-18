package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLShape;

/**
 * Tool for removing police forces.
 */
public class RemovePoliceForceTool extends ShapeTool {
  /**
   * Construct a RemovePoliceForceTool.
   *
   * @param editor The editor instance.
   */
  public RemovePoliceForceTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Remove police force";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return true;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().removePoliceForce(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new RemovePoliceForceEdit(shape.getID()));
  }

  private class RemovePoliceForceEdit extends AbstractUndoableEdit {
    private int id;

    public RemovePoliceForceEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().addPoliceForce(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().removePoliceForce(id);
      editor.updateOverlays();
    }
  }
}