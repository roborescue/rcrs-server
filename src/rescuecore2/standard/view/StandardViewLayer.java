package rescuecore2.standard.view;

import java.awt.Graphics;
import java.awt.Graphics2D;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import rescuecore2.view.ViewLayer;
import rescuecore2.view.RenderedObject;
import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;

/**
   A layer that can view standard robocup rescue entities.
 */
public class StandardViewLayer implements ViewLayer {
    private StandardWorldModel world;
    private List<EntityRenderer> renderers;
    private Map<Class<?>, EntityRenderer> rendererCache;

    /**
       Construct a standard view layer.
     */
    public StandardViewLayer() {
        renderers = new ArrayList<EntityRenderer>();
        rendererCache = new HashMap<Class<?>, EntityRenderer>();
        addDefaultRenderers();
    }

    @Override
    public void setWorldModel(WorldModel<? extends Entity> model) {
        if (model instanceof StandardWorldModel) {
            world = (StandardWorldModel)model;
        }
        else {
            world = new StandardWorldModel();
            if (model != null) {
                world.addEntities(model.getAllEntities());
            }
            world.index();
        }
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
                result.add(new RenderedObject(next, renderer.render(next, copy, transform, world)));
            }
        }
        return result;
    }

    /**
       Add a renderer.
       @param r The renderer to add.
     */
    public void addRenderer(EntityRenderer r) {
        renderers.add(r);
    }

    /**
       Remove a renderer.
       @param r The renderer to remove.
     */
    public void removeRenderer(EntityRenderer r) {
        renderers.remove(r);
        rendererCache.clear();
    }

    /**
       Remove all installed renderers.
     */
    public void removeAllRenderers() {
        renderers.clear();
        rendererCache.clear();
    }

    /**
       Add the default renderer set, i.e. nodes, roads, buildings and humans.
     */
    public void addDefaultRenderers() {
        addRenderer(new NodeRenderer());
        addRenderer(new RoadRenderer());
        addRenderer(new BuildingRenderer());
        addRenderer(new HumanRenderer());
    }

    private EntityRenderer getRenderer(StandardEntity entity) {
        // Find a renderer that will accept this class.
        Class<? extends StandardEntity> clazz = entity.getClass();
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