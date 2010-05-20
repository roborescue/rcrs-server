These maps are NOT ready for use yet! They have roads overlapping buildings, some entrances are incorrect and some roads are self-intersecting which causes incorrect dimensions to be calculated. Do NOT attempt to run the simulator with any of these maps until they are fixed.

You can help fix the maps by starting the GML editor ("ant gml-editor" will do the trick) and adjusting the maps to fix these problems. Many problems can be fixed by simply moving nodes, but some require more work, particularly the self-intersecting road problems.

A brief description of the GML editor tool:

Movement
========
Zoom in and out with the mouse wheel. Hold right button to pan around the map.

Tools
=====
Inspect object: Will show information about the nearest node to a click.
Create node: Creates a new node at the click point.
Create edge: Drag from one node to another to create an edge.
Create road/building/space: Click around the edges of the shape you want to create.
Delete node: Click to delete the highlighted node.
Delete edge: Click to delete the highlighted edge.
Delete road/building/space: Click to delete the highlighted shape.
Move node: Drag a node around the map.
Merge nodes: Drag from one node to another to replace all references to the blue node with the red one. The blue node is deleted. THIS OPERATION CANNOT BE UNDONE!
Merge lines: Click on a node with exactly two attached edges to remove the node and replace the two edges with a single new edge.
Split edge: Click to insert a node inside an edge.

Functions
=========
Scale map: Rescale the map. You probably don't need this.
Fix nearby nodes: This will merge any nodes that are very close (within 1mm). Slow. You probably don't need this.
Fix duplicate edges: Remove any edges that are exactly the same (i.e. same start and end nodes) as other edges in the map. Slow. You probably don't need this.
Split edges: Split any edges that cover nearby (within 1mm) nodes. Very slow. You almost certainly don't need this.
Compute passable edges: Does what you'd expect. You probably don't need this unless you are certain the map is completely correct.

None of the functions can be undone.

Known issues
============
When creating a road/building/space the edge highlighting is sometimes incorrect. Try deleting edges around the area and recreating them and any attached roads or buildings.

Grid drawing is sometimes very slow.
