package maps.gml.view;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import maps.gml.GMLDirectedEdge;
import maps.gml.GMLEdge;
import maps.gml.GMLMap;
import maps.gml.GMLNode;
import maps.gml.GMLObject;
import maps.gml.GMLShape;
import maps.validate.ValidationError;

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
    private Map<Integer, List<ValidationError>> errors;

    /**
       Construct a new GMLObjectInspector.
       @param map The GMLMap to consult for geometry information.
    */
    public GMLObjectInspector(GMLMap map) {
        super(new BorderLayout());
        this.map = map;
        errors = new HashMap<Integer, List<ValidationError>>();
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
        List<ValidationError> e = (node == null) ? null : errors.get(node.getID());
        nodeModel.show(node, e);
    }

    /**
       Inspect a GMLEdge.
       @param edge The edge to inspect.
    */
    public void inspect(GMLEdge edge) {
        table.setModel(edgeModel);
        List<ValidationError> e = (edge == null) ? null : errors.get(edge.getID());
        edgeModel.show(edge, e);
    }

    /**
       Inspect a GMLShape.
       @param shape The shape to inspect.
    */
    public void inspect(GMLShape shape) {
        table.setModel(shapeModel);
        List<ValidationError> e = (shape == null) ? null : errors.get(shape.getID());
        shapeModel.show(shape, e);
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

    /**
     * Set the Collection of ValidationErrors for the Inspector to display in the table.
     * @param err The collection of errors.
     */
    public void setErrors(Collection<ValidationError> err) {
        errors.clear();
        for (ValidationError e : err) {
            if (!errors.containsKey(e.getId())) {
                errors.put(e.getId(), new ArrayList<ValidationError>());
            }
            errors.get(e.getId()).add(e);
        }
    }

    private class NodeTableModel extends AbstractTableModel {
        private GMLNode node;
        private List<ValidationError> errors;

        void show(GMLNode n, List<ValidationError> err) {
            node = n;
            errors = err;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            int errorCount = (errors == null) ? 0 : errors.size();
            return NODE_ROWS + errorCount;
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
                    return "Error";
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
                    int errorCount = (errors == null) ? 0 : errors.size();
                    int index = row - NODE_ROWS;
                    if (index < 0 || index >= errorCount) {
                        throw new IllegalArgumentException("Invalid row: " + row);
                    }
                    return errors.get(index).getMessage();
                }
            }
            else {
                throw new IllegalArgumentException("Unrecognised column: " + col);
            }
        }
    }

    private class EdgeTableModel extends AbstractTableModel {
        private GMLEdge edge;
        private List<ValidationError> errors;

        void show(GMLEdge e, List<ValidationError> err) {
            edge = e;
            errors = err;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            int errorCount = (errors == null) ? 0 : errors.size();
            return EDGE_ROWS + errorCount;
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
                    return "Error";
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
                    int errorCount = (errors == null) ? 0 : errors.size();
                    int index = row - EDGE_ROWS;
                    if (index < 0 || index >= errorCount) {
                        throw new IllegalArgumentException("Invalid row: " + row);
                    }
                    return errors.get(index).getMessage();
                }
            }
            else {
                throw new IllegalArgumentException("Unrecognised column: " + col);
            }
        }
    }

    private static class ShapeTableModel extends AbstractTableModel {
        private GMLShape shape;
        private List<ValidationError> errors;

        void show(GMLShape s, List<ValidationError> err) {
            shape = s;
            errors = err;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            int edgeCount = (shape == null) ? 0 : shape.getEdges().size();
            int errorCount = (errors == null) ? 0 : errors.size();
            return SHAPE_BASE_ROWS + edgeCount + errorCount;
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
                    int edgeCount = (shape == null) ? 0 : shape.getEdges().size();
                    if (row < SHAPE_BASE_ROWS + edgeCount) {
                        return "Edge " + (row - SHAPE_BASE_ROWS + 1);
                    }
                    return "Error";
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
                    int edgeCount = shape.getEdges().size();
                    if (row < SHAPE_BASE_ROWS + edgeCount) {
                        List<GMLDirectedEdge> edges = shape.getEdges();
                        int index = row - SHAPE_BASE_ROWS;
                        if (index < 0 || index >= edges.size()) {
                            throw new IllegalArgumentException("Invalid row: " + row);
                        }
                        return edges.get(index);
                    }
                    int errorCount = (errors == null) ? 0 : errors.size();
                    int index = row - SHAPE_BASE_ROWS - edgeCount;
                    if (index < 0 || index >= errorCount) {
                        throw new IllegalArgumentException("Invalid row: " + row);
                    }
                    return errors.get(index).getMessage();
                }
            }
            else {
                throw new IllegalArgumentException("Unrecognised column: " + col);
            }
        }
    }
}