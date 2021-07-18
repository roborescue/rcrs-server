package gis2.scenario;

import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLShape;

/**
 * Tool for placing civilians.
 */
public class PlaceCivilianTool extends ShapeTool {
  /**
   * Construct a PlaceCivilianTool.
   *
   * @param editor The editor instance.
   */
  public PlaceCivilianTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Place civilian";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return true;
  }

  @Override
  protected void processClick(GMLShape shape) {
    editor.getScenario().addCivilian(shape.getID());
    editor.setChanged();
    editor.updateOverlays();
    editor.addEdit(new AddCivilianEdit(shape.getID()));
  }

  private class AddCivilianEdit extends AbstractUndoableEdit {
    private int id;

    public AddCivilianEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().removeCivilian(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().addCivilian(id);
      editor.updateOverlays();
    }
  }
}