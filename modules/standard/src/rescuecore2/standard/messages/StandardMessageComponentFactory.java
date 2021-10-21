package rescuecore2.standard.messages;

import rescuecore2.registry.AbstractMessageComponentFactory;

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

}
