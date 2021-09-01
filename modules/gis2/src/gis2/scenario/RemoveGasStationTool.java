package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLShape;

/**
 * Tool for removing refuges.
 */
public class RemoveGasStationTool extends ShapeTool {
  /**
   * Construct a RemoveGasStationTool.
   *
   * @param editor The editor instance.
   */
  public RemoveGasStationTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Remove gas station";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLBuilding;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().removeGasStation(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new RemoveGasStationEdit(shape.getID()));
  }

  private class RemoveGasStationEdit extends AbstractUndoableEdit {
    private int id;

    public RemoveGasStationEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().addGasStation(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().removeGasStation(id);
      editor.updateOverlays();
    }
  }
}