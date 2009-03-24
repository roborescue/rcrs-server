// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package sample;

import java.net.*;
import java.util.*;
import yab.agent.*;
import yab.agent.object.*;

public class PoliceForceAgent extends AbstractPoliceForceAgent {
    public PoliceForceAgent(InetAddress address, int port) {
        super(address, port);
    }

    private ArrayList m_reportedBlockades = new ArrayList();

    protected static final Condition HAS_NOT_BEEN_SEEN_CND
        = Property.get("RealObject", "hasBeenSeen").equal(Boolean.FALSE);

    protected void act() throws ActionCommandException {
        clearHere();
        moveReportedBlockades();
        searchBlockades();
    }

    protected void clearHere() throws ActionCommandException {
        MotionlessObject pos = self().motionlessPosition();
        if (!(pos instanceof Road))
            return;
        Road road = (Road) pos;
        if (road.passableLines() == 0)
            clear(road);
    }

    protected void moveReportedBlockades() throws ActionCommandException {
        correctReportedBlockade();
        if (!m_reportedBlockades.isEmpty())
            move(m_reportedBlockades);
    }

    protected void correctReportedBlockade() {
        for (Iterator it = m_reportedBlockades.iterator();  it.hasNext();  ) {
            Road road = (Road) it.next();
            if (road.hasBeenSeen()  &&  road.passableLines() > 0)
                it.remove();
        }
    }

    protected void searchBlockades() throws ActionCommandException {
        List uncheckedRoads = HAS_NOT_BEEN_SEEN_CND.extract(world.roads);
        if (!uncheckedRoads.isEmpty())
            move(uncheckedRoads);
    }

   protected void hearTell(RealObject sender, int channelId, String message) 
	{
         if (!(sender instanceof PoliceOffice))
            return;
        StringTokenizer st = new StringTokenizer(message);
        String command = st.nextToken();
        RealObject target = world.get(Integer.parseInt(st.nextToken()));
        if (command.equals("clear"))
            m_reportedBlockades.add(target);
       // do nothing
	}
}
