package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLShape;

/**
 * Tool for removing ambulance teams.
 */
public class RemoveAmbulanceTeamTool extends ShapeTool {
  /**
   * Construct a RemoveAmbulanceTeamTool.
   *
   * @param editor The editor instance.
   */
  public RemoveAmbulanceTeamTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Remove ambulance team";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return true;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().removeAmbulanceTeam(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new RemoveAmbulanceTeamEdit(shape.getID()));
  }

  private class RemoveAmbulanceTeamEdit extends AbstractUndoableEdit {
    private int id;

    public RemoveAmbulanceTeamEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().addAmbulanceTeam(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().removeAmbulanceTeam(id);
      editor.updateOverlays();
    }
  }
}