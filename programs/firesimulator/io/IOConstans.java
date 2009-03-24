package firesimulator.io;

import firesimulator.world.WorldConstants;

/**
 * @author tn
 */
public interface IOConstans extends WorldConstants{

	final static int PACKAGE_SIZE 			= 1472;
	
	final static int HEADER_NULL 			= 0x00;
	
	final static int SK_CONNECT 			= 0x20;
	final static int SK_ACKNOWLEDGE 	= 0x21;
	final static int SK_UPDATE				= 0x22;
	
	final static int KS_CONNECT_OK 	= 0x23;
	final static int COMMANDS 			= 0x51;
	final static int UPDATE				= 0x50;
	
	final static int INIT_TIME 				= 0;

}
