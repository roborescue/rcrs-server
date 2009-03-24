// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package sample;

import java.net.*;
import java.util.*;
import yab.agent.Agent;
import yab.io.ProtocolConstants;

public final class Main implements ProtocolConstants {
    private static InetAddress m_address;
    private static int m_port;

    private static final int
        FB = 0, FS = 1, AT = 2, AC = 3, PF = 4, PO = 5, NUM_TYPE = 6;
    private static final int[] m_num = new int[NUM_TYPE];

    public static void main(String[] args) {
        parseArgs(args);

        List agents = new ArrayList();
        for (int type = 0;  type < NUM_TYPE;  type ++)
            connectAgents(type, agents);
        yab.io.RCRSSProtocolSocket.clearUpAfterInitialization();
        for (Iterator it = agents.iterator();  it.hasNext();  )
            ((Agent) it.next()).start();
    }

    private static void connectAgents(int type, List agents) {
        for (int i = 0;  i < m_num[type];  i ++)
            try {
                agents.add(connectAgent(type));
            } catch (Error e) {
                String reason = e.getMessage();
                if (reason.equals(REASON_OF_KA_CONNECT_ERROR)
                    && m_num[type] == Integer.MAX_VALUE)
                    break;
                System.err.println("connection failed.\n"
                                   + "    Reason: \"" + reason + "\"");
                //System.exit(1);
		break;
            }
    }

    private static Agent connectAgent(int type) {
        switch (type) {
        default: throw new Error();
        //case CV: return new CivilianAgent(m_address, m_port);
        case FB: return new FireBrigadeAgent(m_address, m_port);
        case FS: return new FireStationAgent(m_address, m_port);
        case AT: return new AmbulanceTeamAgent(m_address, m_port);
        case AC: return new AmbulanceCenterAgent(m_address, m_port);
        case PF: return new PoliceForceAgent(m_address, m_port);
        case PO: return new PoliceOfficeAgent(m_address, m_port);
        }
    }

    private static void parseArgs(String[] args) {
        try {
            m_address = InetAddress.getByName("localhost");
            m_port    = KERNEL_LISTENING_PORT;
            if (args.length == 0) {
                for (int i = 0;  i < NUM_TYPE;  i ++)
                    m_num[i] = Integer.MAX_VALUE;
                return;
            }
            if (args.length < NUM_TYPE) printUsage();
            for (int i = 0;  i < NUM_TYPE;  i ++)
                m_num[i] = (args[i].equals("-"))
                    ? Integer.MAX_VALUE
                    : Integer.parseInt(args[i]);

            if (args.length < NUM_TYPE + 1) return;
            m_address = InetAddress.getByName(args[NUM_TYPE]);

            if (args.length < NUM_TYPE + 2) return;
            m_port = Integer.parseInt(args[NUM_TYPE + 1]);

            if (args.length > NUM_TYPE + 2) printUsage();
        }
        catch (Exception e) { printUsage(); }
    }

    private static void printUsage() {
        String usage
            = "usage:\n"
            + "  java samaple.Main "
            + "[<fb> <fs> <at> <ac> <pf> <po> [<host> [<port>]]]\n"
            + "           or '-' to connect agents as much as the kernel "
            + "accepts\n"
            + "    <fb>   the number of fire brigade agents or '-'\n"
            + "    <fs>   the number of fire station agents or '-'\n"
            + "    <at>   the number of ambulance team agents or '-'\n"
            + "    <ac>   the number of ambulance center agents or '-'\n"
            + "    <pf>   the number of police force agents or '-'\n"
            + "    <po>   the number of police office agents or '-'\n"
            + "    <host> kernel host (defalut : localhost)\n"
            + "    <port> kernel port (default : "+KERNEL_LISTENING_PORT+")\n"
            + "  If the numbers of agents are not given, this program tries to"
            + " connect all\n"
            + "  type agents as much as the kernel accepts.\n";
        System.err.print(usage);
        System.exit(1);
    }
}
