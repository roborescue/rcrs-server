package maps.gml.editor;

import maps.ScaleConversion;
import maps.MapTools;

/**
   A tool for fixing latitude/longitude coordinates.
*/
public class FixLatLongTool extends AbstractTool {
    /**
       Construct a FixLatLongTool.
       @param editor The editor instance.
    */
    public FixLatLongTool(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Fix lat/long";
    }

    @Override
    public void activate() {
        double minX = editor.getMap().getMinX();
        double minY = editor.getMap().getMinY();
        double factor = 1.0 / MapTools.sizeOf1Metre(minX, minY);
        ScaleConversion c = new ScaleConversion(minX, minY, factor, factor);
        editor.getMap().convertCoordinates(c);
        editor.setChanged();
    }

    @Override
    public void deactivate() {
    }
}