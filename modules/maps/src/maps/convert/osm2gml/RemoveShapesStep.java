package maps.convert.osm2gml;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import maps.convert.ConvertStep;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   This step removes shapes that are duplicates or contained entirely inside another shape.
*/
public class RemoveShapesStep extends ConvertStep {
    private static final Log LOG = LogFactory.getLog(RemoveShapesStep.class);

    private TemporaryMap map;

    /**
       Construct a RemoveFacesStep.
       @param map The TemporaryMap to use.
    */
    public RemoveShapesStep(TemporaryMap map) {
        super();
        this.map = map;
    }

    @Override
    public String getDescription() {
        return "Removing extraneous shapes";
    }

    @Override
    protected void step() {
        Collection<TemporaryObject> allObjects = map.getAllObjects();
        setProgressLimit(allObjects.size() * 2);
        Set<TemporaryObject> removed = new HashSet<TemporaryObject>();
        setStatus("Removing duplicate shapes");
        int duplicateCount = 0;
        int interiorCount = 0;
        //        LOG.debug("Removing building duplicates");
        duplicateCount += removeDuplicates(map.getBuildings(), removed, allObjects);
        //        LOG.debug("Removing intersection duplicates");
        duplicateCount += removeDuplicates(map.getIntersections(), removed, allObjects);
        //        LOG.debug("Removing road duplicates");
        duplicateCount += removeDuplicates(map.getRoads(), removed, allObjects);
        //        LOG.debug("Removing interior faces");
        setStatus("Removing interior faces");
        interiorCount += removeInterior(map.getRoads(), removed, allObjects);
        interiorCount += removeInterior(map.getIntersections(), removed, allObjects);
        interiorCount += removeInterior(map.getBuildings(), removed, allObjects);
        setStatus("Removed " + removed.size() + " faces: " + duplicateCount + " duplicates and " + interiorCount + " interior");
    }

    /**
       Remove all shapes that are duplicates of a test set.
       @param test The set of objects to test against.
       @param removed The set of removed objects.
       @param toCheck The set of shapes to check.
       @return The number of objects removed.
    */
    private int removeDuplicates(Collection<? extends TemporaryObject> test, Set<TemporaryObject> removed, Collection<TemporaryObject> toCheck) {
        int count = 0;
        //        LOG.debug(test.size() + " test objects, " + toCheck.size() + " to check, " + removed.size() + " already removed");
        for (TemporaryObject first : test) {
            bumpProgress();
            if (removed.contains(first)) {
                continue;
            }
            for (TemporaryObject second : toCheck) {
                if (removed.contains(second)) {
                    continue;
                }
                if (first == second) {
                    continue;
                }
                if (first.isDuplicate(second)) {
                    map.removeTemporaryObject(second);
                    removed.add(second);
                    ++count;
                    //                    LOG.debug("Removed duplicate object: " + second + " is same as " + first);
                }
            }
        }
        return count;
    }

    /**
       Remove any shapes that are entirely inside another shapes.
       @param toCheck The set of objects to check.
       @param removed The set of removed objects.
       @param allObjects All objects.
       @return The number of removed objects.
    */
    private int removeInterior(Collection<? extends TemporaryObject> toCheck, Set<TemporaryObject> removed, Collection<TemporaryObject> allObjects) {
        int count = 0;
        for (TemporaryObject first : toCheck) {
            bumpProgress();
            if (removed.contains(first)) {
                continue;
            }
            for (TemporaryObject second : allObjects) {
                if (removed.contains(second)) {
                    continue;
                }
                if (first == second) {
                    continue;
                }
                if (first.isEntirelyInside(second)) {
                    map.removeTemporaryObject(first);
                    removed.add(first);
                    ++count;
                }
            }
        }
        return count;
    }
}