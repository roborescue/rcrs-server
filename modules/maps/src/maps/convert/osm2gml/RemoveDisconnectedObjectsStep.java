package maps.convert.osm2gml;

import maps.convert.ConvertStep;
import maps.convert.osm2gml.debug.DebugPalette;
import maps.convert.osm2gml.debug.PolygonLayer;
import maps.convert.osm2gml.debug.StepVisualizer;

import java.util.*;

/**
 * Remove buildings and roads that do not belong to the largest connected
 * component.
 */
public class RemoveDisconnectedObjectsStep extends ConvertStep {
    private final TemporaryMap map;

    /**
     * Constructs a new {@code PruneDisconnectedObjectsStep}.
     *
     * @param map the map
     */
    public RemoveDisconnectedObjectsStep(TemporaryMap map) {
        this.map = map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Pruning disconnected objects";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void step() {
        // Collect all buildings and roads as the connectivity domain.
        Collection<TemporaryObject> domain = map.getAllObjects();

        setProgressLimit(domain.size());

        // Build an adjacency map from shared edges between domain objects.
        Map<TemporaryObject, Set<TemporaryObject>> adjacency = buildAdjacency(domain);

        // Find all connected components via BFS.
        List<Set<TemporaryObject>> components = findConnectedComponents(domain, adjacency);

        // Identify the largest component as the main connected components.
        Set<TemporaryObject> mainComponent = findLargestComponent(components);

        // Collect objects outside the main component for removal.
        List<TemporaryObject> toRemove = new ArrayList<>(domain);
        toRemove.removeAll(mainComponent);

        // Remove all disconnected objects and rebuild internal state.
        for (TemporaryObject object : toRemove) {
            map.removeTemporaryObject(object);
        }

        if (!toRemove.isEmpty()) {
            map.resynchronizeStateFromObjects();
        }

        setStatus("Removed " + toRemove.size() + " disconnected objects across"
            + (components.size() - 1) + " isolated component(s).");
        visualizeResults(toRemove);
    }

    // Build an adjacency map: two objects are adjacent when they share at least one edge.
    private Map<TemporaryObject, Set<TemporaryObject>> buildAdjacency(Collection<TemporaryObject> domain) {
        Map<TemporaryObject, Set<TemporaryObject>> adjacency = new HashMap<>();

        for (TemporaryObject object : domain) {
            Set<TemporaryObject> neighbours = new HashSet<>();

            for (DirectedEdge directedEdge : object.getEdges()) {
                neighbours.addAll(map.getAttachedObjects(directedEdge.getEdge()));
            }
            neighbours.remove(object);
            adjacency.put(object, neighbours);
            bumpProgress();
        }

        return adjacency;
    }

    // Enumerate all connected components using BFS over the adjacency map.
    private List<Set<TemporaryObject>> findConnectedComponents(
            Collection<TemporaryObject> domain,
            Map<TemporaryObject, Set<TemporaryObject>> adjacency) {

        List<Set<TemporaryObject>> components = new ArrayList<>();
        Set<TemporaryObject> visited = new HashSet<>();

        for (TemporaryObject seed : domain) {
            if (visited.contains(seed)) continue;

            Set<TemporaryObject> component = expandComponent(seed, adjacency, visited);
            components.add(component);
        }

        return components;
    }

    // Expand a single connected component from a seed object via BFS.
    private Set<TemporaryObject> expandComponent(
            TemporaryObject seed,
            Map<TemporaryObject, Set<TemporaryObject>> adjacency,
            Set<TemporaryObject> visited) {

        Set<TemporaryObject> component = new HashSet<>();
        Queue<TemporaryObject> queue = new ArrayDeque<>();

        queue.add(seed);
        visited.add(seed);

        while (!queue.isEmpty()) {
            TemporaryObject current = queue.poll();
            component.add(current);

            for (TemporaryObject neighbour : adjacency.get(current)) {
                if (visited.contains(neighbour)) continue;
                visited.add(neighbour);
                queue.add(neighbour);
            }
        }

        return component;
    }

    // Return the largest component from a list of components.
    private Set<TemporaryObject> findLargestComponent(List<Set<TemporaryObject>> components) {
        return components.stream()
                .max(Comparator.comparingInt(Set::size))
                .orElse(Collections.emptySet());
    }

    private void visualizeResults(List<TemporaryObject> removed) {
        StepVisualizer.create(debug)
                .title("Prune Disconnected Objects")
                .layer(PolygonLayer.of(removed)
                        .name("Removed Objects")
                        .fillColor(DebugPalette.CORAL_FILL)
                        .outlineColor(DebugPalette.CORAL_STROKE))
                .backgroundLayer(PolygonLayer.of(map.getAllObjects())
                        .name("Objects")
                        .fillColor(DebugPalette.SLATE_FILL)
                        .outlineColor(DebugPalette.SLATE_STROKE))
                .show();
    }
}
