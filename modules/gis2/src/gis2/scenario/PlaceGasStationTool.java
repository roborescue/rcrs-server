package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLShape;

/**
 * Tool for placing gasStation.
 */
public class PlaceGasStationTool extends ShapeTool {
  /**
   * Construct a PlaceGasStationTool.
   *
   * @param editor The editor instance.
   */
  public PlaceGasStationTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Place gas station";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLBuilding;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().addGasStation(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new AddGasStationEdit(shape.getID()));
  }

  private class AddGasStationEdit extends AbstractUndoableEdit {
    private int id;

    public AddGasStationEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().removeGasStation(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().addGasStation(id);
      editor.updateOverlays();
    }
  }
}