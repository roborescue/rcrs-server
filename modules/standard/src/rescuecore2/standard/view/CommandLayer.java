package rescuecore2.standard.view;

import rescuecore2.messages.Command;
import rescuecore2.view.RenderedObject;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.DrawingTools;

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

    private Collection<Command> commands;

    /**
       Construct a CommandLayer with no commands to view.
    */
    public CommandLayer() {
        commands = new ArrayList<Command>();
    }

    /**
       Set the commands to view.
       @param c The new set of commands.
     */
    public void setCommands(Collection<? extends Command> c) {
        commands.clear();
        commands.addAll(c);
    }

    @Override
    public Collection<RenderedObject> render(Graphics2D g, int width, int height) {
        Collection<RenderedObject> result = new ArrayList<RenderedObject>();
        for (Command next : commands) {
            if (next instanceof AKMove) {
                renderMove((AKMove)next, g);
            }
            if (next instanceof AKExtinguish) {
                renderExtinguish((AKExtinguish)next, g);
            }
            if (next instanceof AKClear) {
                renderClear((AKClear)next, g);
            }
            if (next instanceof AKRescue) {
                renderRescue((AKRescue)next, g);
            }
            if (next instanceof AKLoad) {
                renderLoad((AKLoad)next, g);
            }
            if (next instanceof AKUnload) {
                renderUnload((AKUnload)next, g);
            }
        }
        return result;
    }

    private void renderMove(AKMove move, Graphics2D g) {
        g.setColor(Color.BLACK);
        List<EntityID> path = move.getPath();
        Iterator<EntityID> it = path.iterator();
        StandardEntity first = getWorld().getEntity(it.next());
        Pair<Integer, Integer> firstLocation = first.getLocation(getWorld());
        int startX = transform.scaleX(firstLocation.first());
        int startY = transform.scaleY(firstLocation.second());
        while (it.hasNext()) {
            StandardEntity next = getWorld().getEntity(it.next());
            Pair<Integer, Integer> nextLocation = next.getLocation(getWorld());
            int nextX = transform.scaleX(nextLocation.first());
            int nextY = transform.scaleY(nextLocation.second());
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

    private void renderExtinguish(AKExtinguish ex, Graphics2D g) {
        StandardEntity fb = getWorld().getEntity(ex.getAgentID());
        StandardEntity target = getWorld().getEntity(ex.getTarget());
        Pair<Integer, Integer> fbLocation = fb.getLocation(getWorld());
        Pair<Integer, Integer> targetLocation = target.getLocation(getWorld());
        int fbX = transform.scaleX(fbLocation.first());
        int fbY = transform.scaleY(fbLocation.second());
        int bX = transform.scaleX(targetLocation.first());
        int bY = transform.scaleY(targetLocation.second());
        g.setColor(Color.BLUE);
        g.drawLine(fbX, fbY, bX, bY);
    }

    private void renderClear(AKClear clear, Graphics2D g) {
        renderHumanAction(getWorld().getEntity(clear.getAgentID()), CLEAR_COLOUR, g, null);
    }

    private void renderRescue(AKRescue rescue, Graphics2D g) {
        renderHumanAction(getWorld().getEntity(rescue.getAgentID()), RESCUE_COLOUR, g, null);
    }

    private void renderLoad(AKLoad load, Graphics2D g) {
        renderHumanAction(getWorld().getEntity(load.getAgentID()), LOAD_COLOUR, g, "L");
    }

    private void renderUnload(AKUnload unload, Graphics2D g) {
        renderHumanAction(getWorld().getEntity(unload.getAgentID()), UNLOAD_COLOUR, g, "U");
    }

    private void renderHumanAction(StandardEntity entity, Color colour, Graphics2D g, String s) {
        Pair<Integer, Integer> location = entity.getLocation(getWorld());
        int x = transform.scaleX(location.first()) - SIZE / 2;
        int y = transform.scaleY(location.second()) - SIZE / 2;
        Shape shape = new Ellipse2D.Double(x, y, SIZE, SIZE);
        g.setColor(colour);
        g.fill(shape);
        if (s != null) {
            g.setColor(Color.BLACK);
            FontMetrics metrics = g.getFontMetrics();
            int width = metrics.stringWidth(s);
            int height = metrics.getHeight();
            x = transform.scaleX(location.first());
            y = transform.scaleY(location.second());
            g.drawString(s, x - (width / 2), y + (height / 2));
        }
    }
}