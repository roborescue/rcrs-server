package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLShape;

/**
 * Tool for placing fire stations.
 */
public class PlaceFireStationTool extends ShapeTool {
  /**
   * Construct a PlaceFireStationTool.
   *
   * @param editor The editor instance.
   */
  public PlaceFireStationTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Place fire station";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLBuilding;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().addFireStation(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new AddFireStationEdit(shape.getID()));
  }

  private class AddFireStationEdit extends AbstractUndoableEdit {
    private int id;

    public AddFireStationEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().removeFireStation(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().addFireStation(id);
      editor.updateOverlays();
    }
  }
}