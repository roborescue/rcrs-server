// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.net.*;
import yab.agent.*;
import yab.agent.object.*;
import yab.io.ProtocolConstants;

public abstract class AbstractPoliceOfficeAgent extends CenterAgent {
    protected AbstractPoliceOfficeAgent(InetAddress address, int port) {
        super(ProtocolConstants.AGENT_TYPE_POLICE_OFFICE, address, port);
    }

    protected PoliceOffice self() { return (PoliceOffice) world.self; }

//    protected int hearingLimit() { return world.policeForces.size() * 2; }
}
