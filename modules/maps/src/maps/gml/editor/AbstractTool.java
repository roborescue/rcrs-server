package maps.gml.editor;

/**
   Abstract base class for Tool implementations.
*/
public abstract class AbstractTool implements Tool {
    /** The GMLEditor instance. */
    protected GMLEditor editor;

    /**
       Construct an AbstractTool.
       @param editor The editor instance.
    */
    protected AbstractTool(GMLEditor editor) {
        this.editor = editor;
    }
}