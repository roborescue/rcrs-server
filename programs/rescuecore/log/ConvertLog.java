package rescuecore.log;

import java.io.*;

public class ConvertLog {
	public static void main(String[] args) {
		String inFile = args[0];
		String outFile = args[1];
		try {
			DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(inFile))));
			// Check the header
			byte[] preamble = new byte[Log.HEADER_LENGTH];
			input.read(preamble,0,preamble.length);
			String preambleString = new String(preamble);
			if (Log.HEADER_VERSION_0.equals(preambleString)) {
				// OK
				System.out.println("Original log version 0");
			}
			else if (Log.HEADER_VERSION_1.equals(preambleString)) {
				System.out.println("Original log version 1");
				// Read and discard parameters
				int length = input.readInt();
				input.skip(length);
			}
			else {
				throw new InvalidLogException("Unknown log version: "+preambleString);
			}
			// Write the header
			DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(outFile)));
			preamble = Log.HEADER_VERSION_0.getBytes();
			out.write(preamble);
			byte[] buffer = new byte[1024];
			int received = 0;
			do {
				received = input.read(buffer);
				if (received>0) {
					out.write(buffer,0,received);
				}
			} while (received>0);
			out.flush();
			out.close();
			input.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
