// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io;

import java.io.*;

public class KaSense {
    public final int selfId;
    public final int time;
    public final ObjectElement[] selfAndMap;
    public KaSense(DataInputStream dis) throws IOException {
        selfId = dis.readInt();
        time = dis.readInt();
        selfAndMap = RCRSSProtocol.readObjectsElement(dis);
    }
}
