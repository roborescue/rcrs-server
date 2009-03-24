// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.net.*;
import yab.agent.*;
import yab.agent.object.*;
import yab.io.ProtocolConstants;

public abstract class AbstractAmbulanceCenterAgent extends CenterAgent {
    protected AbstractAmbulanceCenterAgent(InetAddress address, int port) {
        super(ProtocolConstants.AGENT_TYPE_AMBULANCE_CENTER, address, port);
    }

    protected AmbulanceCenter self() { return (AmbulanceCenter) world.self; }

//    protected int hearingLimit() { return world.ambulanceTeams.size() * 2; }
}
