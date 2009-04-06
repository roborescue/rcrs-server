package viewer;

import java.io.*;
import java.net.*;
import java.util.*;
import viewer.object.*;

public class Main implements Constants {
	private static InetAddress m_kernelAddress;
	private static int         m_kernelPort;
	private static File        m_logFile = null;
	//	private static File        m_actLogFile = null;

	private static String m_teamName = null;
	public static String teamName() { return m_teamName; }

	private static IO m_io;
	public static IO io() { return m_io; }

	public static String m_simulationID = null;

	//	public static String ACTION_LOG_FILE_NAME = "action.log";//"action.log";
	public static void main(String[] args) {
		parseArgs(args);
		main();
	}

	private static void main() {
		m_io = (m_logFile instanceof File)
			? (IO) new LogIO(m_logFile/*, m_actLogFile*/)
			//      : (IO) new UdpIO(m_kernelAddress, m_kernelPort);
			: (IO) new TCPIO(m_kernelAddress, m_kernelPort);
		m_io.connect();
		new Thread(m_io).start();
		VIEWER.setVisible(true);
		while (true) {
			//			System.out.println("Here: "+WORLD.time());
			while(!m_io.hasUpdateData(WORLD.time() + 1))
				try { Thread.sleep(100);}
				catch (InterruptedException e) { Util.myassert(false); }
			WORLD.parseCommands();
			if (VIEWER.doAnimation())
				VIEWER.animate();
			WORLD.progress();
			if (!VIEWER.doAnimation())
				VIEWER.repaintWithWaiting();
		}
	}

	private static void parseArgs(String[] args) {
		try {
			final int UDP = 0x01;
			final int LOG = 0x02;
			int flag = 0x00;
			m_kernelAddress = InetAddress.getByName("localhost"); // .getLocalHost();
			m_kernelPort    = DEFAULT_KERNEL_PORT;
			int i = 0;
			while (i < args.length) {
				char   option = args[i ++].charAt(1);
				String val    = args[i ++];
				switch(option) {
				default:   printUsage();
				case 't':  m_teamName = val;  break;
				case 'h':  flag |= UDP;  Util.myassert(flag == UDP);  m_kernelAddress = InetAddress.getByName(val);  break;
				case 'p':  flag |= UDP;  Util.myassert(flag == UDP);  m_kernelPort    = Integer.parseInt(val);       break;
				case 'l':  flag |= LOG;  Util.myassert(flag == LOG);  m_logFile    = new File(val); break;
					//				case 'a':  flag |= LOG;  Util.myassert(flag == LOG);  m_actLogFile = new File(val); break;
				case 'i':  m_simulationID = val; break;
					//				case 'q': ACTION_LOG_FILE_NAME = val; break;
				}
			}
		} catch (Exception e) {
			printUsage();
			//} catch (AssertionError e) {
			//  printUsage();
		}
	}

	private static void printUsage() {
		String usage
			= "usage: java viewer.Main [-t <team>] [[-h <host>][-p <port>]|[-l <log>]]\n"
			+ " <team>   team name\n"
			+ " <host>   kernel host (default: localhost)\n"
			+ " <port>   kernel port (default: " + DEFAULT_KERNEL_PORT + ")\n"
			+ " <log>    log file\n";
			//			+ " <actlog> action log file\n"
			//			+ "          (This viewer saves \"" + ACTION_LOG_FILE_NAME + "\" as the action log file)\n";
		System.err.print(usage);
		System.exit(1);
	}
}
