// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io;

import java.io.DataInputStream;
import java.io.IOException;

public class KaHearTell
{
    public final int selfId;
    public final int senderId;
    public final String message;
    public final int channelId;

    public KaHearTell (DataInputStream dis) throws IOException
    {
        selfId = dis.readInt ();
        senderId = dis.readInt ();
	channelId = dis.readInt();
        message = RCRSSProtocol.readStringElement(dis);
    }
}
