// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package sample;

import java.net.*;
import yab.agent.*;
import yab.agent.object.*;

public class AmbulanceCenterAgent extends AbstractAmbulanceCenterAgent {
    public AmbulanceCenterAgent(InetAddress address, int port) {
        super(address, port);
    }

    protected void hearTell(RealObject sender, int channelId, String message) {
	        // do nothing
	}
}
