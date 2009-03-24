/*
 * Last change: $Date: 2005/03/15 00:46:38 $
 * $Revision: 1.10 $
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

import java.util.*;
import java.io.UnsupportedEncodingException;
import rescuecore.commands.*;

/**
   A collection of useful methods
 */
public class Handy {
	private final static String[] HEX_DIGITS = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
	private final static String CHARACTER_ENCODING = "US-ASCII";

	private final static Object PRINT_LOCK = new Object();

	private Handy() {}

	/**
	   Print an array of bytes to System.out in a nice way
	   @param data The bytes to print out
	*/
	public static void printBytes(byte[] data) {
		printBytes(null,data);
	}

	/**
	   Print an array of bytes to System.out in a nice way
	   @param header A string to print out as a title
	   @param data The bytes to print out
	*/
	public static void printBytes(String header, byte[] data) {
		synchronized(PRINT_LOCK) {
			if (header!=null) System.out.println(header);
			System.out.println("OFFSET\tBYTES");
			for (int i=0;i<data.length;i+=4) {
				printBytes(i,data,"");
			}
		}
	}

	public static void printBytes(InputBuffer in) {
		printBytes(null,in);
	}

	public static void printBytes(String header, InputBuffer in) {
		int position = in.getPosition();
		byte[] temp = new byte[in.available()];
		in.readBytes(temp);
		printBytes(header,temp);
		in.setPosition(position);
	}

	/**
	   Print four bytes to System.out in a nice way
	   @param startIndex The index of the first byte
	   @param data The buffer containing the bytes to print out
	   @param description A description of what these four bytes actually mean
	*/
	public static void printBytes(int startIndex, byte[] data, String description) {
		StringBuffer buffer = new StringBuffer();
		synchronized(PRINT_LOCK) {
			buffer.append(startIndex);
			buffer.append("\t");
			for (int j=0;j<4;++j) {
				if (data.length>startIndex+j) {
					buffer.append(hex(data[startIndex+j]));
					if (j!=3) buffer.append(" ");
				}
			}
			buffer.append("\t");
			char charValue = (char)decodeInt(data,startIndex);
			if (Character.isLetterOrDigit(charValue)) buffer.append(charValue);
			buffer.append("\t");
			buffer.append(decodeInt(data,startIndex));
			buffer.append("\t");
			buffer.append(description);
			System.out.println(buffer.toString());
		}
	}

	/**
	   Turn a byte into a hexadecimal String
	   @param b The byte to convert
	   @return The byte in hexadecimal, as a String
	*/
	public static String hex(byte b) {
		byte left = (byte)((b >> 4) & 0xF);
		byte right = (byte)(b & 0xF);
		return HEX_DIGITS[left]+HEX_DIGITS[right];
	}

	/**
	   Decode a byte from a buffer
	   @param buffer The buffer we are looking at
	   @param off The offset into the buffer to start decoding from
	   @return The next byte in the buffer
	*/
	/*
	  public static byte decodeByte(byte[] buffer, int off) {
	  return buffer[off];
	  }
	*/

	/**
	   Decode a byte array from a buffer
	   @param buffer The buffer we are looking at
	   @param off The offset into the buffer to start decoding from
	   @param length The number of bytes to read
	   @return The next byte array in the buffer
	*/
	/*
	  public static byte[] decodeBytes(byte[] buffer, int off, int length) {
	  byte[] result = new byte[length];
	  System.arraycopy(buffer,off,result,0,length);
	  return result;
	  }
	*/

	/**
	   Decode a short from a buffer
	   @param buffer The buffer we are looking at
	   @param off The offset into the buffer to start decoding from
	   @return The next short in the buffer
	*/
	/*
	  public static short decodeShort(byte[] buffer, int off) {
	  int result = ((buffer[off] << 8) & 0x0000FF00) | (buffer[off+1] & 0x000000FF);
	  return (short)result;
	  }
	*/

	/**
	   Decode an int from a buffer
	   @param buffer The buffer we are looking at
	   @param off The offset into the buffer to start decoding from
	   @return The next int in the buffer
	*/
	private static int decodeInt(byte[] buffer, int off) {
		int result = ((buffer[off] << 24) & 0xFF000000) | ((buffer[off+1] << 16) & 0x00FF0000) | ((buffer[off+2] << 8) & 0x0000FF00) | (buffer[off+3] & 0x000000FF);
		return result;
	}

	/**
	   Decode a String from a buffer
	   @param buffer The buffer we are looking at
	   @param off The offset into the buffer to start decoding from
	   @param length The number of characters in the String
	   @return The next String in the buffer
	*/
	/*
	  public static String decodeString(byte[] buffer, int off, int length) {
	  int realLength = Math.min(length,buffer.length-off);
	  byte[] data = new byte[realLength];
	  System.arraycopy(buffer,off,data,0,realLength);
	  try {
	  return new String(data,CHARACTER_ENCODING);
	  }
	  catch (UnsupportedEncodingException e) {
	  return new String(data);
	  }
	  }
	*/

	/**
	   Encode a byte into a byte array
	   @param value The byte to encode
	   @return A byte array representation of the input value
	*/
	/*
	  public static byte[] encodeByte(int value) {
	  return new byte[] {(byte)(value & 0xFF)};
	  }
	*/

	/**
	   Encode a byte into a buffer
	   @param value The byte to encode
	   @param buf The buffer to write the result into
	   @param off The offset to start writing at
	*/
	/*
	  public static void encodeByte(int value, byte[] buf, int off) {
	  buf[off] = (byte)(value&0xFF);
	  }
	*/

	/**
	   Encode a byte arrray into a buffer
	   @param bytes The byte array to encode
	   @param buf The buffer to write the result into
	   @param off The offset to start writing at
	*/
	/*
	  public static void encodeBytes(byte[] bytes, byte[] buf, int off) {
	  System.arraycopy(bytes,0,buf,off,bytes.length);
	  }
	*/

	/**
	   Encode part of a byte array into a buffer
	   @param bytes The byte arrray to encode
	   @param bytesOffset The offset into bytes to start writing from
	   @param bytesLength The number of bytes to write
	   @param buf The buffer to write the result into
	   @param off The offset to start writing at
	*/
	/*
	  public static void encodeBytes(byte[] bytes, int bytesOffset, int bytesLength, byte[] buf, int off) {
	  System.arraycopy(bytes,bytesOffset,buf,off,bytesLength);
	  }
	*/

	/**
	   Encode a short into a byte array
	   @param value The short to encode
	   @return A byte array representation of the input value
	*/
	/*
	  public static byte[] encodeShort(int value) {
	  byte[] result = new byte[2];
	  result[0] = (byte)(value >> 8 & 0xFF);
	  result[1] = (byte)(value & 0xFF);
	  return result;	
	  }
	*/

	/**
	   Encode a short into a buffer
	   @param value The short to encode
	   @param buf The buffer to write the result into
	   @param off The offset to start writing at
	*/
	/*
	  public static void encodeShort(int value, byte[] buf, int off) {
	  buf[off] = (byte)(value >> 8 & 0xFF);
	  buf[off+1] = (byte)(value & 0xFF);
	  }
	*/

	/**
	   Encode an int into a byte array
	   @param value The int to encode
	   @return A byte array representation of the input value
	*/
	/*
	  public static byte[] encodeInt(int value) {
	  byte[] result = new byte[4];
	  result[0] = (byte)(value >> 24 & 0xFF);
	  result[1] = (byte)(value >> 16 & 0xFF);
	  result[2] = (byte)(value >> 8 & 0xFF);
	  result[3] = (byte)(value & 0xFF);
	  return result;
	  }
	*/

	/**
	   Encode an int into a buffer
	   @param value The int to encode
	   @param buf The buffer to write the result into
	   @param off The offset to start writing at
	*/
	/*
	  public static void encodeInt(int value, byte[] buf, int off) {
	  buf[off] = (byte)(value >> 24 & 0xFF);
	  buf[off+1] = (byte)(value >> 16 & 0xFF);
	  buf[off+2] = (byte)(value >> 8 & 0xFF);
	  buf[off+3] = (byte)(value & 0xFF);
	  }
	*/

	/**
	   Encode a String into a byte array
	   @param value The String to encode
	   @param length The maximum number of bytes to use
	   @return A byte array representation of the input byte
	*/
	/*
	  public static byte[] encodeString(String value, int length) {
	  byte[] result = new byte[length];
	  byte[] data;
	  try {
	  data = value.getBytes(CHARACTER_ENCODING);
	  }
	  catch (UnsupportedEncodingException e) {
	  data = value.getBytes();
	  }
	  System.arraycopy(data,0,result,0,Math.min(data.length,length));
	  return result;
	  }
	*/

	/**
	   Encode a String into a buffer
	   @param value The String to encode
	   @param length The maximum number of bytes to write
	   @param buf The buffer to write the result into
	   @param off The offset to start writing at
	*/
	/*
	  public static void encodeString(String value, int length, byte[] buf, int off) {
	  byte[] data;
	  try {
	  data = value.getBytes(CHARACTER_ENCODING);
	  }
	  catch (UnsupportedEncodingException e) {
	  data = value.getBytes();
	  }
	  System.arraycopy(data,0,buf,off,Math.min(data.length,length));
	  }
	*/

    /**
       Find out if two int arrays are different. The two arrays are not different if and only if they are the same size and contain the same elements (possibly out of order).
       @param a The first array
       @param b The second array
       @return true if and only if the two arrays are different
    */
	/*
	  public static boolean isDifferent(int[] a, int[] b) {
	  if (a==null && b==null) return false;
	  if (a==null || b==null) return true;
	  if (a.length!=b.length) return true;
	  int[] aSorted = new int[a.length];
	  int[] bSorted = new int[b.length];
	  System.arraycopy(a,0,aSorted,0,a.length);
	  System.arraycopy(b,0,bSorted,0,b.length);
	  Arrays.sort(aSorted);
	  Arrays.sort(bSorted);
	  for (int i=0;i<a.length;++i) if (aSorted[i]!=bSorted[i]) return true;
	  return false;
	  }
	*/

	/**
	   Translate a type name into a human-readable string
	   @param type The type we want to convert
	   @return A human-readable String showing the type given
	   @see RescueConstants#TYPE_CIVILIAN
	   @see RescueConstants#TYPE_FIRE_BRIGADE
	   @see RescueConstants#TYPE_AMBULANCE_TEAM
	   @see RescueConstants#TYPE_POLICE_FORCE
	   @see RescueConstants#TYPE_ROAD
	   @see RescueConstants#TYPE_NODE
	   @see RescueConstants#TYPE_RIVER
	   @see RescueConstants#TYPE_RIVER_NODE
	   @see RescueConstants#TYPE_BUILDING
	   @see RescueConstants#TYPE_REFUGE
	   @see RescueConstants#TYPE_FIRE_STATION
	   @see RescueConstants#TYPE_AMBULANCE_CENTER
	   @see RescueConstants#TYPE_POLICE_OFFICE
	   @see RescueConstants#TYPE_WORLD
	   @see RescueConstants#TYPE_CAR
	*/
	public static String getTypeName(int type) {
		switch(type) {
		case RescueConstants.TYPE_NULL:
			return "TYPE_NULL";
		case RescueConstants.TYPE_CIVILIAN:
			return "TYPE_CIVILIAN";
		case RescueConstants.TYPE_FIRE_BRIGADE:
			return "TYPE_FIRE_BRIGADE";
		case RescueConstants.TYPE_AMBULANCE_TEAM:
			return "TYPE_AMBULANCE_TEAM";
		case RescueConstants.TYPE_POLICE_FORCE:
			return "TYPE_POLICE_FORCE";
		case RescueConstants.TYPE_ROAD:
			return "TYPE_ROAD";
		case RescueConstants.TYPE_NODE:
			return "TYPE_NODE";
		case RescueConstants.TYPE_RIVER:
			return "TYPE_RIVER";
		case RescueConstants.TYPE_RIVER_NODE:
			return "TYPE_RIVER_NODE";
		case RescueConstants.TYPE_BUILDING:
			return "TYPE_BUILDING";
		case RescueConstants.TYPE_REFUGE:
			return "TYPE_REFUGE";
		case RescueConstants.TYPE_FIRE_STATION:
			return "TYPE_FIRE_STATION";
		case RescueConstants.TYPE_AMBULANCE_CENTER:
			return "TYPE_AMBULANCE_CENTER";
		case RescueConstants.TYPE_POLICE_OFFICE:
			return "TYPE_POLICE_OFFICE";
		case RescueConstants.TYPE_WORLD:
			return "TYPE_WORLD";
		case RescueConstants.TYPE_CAR:
			return "TYPE_CAR";
		default:
			return "Unknown type: "+type;
		}
	}

	/**
	   Translate a property name into a human-readable string
	   @param type The property we want to convert
	   @return A human-readable String showing the property given
	   @see RescueConstants#PROPERTY_NULL
	   @see RescueConstants#PROPERTY_START_TIME
	   @see RescueConstants#PROPERTY_LONGITUDE
	   @see RescueConstants#PROPERTY_LATITUDE
	   @see RescueConstants#PROPERTY_WIND_FORCE
	   @see RescueConstants#PROPERTY_WIND_DIRECTION
	   @see RescueConstants#PROPERTY_X
	   @see RescueConstants#PROPERTY_Y
	   @see RescueConstants#PROPERTY_DIRECTION
	   @see RescueConstants#PROPERTY_POSITION
	   @see RescueConstants#PROPERTY_POSITION_HISTORY
	   @see RescueConstants#PROPERTY_POSITION_EXTRA
	   @see RescueConstants#PROPERTY_STAMINA
	   @see RescueConstants#PROPERTY_HP
	   @see RescueConstants#PROPERTY_DAMAGE
	   @see RescueConstants#PROPERTY_BURIEDNESS
	   @see RescueConstants#PROPERTY_FLOORS
	   @see RescueConstants#PROPERTY_BUILDING_ATTRIBUTES
	   @see RescueConstants#PROPERTY_IGNITION
	   @see RescueConstants#PROPERTY_BROKENNESS
	   @see RescueConstants#PROPERTY_FIERYNESS
	   @see RescueConstants#PROPERTY_ENTRANCES
	   @see RescueConstants#PROPERTY_BUILDING_SHAPE_ID
	   @see RescueConstants#PROPERTY_BUILDING_CODE
	   @see RescueConstants#PROPERTY_BUILDING_AREA_GROUND
	   @see RescueConstants#PROPERTY_BUILDING_AREA_TOTAL
	   @see RescueConstants#PROPERTY_BUILDING_APEXES
	   @see RescueConstants#PROPERTY_WATER_QUANTITY
	   @see RescueConstants#PROPERTY_STRETCHED_LENGTH
	   @see RescueConstants#PROPERTY_HEAD
	   @see RescueConstants#PROPERTY_TAIL
	   @see RescueConstants#PROPERTY_LENGTH
	   @see RescueConstants#PROPERTY_ROAD_KIND
	   @see RescueConstants#PROPERTY_CARS_PASS_TO_HEAD
	   @see RescueConstants#PROPERTY_CARS_PASS_TO_TAIL
	   @see RescueConstants#PROPERTY_HUMANS_PASS_TO_HEAD
	   @see RescueConstants#PROPERTY_HUMANS_PASS_TO_TAIL
	   @see RescueConstants#PROPERTY_WIDTH
	   @see RescueConstants#PROPERTY_BLOCK
	   @see RescueConstants#PROPERTY_REPAIR_COST
	   @see RescueConstants#PROPERTY_MEDIAN_STRIP
	   @see RescueConstants#PROPERTY_LINES_TO_HEAD
	   @see RescueConstants#PROPERTY_LINES_TO_TAIL
	   @see RescueConstants#PROPERTY_WIDTH_FOR_WALKERS
	   @see RescueConstants#PROPERTY_EDGES
	   @see RescueConstants#PROPERTY_SIGNAL
	   @see RescueConstants#PROPERTY_SIGNAL_TIMING
	   @see RescueConstants#PROPERTY_SHORTCUT_TO_TURN
	   @see RescueConstants#PROPERTY_POCKET_TO_TURN_ACROSS
	*/
	public static String getPropertyName(int type) {
		switch (type) {
		case RescueConstants.PROPERTY_NULL:
			return "PROPERTY_NULL";
		case RescueConstants.PROPERTY_START_TIME:
			return "PROPERTY_START_TIME";
		case RescueConstants.PROPERTY_LONGITUDE:
			return "PROPERTY_LONGITUDE";
		case RescueConstants.PROPERTY_LATITUDE:
			return "PROPERTY_LATITUDE";
		case RescueConstants.PROPERTY_WIND_FORCE:
			return "PROPERTY_WIND_FORCE";
		case RescueConstants.PROPERTY_WIND_DIRECTION:
			return "PROPERTY_WIND_DIRECTION";
		case RescueConstants.PROPERTY_X:
			return "PROPERTY_X";
		case RescueConstants.PROPERTY_Y:
			return "PROPERTY_Y";
		case RescueConstants.PROPERTY_DIRECTION:
			return "PROPERTY_DIRECTION";
		case RescueConstants.PROPERTY_POSITION:
			return "PROPERTY_POSITION";
		case RescueConstants.PROPERTY_POSITION_HISTORY:
			return "PROPERTY_POSITION_HISTORY";
		case RescueConstants.PROPERTY_POSITION_EXTRA:
			return "PROPERTY_POSITION_EXTRA";
		case RescueConstants.PROPERTY_STAMINA:
			return "PROPERTY_STAMINA";
		case RescueConstants.PROPERTY_HP:
			return "PROPERTY_HP";
		case RescueConstants.PROPERTY_DAMAGE:
			return "PROPERTY_DAMAGE";
		case RescueConstants.PROPERTY_BURIEDNESS:
			return "PROPERTY_BURIEDNESS";
		case RescueConstants.PROPERTY_FLOORS:
			return "PROPERTY_FLOORS";
		case RescueConstants.PROPERTY_BUILDING_ATTRIBUTES:
			return "PROPERTY_BUILDING_ATTRIBUTES";
		case RescueConstants.PROPERTY_IGNITION:
			return "PROPERTY_IGNITION";
		case RescueConstants.PROPERTY_BROKENNESS:
			return "PROPERTY_BROKENNESS";
		case RescueConstants.PROPERTY_FIERYNESS:
			return "PROPERTY_FIERYNESS";
		case RescueConstants.PROPERTY_ENTRANCES:
			return "PROPERTY_ENTRANCES";
			//		case RescueConstants.PROPERTY_BUILDING_SHAPE_ID:
			//			return "PROPERTY_BUILDING_SHAPE_ID";
		case RescueConstants.PROPERTY_BUILDING_CODE:
			return "PROPERTY_BUILDING_CODE";
		case RescueConstants.PROPERTY_BUILDING_AREA_GROUND:
			return "PROPERTY_BUILDING_AREA_GROUND";
		case RescueConstants.PROPERTY_BUILDING_AREA_TOTAL:
			return "PROPERTY_BUILDING_AREA_TOTAL";
		case RescueConstants.PROPERTY_BUILDING_APEXES:
			return "PROPERTY_BUILDING_APEXES";
		case RescueConstants.PROPERTY_WATER_QUANTITY:
			return "PROPERTY_WATER_QUANTITY";
			//		case RescueConstants.PROPERTY_STRETCHED_LENGTH:
			//			return "PROPERTY_STRETCHED_LENGTH";
		case RescueConstants.PROPERTY_HEAD:
			return "PROPERTY_HEAD";
		case RescueConstants.PROPERTY_TAIL:
			return "PROPERTY_TAIL";
		case RescueConstants.PROPERTY_LENGTH:
			return "PROPERTY_LENGTH";
		case RescueConstants.PROPERTY_ROAD_KIND:
			return "PROPERTY_ROAD_KIND";
		case RescueConstants.PROPERTY_CARS_PASS_TO_HEAD:
			return "PROPERTY_CARS_PASS_TO_HEAD";
		case RescueConstants.PROPERTY_CARS_PASS_TO_TAIL:
			return "PROPERTY_CARS_PASS_TO_TAIL";
		case RescueConstants.PROPERTY_HUMANS_PASS_TO_HEAD:
			return "PROPERTY_HUMANS_PASS_TO_HEAD";
		case RescueConstants.PROPERTY_HUMANS_PASS_TO_TAIL:
			return "PROPERTY_HUMANS_PASS_TO_TAIL";
		case RescueConstants.PROPERTY_WIDTH:
			return "PROPERTY_WIDTH";
		case RescueConstants.PROPERTY_BLOCK:
			return "PROPERTY_BLOCK";
		case RescueConstants.PROPERTY_REPAIR_COST:
			return "PROPERTY_REPAIR_COST";
		case RescueConstants.PROPERTY_MEDIAN_STRIP:
			return "PROPERTY_MEDIAN_STRIP";
		case RescueConstants.PROPERTY_LINES_TO_HEAD:
			return "PROPERTY_LINES_TO_HEAD";
		case RescueConstants.PROPERTY_LINES_TO_TAIL:
			return "PROPERTY_LINES_TO_TAIL";
		case RescueConstants.PROPERTY_WIDTH_FOR_WALKERS:
			return "PROPERTY_WIDTH_FOR_WALKERS";
		case RescueConstants.PROPERTY_EDGES:
			return "PROPERTY_EDGES";
		case RescueConstants.PROPERTY_SIGNAL:
			return "PROPERTY_SIGNAL";
		case RescueConstants.PROPERTY_SIGNAL_TIMING:
			return "PROPERTY_SIGNAL_TIMING";
		case RescueConstants.PROPERTY_SHORTCUT_TO_TURN:
			return "PROPERTY_SHORTCUT_TO_TURN";
		case RescueConstants.PROPERTY_POCKET_TO_TURN_ACROSS:
			return "PROPERTY_POCKET_TO_TURN_ACROSS";
		case RescueConstants.PROPERTY_BUILDING_IMPORTANCE:
			return "PROPERTY_BUILDING_IMPORTANCE";
		case RescueConstants.PROPERTY_BUILDING_TEMPERATURE:
			return "PROPERTY_BUILDING_TEMPERATURE";
		default:
			return "Unknown property: "+type;
		}
	}

	/**
	   Translate a command type into a human-readable string
	   @param header The type we want to convert
	   @return A human-readable String showing the type given
	   @see RescueConstants#HEADER_NULL
	   @see RescueConstants#AK_CONNECT
	   @see RescueConstants#AK_ACKNOWLEDGE
	   @see RescueConstants#AK_REST
	   @see RescueConstants#AK_MOVE
	   @see RescueConstants#AK_EXTINGUISH
	   @see RescueConstants#AK_RESCUE
	   @see RescueConstants#AK_CLEAR
	   @see RescueConstants#AK_LOAD
	   @see RescueConstants#AK_UNLOAD
	   @see RescueConstants#KA_CONNECT_OK
	   @see RescueConstants#KA_CONNECT_ERROR
	   @see RescueConstants#KA_SENSE
	   @see RescueConstants#KA_HEAR
	   @see RescueConstants#SK_CONNECT
	   @see RescueConstants#SK_ACKNOWLEDGE
	   @see RescueConstants#SK_UPDATE
	   @see RescueConstants#KS_CONNECT_OK
	   @see RescueConstants#KS_CONNECT_ERROR
	   @see RescueConstants#KS_COMMANDS
	   @see RescueConstants#KG_CONNECT
	   @see RescueConstants#KG_ACKNOWLEDGE
	   @see RescueConstants#KG_UPDATE
	   @see RescueConstants#GK_CONNECT_OK
	   @see RescueConstants#GK_CONNECT_ERROR
	*/
	public static String getCommandTypeName(int header) {
		switch (header) {
		case RescueConstants.HEADER_NULL:
			return "HEADER_NULL";

		case RescueConstants.GK_CONNECT_OK:
			return "GK_CONNECT_OK";
		case RescueConstants.GK_CONNECT_ERROR:
			return "GK_CONNECT_ERROR";
		case RescueConstants.KG_CONNECT:
			return "KG_CONNECT";
		case RescueConstants.KG_ACKNOWLEDGE:
			return "KG_ACKNOWLEDGE";

		case RescueConstants.SK_CONNECT:
			return "SK_CONNECT";
		case RescueConstants.SK_ACKNOWLEDGE:
			return "SK_ACKNOWLEDGE";
		case RescueConstants.SK_UPDATE:
			return "SK_UPDATE";
		case RescueConstants.KS_CONNECT_OK:
			return "KS_CONNECT_OK";
		case RescueConstants.KS_CONNECT_ERROR:
			return "KS_CONNECT_ERROR";
		case RescueConstants.COMMANDS:
			return "COMMANDS";
		case RescueConstants.UPDATE:
			return "UPDATE";

		case RescueConstants.VK_CONNECT:
			return "VK_CONNECT";
		case RescueConstants.VK_ACKNOWLEDGE:
			return "VK_ACKNOWLEDGE";
		case RescueConstants.KV_CONNECT_OK:
			return "KV_CONNECT_OK";
		case RescueConstants.KV_CONNECT_ERROR:
			return "KV_CONNECT_ERROR";
			//		case RescueConstants.KV_UPDATE:
			//			return "KV_UPDATE";

		case RescueConstants.AK_CONNECT:
			return "AK_CONNECT";
		case RescueConstants.AK_ACKNOWLEDGE:
			return "AK_ACKNOWLEDGE";
		case RescueConstants.AK_REST:
			return "AK_REST";
		case RescueConstants.AK_MOVE:
			return "AK_MOVE";
		case RescueConstants.AK_EXTINGUISH:
			return "AK_EXTINGUISH";
		case RescueConstants.AK_CLEAR:
			return "AK_CLEAR";
		case RescueConstants.AK_RESCUE:
			return "AK_RESCUE";
		case RescueConstants.AK_LOAD:
			return "AK_LOAD";
		case RescueConstants.AK_UNLOAD:
			return "AK_UNLOAD";
		case RescueConstants.AK_CHANNEL:
			return "AK_CHANNEL";
		case RescueConstants.AK_REPAIR:
			return "AK_REPAIR";
		case RescueConstants.KA_CONNECT_OK:
			return "KA_CONNECT_OK";
		case RescueConstants.KA_CONNECT_ERROR:
			return "KA_CONNECT_ERROR";
		case RescueConstants.KA_SENSE:
			return "KA_SENSE";
		case RescueConstants.KA_HEAR:
			return "KA_HEAR";
			//		case RescueConstants.KA_HEAR_SAY:
			//			return "KA_HEAR_SAY";
			//		case RescueConstants.KA_HEAR_TELL:
			//			return "KA_HEAR_TELL";
		default:
			return "Unknown header: "+header;
		}
	}

	/**
	   Turn an agent type into a human-readable String
	   @param type The agent type to convert
	   @return A human-readable version of the type given
	*/
	public static String getAgentTypeName(int type) {
		StringBuffer result = new StringBuffer();
		if ((type & RescueConstants.AGENT_TYPE_CIVILIAN)==RescueConstants.AGENT_TYPE_CIVILIAN) result.append("Civilian, ");
		if ((type & RescueConstants.AGENT_TYPE_FIRE_BRIGADE)==RescueConstants.AGENT_TYPE_FIRE_BRIGADE) result.append("Fire Brigade, ");
		if ((type & RescueConstants.AGENT_TYPE_FIRE_STATION)==RescueConstants.AGENT_TYPE_FIRE_STATION) result.append("Fire Station, ");
		if ((type & RescueConstants.AGENT_TYPE_AMBULANCE_TEAM)==RescueConstants.AGENT_TYPE_AMBULANCE_TEAM) result.append("Ambulance Team, ");
		if ((type & RescueConstants.AGENT_TYPE_AMBULANCE_CENTER)==RescueConstants.AGENT_TYPE_AMBULANCE_CENTER) result.append("Ambulance Center, ");
		if ((type & RescueConstants.AGENT_TYPE_POLICE_FORCE)==RescueConstants.AGENT_TYPE_POLICE_FORCE) result.append("Police Force, ");
		if ((type & RescueConstants.AGENT_TYPE_POLICE_OFFICE)==RescueConstants.AGENT_TYPE_POLICE_OFFICE) result.append("Police Office, ");
		String s = result.toString();
		if (s.length()>0) return s.substring(0,s.length()-2);
		return "Unknown";
	}

	/**
	   Turn an array of integers into a String containing a comma-seperated list of numbers
	   @param array The array to convert
	   @return A human-readable String containing a comma-seperated list of numbers
	*/
	public static String arrayAsString(int[] array) {
		if (array==null) return "null";
		StringBuffer result = new StringBuffer();
		for (int i=0;i<array.length;++i) {
			result.append(array[i]);
			if (i<array.length-1) result.append(", ");
		}
		return result.toString();
	}

	public static String arrayAsString(Object[] array) {
		if (array==null) return "null";
		StringBuffer result = new StringBuffer();
		for (int i=0;i<array.length;++i) {
			result.append(array[i]==null?"null":array[i].toString());
			if (i<array.length-1) result.append(", ");
		}
		return result.toString();
	}

	public static String collectionAsString(Collection c) {
		if (c==null) return "null";
		StringBuffer result = new StringBuffer();
		for (Iterator it = c.iterator();it.hasNext();) {
			Object next = it.next();
			result.append(next==null?"null":next.toString());
			if (it.hasNext()) result.append(", ");
		}
		return result.toString();
	}

	public static RescueObject[] merge(RescueObject[] o1, RescueObject[] o2) {
		RescueObject[] result = new RescueObject[o1.length+o2.length];
		System.arraycopy(o1,0,result,0,o1.length);
		System.arraycopy(o2,0,result,o1.length,o2.length);
		return result;
	}
}
