package maps.gml.editor;

/**
   A tool for panning and zooming the view.
*/
public class PanZoomTool extends AbstractTool {
    /**
       Construct a PanZoomTool.
       @param editor The editor instance.
    */
    public PanZoomTool(GMLEditor editor) {
        super(editor);
    }

    @Override
    public String getName() {
        return "Pan/Zoom";
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
    }
}