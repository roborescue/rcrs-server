package rescuecore2.standard.view;

import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.DrawingTools;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.view.RenderedObject;
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

    /**
       Construct a new CommandLayer.
    */
    public CommandLayer() {
        commands = new ArrayList<Command>();
    }

    @Override
    public String getName() {
        return "Commands";
    }

    @Override
    public Rectangle2D view(Object... objects) {
        commands.clear();
        return super.view(objects);
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
        Collection<RenderedObject> result = new ArrayList<RenderedObject>();
        g = graphics;
        t = transform;
        for (Command next : commands) {
            if (next instanceof AKMove) {
                renderMove((AKMove)next);
            }
            if (next instanceof AKExtinguish) {
                renderExtinguish((AKExtinguish)next);
            }
            if (next instanceof AKClear) {
                renderClear((AKClear)next);
            }
            if (next instanceof AKRescue) {
                renderRescue((AKRescue)next);
            }
            if (next instanceof AKLoad) {
                renderLoad((AKLoad)next);
            }
            if (next instanceof AKUnload) {
                renderUnload((AKUnload)next);
            }
        }
        return result;
    }

    private void renderMove(AKMove move) {
        g.setColor(Color.BLACK);
        List<EntityID> path = move.getPath();
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
            int dx = nextX - startX;
            int dy = nextY - startY;
            int headX = startX + (dx / 2);
            int headY = startY + (dy / 2);
            Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> barbs = DrawingTools.getArrowHeads(startX, startY, headX, headY, ARROW_ANGLE, ARROW_LENGTH);
            int leftX = barbs.first().first();
            int leftY = barbs.first().second();
            int rightX = barbs.second().first();
            int rightY = barbs.second().second();
            g.drawLine(leftX, leftY, headX, headY);
            g.drawLine(rightX, rightY, headX, headY);
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
}