package rescuecore2.standard.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import rescuecore2.view.ViewLayer;
import rescuecore2.view.RenderedObject;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Node;

public class StandardViewLayer implements ViewLayer {
    private StandardWorldModel world;
    private List<EntityRenderer> renderers;
    private Map<Class<?>, EntityRenderer> rendererCache;

    public StandardViewLayer(StandardWorldModel world) {
        this.world = world;
        renderers = new ArrayList<EntityRenderer>();
        rendererCache = new HashMap<Class<?>, EntityRenderer>();
        addDefaultRenderers(world);
    }

    @Override
    public Collection<RenderedObject> render(Graphics g, int width, int height) {
        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> bounds = world.getWorldBounds();
        List<RenderedObject> result = new ArrayList<RenderedObject>();
        Graphics2D copy = (Graphics2D)g.create();
        double xMin = bounds.first().first();
        double yMin = bounds.first().second();
        double xRange = bounds.second().first() - bounds.first().first();
        double yRange = bounds.second().second() - bounds.first().second();
        ScreenTransform transform = new ScreenTransform(xMin, yMin, xRange, yRange, width, height);
        for (StandardEntity next : world) {
            EntityRenderer renderer = getRenderer(next);
            if (renderer != null) {
                result.add(new RenderedObject(next, renderer.render(next, copy, transform)));
            }
        }
        return result;
    }

    public void addRenderer(EntityRenderer r) {
        renderers.add(r);
    }

    public void removeRenderer(EntityRenderer r) {
        renderers.remove(r);
        rendererCache.clear();
    }

    public void removeAllRenderers() {
        renderers.clear();
        rendererCache.clear();
    }

    public void addDefaultRenderers(StandardWorldModel model) {
        addRenderer(new NodeRenderer());
        addRenderer(new RoadRenderer(model));
        addRenderer(new BuildingRenderer(model));
        addRenderer(new HumanRenderer(model));
    }

    private EntityRenderer getRenderer(StandardEntity entity) {
        // Find a renderer that will accept this class.
        Class<?> clazz = entity.getClass();
        if (rendererCache.containsKey(clazz)) {
            return rendererCache.get(clazz);
        }
        for (EntityRenderer next : renderers) {
            if (next.canRender(clazz)) {
                rendererCache.put(clazz, next);
                return next;
            }
        }
        return null;
    }
}