package rescuecore2.view;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;

import javax.swing.JPopupMenu;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.Action;
import javax.swing.AbstractAction;

import rescuecore2.misc.gui.ScreenTransform;

/**
   A ViewComponent that uses layers.
 */
public class LayerViewComponent extends ViewComponent {
    private static final Icon TICK = new ImageIcon(LayerViewComponent.class.getClassLoader().getResource("rescuecore2/view/tick.png"));
    private static final Icon CROSS = new ImageIcon(LayerViewComponent.class.getClassLoader().getResource("rescuecore2/view/cross.png"));

    private List<ViewLayer> layers;
    private List<Action> layerActions;
    private Object[] data;
    private Rectangle2D bounds;

    /**
       Construct a new LayerViewComponent.
     */
    public LayerViewComponent() {
        layers = new ArrayList<ViewLayer>();
        layerActions = new ArrayList<Action>();
        addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
                        showPopupMenu(e.getX(), e.getY());
                    }
                }
                @Override
                public void mousePressed(MouseEvent e) {
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                }
                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
    }

    /**
       Add a view layer.
       @param layer The layer to add.
     */
    public void addLayer(ViewLayer layer) {
        layers.add(layer);
        layerActions.add(new LayerAction(layer));
        computeBounds();
    }

    /**
       Remove a view layer.
       @param layer The layer to remove.
     */
    public void removeLayer(ViewLayer layer) {
        int index = layers.indexOf(layer);
        if (index != -1) {
            layers.remove(index);
            layerActions.remove(index);
            computeBounds();
        }
    }

    /**
       Remove all view layers.
     */
    public void removeAllLayers() {
        layers.clear();
        layerActions.clear();
        computeBounds();
    }

    @Override
    public void view(Object... objects) {
        data = objects;
        computeBounds();
        super.view(objects);
    }

    @Override
    protected Collection<RenderedObject> render(Graphics2D g, ScreenTransform transform, int width, int height) {
        Collection<RenderedObject> result = new HashSet<RenderedObject>();
        prepaint();
        for (ViewLayer next : layers) {
            if (next.isVisible()) {
                Graphics2D copy = (Graphics2D)g.create();
                result.addAll(next.render(copy, transform, width, height));
            }
        }
        postpaint();
        return result;
    }

    /**
       Get all installed layers.
       @return All installed layers.
    */
    protected List<ViewLayer> getLayers() {
        return Collections.unmodifiableList(layers);
    }

    /**
       Do whatever needs doing before the layers are painted. The default implementation does nothing.
     */
    protected void prepaint() {
    }

    /**
       Do whatever needs doing after the layers are painted. The default implementation does nothing.
     */
    protected void postpaint() {
    }

    private void computeBounds() {
        Rectangle2D oldBounds = bounds;
        bounds = null;
        for (ViewLayer next : layers) {
            expandBounds(next.view(data));
        }
        if (bounds == null) {
            updateBounds(0, 0, 1, 1);
        }
        else if (oldBounds == null
                 || oldBounds.getMinX() != bounds.getMinX()
                 || oldBounds.getMinY() != bounds.getMinY()
                 || oldBounds.getMaxX() != bounds.getMaxX()
                 || oldBounds.getMaxY() != bounds.getMaxY()) {
            updateBounds(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());
        }
    }

    private void expandBounds(Rectangle2D next) {
        if (next == null) {
            return;
        }
        if (bounds == null) {
            bounds = next;
        }
        else {
            Rectangle2D.union(bounds, next, bounds);
        }
    }

    private void showPopupMenu(int x, int y) {
        JPopupMenu menu = new JPopupMenu("Layers");
        for (Action next : layerActions) {
            menu.add(next);
        }
        menu.show(this, x, y);
    }

    private class LayerAction extends AbstractAction {
        private ViewLayer layer;

        public LayerAction(ViewLayer layer) {
            super(layer.getName());
            this.layer = layer;
            putValue(Action.SELECTED_KEY, Boolean.valueOf(layer.isVisible()));
            putValue(Action.SMALL_ICON, layer.isVisible() ? TICK : CROSS);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean selected = ((Boolean)getValue(Action.SELECTED_KEY)).booleanValue();
            putValue(Action.SELECTED_KEY, Boolean.valueOf(!selected));
            putValue(Action.SMALL_ICON, !selected ? TICK : CROSS);
            layer.setVisible(!selected);
            LayerViewComponent.this.repaint();
        }
    }
}