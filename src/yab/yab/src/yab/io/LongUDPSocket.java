// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io;

import java.io.*;
import java.net.*;

public class LongUDPSocket implements ProtocolConstants {
    private final int HEADER_SIZE = 8;
    private final byte[] m_buf = new byte[UDP_PACKET_SIZE];
    private InetAddress m_address;
    private int m_port;
    private DatagramSocket m_socket;
    private short m_sendingId = 0;
    private DatagramPacket m_packet;

    public LongUDPSocket(InetAddress kernelAddress, int kernelPort) {
        try {
            m_address = kernelAddress;
            m_port    = kernelPort;
            m_socket  = new DatagramSocket();
        } catch (Exception e) { throw new Error(e); }
    }

    public byte[] receive() {
        byte[][] udpps = null;
        int numUdpps = 0;
        while (true) {
            try {
                m_packet = new DatagramPacket(m_buf, m_buf.length);
                m_socket.receive(m_packet);

                ByteArrayInputStream bais =
                    new ByteArrayInputStream(m_packet.getData());
                DataInputStream dis = new DataInputStream(bais);
                dis.readShort(); // magic number
                dis.readShort(); // id
                short number = dis.readShort();
                short total  = dis.readShort();
                byte[] body = new byte[m_packet.getLength() - HEADER_SIZE];
                dis.read(body, 0, body.length);
                dis.close();
                bais.close();

                if (udpps == null)
                    udpps = new byte[(int) total][];
                udpps[(int) number] = body;
                numUdpps ++;
                if (numUdpps < total)
                    continue;
                byte[] ludpp = udppsToLudpp(udpps);
                return ludpp;
            } catch (Exception e) { throw new Error(e); }
        }
    }

    private byte[] udppsToLudpp(byte[][] udpps) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for (int i = 0;  i < udpps.length;  i ++)
            dos.write(udpps[i], 0, udpps[i].length);
        byte[] ludpp = baos.toByteArray();
        dos.close();
        baos.close();
        return ludpp;
    }

    public void send(byte[] data) {
        final int MAX_BODY_SIZE = UDP_PACKET_SIZE - HEADER_SIZE;
        final short ID = m_sendingId ++;
        final short TOTAL =
            (short) ((data.length + MAX_BODY_SIZE - 1) / MAX_BODY_SIZE);
        int offset = 0;
        for (short i = 0;  i < TOTAL;  i ++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            try {
                dos.writeShort(0x0008); // magic number
                dos.writeShort(ID);
                dos.writeShort(i);
                dos.writeShort(TOTAL);
                int size = Math.min(data.length - offset, MAX_BODY_SIZE);
                dos.write(data, offset, size);
                offset += size;
                byte[] buf = baos.toByteArray();
                dos.close();
                baos.close();
                DatagramPacket pkt
                    = new DatagramPacket(buf, buf.length, m_address, m_port);
                m_socket.send(pkt);
            } catch (Exception e) { throw new Error(e); }
        }
    }

    public void setLastReceivedPackeSourceToDestination() {
        m_address = m_packet.getAddress();
        m_port    = m_packet.getPort();
    }
}
