package rescuecore2.worldmodel.entities.legacy;

import rescuecore2.worldmodel.entities.AbstractEntity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

public abstract class AbstractLegacyEntity extends AbstractEntity implements LegacyEntity {
    private EntityType type;

    protected AbstractLegacyEntity(EntityType type, EntityID id, Property... props) {
	super(id, props);
	this.type = type;
    }

    @Override
    public EntityType getType() {
	return type;
    }
}