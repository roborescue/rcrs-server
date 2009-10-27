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
    private final static String NAME = "Legacy traffic simulator";

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
		out.writeString("SK_CONNECT");
		out.writeInt((RescueConstants.INT_SIZE * 3) + NAME.length()); // Size
		out.writeInt(REQUEST_ID);
		out.writeInt(VERSION);
		out.writeString(NAME);
		out.writeInt(RescueConstants.HEADER_NULL);
		send(out.getBytes());
	}

	public int receiveConnectOk() {
		long now = System.currentTimeMillis();
		long end = now + TIMEOUT;
		while (now < end) {
			byte[] data = receive();
			InputBuffer in = new InputBuffer(data);
			String urn = in.readString();
			while (!"".equals(urn)) {
				int size = in.readInt();
                                if ("KS_CONNECT_OK".equals(urn)) {
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
                                }
                                else if ("KS_CONNECT_ERROR".equals(urn)) {
                                    System.out.println("KS_CONNECT_ERROR: "+in.readString());
                                    System.exit(-1);
                                }
                                else {
                                    in.skip(size);
                                    System.err.println("I don't know how to deal with "+urn);
				}
                                urn = in.readString();
			}
		}
		System.err.println("Timeout connecting to kernel");
		System.exit(-2);
		return 0;
	}

    public void sendAcknowledge() {
		OutputBuffer out = new OutputBuffer();
		out.writeString("SK_ACKNOWLEDGE");
		out.writeInt(RescueConstants.INT_SIZE * 2); // Size
		out.writeInt(REQUEST_ID);
		out.writeInt(simID);
		out.writeInt(RescueConstants.HEADER_NULL);
		send(out.getBytes());
	}

	public void receiveCommands() {
            byte[] data = receive();
            InputBuffer in = new InputBuffer(data);
            String urn = in.readString();
            if ("".equals(urn)) return;
            int size = in.readInt();
            if (!"COMMANDS".equals(urn)) {
                System.out.println("I don't know how to deal with "+urn);
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
		out.writeString("SK_UPDATE");

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
		String urn = in.readString();
		if ("".equals(urn)) return;
		int size = in.readInt();
		if (!"UPDATE".equals(urn)) {
                    System.out.println("I don't know how to deal with "+urn);
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
