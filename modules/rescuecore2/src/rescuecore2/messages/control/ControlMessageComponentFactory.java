package rescuecore2.messages.control;

import com.google.common.base.Enums;

import rescuecore2.messages.control.ControlMessageURN.ControlMessageURN_V1;
import rescuecore2.registry.AbstractMessageComponentFactory;

/**
 * A factory for control messages.
 */
public final class ControlMessageComponentFactory
		extends AbstractMessageComponentFactory<ControlMessageComponentURN> {
	/** Singleton instance. */
	public static final ControlMessageComponentFactory INSTANCE = new ControlMessageComponentFactory();

	private ControlMessageComponentFactory() {
		super(ControlMessageComponentURN.class);
	}

	@Override
	public String getV1Equiv(int urnId) {
		ControlMessageURN_V1 item = Enums
				.getIfPresent(ControlMessageURN_V1.class,
						ControlMessageURN.fromInt(urnId).name())
				.orNull();
		return item == null ? null : item.toString();
	}
	
}
