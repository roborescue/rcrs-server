package rescuecore2.standard.view;

import java.util.Set;
import java.util.HashSet;

import rescuecore2.standard.entities.StandardEntity;

/**
   Abstract base class for EntityRenderer implementations.
 */
public abstract class AbstractEntityRenderer implements EntityRenderer {
    private Set<Class<? extends StandardEntity>> okClasses;

    /**
       Construct an AbstractEntityRenderer.
       @param classes The classes that this renderer can render.
     */
    protected AbstractEntityRenderer(Class<? extends StandardEntity>... classes) {
        okClasses = new HashSet<Class<? extends StandardEntity>>(classes.length);
        for (Class<? extends StandardEntity> next : classes) {
            okClasses.add(next);
        }
    }

    @Override
    public boolean canRender(Class<? extends StandardEntity> clazz) {
        for (Class<? extends StandardEntity> next : okClasses) {
            if (next.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
}