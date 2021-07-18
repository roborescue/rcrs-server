package gis2.scenario;

import gis2.GisScenario;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import maps.gml.GMLMap;
import maps.gml.GMLShape;

/**
 * Function for placing agents.
 */
public class RandomHydrantPlacementFunction extends AbstractFunction {
  private Random random;

  /**
   * Construct a place agents function.
   *
   * @param editor The editor instance.
   */
  public RandomHydrantPlacementFunction(ScenarioEditor editor) {
    super(editor);
    random = new Random();
  }

  @Override
  public String getName() {
    return "Random Hydrant Placement";
  }

  @Override
  public void execute() {
    JPanel panel = new JPanel(new GridLayout(3, 2));
    JTextField numberField = new JTextField("1");
    GMLMap map = editor.getMap();
    double height = (map.getMaxX() - map.getMinX());
    double width = (map.getMaxY() - map.getMinY());
    int suggestedCount = (int) (height * width / 30000);
    panel.add(new JLabel("Number: suggested number:" + suggestedCount));
    panel.add(numberField);
    HashSet<Integer> selectedIds = new HashSet<>();
    List<GMLShape> all = new ArrayList<GMLShape>(editor.getMap().getRoads());
    if (JOptionPane.showConfirmDialog(null, panel, "Add agents",
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
      GisScenario s = editor.getScenario();
      try {
        int number = Integer.parseInt(numberField.getText());
        for (int i = 0; i < number; ++i) {
          int id = all.get(random.nextInt(all.size())).getID();
          if (selectedIds.contains(id))
            i--;
          else {
            s.addHydrant(id);
            selectedIds.add(id);
          }
        }
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    editor.setChanged();
    editor.updateOverlays();
  }
}