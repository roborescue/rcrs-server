// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.net.*;
import java.util.Random;
import yab.agent.*;
import yab.agent.object.*;
import yab.io.*;

public abstract class Agent extends Thread implements Constants {
    private final RCRSSProtocolSocket m_socket;
    protected final DisasterSpace world;
    protected final Random random;

    protected Agent(int agentType, InetAddress kernelAddress, int kernelPort) {
        m_socket = new RCRSSProtocolSocket(kernelAddress, kernelPort);
        socket().akConnect(0, agentType);
        Object data = socket().receive();
        if (data instanceof KaConnectError)
            throw new Error(((KaConnectError) data).reason);
        KaConnectOk ok = (KaConnectOk) data;
        world = new DisasterSpace(ok.selfId, ok.selfAndMap);
        socket().akAcknowledge(self().id);

        System.out.println(self());
        random = new Random((long) self().id);
    }

    /** This method decides action of the agent at each cycle. */
    protected abstract void act() throws ActionCommandException;

    /** This method decides reaction to hearing <code>message</code>
     *  from <code>sender</code>.
     */
	protected abstract void hearTell(RealObject sender, int channelId, String message);

    protected void prepareForAct() { /* do nothing */ }
//    protected int utteranceLimit() { return UTTERANCE_LIMIT; }
//    protected abstract int hearingLimit();

    protected RCRSSProtocolSocket socket() { return m_socket; }
    protected int time() { return world.time(); }
    private RealObject self() { return world.self; }

    protected final Property distancePrp = new Property() {
            public Object eval(Object obj) {
                return new Integer(self().distance((RealObject) obj)); }};

    private int m_numUtterance;
    private int m_numHearing;

    public void run() {
        while (true) {
            Object data = socket().receive();
			socket().akChannel(self().id, new byte[]{1,2});
            if (data instanceof KaSense) {
                KaSense sense = (KaSense) data;
                world.update(sense.selfAndMap, sense.time);
                m_numUtterance = 0;
                m_numHearing = 0;
                prepareForAct();
                try {
                    act();
                } catch (ActionCommandException sce) { /* do nothing */ }
/*            } else if (data instanceof KaHear) {
                KaHear hear = (KaHear) data;
                RealObject sender = world.get(hear.senderId);
                if (sender != self()
                    && m_numHearing ++ < hearingLimit())
                    hear(sender, hear.message);
*/            } else if (data instanceof KaHearTell) {
                KaHearTell hearTell = (KaHearTell) data;
                RealObject sender = world.get(hearTell.senderId);
                if (sender != self()
//                    && m_numHearing ++ < hearingLimit()
		    )
                    hearTell(sender, hearTell.channelId, hearTell.message);
/*            }else if (data instanceof KaHearSay) {
                KaHearSay hearSay = (KaHearSay) data;
                RealObject sender = world.get(hearSay.senderId);
                if (sender != self()
                    && m_numHearing ++ < hearingLimit())
                    hearSay(sender, hearSay.message);
*/            }else {
                throw new Error("receives an illegal packet.");
            }
        }
    }

    /** This exception is thrown in order to exit from {@link #act()
     *  act} method.
     */
    protected static class ActionCommandException extends Exception {
        public ActionCommandException() { super(); }
    }

    protected void rest() throws ActionCommandException {
        socket().akRest(self().id);
        throw new ActionCommandException();
    }

    protected void say(String message) {
//        if (m_numUtterance ++ < utteranceLimit())
            socket().akSay(self().id, message);
    }

    protected void tell(String message) {
//        if (m_numUtterance ++ < utteranceLimit())
            socket().akTell(self().id, 1, message);
    }
}
