package maps.osm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Assembles closed rings from the way segments of a multipolygon relation.
 */
public class OSMRingAssembler {

    private OSMRingAssembler() {
    }

    /**
     * Returns the closed rings formed by joining the specified segments
     * end to end. Segments that do not form a closed ring are discarded.
     *
     * @param segments the node ID lists of the ways, in any order and direction
     * @return the closed rings, each starting and ending with the same node ID
     */
    static List<List<Long>> assemble(List<List<Long>> segments) {
        List<List<Long>> remaining = segments.stream()
                .filter(segment -> 2 <= segment.size())
                .collect(Collectors.toCollection(LinkedList::new));

        // Each pass consumes the segments of one ring, or discards a chain that cannot close
        List<List<Long>> rings = new ArrayList<>();
        while (!remaining.isEmpty()) {
            buildRing(remaining.removeFirst(), remaining).ifPresent(rings::add);
        }
        return rings;
    }

    // Joins segments to the first one until the ring closes, or returns empty if none continues it
    private static Optional<List<Long>> buildRing(List<Long> first, List<List<Long>> remaining) {
        List<Long> ring = new ArrayList<>(first);
        while (!isClosed(ring)) {
            if (!extend(ring, remaining)) return Optional.empty();
        }
        return Optional.of(ring);
    }

    // A ring needs at least three distinct nodes plus the repeated first node
    private static boolean isClosed(List<Long> ring) {
        return 4 <= ring.size() && ring.getFirst().equals(ring.getLast());
    }

    // Appends the first segment that continues the ring and removes it from the list
    private static boolean extend(List<Long> ring, List<List<Long>> remaining) {
        Long tail = ring.getLast();
        for (List<Long> segment : remaining) {
            Optional<List<Long>> oriented = orientedFrom(segment, tail);
            if (oriented.isEmpty()) continue;

            List<Long> path = oriented.get();
            ring.addAll(path.subList(1, path.size()));
            remaining.remove(segment);
            return true;
        }
        return false;
    }

    // Returns the segment oriented to start at the node, or empty if it does not touch the node
    private static Optional<List<Long>> orientedFrom(List<Long> segment, Long node) {
        if (segment.getFirst().equals(node)) return Optional.of(segment);
        if (!segment.getLast().equals(node)) return Optional.empty();

        List<Long> reversed = new ArrayList<>(segment);
        Collections.reverse(reversed);
        return Optional.of(reversed);
    }
}
