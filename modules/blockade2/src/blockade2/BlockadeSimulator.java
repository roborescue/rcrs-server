package blockade2;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import org.util.xml.element.Element;
import org.util.xml.element.TagElement;
import org.util.xml.parse.ElementParser;
import org.util.xml.parse.policy.*;


import java.util.*;
import rescuecore2.worldmodel.*;
import rescuecore2.registry.Registry;
import rescuecore2.connection.*;
import rescuecore2.messages.*;
import rescuecore2.messages.control.*;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.messages.*;
import rescuecore2.connection.TCPConnection;

import traffic3.log.event.*;
import traffic3.manager.*;
import traffic3.objects.area.*;
import traffic3.objects.*;
import static traffic3.log.Logger.log;

/**
 * GIS Server.<br/>
 * java gis2.Main config/gis2.xml
 */
public class BlockadeSimulator {

    private static final String SIMULATOR_NAME = "Blockade Simulator(Area Model).";
    private int state_ = -1;
    private int version_ = 1;
    private int request_id_ = 8;
    private int simulator_id_;
    private int time_;
    private HashMap<EntityID, Entity> id_entity_map_ = new HashMap<EntityID, Entity>();
    private java.util.Collection<Entity> update_list_;

    public BlockadeSimulator(File file1, String host, int port) throws Exception {
	state_ = 0;
	Registry.getCurrentRegistry().registerMessageFactory(StandardMessageFactory.INSTANCE);
	Registry.getCurrentRegistry().registerEntityFactory(StandardEntityFactory.INSTANCE);
        Registry.getCurrentRegistry().registerPropertyFactory(StandardPropertyFactory.INSTANCE);
	
	update_list_ = new java.util.ArrayList<Entity>();

	log("blockade simulator");
	log("config file: " + file1.getAbsolutePath());
	log("kernel address: " + host);
	log("kernel port: " + port);
	System.err.println("started blockade simulator");
	
	connectToKernel(host, port);

	//HashMap<EntityID, Entity> pool = importFromFile(file1);
	//checkObjects(pool);
	System.err.println("connecting to kernel");
	//startWaiting(port, pool);
    }



    
    private void connectToKernel(String address, int port) throws Exception {

	final TCPConnection connection = new TCPConnection(address, port);
	state_ = 1;
	connection.addConnectionListener(new ConnectionListener(){
		public void messageReceived(Connection c, Message msg) {
		    if(state_ == 1 && msg instanceof KSConnectOK) {
			KSConnectOK connect_ok = (KSConnectOK)msg;
			simulator_id_ = connect_ok.getSimulatorID();
			
			for(Entity entity : connect_ok.getEntities())
			    id_entity_map_.put(entity.getID(), entity);
			
			SKAcknowledge ack = new SKAcknowledge(request_id_, simulator_id_);
			log("received KSConnectOK: "+msg);
			try{
			    connection.sendMessage(ack);
			}catch(Exception e) {
			    e.printStackTrace();
			}
			log("send SKAcknowledge: "+ack);
			state_ = 2;
		    } else if(state_ == 2 && msg instanceof KSCommands) {
			
			KSCommands commands = (KSCommands)msg;
			time_ = commands.getTime();

			try{
                            ChangeSet cs = new ChangeSet();
                            cs.addAll(update_list_);
			    SKUpdate sk_update = new SKUpdate(simulator_id_, time_, cs);
			    connection.sendMessage(sk_update);
			}catch(Exception exc) {
			    exc.printStackTrace();
			}
			
			state_ = 3;
		    } else if(state_ == 3 && msg instanceof KSUpdate) {
			
			state_ = 4;
		    } else if(state_ == 4 && msg instanceof KSCommands) {
			KSCommands commands = (KSCommands)msg;
			time_ = commands.getTime();

			for(Command command : commands.getCommands())
			    if(command instanceof AKClear) {
				EntityID blockade_id = ((AKClear)command).getTarget();
				Blockade blockade = (Blockade)id_entity_map_.get(blockade_id);
				int cx = blockade.getCenterX();
				int cy = blockade.getCenterY();
				double d = 0.03;
				int[] xy_list = blockade.getShape();
				int length = xy_list.length/2;
				boolean flag = true;
				for(int i=0; i<length; i++) {
				    double x = xy_list[i*2];
				    double y = xy_list[i*2+1];
				    double dx = x-cx;
				    double dy = y-cy;
				    double dd = 5000;
				    double distance = Math.sqrt(dx*dx + dy*dy);
				    if(distance>dd*2) {
					xy_list[i*2] = (int)(x - dx*(dd)/distance);
					xy_list[i*2+1] = (int)(y - dy*(dd)/distance);
					flag = false;
				    }
				}
				if(flag) {
				    Area area = (Area)id_entity_map_.get(blockade.getArea());
				    List<EntityID> idlist = area.getBlockadeList();
				    ArrayList<EntityID> newidlist = new ArrayList<EntityID>();
				    for(EntityID idi : idlist)
					if(idi.getValue()!=blockade_id.getValue())
					    newidlist.add(idi);
				    area.setBlockadeList(newidlist);
				    update_list_.add(area);
				} else {
				    blockade.setShape(xy_list);
				    update_list_.add(blockade);
				}
			    }

			try{
                            ChangeSet cs = new ChangeSet();
                            cs.addAll(update_list_);
			    SKUpdate sk_update = new SKUpdate(simulator_id_, time_, cs);
			    connection.sendMessage(sk_update);
			    update_list_.clear();
			}catch(Exception exc) {
			    exc.printStackTrace();
			}
			state_ = 3;
		    } else {
			alert(msg);
		    }
		}
	    });
	connection.startup();

	SKConnect sk_connect = new SKConnect(request_id_, version_, SIMULATOR_NAME);
	connection.sendMessage(sk_connect);
	
	
    }
    

    public static void alert(Object message) {
	javax.swing.JOptionPane.showMessageDialog(null, message);
    }

    public static void alert2(final Object message) {
	new Thread(new Runnable(){
		public void run() {
		    try{
			javax.swing.JOptionPane.showMessageDialog(null, message);
		    }catch(Exception e){ e.printStackTrace();}
		}
	    }, "alert").start();
    }

    public static void log(Object message) {
	System.out.println(message);
    }
}
