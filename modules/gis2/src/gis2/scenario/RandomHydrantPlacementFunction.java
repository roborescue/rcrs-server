package gis2.scenario;

import gis2.GisScenario;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import java.awt.GridLayout;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JLabel;

import rescuecore2.log.Logger;

import maps.gml.GMLMap;
import maps.gml.GMLShape;

/**
   Function for placing agents.
*/
public class RandomHydrantPlacementFunction extends AbstractFunction {
    private Random random;

    /**
       Construct a place agents function.
       @param editor The editor instance.
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
        // CHECKSTYLE:OFF:MagicNumber
        JPanel panel = new JPanel(new GridLayout(3, 2));
        // CHECKSTYLE:ON:MagicNumber
        JTextField numberField = new JTextField("1");
        GMLMap map=editor.getMap();
        double heigth = (map.getMaxX()-map.getMinX());
        double width= (map.getMaxY()-map.getMinY());
        int suggestedCount = (int)(heigth*width/30000);
        panel.add(new JLabel("Number: suggested number:"+suggestedCount));
        panel.add(numberField);
        HashSet<Integer> selectedIds=new HashSet<>();
        List<GMLShape> all = new ArrayList<GMLShape>(editor.getMap().getRoads());
        if (JOptionPane.showConfirmDialog(null, panel, "Add agents", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        	GisScenario s = editor.getScenario();
            try {
                int number = Integer.parseInt(numberField.getText());
                for (int i = 0; i < number; ++i) {
                	int id = all.get(random.nextInt(all.size())).getID();
                	if(selectedIds.contains(id))
                		i--;
                	else{
                		s.addHydrant(id);
                		selectedIds.add(id);
                	}
                }
            }
            catch (NumberFormatException e) {
                Logger.error("Error parsing number", e);
            }
        }
        editor.setChanged();
        editor.updateOverlays();
    }
}