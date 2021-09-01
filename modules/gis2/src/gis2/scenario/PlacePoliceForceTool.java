package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLShape;

/**
 * Tool for placing police forces.
 */
public class PlacePoliceForceTool extends ShapeTool {
  /**
   * Construct a PlacePoliceForceTool.
   *
   * @param editor The editor instance.
   */
  public PlacePoliceForceTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Place police force";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return true;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().addPoliceForce(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new AddPoliceForceEdit(shape.getID()));
  }

  private class AddPoliceForceEdit extends AbstractUndoableEdit {
    private int id;

    public AddPoliceForceEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().removePoliceForce(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().addPoliceForce(id);
      editor.updateOverlays();
    }
  }
}