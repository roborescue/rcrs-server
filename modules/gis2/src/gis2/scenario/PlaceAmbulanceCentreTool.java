package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLShape;

/**
 * Tool for placing ambulance centres.
 */
public class PlaceAmbulanceCentreTool extends ShapeTool {
  /**
   * Construct a PlaceAmbulanceCentreTool.
   *
   * @param editor The editor instance.
   */
  public PlaceAmbulanceCentreTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Place ambulance centre";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLBuilding;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().addAmbulanceCentre(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new AddAmbulanceCentreEdit(shape.getID()));
  }

  private class AddAmbulanceCentreEdit extends AbstractUndoableEdit {
    private int id;

    public AddAmbulanceCentreEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().removeAmbulanceCentre(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().addAmbulanceCentre(id);
      editor.updateOverlays();
    }
  }
}