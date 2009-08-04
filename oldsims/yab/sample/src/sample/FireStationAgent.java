// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package sample;

import java.net.*;
import yab.agent.*;
import yab.agent.object.*;

public class FireStationAgent extends AbstractFireStationAgent {
    public FireStationAgent(InetAddress address, int port) {
        super(address, port);
    }

    protected void hearTell(RealObject sender, int channelId, String message) 
	{
        if (sender instanceof FireBrigade && channelId != CHANNEL_SAY)
            tell(message);
	}
}
