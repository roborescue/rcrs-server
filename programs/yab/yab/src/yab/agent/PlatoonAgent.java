// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.net.*;
import yab.agent.*;
import yab.agent.object.*;

public abstract class PlatoonAgent extends HumanoidAgent {
    protected PlatoonAgent(int agentType, InetAddress address, int port) {
        super(agentType, address, port);
    }

    private Humanoid self() { return (Humanoid) world.self; }
}
