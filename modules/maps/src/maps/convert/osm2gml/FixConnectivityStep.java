package maps.convert.osm2gml;

import java.util.*;

/**
 * This step removes buildings and roads that are not reachable from the largest
 * connected component of the map. Objects are considered adjacent when they share
 * a common edge, which is consistent with the passable-edge logic in
 * {@code TemporaryMap}.
 *
 * <p>
 * This step should run after {@code CommonBuildingStep} and
 * {@code PruneOrphanBuildingStep}, before any further geometric processing.
 */
public class FixConnectivityStep extends BaseModificationStep {

    public FixConnectivityStep(final TemporaryMap map) {
        super(map);
    }

    @Override
    public String getDescription() {
        return "Fixing map connectivity";
    }

    @Override
    protected void step() {
        // Collect all buildings and roads as the connectivity domain.
        final Collection<TemporaryObject> domain = map.getAllObjects();

        setProgressLimit(domain.size());

        // Build an adjacency map from shared edges between domain objects.
        final Map<TemporaryObject, Set<TemporaryObject>> adjacency = buildAdjacency(domain);

        // Find all connected components via BFS.
        final List<Set<TemporaryObject>> components = findConnectedComponents(domain, adjacency);

        // Identify the largest component as the main connected components.
        final Set<TemporaryObject> mainComponent = findLargestComponent(components);

        // Collect objects outside the main component for removal.
        final List<TemporaryObject> toRemove = new ArrayList<>(domain);
        toRemove.removeAll(mainComponent);

        // Remove all disconnected objects and rebuild internal state.
        for (final TemporaryObject object : toRemove) {
            map.removeTemporaryObject(object);
        }

        if (!toRemove.isEmpty()) {
            map.resynchronizeStateFromObjects();
        }

        setStatus("Removed " + toRemove.size() + " disconnected objects across"
            + (components.size() - 1) + " isolated component(s).");
        visualizeDifference(domain, mainComponent, "Fix Connectivity Results");
    }

    // Build an adjacency map: two objects are adjacent when they share at least one edge.
    private Map<TemporaryObject, Set<TemporaryObject>> buildAdjacency(
            final Collection<TemporaryObject> domain) {
        final Map<TemporaryObject, Set<TemporaryObject>> adjacency = new HashMap<>();

        for (final TemporaryObject object : domain) {
            final Set<TemporaryObject> neighbours = new HashSet<>();

            for (final DirectedEdge directedEdge : object.getEdges()) {
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
            final Collection<TemporaryObject> domain,
            final Map<TemporaryObject, Set<TemporaryObject>> adjacency) {
        final List<Set<TemporaryObject>> components = new ArrayList<>();
        final Set<TemporaryObject> visited = new HashSet<>();

        for (final TemporaryObject seed : domain) {
            if (visited.contains(seed)) continue;

            final Set<TemporaryObject> component = expandComponent(seed, adjacency, visited);
            components.add(component);
        }

        return components;
    }

    // Expand a single connected component from a seed object via BFS.
    private Set<TemporaryObject> expandComponent(
            final TemporaryObject seed,
            final Map<TemporaryObject, Set<TemporaryObject>> adjacency,
            final Set<TemporaryObject> visited) {
        final Set<TemporaryObject> component = new HashSet<>();
        final Queue<TemporaryObject> queue = new ArrayDeque<>();

        queue.add(seed);
        visited.add(seed);

        while (!queue.isEmpty()) {
            final TemporaryObject current = queue.poll();
            component.add(current);

            for (final TemporaryObject neighbour : adjacency.get(current)) {
                if (visited.contains(neighbour)) continue;
                visited.add(neighbour);
                queue.add(neighbour);
            }
        }

        return component;
    }

    // Return the largest component from a list of components.
    private Set<TemporaryObject> findLargestComponent(final List<Set<TemporaryObject>> components) {
        return components.stream()
                .max(Comparator.comparingInt(Set::size))
                .orElse(Collections.emptySet());
    }
}
