package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLShape;

/**
 * Tool for placing police offices.
 */
public class PlacePoliceOfficeTool extends ShapeTool {
  /**
   * Construct a PlacePoliceOfficeTool.
   *
   * @param editor The editor instance.
   */
  public PlacePoliceOfficeTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Place police office";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLBuilding;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().addPoliceOffice(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new AddPoliceOfficeEdit(shape.getID()));
  }

  private class AddPoliceOfficeEdit extends AbstractUndoableEdit {
    private int id;

    public AddPoliceOfficeEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().removePoliceOffice(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().addPoliceOffice(id);
      editor.updateOverlays();
    }
  }
}