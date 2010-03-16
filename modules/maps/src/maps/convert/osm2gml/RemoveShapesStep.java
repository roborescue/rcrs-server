package maps.convert.osm2gml;

import java.awt.Color;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import maps.convert.ConvertStep;

import rescuecore2.log.Logger;

/**
   This step removes shapes that are duplicates or contained entirely inside another shape.
*/
public class RemoveShapesStep extends ConvertStep {
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
        debug.setBackground(ConvertTools.getAllDebugShapes(map));
        debug.setAutozoomEnabled(false);
        Collection<TemporaryObject> allObjects = map.getAllObjects();
        setProgressLimit(allObjects.size() * 2);
        Set<TemporaryObject> removed = new HashSet<TemporaryObject>();
        setStatus("Removing duplicate shapes");
        int duplicateCount = 0;
        int interiorCount = 0;
        Logger.debug("Removing building duplicates");
        duplicateCount += removeDuplicates(map.getBuildings(), removed, allObjects);
        Logger.debug("Removing intersection duplicates");
        duplicateCount += removeDuplicates(map.getIntersections(), removed, allObjects);
        Logger.debug("Removing road duplicates");
        duplicateCount += removeDuplicates(map.getRoads(), removed, allObjects);
        Logger.debug("Removing interior faces");
        setStatus("Removing interior faces");
        interiorCount += removeInterior(map.getRoads(), removed, allObjects);
        interiorCount += removeInterior(map.getIntersections(), removed, allObjects);
        interiorCount += removeInterior(map.getBuildings(), removed, allObjects);
        setStatus("Removed " + removed.size() + " faces: " + duplicateCount + " duplicates and " + interiorCount + " interior");
        debug.clearBackground();
        debug.activate();
        debug.show("Result", ConvertTools.getAllDebugShapes(map));
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
        Logger.debug(test.size() + " test objects, " + toCheck.size() + " to check, " + removed.size() + " already removed");
        for (TemporaryObject first : test) {
            bumpProgress();
            if (removed.contains(first)) {
                continue;
            }
            Logger.debug("Next test object: " + first);
            for (TemporaryObject second : toCheck) {
                if (removed.contains(second)) {
                    continue;
                }
                if (first == second) {
                    continue;
                }
                Logger.debug("Next check object: " + second);
                if (first.isDuplicate(second)) {
                    map.removeTemporaryObject(second);
                    removed.add(second);
                    ++count;
                    Logger.debug("Removed duplicate object: " + second + " is same as " + first);
                }
                debug.show("Checking for duplicates",
                           new TemporaryObjectInfo(first, "First", Color.WHITE, Constants.TRANSPARENT_LIME),
                           new TemporaryObjectInfo(second, "Second", Color.WHITE, Constants.TRANSPARENT_BLUE));
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
