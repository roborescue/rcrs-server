// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.net.*;
import yab.agent.*;
import yab.agent.object.*;
import yab.io.ProtocolConstants;

public abstract class AbstractFireStationAgent extends CenterAgent {
    protected AbstractFireStationAgent(InetAddress address, int port) {
        super(ProtocolConstants.AGENT_TYPE_FIRE_STATION, address, port);
    }

    protected FireStation self() { return (FireStation) world.self; }

//    protected int hearingLimit() { return world.fireBrigades.size() * 2; }
}
