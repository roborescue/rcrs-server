// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.io;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RCRSSProtocol implements ProtocolConstants
{
    public static final byte[] body (Object[] elements) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        DataOutputStream dos = new DataOutputStream (baos);
        for (int i = 0; i < elements.length; i++)
        {
            Object e = elements[i];
            if (e instanceof Integer)
                writeIntElement (dos, e);
            else if (e instanceof int[])
                writeIDsElement (dos, e);
            else if (e instanceof String)
                writeStringElement (dos, e);
            else if (e instanceof byte[])
                writeByteArray (dos, e);
            else
                throw new Error ("illegal element type:" + e);
        }
        byte[] body = baos.toByteArray ();
        dos.close ();
        baos.close ();
        return body;
    }

    public static final void writeIntElement (DataOutputStream dos, Object e)
            throws IOException
    {
        int integer = ((Integer) e).intValue ();
        dos.writeInt (integer);
    }

    public static final void writeIDsElement (DataOutputStream dos, Object e)
            throws IOException
    {
        int[] ids = (int[]) e;
        for (int i = 0; i < ids.length; i++)
            dos.writeInt (ids[i]);
        dos.writeInt (0);
    }

    public static final void writeStringElement (DataOutputStream dos, Object e)
            throws IOException
    {
        byte[] str = ((String) e).getBytes ();
        dos.writeInt (str.length);
        dos.write (str, 0, str.length);
        int size = (str.length + 3) & ~3;
        for (int i = str.length; i <= size; i++)
            dos.write (0);
    }

    public static final void writeByteArray (DataOutputStream dos, Object e) throws IOException
    {
        byte[] b = (byte[]) e;
        dos.writeInt (b.length);
        dos.write (b);
        int size = (b.length + 3) & ~3;
        for (int i = b.length; i < size; i++)
            dos.write (0);
    }

        public static final ObjectElement[] readObjectsElement(DataInputStream dis)
            throws IOException {
            List objs = new ArrayList();
            int type = dis.readInt();
            while (type != TYPE_NULL) {
                int length = dis.readInt();
                int id = dis.readInt();
                objs.add(new ObjectElement(type, length, id, readPropertiesElement(dis)));
                type = dis.readInt();
            }
            return (ObjectElement[]) objs.toArray(new ObjectElement[0]);
        }

        public static final ObjectElement readObjectElement(DataInputStream dis)
            throws IOException {

            ObjectElement result = null;
            int type = dis.readInt();
            int length = dis.readInt();
            int id = dis.readInt();
            result = new ObjectElement(type, length, id, readPropertiesElement(dis));
            return result;
        }

    public static final PropertyElement[]
        readPropertiesElement(DataInputStream dis) throws IOException {
        List properties = new ArrayList();
        int type = dis.readInt();
        int length;
        while (type != PROPERTY_NULL) {
            length = dis.readInt();
            properties.add(new PropertyElement(type, length, propertyValue(type,length,dis)));
            type = dis.readInt();
        }
        return (PropertyElement[]) properties.toArray(new PropertyElement[0]);
    }


    private static final int[] propertyValue(int type, int length, DataInputStream dis)
        throws IOException {
        if (length == 4)                 // 4: sizeof(int)
            return new int[] { readIntElement(dis) };

        else if (length > 4)
            return readListElement(dis);

        return readIDsElement(dis);
    }

    private static final int readIntElement(DataInputStream dis)
        throws IOException {
            int el = dis.readInt();

        return el;
    }

    private static final int[] readListElement(DataInputStream dis)
        throws IOException {
        int size = dis.readInt();
        int[] result = new int[size];  // 4: sizeof(int)

        for (int i = 0; i < result.length; i ++)
        {
            result[i] = dis.readInt();
        }
        return result;
    }

    private static final int[] m_buf = new int[1024];

    private static final int[] readIDsElement (DataInputStream dis)
            throws IOException
    {
        int i = -1;
        do
        {
            m_buf[++i] = dis.readInt ();
        }
        while (m_buf[i] != 0);
        int[] result = new int[i];
        System.arraycopy (m_buf, 0, result, 0, i);
        return result;
    }

    public static final String readStringElement (DataInputStream dis)
            throws IOException
    {

        byte[] buf = new byte[dis.readInt ()];

		dis.read (buf, 0, buf.length);
        int size = (buf.length + 3) & ~3;
        int skipSize = size - buf.length;
        //dis.skip (skipSize); // See also the writeStringElement method
        return new String (buf);
    }

    public static final byte[] readByteElement (DataInputStream dis)
            throws IOException
    {
        byte[] buf = new byte[dis.readInt ()];
        dis.read (buf, 0, buf.length);
        int size = (buf.length + 3) & ~3;
        int skipSize = size - buf.length;        
        //dis.skip (skipSize); // See also the writeStringElement method
        return buf;
    }
}
