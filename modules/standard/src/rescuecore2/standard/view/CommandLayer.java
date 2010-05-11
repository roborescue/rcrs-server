package rescuecore2.standard.view;

import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.DrawingTools;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.view.RenderedObject;
import rescuecore2.view.Icons;
import rescuecore2.worldmodel.EntityID;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKExtinguish;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKRescue;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.standard.messages.AKUnload;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

/**
   A layer for viewing commands.
 */
public class CommandLayer extends StandardViewLayer {
    private static final int SIZE = 15;
    private static final Color CLEAR_COLOUR = new Color(0, 0, 255, 128);
    private static final Color RESCUE_COLOUR = new Color(255, 255, 255, 128);
    private static final Color LOAD_COLOUR = new Color(255, 255, 255, 128);
    private static final Color UNLOAD_COLOUR = new Color(255, 255, 255, 128);

    private static final double ARROW_ANGLE = Math.toRadians(135);
    private static final double ARROW_LENGTH = 5;

    private Graphics2D g;
    private ScreenTransform t;
    private Collection<Command> commands;

    private boolean renderMove;
    private boolean renderExtinguish;
    private boolean renderClear;
    private boolean renderLoad;
    private boolean renderUnload;
    private boolean renderRescue;

    private RenderMoveAction renderMoveAction;
    private RenderExtinguishAction renderExtinguishAction;
    private RenderClearAction renderClearAction;
    private RenderRescueAction renderRescueAction;
    private RenderLoadAction renderLoadAction;
    private RenderUnloadAction renderUnloadAction;

    /**
       Construct a new CommandLayer.
    */
    public CommandLayer() {
        commands = new ArrayList<Command>();
        renderMove = true;
        renderExtinguish = true;
        renderClear = true;
        renderLoad = true;
        renderUnload = true;
        renderRescue = true;
        renderMoveAction = new RenderMoveAction();
        renderExtinguishAction = new RenderExtinguishAction();
        renderClearAction = new RenderClearAction();
        renderRescueAction = new RenderRescueAction();
        renderLoadAction = new RenderLoadAction();
        renderUnloadAction = new RenderUnloadAction();
    }

    /**
       Set whether to render Move commands.
       @param render True if move commands should be rendered, false otherwise.
    */
    public void setRenderMove(boolean render) {
        renderMove = render;
        renderMoveAction.update();
    }

    /**
       Set whether to render Extinguish commands.
       @param render True if extinguish commands should be rendered, false otherwise.
    */
    public void setRenderExtinguish(boolean render) {
        renderExtinguish = render;
        renderExtinguishAction.update();
    }

    /**
       Set whether to render Clear commands.
       @param render True if clear commands should be rendered, false otherwise.
    */
    public void setRenderClear(boolean render) {
        renderClear = render;
        renderClearAction.update();
    }

    /**
       Set whether to render Load commands.
       @param render True if load commands should be rendered, false otherwise.
    */
    public void setRenderLoad(boolean render) {
        renderLoad = render;
        renderLoadAction.update();
    }

    /**
       Set whether to render Unload commands.
       @param render True if unload commands should be rendered, false otherwise.
    */
    public void setRenderUnload(boolean render) {
        renderUnload = render;
        renderUnloadAction.update();
    }

    /**
       Set whether to render Rescue commands.
       @param render True if rescue commands should be rendered, false otherwise.
    */
    public void setRenderRescue(boolean render) {
        renderRescue = render;
        renderRescueAction.update();
    }

    @Override
    public List<JMenuItem> getPopupMenuItems() {
        List<JMenuItem> result = new ArrayList<JMenuItem>();
        result.add(new JMenuItem(renderMoveAction));
        result.add(new JMenuItem(renderClearAction));
        result.add(new JMenuItem(renderExtinguishAction));
        result.add(new JMenuItem(renderRescueAction));
        result.add(new JMenuItem(renderLoadAction));
        result.add(new JMenuItem(renderUnloadAction));
        return result;
    }

    @Override
    public String getName() {
        return "Commands";
    }

    @Override
    public Rectangle2D view(Object... objects) {
        synchronized (commands) {
            commands.clear();
            return super.view(objects);
        }
    }

    @Override
    protected void viewObject(Object o) {
        super.viewObject(o);
        if (o instanceof Command) {
            commands.add((Command)o);
        }
    }

    @Override
    public Collection<RenderedObject> render(Graphics2D graphics, ScreenTransform transform, int width, int height) {
        synchronized (commands) {
            Collection<RenderedObject> result = new ArrayList<RenderedObject>();
            g = graphics;
            t = transform;
            for (Command next : commands) {
                if (renderMove && next instanceof AKMove) {
                    renderMove((AKMove)next);
                }
                if (renderExtinguish && next instanceof AKExtinguish) {
                    renderExtinguish((AKExtinguish)next);
                }
                if (renderClear && next instanceof AKClear) {
                    renderClear((AKClear)next);
                }
                if (renderRescue && next instanceof AKRescue) {
                    renderRescue((AKRescue)next);
                }
                if (renderLoad && next instanceof AKLoad) {
                    renderLoad((AKLoad)next);
                }
                if (renderUnload && next instanceof AKUnload) {
                    renderUnload((AKUnload)next);
                }
            }
            return result;
        }
    }

    private void renderMove(AKMove move) {
        g.setColor(Color.BLACK);
        List<EntityID> path = move.getPath();
        if (path.isEmpty()) {
            return;
        }
        Iterator<EntityID> it = path.iterator();
        StandardEntity first = world.getEntity(it.next());
        Pair<Integer, Integer> firstLocation = first.getLocation(world);
        int startX = t.xToScreen(firstLocation.first());
        int startY = t.yToScreen(firstLocation.second());
        while (it.hasNext()) {
            StandardEntity next = world.getEntity(it.next());
            Pair<Integer, Integer> nextLocation = next.getLocation(world);
            int nextX = t.xToScreen(nextLocation.first());
            int nextY = t.yToScreen(nextLocation.second());
            g.drawLine(startX, startY, nextX, nextY);
            // Draw an arrow partway along the length
            DrawingTools.drawArrowHeads(startX, startY, nextX, nextY, g);
            startX = nextX;
            startY = nextY;
        }
    }

    private void renderExtinguish(AKExtinguish ex) {
        StandardEntity fb = world.getEntity(ex.getAgentID());
        StandardEntity target = world.getEntity(ex.getTarget());
        Pair<Integer, Integer> fbLocation = fb.getLocation(world);
        Pair<Integer, Integer> targetLocation = target.getLocation(world);
        int fbX = t.xToScreen(fbLocation.first());
        int fbY = t.yToScreen(fbLocation.second());
        int bX = t.xToScreen(targetLocation.first());
        int bY = t.yToScreen(targetLocation.second());
        g.setColor(Color.BLUE);
        g.drawLine(fbX, fbY, bX, bY);
    }

    private void renderClear(AKClear clear) {
        renderHumanAction(world.getEntity(clear.getAgentID()), CLEAR_COLOUR, null);
    }

    private void renderRescue(AKRescue rescue) {
        renderHumanAction(world.getEntity(rescue.getAgentID()), RESCUE_COLOUR, null);
    }

    private void renderLoad(AKLoad load) {
        renderHumanAction(world.getEntity(load.getAgentID()), LOAD_COLOUR, "L");
    }

    private void renderUnload(AKUnload unload) {
        renderHumanAction(world.getEntity(unload.getAgentID()), UNLOAD_COLOUR, "U");
    }

    private void renderHumanAction(StandardEntity entity, Color colour, String s) {
        Pair<Integer, Integer> location = entity.getLocation(world);
        int x = t.xToScreen(location.first()) - SIZE / 2;
        int y = t.yToScreen(location.second()) - SIZE / 2;
        Shape shape = new Ellipse2D.Double(x, y, SIZE, SIZE);
        g.setColor(colour);
        g.fill(shape);
        if (s != null) {
            g.setColor(Color.BLACK);
            FontMetrics metrics = g.getFontMetrics();
            int width = metrics.stringWidth(s);
            int height = metrics.getHeight();
            x = t.xToScreen(location.first());
            y = t.yToScreen(location.second());
            g.drawString(s, x - (width / 2), y + (height / 2));
        }
    }

    private final class RenderMoveAction extends AbstractAction {
        public RenderMoveAction() {
            super("Show move commands");
            update();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setRenderMove(!renderMove);
            component.repaint();
        }

        void update() {
            putValue(Action.SELECTED_KEY, Boolean.valueOf(renderMove));
            putValue(Action.SMALL_ICON, renderMove ? Icons.TICK : Icons.CROSS);
        }
    }

    private final class RenderClearAction extends AbstractAction {
        public RenderClearAction() {
            super("Show clear commands");
            update();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setRenderClear(!renderClear);
            component.repaint();
        }

        void update() {
            putValue(Action.SELECTED_KEY, Boolean.valueOf(renderClear));
            putValue(Action.SMALL_ICON, renderClear ? Icons.TICK : Icons.CROSS);
        }
    }

    private final class RenderExtinguishAction extends AbstractAction {
        public RenderExtinguishAction() {
            super("Show extinguish commands");
            update();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setRenderExtinguish(!renderExtinguish);
            component.repaint();
        }

        void update() {
            putValue(Action.SELECTED_KEY, Boolean.valueOf(renderExtinguish));
            putValue(Action.SMALL_ICON, renderExtinguish ? Icons.TICK : Icons.CROSS);
        }
    }

    private final class RenderRescueAction extends AbstractAction {
        public RenderRescueAction() {
            super("Show rescue commands");
            update();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setRenderRescue(!renderRescue);
            component.repaint();
        }

        void update() {
            putValue(Action.SELECTED_KEY, Boolean.valueOf(renderRescue));
            putValue(Action.SMALL_ICON, renderRescue ? Icons.TICK : Icons.CROSS);
        }
    }

    private final class RenderLoadAction extends AbstractAction {
        public RenderLoadAction() {
            super("Show load commands");
            update();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setRenderLoad(!renderLoad);
            component.repaint();
        }

        void update() {
            putValue(Action.SELECTED_KEY, Boolean.valueOf(renderLoad));
            putValue(Action.SMALL_ICON, renderLoad ? Icons.TICK : Icons.CROSS);
        }
    }

    private final class RenderUnloadAction extends AbstractAction {
        public RenderUnloadAction() {
            super("Show unload commands");
            update();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setRenderUnload(!renderUnload);
            component.repaint();
        }

        void update() {
            putValue(Action.SELECTED_KEY, Boolean.valueOf(renderUnload));
            putValue(Action.SMALL_ICON, renderUnload ? Icons.TICK : Icons.CROSS);
        }
    }
}
