package maps.gml.view;

import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;

import maps.gml.GMLNode;
import maps.gml.GMLEdge;
import maps.gml.GMLObject;

/**
   A class for inspecting GML objects.
*/
public class GMLObjectInspector extends JPanel {
    private static final int NODE_ROW_ID = 0;
    private static final int NODE_ROW_X = 1;
    private static final int NODE_ROW_Y = 2;
    private static final int NODE_ROWS = 3;

    private static final int EDGE_ROW_ID = 0;
    private static final int EDGE_ROW_START = 1;
    private static final int EDGE_ROW_END = 2;
    private static final int EDGE_ROW_PASSABLE = 3;
    private static final int EDGE_ROWS = 4;

    private JTable table;
    private NodeTableModel nodeModel;
    private EdgeTableModel edgeModel;

    /**
       Construct a new GMLObjectInspector.
    */
    public GMLObjectInspector() {
        super(new BorderLayout());
        nodeModel = new NodeTableModel();
        edgeModel = new EdgeTableModel();
        table = new JTable();
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);
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
       Inspect a GMLObject.
       @param object The object to inspect.
    */
    public void inspect(GMLObject object) {
        if (object instanceof GMLNode) {
            inspect((GMLNode)object);
        }
        else if (object instanceof GMLEdge) {
            inspect((GMLEdge)object);
        }
        else {
            throw new IllegalArgumentException("Don't know how to inspect " + object);
        }
    }

    private static class NodeTableModel extends AbstractTableModel {
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
                default:
                    throw new IllegalArgumentException("Unrecognised row: " + row);
                }
            }
            else {
                throw new IllegalArgumentException("Unrecognised column: " + col);
            }
        }
    }

    private static class EdgeTableModel extends AbstractTableModel {
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
                default:
                    throw new IllegalArgumentException("Unrecognised row: " + row);
                }
            }
            else {
                throw new IllegalArgumentException("Unrecognised column: " + col);
            }
        }
    }
}