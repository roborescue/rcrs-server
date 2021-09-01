package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLShape;

/**
 * Tool for placing fires.
 */
public class PlaceFireTool extends ShapeTool {
  /**
   * Construct a PlaceFireTool.
   *
   * @param editor The editor instance.
   */
  public PlaceFireTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Place fire";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLBuilding;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().addFire(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new AddFireEdit(shape.getID()));
  }

  private class AddFireEdit extends AbstractUndoableEdit {
    private int id;

    public AddFireEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().removeFire(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().addFire(id);
      editor.updateOverlays();
    }
  }
}