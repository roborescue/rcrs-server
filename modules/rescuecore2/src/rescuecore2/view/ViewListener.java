package rescuecore2.view;

import java.util.List;

/**
   A listener for view events.
 */
public interface ViewListener {
    /**
       Notification that a set of objects were clicked.
       @param view The ViewComponent that was clicked.
       @param objects The list of objects that were under the click point.
     */
    void objectsClicked(ViewComponent view, List<RenderedObject> objects);
}