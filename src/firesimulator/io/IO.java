/*
  Modified LIO to use TCP instead of LongUDP by Cameron Skinner
*/

package firesimulator.io;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author tn
 *
 */
public abstract class IO implements IOConstans{
	public abstract void send(byte[] body);
	public abstract int[] receive();

	/** @return int[] casted from src[from..to] */
	protected int[] getIntArray(byte[] src, int from, int to) {
		int[] result = new int[(to - from) / 4];
		for (int i = 0, b = from;  i < result.length;   i ++, b += 4)
			result[i] = (int) ((src[b  ] & 0xff) << (8 * 3))
				+ (int) ((src[b+1] & 0xff) << (8 * 2))
				+ (int) ((src[b+2] & 0xff) <<  8     )
				+ (int)  (src[b+3] & 0xff);
		return result;
	}

}
