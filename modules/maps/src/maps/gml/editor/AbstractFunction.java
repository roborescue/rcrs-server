package maps.gml.editor;

/**
   Abstract base class for Function implementations.
*/
public abstract class AbstractFunction implements Function {
    /** The GMLEditor instance. */
    protected GMLEditor editor;

    /**
       Construct an AbstractFunction.
       @param editor The editor instance.
    */
    protected AbstractFunction(GMLEditor editor) {
        this.editor = editor;
    }
}