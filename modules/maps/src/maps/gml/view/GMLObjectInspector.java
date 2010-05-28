package maps.gml.view;

import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;

import java.util.Collection;
import java.util.List;

import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLDirectedEdge;
import maps.gml.GMLShape;
import maps.gml.GMLObject;

/**
   A class for inspecting GML objects.
*/
public class GMLObjectInspector extends JPanel {
    private static final int NODE_ROW_ID = 0;
    private static final int NODE_ROW_X = 1;
    private static final int NODE_ROW_Y = 2;
    private static final int NODE_ROW_ATTACHED_EDGES = 3;
    private static final int NODE_ROWS = 4;

    private static final int EDGE_ROW_ID = 0;
    private static final int EDGE_ROW_START = 1;
    private static final int EDGE_ROW_END = 2;
    private static final int EDGE_ROW_PASSABLE = 3;
    private static final int EDGE_ROW_ATTACHED_SHAPES = 4;
    private static final int EDGE_ROWS = 5;

    private static final int SHAPE_ROW_ID = 0;
    private static final int SHAPE_ROW_EDGE_COUNT = 1;
    private static final int SHAPE_BASE_ROWS = 2;

    private static final TableModel EMPTY_MODEL = new AbstractTableModel() {
        @Override
        public int getRowCount() {
            return 0;
        }

        @Override
        public int getColumnCount() {
            return 0;
        }

        @Override
        public Object getValueAt(int row, int col) {
            return null;
        }
        };

    private GMLMap map;
    private JTable table;
    private NodeTableModel nodeModel;
    private EdgeTableModel edgeModel;
    private ShapeTableModel shapeModel;

    /**
       Construct a new GMLObjectInspector.
       @param map The GMLMap to consult for geometry information.
    */
    public GMLObjectInspector(GMLMap map) {
        super(new BorderLayout());
        this.map = map;
        nodeModel = new NodeTableModel();
        edgeModel = new EdgeTableModel();
        shapeModel = new ShapeTableModel();
        table = new JTable();
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);
    }

    /**
       Set the map this inspector should consult for geometry information.
       @param newMap The new map.
    */
    public void setMap(GMLMap newMap) {
        map = newMap;
    }

    /**
       Inspect a GMLNode.
       @param node The node to inspect.
    */
    public void inspect(GMLNode node) {
        table.setModel(nodeModel);
        nodeModel.show(node);
    }

    /**
       Inspect a GMLEdge.
       @param edge The edge to inspect.
    */
    public void inspect(GMLEdge edge) {
        table.setModel(edgeModel);
        edgeModel.show(edge);
    }

    /**
       Inspect a GMLShape.
       @param shape The shape to inspect.
    */
    public void inspect(GMLShape shape) {
        table.setModel(shapeModel);
        shapeModel.show(shape);
    }

    /**
       Inspect a GMLObject.
       @param object The object to inspect.
    */
    public void inspect(GMLObject object) {
        if (object == null) {
            table.setModel(EMPTY_MODEL);
        }
        else if (object instanceof GMLNode) {
            inspect((GMLNode)object);
        }
        else if (object instanceof GMLEdge) {
            inspect((GMLEdge)object);
        }
        else if (object instanceof GMLShape) {
            inspect((GMLShape)object);
        }
        else {
            throw new IllegalArgumentException("Don't know how to inspect " + object);
        }
    }

    private class NodeTableModel extends AbstractTableModel {
        private GMLNode node;

        void show(GMLNode n) {
            node = n;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return NODE_ROWS;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                switch (row) {
                case NODE_ROW_ID:
                    return "Node ID";
                case NODE_ROW_X:
                    return "X";
                case NODE_ROW_Y:
                    return "Y";
                case NODE_ROW_ATTACHED_EDGES:
                    return "Attached edges";
                default:
                    throw new IllegalArgumentException("Unrecognised row: " + row);
                }
            }
            else if (col == 1) {
                if (node == null) {
                    return null;
                }
                switch (row) {
                case NODE_ROW_ID:
                    return node.getID();
                case NODE_ROW_X:
                    return node.getX();
                case NODE_ROW_Y:
                    return node.getY();
                case NODE_ROW_ATTACHED_EDGES:
                    if (map == null) {
                        return "";
                    }
                    Collection<GMLEdge> attached = map.getAttachedEdges(node);
                    StringBuilder result = new StringBuilder();
                    for (GMLEdge next : attached) {
                        result.append(next.toString());
                        result.append("  ");
                    }
                    return result.toString();
                default:
                    throw new IllegalArgumentException("Unrecognised row: " + row);
                }
            }
            else {
                throw new IllegalArgumentException("Unrecognised column: " + col);
            }
        }
    }

    private class EdgeTableModel extends AbstractTableModel {
        private GMLEdge edge;

        void show(GMLEdge e) {
            edge = e;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return EDGE_ROWS;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                switch (row) {
                case EDGE_ROW_ID:
                    return "Edge ID";
                case EDGE_ROW_START:
                    return "Start node";
                case EDGE_ROW_END:
                    return "End node";
                case EDGE_ROW_PASSABLE:
                    return "Passable";
                case EDGE_ROW_ATTACHED_SHAPES:
                    return "Attached shapes";
                default:
                    throw new IllegalArgumentException("Unrecognised row: " + row);
                }
            }
            else if (col == 1) {
                if (edge == null) {
                    return null;
                }
                switch (row) {
                case EDGE_ROW_ID:
                    return edge.getID();
                case EDGE_ROW_START:
                    return edge.getStart().getID();
                case EDGE_ROW_END:
                    return edge.getEnd().getID();
                case EDGE_ROW_PASSABLE:
                    return edge.isPassable();
                case EDGE_ROW_ATTACHED_SHAPES:
                    if (map == null) {
                        return "";
                    }
                    Collection<GMLShape> attached = map.getAttachedShapes(edge);
                    StringBuilder result = new StringBuilder();
                    for (GMLShape next : attached) {
                        result.append(next.toString());
                        result.append("  ");
                    }
                    return result.toString();
                default:
                    throw new IllegalArgumentException("Unrecognised row: " + row);
                }
            }
            else {
                throw new IllegalArgumentException("Unrecognised column: " + col);
            }
        }
    }

    private static class ShapeTableModel extends AbstractTableModel {
        private GMLShape shape;

        void show(GMLShape s) {
            shape = s;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return SHAPE_BASE_ROWS + (shape == null ? 0 : shape.getEdges().size());
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                switch (row) {
                case SHAPE_ROW_ID:
                    return "Shape ID";
                case SHAPE_ROW_EDGE_COUNT:
                    return "Number of edges";
                default:
                    return "Edge " + (row - SHAPE_BASE_ROWS + 1);
                }
            }
            else if (col == 1) {
                if (shape == null) {
                    return null;
                }
                switch (row) {
                case SHAPE_ROW_ID:
                    return shape.getID();
                case SHAPE_ROW_EDGE_COUNT:
                    return shape.getEdges().size();
                default:
                    List<GMLDirectedEdge> edges = shape.getEdges();
                    int index = row - SHAPE_BASE_ROWS;
                    if (index < 0 || index >= edges.size()) {
                        throw new IllegalArgumentException("Invalid row: " + row);
                    }
                    return edges.get(index);
                }
            }
            else {
                throw new IllegalArgumentException("Unrecognised column: " + col);
            }
        }
    }
}