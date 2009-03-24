package yab.io;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author Mohammad Mehdi Saboorian
 */
public class TCPIO implements ProtocolConstants
{
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private InetAddress m_addAddress;
    private int m_port;

    public TCPIO (InetAddress kernelAddress, int kernelPort)
    {
        try
        {
            socket = new Socket (kernelAddress, kernelPort);
            in = new DataInputStream (socket.getInputStream ());
            out = new DataOutputStream (socket.getOutputStream ());
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            System.exit (1);
        }
    }

    public byte[] receive ()
    {
        try
        {
            int length = in.read ();
            length = length << 8 | in.read ();
            length = length << 8 | in.read ();
            length = length << 8 | in.read ();
            int total = 0;
            byte[] result = new byte[length];
            while (total < length)
            {
                total += in.read (result, total, length - total);
            }
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            System.exit (1);
        }
        return null;
    }

    public void send (int header, byte[] body)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream ();
            DataOutputStream dos = new DataOutputStream (baos);
            dos.writeInt (header);
            dos.writeInt (body.length);
            dos.write (body);
            dos.writeInt (HEADER_NULL);
            dos.close ();
            byte[] ludpBody = baos.toByteArray ();
            baos.close ();
            send (ludpBody);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            System.exit (1);
        }
    }

    public void close ()
    {
        try
        {
            socket.close ();
            in.close ();
            out.close ();
        }
        catch (IOException e)
        {
            e.printStackTrace ();
            System.exit (1);
        }
    }

    private void send (byte[] body)
    {
        try
        {
            out.write ((byte) (body.length >> 24) & 0xFF);
            out.write ((byte) (body.length >> 16) & 0xFF);
            out.write ((byte) (body.length >> 8) & 0xFF);
            out.write ((byte) body.length & 0xFF);
            out.write (body, 0, body.length);
            out.flush ();
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            System.exit (1);
        }
    }
}
