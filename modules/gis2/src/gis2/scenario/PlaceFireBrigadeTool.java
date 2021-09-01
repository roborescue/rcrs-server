package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLShape;

/**
 * Tool for placing fire brigades.
 */
public class PlaceFireBrigadeTool extends ShapeTool {
  /**
   * Construct a PlaceFireBrigadeTool.
   *
   * @param editor The editor instance.
   */
  public PlaceFireBrigadeTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Place fire brigade";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return true;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().addFireBrigade(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new AddFireBrigadeEdit(shape.getID()));
  }

  private class AddFireBrigadeEdit extends AbstractUndoableEdit {
    private int id;

    public AddFireBrigadeEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().removeFireBrigade(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().addFireBrigade(id);
      editor.updateOverlays();
    }
  }
}