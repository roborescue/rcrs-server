package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLShape;

/**
 * Tool for removing police offices.
 */
public class RemovePoliceOfficeTool extends ShapeTool {
  /**
   * Construct a RemovePoliceOfficeTool.
   *
   * @param editor The editor instance.
   */
  public RemovePoliceOfficeTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Remove police office";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLBuilding;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().removePoliceOffice(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new RemovePoliceOfficeEdit(shape.getID()));
  }

  private class RemovePoliceOfficeEdit extends AbstractUndoableEdit {
    private int id;

    public RemovePoliceOfficeEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().addPoliceOffice(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().removePoliceOffice(id);
      editor.updateOverlays();
    }
  }
}