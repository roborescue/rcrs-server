package traffic3.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

//import rescuecore2.log.Logger;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.EntityListener;

import rescuecore2.standard.entities.Blockade;

/**
   This class wraps a Blockade object with some extra information.
*/
public class TrafficBlockade {
    private Blockade blockade;
    private List<Line2D> lines = new ArrayList<Line2D>();

    /**
       Construct a TrafficBlockade object.
       @param blockade The wrapped blockade.
    */
    public TrafficBlockade(final Blockade blockade) {
        this.blockade = blockade;
        lines = null;
        blockade.addEntityListener(new EntityListener() {
                @Override
                public void propertyChanged(Entity e, Property p, Object oldValue, Object newValue) {
                    if (p == blockade.getApexesProperty()) {
                        lines = null;
                    }
                }
            });
    }

    /**
       Get the lines that make up the outline of this blockade.
       @return A list of lines.
     */
    public List<Line2D> getLines() {
        if (lines == null) {
            lines = new ArrayList<Line2D>();
            int[] apexes = blockade.getApexes();
            // CHECKSTYLE:OFF:MagicNumber
            for (int i = 0; i < apexes.length - 3; i += 2) {
                Point2D first = new Point2D(apexes[i], apexes[i + 1]);
                Point2D second = new Point2D(apexes[i + 2], apexes[i + 3]);
                lines.add(new Line2D(first, second));
            }
            // CHECKSTYLE:ON:MagicNumber
            // Close the shape
            lines.add(new Line2D(new Point2D(apexes[apexes.length - 2], apexes[apexes.length - 1]), new Point2D(apexes[0], apexes[1])));
        }
        return Collections.unmodifiableList(lines);
    }
}
