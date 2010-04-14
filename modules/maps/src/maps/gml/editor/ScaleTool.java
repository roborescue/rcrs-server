package maps.gml.editor;

import javax.swing.JOptionPane;

import maps.ConstantConversion;

/**
   A tool for scaling the map.
*/
public class ScaleTool extends AbstractTool {
    /**
       Construct a ScaleTool.
       @param editor The editor instance.
    */
    public ScaleTool(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Scale map";
    }

    @Override
    public void activate() {
        String s = JOptionPane.showInputDialog("Enter scale factor");
        if (s != null) {
            try {
                double factor = Double.parseDouble(s);
                editor.getMap().convertCoordinates(new ConstantConversion(factor));
                editor.setChanged();
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deactivate() {
    }
}