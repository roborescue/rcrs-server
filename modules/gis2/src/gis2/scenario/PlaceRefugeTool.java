package gis2.scenario;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.undo.AbstractUndoableEdit;

import maps.gml.GMLBuilding;
import maps.gml.GMLShape;

/**
 * Tool for placing refuges.
 */
public class PlaceRefugeTool extends ShapeTool {
  /**
   * Construct a PlaceRefugeTool.
   *
   * @param editor The editor instance.
   */
  public PlaceRefugeTool(ScenarioEditor editor) {
    super(editor);
  }

  @Override
  public String getName() {
    return "Place refuge";
  }

  @Override
  protected boolean shouldHighlight(GMLShape shape) {
    return shape instanceof GMLBuilding;
  }

  @Override
  protected void processClick(GMLShape shape) {
    JPanel panel = new JPanel(new GridLayout(3, 2));
    JTextField bedNumberField = new JTextField("100");
    panel.add(new JLabel("Insert a bed capacity for the refuge"));
    panel.add(bedNumberField);

    if (JOptionPane.showConfirmDialog(null, panel, "Refuge Capacity",
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
      int bedCapacity = Integer.parseInt(bedNumberField.getText());
      int refillCapacity = 1000;// Integer.parseInt(refillNumberField.getText());

      editor.getScenario().addRefuge(shape.getID(), bedCapacity, refillCapacity);
      editor.setChanged();
      editor.updateOverlays();
      editor.addEdit(new AddRefugeEdit(shape.getID()));
    }
  }

  private class AddRefugeEdit extends AbstractUndoableEdit {
    private int id;

    public AddRefugeEdit(int id) {
      this.id = id;
    }

    @Override
    public void undo() {
      super.undo();
      editor.getScenario().removeRefuge(id);
      editor.updateOverlays();
    }

    @Override
    public void redo() {
      super.redo();
      editor.getScenario().addRefuge(id);
      editor.updateOverlays();
    }
  }
}