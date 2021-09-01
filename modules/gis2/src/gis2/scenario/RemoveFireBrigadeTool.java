package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLShape;

/**
 * Tool for removing fire brigades.
 */
public class RemoveFireBrigadeTool extends ShapeTool {
  /**
   * Construct a RemoveFireBrigadeTool.
   *
   * @param editor The editor instance.
   */
  public RemoveFireBrigadeTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Remove fire brigade";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return true;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().removeFireBrigade(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new RemoveFireBrigadeEdit(shape.getID()));
  }

  private class RemoveFireBrigadeEdit extends AbstractUndoableEdit {
    private int id;

    public RemoveFireBrigadeEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().addFireBrigade(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().removeFireBrigade(id);
      editor.updateOverlays();
    }
  }
}