
/*
 * Last change: $Date: 2004/07/11 22:26:27 $
 * $Revision: 1.30 $
 *
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package rescuecore;

import java.io.*;
import rescuecore.debug.*;
import rescuecore.commands.*;

/**
   This is the base class for all agents. This class handles messages from the server, provides a memory of the simulation environment and convenience methods for path planning etc. This class also enforces the message limits imposed by the robocup rescue rules.
   <p>Agent implementations should provide at least one of the following three constructors:
   <ol><li>A no-arg constructor - e.g. MyAgent()
   <li>A String[] constructor - e.g. MyAgent(String[] args)
   <li>A constructor that takes one or more String arguments - e.g. MyAgent(String arg1, String arg2)
   </ol>
   The reason for this is that the AgentSystem allows arguments to be passed to the Agent via the command line. When creating an instance of the agent it first looks for any constructor that accepts the right number of String arguments, followed by the String[] constructor. Failing that, the no-arg constructor will be used.
   <p>
   For example, assuming we have the three constructors mentioned above, if the command line provides two arguments then the AgentSystem will use the MyAgent(String arg1, String arg2) constructor. If only one argument is provided then the MyAgent(String[] args) constructor is used.
 */
public abstract class Agent extends RescueComponent {
	private int[] agentTypes;
    protected int type;
    protected int id;
	protected int timeStep;
    protected Memory memory;
	/*
    private int numReceived, numSent;
    private int sendMax,receiveMax;
	*/
	private int tempID;
	private volatile boolean running;

	private static int NEXT_ID = 0;

	//	private LogWriter logWriter;
	//	private File logFile;
	//	private int lastLogTime = -1;
	protected boolean debug = false;

    /**
       Create a new agent of a particular type.
       @param types The entity types this agent wants.
    */
    protected Agent(int... types) {
		this.agentTypes = types;
		id = -1;
		timeStep = -1;
		this.type = -1;
		//		numReceived = 0;
		//		numSent = 0;
		tempID = ++NEXT_ID;
		//		tempID = (int)(Math.random()*Integer.MAX_VALUE);
    }

	public final int getComponentType() {
		return RescueConstants.COMPONENT_TYPE_AGENT;
	}

	public final Command generateConnectCommand() {
            return new AKConnect(0,tempID,getClass().getName(),agentTypes);
	}
	
	protected void appendCommand(Command c){
		super.appendCommand(c);
		if(debug)
			logObject(c);
	}

	public final boolean handleConnectOK(Command c) {
		KAConnectOK ok = (KAConnectOK)c;
		int requestID = ok.getRequestID();
		if (requestID==tempID) {
			id = ok.getAgentID();
			System.out.println("Connect succeeded for "+tempID+". Kernel assigned id:"+id);
			try {
				RescueObject[] knowledge = ok.getKnowledge();
				// Initialise
				initialise(knowledge);
				// Send AK_ACKNOWLEDGE
				RescueMessage ack = new RescueMessage();
				ack.append(new AKAcknowledge(requestID, id));
				sendMessage(ack);
			}
			catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
			timeStep = 0;
			running = true;
			return true;
		}
		else {
			System.out.println("Received a KA_CONNECT_OK for agent "+requestID+", but I'm listening for a reply for "+tempID);
		}
		return false;
	}

	public final String handleConnectError(Command c) {
		KAConnectError error = (KAConnectError)c;
		int requestID = error.getRequestID();
		String reason = error.getReason();
		if (requestID==tempID)
			return reason;
		else
			System.out.println("Received a KA_CONNECT_ERROR ("+reason+") for agent "+requestID+", but I'm listening for a reply for "+tempID);
		return null;
	}

	public boolean isRunning() {
		return running;
	}

	public void shutdown() {
		running = false;
	}

	public final void handleMessage(Command c) {
		//		System.out.println("Handling "+c);
		switch (c.getType()) {
		case RescueConstants.KA_SENSE:
			handleSense((KASense)c);
			break;
		case RescueConstants.KA_HEAR:
                case RescueConstants.KA_HEAR_SAY:
                case RescueConstants.KA_HEAR_TELL:
			handleHear(c);
			break;
		case RescueConstants.KA_CONNECT_OK:
			if (running) {
				// Someone obviously didn't get our AK_ACKNOWLEDGE
				KAConnectOK ok = (KAConnectOK)c;
				int requestID = ok.getRequestID();
				System.out.println(this+" just received a KA_CONNECT_OK to "+requestID+" - my tempID is "+tempID);
				if (requestID==tempID) {
					int newID = ok.getAgentID();
					System.out.println("Old ID: "+id+", new ID: "+newID);
					id = newID;
					RescueMessage ack = new RescueMessage();
					ack.append(new AKAcknowledge(requestID, id));
					sendMessage(ack);
				}
			}
			break;
		default:
			handleOtherMessage(c);
			break;
		}
		logObject(c);
	}

	protected void handleOtherMessage(Command c) {
		System.out.println("Timestep "+timeStep+": "+this+" received a weird command: "+Handy.getCommandTypeName(c.getType()));
	}

    /**
       Handle a KA_SENSE message
       @param c The KA_SENSE Command object
	*/
    private void handleSense(Command c) {
		//		System.out.println("Last timestep ("+timeStep+") "+this+" sent "+numSent+" messages and received "+numReceived);
		KASense sense = (KASense)c;
		//		numSent = numReceived = 0;
		try {
			int newTimeStep = sense.getTime();
			if (newTimeStep < timeStep) System.err.println(this+" just moved back in time! It was timestep "+timeStep+" and now it's timestep "+newTimeStep);
			if (newTimeStep > timeStep+1) System.err.println(this+" just skipped ahead in time! It was timestep "+timeStep+" and now it's timestep "+newTimeStep);
			timeStep = newTimeStep;
			memory.update(sense); // Update the memory
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//		long start = System.currentTimeMillis();
		sense();
		//		long end = System.currentTimeMillis();
		//		System.out.println("Sense took "+(end-start)+"ms for "+this);
		flushCommands();
		// Flush the log
		DebugWriter.flush(this);
    }

    /**
       Handle a KA_HEAR (or KA_HEAR_SAY, or KA_HEAR_TELL) message
	   @param hear The KA_HEAR Command object.
	   @see RescueConstants#KA_HEAR
	   @see RescueConstants#KA_HEAR_SAY
	   @see RescueConstants#KA_HEAR_TELL
	*/

    private void handleHear(Command c) {
		KAHear hear = (KAHear)c;
		int toID = hear.getToID();
		int fromID = hear.getFromID();
		int length = hear.getLength();
		byte[] msg = hear.getData();
		byte channel = hear.getChannel();
		//			System.out.println(Handy.getCommandTypeName(type)+" received by "+id+" from "+fromID+" - have already accepted "+numReceived+" messages of "+receiveMax+" this timestep");
		
		//		if (willListenHear(fromID) && canListen()) {
		//		System.out.println("Hear from "+fromID+" to "+toID+" on channel "+channel);
		hear(fromID,msg,channel);
		//			++numReceived;
		//		}
    }

	/*
	private boolean canListen() {
		if (numReceived < receiveMax) return true;
		System.err.println("WARNING: "+this+" tried to receive too many messages in timestep "+timeStep+" (maximum="+receiveMax+")");
		return false;
	}
	*/

    private RescueObject me() {
		return memory.lookup(id);
    }

    public String toString() {
		if (!running) return "Unconnected agent. Temporary ID: "+tempID;
		return Handy.getTypeName(type)+" ("+id+")";
    }

    /**
       Get this agents Memory
       @return The agents Memory
    */
    public final Memory getMemory() {
		return memory;
    }

    /**
       Get the type of RescueObject that this agent represents
       @see RescueConstants#TYPE_CIVILIAN
       @see RescueConstants#TYPE_CAR
       @see RescueConstants#TYPE_FIRE_BRIGADE
       @see RescueConstants#TYPE_FIRE_STATION
       @see RescueConstants#TYPE_POLICE_FORCE
       @see RescueConstants#TYPE_POLICE_OFFICE
       @see RescueConstants#TYPE_AMBULANCE_TEAM
       @see RescueConstants#TYPE_AMBULANCE_CENTER
	*/
    public final int getType() {
		return type;
    }

    /**
       Get this agent's unique id, assigned by the kernel
	*/
    public final int getID() {
		return id;
    }

	/**
	   Enable debugging
	   @param file The File to log information to. 
	*/
	protected void enableDebug(String target) {
		debug = true;
		DebugWriter.register(this,this.toString(),target);
	}

	/**
	   Disable debugging
	*/
	protected void disableDebug() {
		debug = false;
	}

	/**
	 * Writes an Update to a RescueObject to the log.
	 **/
	//	public final void logUpdate(rescuecore.debug.Update upd){
	//		if (debug) logWriter.addUpdate(upd, id, timeStep);
	//	}
	/**
	 * Writes a transient Object to the log.
	 **/
	public final void logObject(Object obj){
		//		if (debug) logWriter.addObject(obj,id,timeStep);
		if (debug) DebugWriter.logUserObject(this,obj,timeStep);
	}

    /**
       Initialise this agent. Subclasses that override this method should invoke super.initialise(knowledge,self) at some point.
       @param knowledge This agent's knowledge of the world
       @param self The RescueObject describing this agent
	*/
    protected void initialise(RescueObject[] knowledge) {
		memory = generateMemory();
		for (int i=0;i<knowledge.length;++i) {
			memory.add(knowledge[i],0,RescueConstants.SOURCE_INITIAL);
		}
                type = me().getType();
		if(debug){
			DebugWriter.logInitialObjects(this,memory.getAllObjects());
			memory.addMemoryListener(new DebugMemoryListener(this));
		}
    }

    /**
       Construct a new Memory object for use by this Agent. This method allows Agents to customise their choice of Memory object. The default implementation returns a {@link HashMemory}.
       @return A new Memory object
	*/
    protected Memory generateMemory() {
		return new HashMemory();
    }

    /**
       Called after a KA_SENSE is received
	*/
    protected abstract void sense();

    /**
       Called after a KA_HEAR is received
       @param from The agent that sent the message
       @param msg The message body
	   @param channel The channel that this message was received on
    */
    protected void hear(int from, byte[] msg, byte channel){}
	//    protected boolean hearTell(int from, byte[] msg){return false;}
	//    protected boolean hearSay(int from, byte[] msg){return false;}
	//	protected boolean willListenHear(int from) {return false;}
	//	protected boolean willListenHearSay(int from) {return false;}
	//	protected boolean willListenHearTell(int from) {return false;}

    /**
       How many messages can this agent send per timestep?
       @return The maximum number of messages this agent can send per timestep
	*/
	//    protected final int getMaxSend() {
		//		return sendMax;
	//    }

    /**
       How many messages can this agent receive per timestep?
       @return The maximum number of messages this agent can receive per timestep
	*/
	//    protected final int getMaxReceive() {
	//		return receiveMax;
	//    }

    /**
       Send an AK_SAY message to the kernel. If this agent has already send too many messages this timestep then this will be silently ignored
       @param message The message
	*/
    protected final void say(byte[] message) {
		//		if (numSent < sendMax) {
		appendCommand(Command.SAY(id,timeStep,message,message.length));
		//			++numSent;
		//		}
    }

    /**
       Send an AK_TELL message to the kernel. If this agent has already send too many messages this timestep then this will be silently ignored
       @param message The message
	*/
    protected final void tell(byte[] message, byte channel) {
		//		if (numSent < sendMax) {
		appendCommand(Command.TELL(id,timeStep,message,message.length,channel));
		//			++numSent;
		//		}
    }
}
