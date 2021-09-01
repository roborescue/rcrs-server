package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLShape;

/**
 * Tool for removing fire stations.
 */
public class RemoveFireStationTool extends ShapeTool {
  /**
   * Construct a RemoveFireStationTool.
   *
   * @param editor The editor instance.
   */
  public RemoveFireStationTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Remove fire station";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLBuilding;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().removeFireStation(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new RemoveFireStationEdit(shape.getID()));
  }

  private class RemoveFireStationEdit extends AbstractUndoableEdit {
    private int id;

    public RemoveFireStationEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().addFireStation(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().removeFireStation(id);
      editor.updateOverlays();
    }
  }
}