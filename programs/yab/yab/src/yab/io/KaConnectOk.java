// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io;

import java.io.*;

public class KaConnectOk {
    public final int temporaryId;
    public final int selfId;
    public final ObjectElement[] selfAndMap;
    public KaConnectOk(DataInputStream dis) throws IOException {
        temporaryId = dis.readInt();
        selfId = dis.readInt();
        selfAndMap = RCRSSProtocol.readObjectsElement(dis);
    }

    /** This constructor is used to establish the connection as fast
     *  as possible, with submitting 1 as the version of an AK_CONNECT
     *  block.
     */
    public KaConnectOk(DataInputStream dis, ObjectElement[] map)
        throws IOException {
        temporaryId = dis.readInt();
        selfId = dis.readInt();
        selfAndMap = new ObjectElement[1 + map.length];
        selfAndMap[0] = RCRSSProtocol.readObjectsElement(dis)[0]; // self
        System.arraycopy(map, 0, selfAndMap, 1, map.length);
    }
    public ObjectElement[] map() {
        ObjectElement[] map = new ObjectElement[selfAndMap.length - 1];
        System.arraycopy(selfAndMap, 1, map, 0, map.length);
        return map;
    }
}
