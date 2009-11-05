package firesimulator.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;

import firesimulator.world.RescueObject;
import firesimulator.world.World;
import firesimulator.world.Building;
import firesimulator.world.FireBrigade;

import rescuecore.OutputBuffer;
import rescuecore.InputBuffer;

/**
 * @author tn
 * Modified by Cameron Skinner
 *
 */
public class RIO implements IOConstans{
    private final static String NAME = "ResQ fire simulator";

    private IO io;

    public RIO(InetAddress kernelIP, int kernelPort){
        io=new TCPIO(kernelIP,kernelPort);
    }
	
    public int connect(World world){
        System.out.print("sending SK_CONNECT..");
        sendConnect();
        System.out.print("ok\nwaiting for KS_CONNECT_OK..");
        int id = receiveConnectOK(world);
        System.out.println("ok");
        return id;
    }
	
    public void receiveUpdate(World world){
        InputBuffer data = receive();
        String urn = data.readString();
        if(!"UPDATE".equals(urn)) {
            System.out.println("warning: received " + urn + " instead of UPDATE");
        }
        // Skip size, id
        data.readInt();
        data.readInt();
        int time = data.readInt();
        world.processChangeSet(data, time);
    }
	
    public void receiveCommands(World world){
        InputBuffer data=receive();
        String urn = data.readString();
        if(!"COMMANDS".equals(urn)) {
            System.out.println("warning: received " + urn + " instead of COMMANDS");
        }
        world.processCommands(data);
    }
	
	
    public int receiveConnectOK(World world){
        InputBuffer data = receive();
        String urn = data.readString();
        if(!"KS_CONECT_OK".equals(urn)) {
            System.out.println("warning: received " + urn + " instead of KS_CONNECT_OK");
        }
        // Skip size, requestID
        data.readInt();
        data.readInt();
        int id = data.readInt();
        world.processConnectOK(data);
        return id;
    }
	
    private InputBuffer receive(){
        return io.receive();
    }
	
    public void sendReadyness(int id){
        System.out.print("sending SK_ACKNOWLEDGE..");
        sendAcknowledge(0, id);
        System.out.println("ok");
    }
	
    public void sendConnect(){
        OutputBuffer out = new OutputBuffer();
        out.writeInt(0); // Request ID
        out.writeInt(0); // Version
        out.writeString(NAME);
        send("SK_CONNECT", out.getBytes());
    }
	
    public void sendAcknowledge(int requestId, int simId){
        OutputBuffer out = new OutputBuffer();
        out.writeInt(requestId);
        out.writeInt(simId);
        send("SK_ACKNOWLEDGE", out.getBytes());
    }
	
    public void sendUpdate(int id, int time, Collection objects){
        OutputBuffer out = new OutputBuffer();
        try {
            out.writeInt(id);
            out.writeInt(time);
            out.writeInt(objects.size());
            for(Iterator i=objects.iterator();i.hasNext();){
                RescueObject o=(RescueObject)i.next();
                // ID
                out.writeInt(o.getID());
                if (o instanceof FireBrigade) {
                    out.writeInt(1); // 1 property
                    out.writeString("WATER_QUANTITY"); // URN
                    out.writeInt(4); // Size
                    out.writeInt(((FireBrigade)o).getWaterQuantity()); // Value
                }
                else if (o instanceof Building) {
                    out.writeInt(1); // 1 property
                    out.writeString("FIERYNESS"); // URN
                    out.writeInt(4); // Size
                    out.writeInt(((Building)o).getFieryness()); // Value
                }
                else {
                    out.writeInt(0); // No properties
                }
            }
            send("SK_UPDATE", out.getBytes());
        } catch (Exception e) {
            e.printStackTrace();  
            System.exit(1); 
        }
    }
	
    private void send(String urn, byte[] body) {
        try {
            OutputBuffer out = new OutputBuffer();
            out.writeString(urn);
            out.writeInt(body.length);
            out.writeBytes(body);
            out.writeString("");
            byte[] ludpBody=out.getBytes();
            io.send(ludpBody);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

}
