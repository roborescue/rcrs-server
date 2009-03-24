package traffic;

import java.net.*;

public class Main implements Constants{
  private static InetAddress m_kernelAddress;
  private static int         m_kernelPort;

  public static void main(String[] args) {
    if(ASSERT)Util.myassert(60 % UNIT_SEC == 0, "wrong UNIT_SEC", UNIT_SEC);
    parseArgs(args);
    Simulator simulator = new Simulator(m_kernelAddress, m_kernelPort);
    simulator.simulate();
  }

  private static void parseArgs(String[] args) {
    try {
      m_kernelAddress = InetAddress.getByName("localhost"); // .getLocalHost();
      m_kernelPort    = DEFAULT_KERNEL_PORT;

      if (args.length < 1) return;
      m_kernelAddress = InetAddress.getByName(args[0]);

      if (args.length < 2) return;
      m_kernelPort = Integer.parseInt(args[1]);

      if (args.length > 2) { printUsage(); System.exit(1); }
    }
    catch (Exception e) { printUsage();  System.exit(1); }
  }

  private static void printUsage() {
    String usage
      = "usage: java traffic.Main [ hostname [ port ] ]\n"
      + "    hostname : name of kernel host (default: localhost)\n"
      + "    port     : kernel port (default: " + DEFAULT_KERNEL_PORT + ")\n";
    System.err.print(usage);
  }
}
