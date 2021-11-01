/*
 * Last change: $Date: 2005/02/20 01:29:55 $
 * $Revision: 1.15 $
 *
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package rescuecore;

import java.io.UnsupportedEncodingException;

import rescuecore.commands.Command;

public class OutputBuffer {
	private byte[] data;
	private int index;

	public OutputBuffer() {
		this(256);
	}

	public OutputBuffer(int capacity) {
		data = new byte[capacity];
	}

	public void writeByte(byte b) {
		expand(RescueConstants.BYTE_SIZE);
		data[index++] = b;
	}

	public void writeBytes(byte[] b) {
		expand(b.length*RescueConstants.BYTE_SIZE);
		for (int i=0;i<b.length;++i) data[index++] = b[i];
	}

	public void writeShort(int s) {
		expand(RescueConstants.SHORT_SIZE);
		data[index++] = (byte)(s>>8 & 0xFF);
		data[index++] = (byte)(s & 0xFF);
	}

	public void writeInt(int i) {
		expand(RescueConstants.INT_SIZE);
		data[index++] = (byte)(i >> 24 & 0xFF);
		data[index++] = (byte)(i >> 16 & 0xFF);
		data[index++] = (byte)(i >> 8 & 0xFF);
		data[index++] = (byte)(i & 0xFF);
	}

	public void writeString(String s) {
            try {
		byte[] bytes = s.getBytes("UTF-8");
		writeInt(bytes.length);
		writeBytes(bytes);
            }
            catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 encoding not found!", e);
            }
	}

	public void writeObject(RescueObject o) {
		writeInt(o.getType());
                writeInt(o.getID());
		int base = markBlock();
		o.write(this);
		writeBlockSize(base);
	}

	public void writeObjects(RescueObject[] objects) {
            writeInt(objects.length);
            for (int i=0;i<objects.length;++i) writeObject(objects[i]);
	}

	public void writeCommand(Command c) {
		writeInt(c.getType());
		int base = markBlock();
		c.write(this);
		writeBlockSize(base);
	}

	public void writeCommands(Command[] c) {
            writeInt(c.length);
            for (int i=0;i<c.length;++i) writeCommand(c[i]);
	}

	public int markBlock() {
		writeInt(0);
		return index;
	}

	public void writeBlockSize(int mark) {
		int size = index-mark;
		writeInt(size,mark-RescueConstants.INT_SIZE);
	}

	public void clear() {
		index = 0;
	}

	public int getSize() {
		return index;
	}

	public byte[] getBytes() {
		byte[] result = new byte[index];
		System.arraycopy(data,0,result,0,index);
		return result;
	}

	public void ensureCapacity(int capacity) {
		expand(capacity);
	}

	public void trim(int size) {
		index = Math.min(index,size);
	}

	private void writeInt(int i, int position) {
		int old = index;
		index = position;
		writeInt(i);
		if (index < old) index = old; // If we went past the old position then just leave things as they are
	}

	private void expand(int needed) {
		if (index+needed < data.length) return;
		byte[] newData = new byte[Math.max(index+needed,data.length*2)];
		System.arraycopy(data,0,newData,0,data.length);
		data = newData;
	}
}
