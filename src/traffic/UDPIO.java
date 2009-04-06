package traffic;

import java.io.*;
import java.net.*;
import java.util.*;
import traffic.object.*;
import rescuecore.RescueConstants;
import rescuecore.OutputBuffer;
import rescuecore.commands.*;

public class UDPIO extends IO {
	private short m_ludpId = 0;
	private DatagramSocket m_socket;
	private final byte[] m_buf = new byte[PACKET_SIZE];

	public UDPIO(InetAddress kernelAddress, int kernelPort) {
		super(kernelAddress,kernelPort);
	}

	public byte[] receive() {
		HashMap idLudpMap = new HashMap();
		while (true) {
			try {
				DatagramPacket pkt = new DatagramPacket(m_buf, m_buf.length);
				m_socket.receive(pkt);

				// [2..3]: LongUDP packet ID
				// [4..5]: order in this LongUDP packet
				// [6..7]: total UDP packet number
				// [8.. ]: body of LongUDP packet data
				int ludpID = (short) ((((int) m_buf[2] & 0xff) << 8) | ((int) m_buf[3] & 0xff));
				int nth    = (short) ((((int) m_buf[4] & 0xff) << 8) | ((int) m_buf[5] & 0xff));
				int total  = (short) ((((int) m_buf[6] & 0xff) << 8) | ((int) m_buf[7] & 0xff));
				byte[] body = new byte[pkt.getLength()-8]; //getIntArray(pkt.getData(), 8, pkt.getLength());
				System.arraycopy(pkt.getData(),8,body,0,body.length);

				byte[][] data = (byte[][]) idLudpMap.get(new Integer(ludpID));
				if (data == null) {
					data = new byte[total][];
					idLudpMap.put(new Integer(ludpID), data);
				}
				data[nth] = body;

				Iterator it = idLudpMap.entrySet().iterator();
				loop:
				while (it.hasNext()) {
					byte[][] udps = (byte[][]) ((Map.Entry) it.next()).getValue();
					for (int i = 0;  i < udps.length;  i ++)
						if (udps[i] == null)
							continue loop;
					it.remove();
					byte[] result = udpsToLudp(udps);
					//	  if (result[0] == KS_CONNECT_OK) {
					//	    m_address = pkt.getAddress();
					//	    m_port    = pkt.getPort();
					//	  }
					return result;
				}
			} catch (Exception e) { e.printStackTrace();  System.exit(1); }
		}
	}

	private byte[] udpsToLudp(byte[][] udps) {
		int size = 0;
		for (int i = 0;  i < udps.length;  i ++)
			size += udps[i].length;

		byte[] result = new byte[size];
		for (int i = 0, pos = 0;  i < udps.length;  i++) {
			int len = udps[i].length;
			System.arraycopy(udps[i], 0, result, pos, len);
			pos += len;
		}

		return result;
	}

	public void send(byte[] body) {
		final int MAX_BODY_SIZE = PACKET_SIZE - 8;  // 8: LongUDP header

		byte[] ludpBody = body;

		final short LUDPID = m_ludpId ++;
		final short NUM = (short) ((ludpBody.length + MAX_BODY_SIZE - 1) / MAX_BODY_SIZE);
		int offset = 0;
		for (short i = 0;  i < NUM;  i ++) {
			int size = Math.min(ludpBody.length - offset, MAX_BODY_SIZE);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			try {
				// header for assembring LongUDP packets
				dos.writeShort(0x0008);       // magic number 0x0008
				dos.writeShort(LUDPID);  // LongUDP packet ID
				dos.writeShort(i);    // order of this LongUDP packete
				dos.writeShort(NUM);  // total UDP packet number

				dos.write(ludpBody, offset, size);
				dos.close();

				byte[] buf = baos.toByteArray();
				baos.close();
				m_socket.send(new DatagramPacket(buf, buf.length, m_address, m_port));
			} catch (Exception e) { e.printStackTrace();  System.exit(1); }
			offset += size;
		}
		if(ASSERT)Util.myassert(offset == ludpBody.length);
	}
}
