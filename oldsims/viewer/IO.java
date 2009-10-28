package viewer;

import java.io.*;
import java.net.*;
import java.util.*;
import viewer.object.*;
import rescuecore.InputBuffer;
import rescuecore.OutputBuffer;
import rescuecore.RescueConstants;

public abstract class IO implements Runnable, Constants {
    private final static int REQUEST_ID = 34;
    private final static String NAME = "Legacy viewer";

    protected abstract byte[] receive();
    protected abstract void send(byte[] body);

    protected final ArrayList updateDataList   = new ArrayList();
    protected final ArrayList commandsDataList = new ArrayList();

    private int viewerID;

    public IO() {
        commandsDataList.add(null);  // Dummy, because KS_COMMANDS is not sent by kernel at time 0
    }

    public void connect() {
        System.out.print("connecting ... ");
        OutputBuffer out = new OutputBuffer();
        out.writeString("VK_CONNECT");
        out.writeInt((RescueConstants.INT_SIZE * 3) + NAME.length());
        out.writeInt(REQUEST_ID);
        out.writeInt(0); // Version
        out.writeString(NAME);
        out.writeString("");
        send(out.getBytes());
        byte[] data = receive();
        System.out.println("done");

        InputBuffer in = new InputBuffer(data);
        int size;
        String urn = in.readString();
        byte[] body;
        int requestID = 0;
        viewerID = 0;
        if ("KV_CONNECT_OK".equals(urn)) {
            size = in.readInt();
            System.out.print("initializing ... ");
            requestID = in.readInt();
            viewerID = in.readInt();
            System.out.print("id " + viewerID + " ... ");
            if (requestID != REQUEST_ID) {
                System.err.println("Unexpected request ID from kernel: " + requestID);
                System.exit(-3);
            }
            body = new byte[size - (RescueConstants.INT_SIZE * 2)];
            in.readBytes(body);
            storeData(updateDataList, body, 0);
        }
        else if ("KV_CONNECT_ERROR".equals(urn)) {
            size = in.readInt();
            requestID = in.readInt();
            if (requestID != REQUEST_ID) {
                System.err.println("Unexpected request ID from kernel: " + requestID);
            }
            System.out.println("Error connecting to kernel: "+in.readString());
            System.exit(-1);
        }
        else {
            System.err.println("Unexpected reply from kernel: "+urn);
            System.exit(-2);
        }
        WORLD.playback(0);
        WORLD.setWorldRange();
        System.out.println("done");

        out = new OutputBuffer();
        out.writeString("VK_ACKNOWLEDGE");
        out.writeInt(RescueConstants.INT_SIZE * 2); // Size
        out.writeInt(REQUEST_ID);
        out.writeInt(viewerID);
        out.writeString("");
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
            String urn = dis.readString();
            while (!"".equals(urn)) {
                int size = dis.readInt();
                byte[] content = new byte[size];
                dis.readBytes(content);
                if ("COMMANDS".equals(urn)) {
                    storeData(commandsDataList, content, lastTime + 1);
                }
                else if ("UPDATE".equals(urn)) {
                    InputBuffer in = new InputBuffer(content);
                    int id = in.readInt();
                    if (id == viewerID) {
                        int time = in.readInt();
                        Util.myassert(time == lastTime + 1, "received an update with the wrong simulation time: expected " + (lastTime + 1) + ", got " + time);
                        lastTime = time;
                        storeData(updateDataList, content, time);
                    }
                    // VIEWER.timeSlider.setMaximum(time);
                }
                else {
                    Util.myassert(false);
                }
                urn = dis.readString();
            }
        }
    }

    private void storeData(List list, byte[] data, int time) {
        synchronized (list) {
            if (list.size() > time)
                return;
            Util.myassert(list.size() == time, "Some data was skipped or lost.");
            list.add(data);
        }
    }

    public InputBuffer updateData(int time)   { return getData(time, updateDataList,  (time == 0) ? 0 : 2); }
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
