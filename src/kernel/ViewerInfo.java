package kernel;

/**
   Information about a viewer.
 */
public class ViewerInfo {
    private String description;

    /**
       Construct a ViewerInfo.
       @param description A description of the viewer.
     */
    public ViewerInfo(String description) {
        this.description = description;
    }

    /**
       Get the description of the viewer.
       @return The description.
    */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Viewer: " + description;
    }
}