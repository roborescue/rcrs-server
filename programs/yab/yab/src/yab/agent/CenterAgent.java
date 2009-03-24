// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.net.*;
import yab.agent.*;
import yab.agent.object.*;

public abstract class CenterAgent extends Agent {
    protected CenterAgent(int agentType, InetAddress address, int port) {
        super(agentType, address, port);
    }

    private Building self() { return (Building) world.self; }

    /** This method submits a rest action to the kernel. */
    public void act() throws ActionCommandException { rest(); }
}
