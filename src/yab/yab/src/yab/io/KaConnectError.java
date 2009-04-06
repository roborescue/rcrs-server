// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io;

import java.io.*;

public class KaConnectError {
    public final int temporaryId;
    public final String reason;
    public KaConnectError(DataInputStream dis) throws IOException {
        temporaryId = dis.readInt();
        reason = RCRSSProtocol.readStringElement(dis);
    }
}
