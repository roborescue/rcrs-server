package rescuecore2.standard.view;

import java.util.Set;
import java.util.HashSet;

/**
   Abstract base class for EntityRenderer implementations.
 */
public abstract class AbstractEntityRenderer implements EntityRenderer {
    private Set<Class<?>> okClasses;

    /**
       Construct an AbstractEntityRenderer.
       @param classes The classes that this renderer can render.
     */
    protected AbstractEntityRenderer(Class<?>... classes) {
        okClasses = new HashSet<Class<?>>(classes.length);
        for (Class<?> next : classes) {
            okClasses.add(next);
        }
    }

    @Override
    public boolean canRender(Class<?> clazz) {
        for (Class<?> next : okClasses) {
            if (next.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
}