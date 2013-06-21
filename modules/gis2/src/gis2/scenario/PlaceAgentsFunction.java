package gis2.scenario;

import gis2.GisScenario;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import java.awt.GridLayout;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JLabel;

import rescuecore2.log.Logger;

import maps.gml.GMLShape;

/**
   Function for placing agents.
*/
public class PlaceAgentsFunction extends AbstractFunction {
    private static final int TYPE_FIRE = 0;
    private static final int TYPE_POLICE = 1;
    private static final int TYPE_AMBULANCE = 2;
    private static final int TYPE_CIVILIAN = 3;

    private Random random;

    /**
       Construct a place agents function.
       @param editor The editor instance.
    */
    public PlaceAgentsFunction(ScenarioEditor editor) {
        super(editor);
        random = new Random();
    }

    @Override
    public String getName() {
        return "Place agents";
    }

    @Override
    public void execute() {
        // CHECKSTYLE:OFF:MagicNumber
        JPanel panel = new JPanel(new GridLayout(3, 2));
        // CHECKSTYLE:ON:MagicNumber
        JTextField numberField = new JTextField("1");
        JComboBox typeCombo = new JComboBox(new String[] {"Fire", "Police", "Ambulance","Civilian"});

        JCheckBox  buildingBox = new JCheckBox ("In buildings?", false);
        JCheckBox  roadBox = new JCheckBox ("In Roads?", true);
        JPanel jp=new JPanel();
        jp.add(buildingBox);
        jp.add(roadBox);

        panel.add(new JLabel("Type"));
        panel.add(typeCombo);
        panel.add(new JLabel("Number"));
        panel.add(numberField);
        panel.add(jp);
        List<Integer> ids = new ArrayList<Integer>();
        int type = -1;
        List<GMLShape> all = new ArrayList<GMLShape>();
        if (JOptionPane.showConfirmDialog(null, panel, "Add agents", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int number = Integer.parseInt(numberField.getText());
                type = typeCombo.getSelectedIndex();
                if(roadBox.isSelected())
                	all.addAll(editor.getMap().getRoads());
                if (buildingBox.isSelected())
                    all.addAll(editor.getMap().getBuildings());
                if(all.size()==0){
                	JOptionPane.showMessageDialog(null, "No Area to Place... Please choose In Road or Building...","Error",JOptionPane.ERROR_MESSAGE);
                	return;
                }
                for (int i = 0; i < number; ++i) {
                    ids.add(all.get(random.nextInt(all.size())).getID());
                }
            }
            catch (NumberFormatException e) {
                Logger.error("Error parsing number", e);
            }
        }
         GisScenario s = editor.getScenario();
        switch (type) {
        case TYPE_FIRE:
            for (int id : ids) {
                s.addFireBrigade(id);
            }
            break;
        case TYPE_POLICE:
            for (int id : ids) {
                s.addPoliceForce(id);
            }
            break;
        case TYPE_AMBULANCE:
            for (int id : ids) {
                s.addAmbulanceTeam(id);
            }
            break;
        case TYPE_CIVILIAN:
        	for (int id : ids) {
                s.addCivilian(id);
            }
        	break;
        default:
            throw new IllegalArgumentException("Unexpected type: " + type);
        }
        editor.setChanged();
        editor.updateOverlays();
    }
}