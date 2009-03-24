package viewer;

import java.io.*;
import java.util.*;
import viewer.object.*;
import rescuecore.RescueConstants;

public class LogIO extends IO {
	static final String FILE_TYPE_NAME_0 = "RoboCup-Rescue Prototype Log 00\0";
	static final String FILE_TYPE_NAME_1 = "RoboCup-Rescue Prototype Log 01\0";
	static final String FILE_TYPE_NAME_2 = "RoboCup-Rescue Prototype Log 02\0";
	private DataInputStream m_dis;
	//	private DataInputStream m_adis = null;

	public LogIO(File logFile/*, File actLogFile*/) {
		try {
			m_dis = new DataInputStream(new FileInputStream(logFile));
			byte[] fileType = new byte[FILE_TYPE_NAME_0.length()];
			m_dis.read(fileType, 0, fileType.length);
			String logHeader = new String(fileType);
			Util.myassert(logHeader.equals(FILE_TYPE_NAME_0) || logHeader.equals(FILE_TYPE_NAME_1) || logHeader.equals(FILE_TYPE_NAME_2),
						  logFile + " is not a log file of RoboCupRescue Prototype Simulation System.");
			if (logHeader.equals(FILE_TYPE_NAME_1)) {
				// Show the parameters
				System.out.println("Number of entries: "+(m_dis.readInt()/4));
				System.out.println("random_IDs = "+m_dis.readInt());
				System.out.println("additional_hearing = "+m_dis.readInt());
				System.out.println("max_extinguish_power_sum = "+m_dis.readInt());
				System.out.println("max_extinguish_power = "+m_dis.readInt());
				System.out.println("max_nozzles = "+m_dis.readInt());
				System.out.println("step = "+m_dis.readInt());
				System.out.println("notify_unchangeable_informaion = "+m_dis.readInt());
				System.out.println("use_gettimeofday = "+m_dis.readInt());
				System.out.println("fire_cognition_spredding_speed = "+m_dis.readInt());
				System.out.println("simulate_tank_quantity = "+m_dis.readInt());
				System.out.println("tank_quantity_maximum = "+m_dis.readInt());
				System.out.println("say_max_bytes = "+m_dis.readInt());
				System.out.println("ignore_nozzle_position = "+m_dis.readInt());
				System.out.println("area_per_repair_cost = "+m_dis.readInt());
				System.out.println("round_down_quantity = "+m_dis.readInt());
				System.out.println("accept_multiple_nozzles = "+m_dis.readInt());
				System.out.println("near_agents_rescuable = "+m_dis.readInt());
				System.out.println("steps_far_fire_invisible = "+m_dis.readInt());
				System.out.println("steps_agents_freezed = "+m_dis.readInt());
				System.out.println("notify_initial_position = "+m_dis.readInt());
				System.out.println("notify_position_history = "+m_dis.readInt());
				System.out.println("miscsimulator_supports_load = "+m_dis.readInt());
				System.out.println("notify_only_fire_for_far_buildings = "+m_dis.readInt());
				System.out.println("port = "+m_dis.readInt());
				System.out.println("gis_port = "+m_dis.readInt());
				System.out.println("period = "+m_dis.readInt());
				System.out.println("vision = "+m_dis.readInt());
				System.out.println("voice = "+m_dis.readInt());
				System.out.println("misc_random_seed = "+m_dis.readInt());
			}
			if (logHeader.equals(FILE_TYPE_NAME_2)) {
				// Show the parameters
				int size = m_dis.readInt();
				int count = m_dis.readInt();
				for (int i=0;i<count;++i) {
					int keySize = m_dis.readInt();
					byte[] keyData = new byte[keySize];
					m_dis.read(keyData);
					int valueSize = m_dis.readInt();
					byte[] valueData = new byte[valueSize];
					m_dis.read(valueData);
					System.out.print(new String(keyData));
					System.out.print(" = ");
					System.out.println(new String(valueData));
				}
			}

			//			if (actLogFile instanceof File)
			//				m_adis = new DataInputStream(new FileInputStream(actLogFile));
		} catch (IOException e) { Util.myassert(false, e); }
	}

	private boolean m_isNextUpdate = false;
	private static final byte[] DUMMY_COMMANDS_DATA
		= new byte[] { 0,0,0,(byte)RescueConstants.COMMANDS, 0,0,0,4, 0,0,0,0, 0,0,0,0 }; // {header,length,time,null}

	protected byte[] receive() {
		//		m_isNextUpdate = !m_isNextUpdate;
		//		if (m_isNextUpdate)
			return readLogFile(m_dis);
			//		if (m_adis == null)
			//			return DUMMY_COMMANDS_DATA;
			//		return readLogFile(m_adis);
	}

	private byte[] readLogFile(DataInputStream dis) {
		try {
			if (dis.available() == 0) {
				System.out.println("\nfinish reading log file");
				dis.close();
				return null;
			}
			int size = dis.readInt();
			//			System.out.println("Reading "+size+" bytes");
			Util.myassert(size % 4 == 0, "illegal data");
			byte[] buf = new byte[size];
			dis.read(buf, 0, size);
			return buf;
		} catch (IOException e) { Util.myassert(false, e); throw new Error(e); }
	}

	protected void send(byte[] body) {
		// nothing to do
	}
}
