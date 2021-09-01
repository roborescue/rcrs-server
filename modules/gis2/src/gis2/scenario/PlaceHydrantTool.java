package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLRoad;
import maps.gml.GMLShape;

/**
 * Tool for placing refuges.
 */
public class PlaceHydrantTool extends ShapeTool {
  /**
   * Construct a PlaceHydrantTool.
   *
   * @param editor The editor instance.
   */
  public PlaceHydrantTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Place hydrant";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLRoad;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().addHydrant(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new AddHydrantEdit(shape.getID()));
  }

  private class AddHydrantEdit extends AbstractUndoableEdit {
    private int id;

    public AddHydrantEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().removeHydrant(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().addHydrant(id);
      editor.updateOverlays();
    }
  }
}