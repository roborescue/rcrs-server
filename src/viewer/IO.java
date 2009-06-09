package viewer;

import java.io.*;
import java.net.*;
import java.util.*;
import viewer.object.*;
import rescuecore.InputBuffer;
import rescuecore.OutputBuffer;
import rescuecore.RescueConstants;

public abstract class IO implements Runnable, Constants {
	protected abstract byte[] receive();
	protected abstract void send(byte[] body);

	protected final ArrayList updateDataList   = new ArrayList();
	protected final ArrayList commandsDataList = new ArrayList();

	public IO() {
		commandsDataList.add(null);  // Dummy, because KS_COMMANDS is not sent by kernel at time 0
	}

	public void connect() {
		System.out.print("connecting ... ");
		OutputBuffer out = new OutputBuffer();
		out.writeInt(RescueConstants.VK_CONNECT);
		out.writeInt(RescueConstants.INT_SIZE * 2);
		out.writeInt(0); // Version
		out.writeInt(0); // RequestID
		out.writeInt(RescueConstants.HEADER_NULL);
		send(out.getBytes());
		byte[] data = receive();
		System.out.println("done");

		InputBuffer in = new InputBuffer(data);
		int size;
		int header = in.readInt();
		byte[] body;
                int requestID;
		switch (header) {
		case RescueConstants.KV_CONNECT_OK:
			size = in.readInt();
			System.out.print("initializing ... ");
			body = new byte[size];
                        requestID = in.readInt();
                        // request ID should be 0 - that's the request ID we sent
                        if (requestID != 0) {
                            System.err.println("Unexpected request ID from kernel: " + requestID);
                            System.exit(-3);
                        }
			in.readBytes(body);
			storeData(updateDataList, body, 0);
			break;
		case RescueConstants.KV_CONNECT_ERROR:
			size = in.readInt();
                        requestID = in.readInt();
                        // request ID should be 0 - that's the request ID we sent
                        if (requestID != 0) {
                            System.err.println("Unexpected request ID from kernel: " + requestID);
                        }
			System.out.println("Error connecting to kernel: "+in.readString());
			System.exit(-1);
			break;
		case RescueConstants.UPDATE:
			size = in.readInt();
			int time = in.readInt();
			body = new byte[size-RescueConstants.INT_SIZE];
			in.readBytes(body);
			storeData(updateDataList,body,time);
			break;
		default:
			System.err.println("Unexpected reply from kernel: "+header);
			System.exit(-2);
			break;
		}
		WORLD.playback(0);
		WORLD.setWorldRange();
		System.out.println("done");

		out = new OutputBuffer();
		out.writeInt(RescueConstants.VK_ACKNOWLEDGE);
		out.writeInt(RescueConstants.INT_SIZE); // Size
		out.writeInt(0); // Request ID
		out.writeInt(RescueConstants.HEADER_NULL);
		send(out.getBytes());

		System.out.println("start\n");
	}

	public void run() {
		int lastTime = WORLD.time();
		while (true) {
			byte[] data = receive();
			if (data == null)
				return;
			InputBuffer dis = new InputBuffer(data);
			//			rescuecore.Handy.printBytes("Next block of data",dis);
			int header = dis.readInt();
			while (header!=RescueConstants.HEADER_NULL) {
				int size = dis.readInt();
				byte[] content = new byte[size];
				dis.readBytes(content);
				//				System.out.println("Header: "+rescuecore.Handy.getCommandTypeName(header));
				//				System.out.println("Length: "+size);
				switch (header) {
				case RescueConstants.COMMANDS:
					storeData(commandsDataList, content, lastTime + 1);

					//DEBUG
					/* System.out.println("");
					   System.out.println("******************************");
					   System.out.println("Time = " + time);
					   while(dis.available() >= 4)
					   {
					   System.out.print(dis.readInt());
					   System.out.print(", ");
					   }*/
					//END DEBUG

					break;
				case RescueConstants.UPDATE:
					InputBuffer in = new InputBuffer(content);
					int time = in.readInt();
					Util.myassert(time == lastTime + 1, "received a Long UDP packet having wrong simulation time");
					lastTime = time;
					storeData(updateDataList, content, time);
					// VIEWER.timeSlider.setMaximum(time);
					break;
				default: Util.myassert(false);
				}
				header = dis.readInt();
			}
		}
	}

	private void storeData(List list, byte[] data, int time) {
		synchronized (list) {
			if (list.size() > time)
				return;
			Util.myassert(list.size() == time, "Some datas was skipped or lost.");
			list.add(data);
		}
	}

	public InputBuffer updateData(int time)   { return getData(time, updateDataList,  (time == 0) ? 0 : 1); }
	public InputBuffer commandsData(int time) { return getData(time, commandsDataList, 0); }

	private InputBuffer getData(int time, List list, int skip) {
		byte[] data;
		synchronized (list) { data = (byte[]) list.get(time); }
		InputBuffer dis = new InputBuffer(data);
		dis.skip(4 * skip);
		return dis;
	}

	public boolean hasUpdateData(int time)   { synchronized (updateDataList)   { return time < updateDataList.size();   } }
	public boolean hasCommandsData(int time) { synchronized (commandsDataList) { return time < commandsDataList.size(); } }
}
