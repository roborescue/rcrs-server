package rescuecore2.standard.messages;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Enums;

import java.io.IOException;

import rescuecore2.log.Logger;
import rescuecore2.messages.Message;
import rescuecore2.messages.protobuf.RCRSProto.MessageProto;
import rescuecore2.registry.AbstractMessageComponentFactory;
import rescuecore2.registry.AbstractMessageFactory;
import rescuecore2.standard.messages.StandardMessageURN.StandardMessageURN_V1;

/**
 * A factory for standard messages.
 */
public final class StandardMessageComponentFactory
		extends AbstractMessageComponentFactory<StandardMessageComponentURN> {

	/** Singleton instance. */
	public static final StandardMessageComponentFactory INSTANCE = new StandardMessageComponentFactory();

	/**
	 * Singleton class: private constructor.
	 */
	private StandardMessageComponentFactory() {
		super(StandardMessageComponentURN.class);
	}

	@Override
	public String getV1Equiv(int urnId) {
		StandardMessageURN_V1 item = Enums
				.getIfPresent(StandardMessageURN_V1.class,
						StandardMessageURN.fromInt(urnId).name())
				.orNull();
		return item == null ? null : item.toString();
	}
}
