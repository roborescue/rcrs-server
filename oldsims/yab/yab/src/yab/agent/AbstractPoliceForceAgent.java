// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.net.*;
import yab.agent.*;
import yab.agent.object.*;
import yab.io.ProtocolConstants;

public abstract class AbstractPoliceForceAgent extends PlatoonAgent {
    protected AbstractPoliceForceAgent(InetAddress address, int port) {
        super(ProtocolConstants.AGENT_TYPE_POLICE_FORCE, address, port);
    }

    protected PoliceForce self() { return (PoliceForce) world.self; }

    protected void clear(Road target) throws ActionCommandException {
        socket().akClear(self().id, target.id);
        throw new ActionCommandException();
    }
}
