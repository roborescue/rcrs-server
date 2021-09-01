package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLShape;

/**
 * Tool for placing ambulance teams.
 */
public class PlaceAmbulanceTeamTool extends ShapeTool {
  /**
   * Construct a PlaceAmbulanceTeamTool.
   *
   * @param editor The editor instance.
   */
  public PlaceAmbulanceTeamTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Place ambulance team";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return true;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().addAmbulanceTeam(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new AddAmbulanceTeamEdit(shape.getID()));
  }

  private class AddAmbulanceTeamEdit extends AbstractUndoableEdit {
    private int id;

    public AddAmbulanceTeamEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().removeAmbulanceTeam(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().addAmbulanceTeam(id);
      editor.updateOverlays();
    }
  }
}