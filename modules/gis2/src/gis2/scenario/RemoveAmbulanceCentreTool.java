package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLShape;

/**
 * Tool for removing ambulance centres.
 */
public class RemoveAmbulanceCentreTool extends ShapeTool {
  /**
   * Construct a RemoveAmbulanceCentreTool.
   *
   * @param editor The editor instance.
   */
  public RemoveAmbulanceCentreTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Remove ambulance centre";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLBuilding;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().removeAmbulanceCentre(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new RemoveAmbulanceCentreEdit(shape.getID()));
  }

  private class RemoveAmbulanceCentreEdit extends AbstractUndoableEdit {
    private int id;

    public RemoveAmbulanceCentreEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().addAmbulanceCentre(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().removeAmbulanceCentre(id);
      editor.updateOverlays();
    }
  }
}