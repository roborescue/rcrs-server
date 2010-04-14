package maps.gml.editor;

/**
   Interface for an editing tool.
*/
public interface Tool {
    /**
       Get the name of this tool.
       @return The name of the tool.
    */
    String getName();

    /**
       Activate this tool.
    */
    void activate();

    /**
       Deactivate this tool.
    */
    void deactivate();
}