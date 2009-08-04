// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;

public class RCRSSProtocolSocket implements ProtocolConstants
{
    private final TCPIO m_tcpio;
    private final LinkedList m_blocks = new LinkedList ();

    private static final boolean SHARE_INITIAL_MAP = true;
    private static ObjectElement[] m_initialMap = null;

    public RCRSSProtocolSocket (InetAddress kernelAddress, int kernelPort)
    {
        m_tcpio = new TCPIO (kernelAddress, kernelPort);
    }

    public Object receive ()
    {
        if (m_blocks.isEmpty ())
        {
            byte[] data = m_tcpio.receive ();
            ByteArrayInputStream bais = new ByteArrayInputStream (data);
            DataInputStream dis = new DataInputStream (bais);
            try
            {
                while (dis.available () > 4) // 4: sizeof(HEADER_NULL)
                    m_blocks.addLast (getBlock (dis));
            }
            catch (IOException ioe)
            {
                throw new Error (ioe);
            }
        }
        return m_blocks.removeFirst ();
    }

    private Object getBlock (DataInputStream dis) throws IOException
    {
        int header = dis.readInt ();
        int length = dis.readInt ();  // body length
        switch (header)
        {
            case KA_SENSE:
                return new KaSense (dis);
            case KA_HEAR:
/*                return new KaHear (dis);
            case KA_HEAR_SAY:
                return new KaHearSay (dis);
            case KA_HEAR_TELL:
*/                return new KaHearTell (dis);
            case KA_CONNECT_ERROR:
                return new KaConnectError (dis);
            case KA_CONNECT_OK:
//            m_ludpSkt.setLastReceivedPackeSourceToDestination();
                return getKaConnectOkBlock (dis);
            default:
                throw new Error ("received illegal header:" + header);
        }
    }

    private KaConnectOk getKaConnectOkBlock (DataInputStream dis)
            throws IOException
    {
        if (!SHARE_INITIAL_MAP)
            return new KaConnectOk (dis);
        if (m_initialMap != null)
            return new KaConnectOk (dis, m_initialMap);
        KaConnectOk ok = new KaConnectOk (dis);
        m_initialMap = ok.map ();
        return ok;
    }

    public void send (int header, Object[] elements)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        try
        {
            byte[] body = RCRSSProtocol.body (elements);
            m_tcpio.send (header, body);
            baos.close ();
        }
        catch (IOException ioe)
        {
            throw new Error (ioe);
        }
    }

    public static void clearUpAfterInitialization ()
    {
        m_initialMap = null;
    }

    /**
     * This method is used to establish the connection as fast as
     * possible by adjusting the version of an AK_CONNECT block.
     */
    public void akConnect (int temporaryId, int agentType)
    {
        int version = (SHARE_INITIAL_MAP && m_initialMap != null) ? 1 : 0;
        akConnect (temporaryId, version, agentType);
    }

    public void akConnect (int temporaryId, int version, int agentType)
    {
        send (AK_CONNECT, new Object[]{
            new Integer (temporaryId),
            new Integer (version),
            new Integer (agentType)});
    }

    public void akAcknowledge (int selfId)
    {
        send (AK_ACKNOWLEDGE, new Object[]{
            new Integer (selfId)});
        System.out.println ("Connected!");
    }

    public void akMove (int selfId, int[] routePlan)
    {
        send(AK_MOVE, new Object[] {
            new Integer(selfId), new Integer(routePlan.length),
            routePlan});
    }

    public void akRescue (int selfId, int targtId)
    {
        send (AK_RESCUE, new Object[]{
            new Integer (selfId),
            new Integer (targtId)});
    }

    public void akLoad (int selfId, int targetId)
    {
        send (AK_LOAD, new Object[]{
            new Integer (selfId),
            new Integer (targetId)});
    }

    public void akUnload (int selfId)
    {
        send (AK_UNLOAD, new Object[]{
            new Integer (selfId)});
    }

    public void akExtinguish (int selfId, NozzleElement[] nozzles)
    {
        Object[] elements
                = new Object[1 + nozzles.length * NozzleElement.NUM_ELEMENTS + 1];
        elements[0] = new Integer (selfId);
        for (int i = 0; i < nozzles.length; i++)
            for (int j = 0; j < NozzleElement.NUM_ELEMENTS; j++)
                elements[1 + i * NozzleElement.NUM_ELEMENTS + j] = new Integer (nozzles[i].elements[j]);

        elements[elements.length - 1] = new Integer (0);
        send (AK_EXTINGUISH, elements);
    }

    public void akClear (int selfId, int targetId)
    {
        send (AK_CLEAR, new Object[]{
            new Integer (selfId),
            new Integer (targetId)});
    }

    public void akRest (int selfId)
    {
        send (AK_REST, new Object[]{
            new Integer (selfId)});
    }

    public void akSay (int selfId, String message)
    {
	    akTell(selfId, 0, message);
//        send (AK_SAY, new Object[]{
//            new Integer (selfId),
//            message});
    }

    public void akTell (int selfId, int channel, String message)
    {

        send(AK_TELL, new Object[] {
            new Integer(selfId), new Integer(channel),
            message});
    }

    public void akChannel (int selfId, byte[] channels)
    {
	    send(AK_CHANNEL, new Object[]{new Integer(selfId), channels});
    }

    public void close ()
    {
        m_tcpio.close ();
    }
}
