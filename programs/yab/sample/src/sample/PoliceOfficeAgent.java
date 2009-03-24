// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package sample;

import java.net.*;
import yab.agent.*;
import yab.agent.object.*;

public class PoliceOfficeAgent extends AbstractPoliceOfficeAgent {
    public PoliceOfficeAgent(InetAddress address, int port) {
        super(address, port);
    }

    protected void hearTell(RealObject sender, int channelId, String message) {
	    if (sender instanceof FireStation && channelId != CHANNEL_SAY)
            tell(message);
	}
}
