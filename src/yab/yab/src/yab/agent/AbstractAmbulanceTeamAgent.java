// Copyright (C) 2002 Takeshi Morimoto <morimoto@takopen.cs.uec.ac.jp>
// All rights reserved.
package yab.agent;

import java.net.*;
import yab.agent.*;
import yab.agent.object.*;
import yab.io.ProtocolConstants;

public abstract class AbstractAmbulanceTeamAgent extends PlatoonAgent {
    protected AbstractAmbulanceTeamAgent(InetAddress address, int port) {
        super(ProtocolConstants.AGENT_TYPE_AMBULANCE_TEAM, address, port);
    }

    protected AmbulanceTeam self() { return (AmbulanceTeam) world.self; }

    private Humanoid m_loadingInjured = null;
    protected Humanoid loadingInjured() { return m_loadingInjured; }
    protected boolean isLoadingInjured() { return m_loadingInjured != null; }
    protected void checkLoadingInjured() {
        if (loadingInjured() instanceof Humanoid
            && loadingInjured().position() != self())
            m_loadingInjured = null;
    }

    protected void prepareForAct() {
        super.prepareForAct();
        checkLoadingInjured();
    }

    protected void rescue(Humanoid target) throws ActionCommandException {
        socket().akRescue(self().id, target.id);
        throw new ActionCommandException();
    }

    protected void load(Humanoid target) throws ActionCommandException {
        socket().akLoad(self().id, target.id);
        m_loadingInjured = target;
        throw new ActionCommandException();
    }

    protected void unload() throws ActionCommandException {
        socket().akUnload(self().id);
        m_loadingInjured = null;
        throw new ActionCommandException();
    }
}
