package maps.gml.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.Point;
import java.awt.Insets;

import maps.gml.view.NodeDecorator;
import maps.gml.view.SquareNodeDecorator;
import maps.gml.GMLNode;
import maps.gml.GMLCoordinates;

/**
   A tool for merging nodes.
*/
public class MergeNodesTool extends AbstractTool {
    private static final Color HOVER_COLOUR = Color.BLUE;
    private static final Color MERGE_COLOUR = Color.RED;
    private static final int HIGHLIGHT_SIZE = 6;

    private Listener listener;
    private NodeDecorator hoverHighlight;
    private NodeDecorator mergeHighlight;
    private GMLNode hover;
    private GMLNode merge;
    private boolean merging;

    /**
       Construct a MergeNodesTool.
       @param editor The editor instance.
    */
    public MergeNodesTool(GMLEditor editor) {
        super(editor);
        listener = new Listener();
        hoverHighlight = new SquareNodeDecorator(HOVER_COLOUR, HIGHLIGHT_SIZE);
        mergeHighlight = new SquareNodeDecorator(MERGE_COLOUR, HIGHLIGHT_SIZE);
    }

    @Override
    public String getName() {
        return "Merge nodes";
    }

    @Override
    public void activate() {
        editor.getViewer().addMouseListener(listener);
        editor.getViewer().addMouseMotionListener(listener);
        hover = null;
        merge = null;
        merging = false;
    }

    @Override
    public void deactivate() {
        editor.getViewer().removeMouseListener(listener);
        editor.getViewer().removeMouseMotionListener(listener);
        editor.getViewer().clearAllNodeDecorators();
        editor.getViewer().repaint();
    }

    private void hover(GMLNode node) {
        if (hover == node) {
            return;
        }
        if (hover != null) {
            editor.getViewer().clearNodeDecorator(hover);
        }
        hover = node;
        if (hover != null) {
            editor.getViewer().setNodeDecorator(hoverHighlight, hover);
        }
        editor.getViewer().repaint();
    }

    private void setMerge(GMLNode node) {
        if (merge == node) {
            return;
        }
        if (node == hover) {
            return;
        }
        if (merge != null) {
            editor.getViewer().clearNodeDecorator(merge);
        }
        merge = node;
        if (merge != null) {
            editor.getViewer().setNodeDecorator(mergeHighlight, merge);
        }
        editor.getViewer().repaint();
    }

    private class Listener implements MouseListener, MouseMotionListener {
        @Override
        public void mouseMoved(MouseEvent e) {
            Point p = fixEventPoint(e.getPoint());
            GMLCoordinates c = editor.getViewer().getCoordinatesAtPoint(p.x, p.y);
            GMLNode node = editor.getMap().findNearestNode(c.getX(), c.getY());
            hover(node);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (hover == null) {
                return;
            }
            if (!merging) {
                return;
            }
            Point p = fixEventPoint(e.getPoint());
            GMLCoordinates c = editor.getViewer().getCoordinatesAtPoint(p.x, p.y);
            GMLNode node = editor.getMap().findNearestNode(c.getX(), c.getY());
            setMerge(node);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (merging) {
                    if (hover != null && merge != null) {
                        editor.getMap().replaceNode(hover, merge);
                        editor.getMap().removeNode(hover);
                        editor.setChanged();
                    }
                    if (hover != null) {
                        editor.getViewer().clearNodeDecorator(hover);
                    }
                    if (merge != null) {
                        editor.getViewer().clearNodeDecorator(merge);
                    }
                    editor.getViewer().repaint();
                    hover = null;
                    merge = null;
                    merging = false;
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                merging = true;
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }
        @Override
        public void mouseEntered(MouseEvent e) {
        }
        @Override
        public void mouseExited(MouseEvent e) {
        }

        private Point fixEventPoint(Point p) {
            Insets insets = editor.getViewer().getInsets();
            return new Point(p.x - insets.left, p.y - insets.top);
        }
    }
}