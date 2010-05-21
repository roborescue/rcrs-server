package maps.gml.editor;

import javax.swing.JOptionPane;

import maps.ScaleConversion;

/**
   A function for scaling the map.
*/
public class ScaleFunction extends AbstractFunction {
    /**
       Construct a ScaleFunction.
       @param editor The editor instance.
    */
    public ScaleFunction(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Scale map";
    }

    @Override
    public void execute() {
        String s = JOptionPane.showInputDialog("Enter scale factor");
        if (s != null) {
            try {
                double factor = Double.parseDouble(s);
                editor.getMap().convertCoordinates(new ScaleConversion(editor.getMap().getMinX(), editor.getMap().getMinY(), factor, factor));
                editor.setChanged();
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
}