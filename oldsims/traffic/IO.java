package traffic;

import java.io.*;
import java.net.*;
import java.util.*;
import traffic.object.*;
import rescuecore.RescueConstants;
import rescuecore.InputBuffer;
import rescuecore.OutputBuffer;

public abstract class IO implements Constants {
    private final static int REQUEST_ID = 1000;
    private final static int VERSION = 0;

	protected InetAddress m_address;
	protected int m_port;

    private int simID;

	public final static long TIMEOUT = 60000; // 60 second connection timeout

	public IO(InetAddress kernelAddress, int kernelPort) {
		try{
			m_address = kernelAddress;
			m_port    = kernelPort;
		} catch (Exception e) { e.printStackTrace();  System.exit(1); }
	}

	public abstract byte[] receive();
	public abstract void send(byte[] body);

	public void close() {}

	public void sendConnect() {
		System.out.println("connecting");
		OutputBuffer out = new OutputBuffer();
		out.writeInt(RescueConstants.SK_CONNECT);
		out.writeInt(RescueConstants.INT_SIZE * 2); // Size
		out.writeInt(REQUEST_ID);
		out.writeInt(VERSION);
		out.writeInt(RescueConstants.HEADER_NULL);
		send(out.getBytes());
	}

	public int receiveConnectOk() {
		long now = System.currentTimeMillis();
		long end = now + TIMEOUT;
		while (now < end) {
			byte[] data = receive();
			InputBuffer in = new InputBuffer(data);
			int header = in.readInt();
			while (header!=RescueConstants.HEADER_NULL) {
				int size = in.readInt();
				switch (header) {
				case RescueConstants.KS_CONNECT_OK:
					System.out.println("initializing");
					int requestId = in.readInt();
                                        simID = in.readInt();
                                        if (requestId != REQUEST_ID) {
                                            System.out.println("Received unexpected connect ok: expecting request ID of " + REQUEST_ID + " but got " + requestId);
                                            System.exit(-3);
                                        }
					WORLD.update(in, INITIALIZING_TIME);
					WORLD.initialize();
					return simID;
				case RescueConstants.KS_CONNECT_ERROR:
					System.out.println("KS_CONNECT_ERROR: "+in.readString());
					System.exit(-1);
					break;
				default:
					in.skip(size);
					System.err.println("I don't know how to deal with "+header);
					break;
				}
				header = in.readInt();
			}
		}
		System.err.println("Timeout connecting to kernel");
		System.exit(-2);
		return 0;
	}

    public void sendAcknowledge() {
		OutputBuffer out = new OutputBuffer();
		out.writeInt(RescueConstants.SK_ACKNOWLEDGE);
		out.writeInt(RescueConstants.INT_SIZE * 2); // Size
		out.writeInt(REQUEST_ID);
		out.writeInt(simID);
		out.writeInt(RescueConstants.HEADER_NULL);
		send(out.getBytes());
	}

	public void receiveCommands() {
		byte[] data = receive();
		InputBuffer in = new InputBuffer(data);
		int type = in.readInt();
		if (type==RescueConstants.HEADER_NULL) return;
		int size = in.readInt();
		if (type!=RescueConstants.COMMANDS) {
			System.out.println("I don't know how to deal with "+type);
			in.skip(size);
		}
		else {
                    int id = in.readInt();
                    if (id == simID) {
                        WORLD.parseCommands(in);
                    }
                }
	}

	public void sendUpdate() {
		System.out.println("Sending update");
		OutputBuffer out = new OutputBuffer();
		out.writeInt(RescueConstants.SK_UPDATE);

		OutputBuffer temp = new OutputBuffer();
		temp.writeInt(simID);
		temp.writeInt(WORLD.time());
		MovingObject[] objects = WORLD.movingObjectArray();
                List<MovingObject> toUpdate = new ArrayList<MovingObject>(objects.length);
                for (MovingObject next : objects) {
                    if (next.needsUpdate()) {
                        toUpdate.add(next);
                    }
                }
                temp.writeInt(toUpdate.size());
		for (MovingObject next : toUpdate) {
			next.output(temp);
		}

		byte[] body = temp.getBytes();
		out.writeInt(body.length); // Size
		out.writeBytes(body);
		out.writeInt(RescueConstants.HEADER_NULL);
		send(out.getBytes());
	}

	public void receiveUpdate() {
		byte[] data = receive();
		InputBuffer in = new InputBuffer(data);
		int type = in.readInt();
		if (type==RescueConstants.HEADER_NULL) return;
		int size = in.readInt();
		if (type!=RescueConstants.UPDATE) {
			System.out.println("I don't know how to deal with "+type);
			in.skip(size);
		}
		else {
                        int id = in.readInt();
			int time = in.readInt();
                        if (id == simID) {
                            WORLD.update(in,time);
                        }
		}
	}
}
