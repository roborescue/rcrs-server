package traffic;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.KSUpdate;
import rescuecore2.messages.control.KSCommands;
import rescuecore2.standard.components.StandardSimulator;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.standard.messages.AKUnload;

import traffic.object.RescueObject;
import traffic.object.RealObject;
import traffic.object.MovingObject;
import traffic.object.Road;
import traffic.object.Node;
import traffic.object.Building;
import traffic.object.Refuge;
import traffic.object.FireStation;
import traffic.object.AmbulanceCenter;
import traffic.object.PoliceOffice;
import traffic.object.Humanoid;
import traffic.object.Civilian;
import traffic.object.FireBrigade;
import traffic.object.AmbulanceTeam;
import traffic.object.PoliceForce;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A rescuecore2 Simulator that wraps the old traffic simulator.
 */
public class TrafficSimulatorWrapper extends StandardSimulator {
    private static final Log LOG = LogFactory.getLog(TrafficSimulatorWrapper.class);

    private Simulator sim;

    @Override
    protected void postConnect() {
        super.postConnect();
        sim = new Simulator();
        // Map each entity to a traffic simulator object
        for (Entity next : model) {
            RealObject r = mapEntity(next);
            if (r != null) {
                Constants.WORLD.add(r);
            }
        }
        Constants.WORLD.initialize();
        sim.setInitialPosition();
    }

    @Override
    protected void handleUpdate(KSUpdate u) {
        super.handleUpdate(u);
        // Merge objects
        for (EntityID id : u.getChangeSet().getChangedEntities()) {
            Entity e = model.getEntity(id);
            RescueObject r = Constants.WORLD.get(id.getValue());
            if (r == null) {
                Constants.WORLD.add(mapEntity(e));
            }
            else {
                if (r instanceof Building && e instanceof rescuecore2.standard.entities.Building) {
                    mapBuildingProperties((rescuecore2.standard.entities.Building)e, (Building)r);
                }
                else if (r instanceof Humanoid && e instanceof rescuecore2.standard.entities.Human) {
                    mapHumanProperties((rescuecore2.standard.entities.Human)e, (Humanoid)r);
                }
                else if (r instanceof Node && e instanceof rescuecore2.standard.entities.Node) {
                    mapNodeProperties((rescuecore2.standard.entities.Node)e, (Node)r);
                }
                else if (r instanceof Road && e instanceof rescuecore2.standard.entities.Road) {
                    mapRoadProperties((rescuecore2.standard.entities.Road)e, (Road)r);
                }
                else {
                    LOG.error("Don't know how to map " + r + " from " + e);
                }
            }
        }
    }

    @Override
    protected void processCommands(KSCommands c, ChangeSet changes) {
        Constants.WORLD.setTime(c.getTime());
        for (Command next : c.getCommands()) {
            if (next instanceof AKMove) {
                AKMove move = (AKMove)next;
                int agentID = move.getAgentID().getValue();
                int[] path = collectionToIDArray(move.getPath());
                Constants.WORLD.processMove(agentID, path);
            }
            if (next instanceof AKLoad) {
                AKLoad load = (AKLoad)next;
                int agentID = load.getAgentID().getValue();
                int targetID = load.getTarget().getValue();
                Constants.WORLD.processLoad(agentID, targetID);
            }
            if (next instanceof AKUnload) {
                AKUnload unload = (AKUnload)next;
                int agentID = unload.getAgentID().getValue();
                Constants.WORLD.processUnload(agentID);
            }
        }
        // Simulate
        sim.step();
        // Find changes
        for (MovingObject next : Constants.WORLD.movingObjectArray()) {
            if (next.needsUpdate()) {
                rescuecore2.standard.entities.Human h = (rescuecore2.standard.entities.Human)model.getEntity(new EntityID(next.id));
                next.roundPositionExtra();
                h.setPosition(new EntityID(next.position().id));
                h.setPositionExtra((int)next.positionExtra());
                h.setPositionHistory(idArrayToEntityIDList(next.positionHistory()));
                changes.addChange(h, h.getPositionProperty());
                changes.addChange(h, h.getPositionExtraProperty());
                changes.addChange(h, h.getPositionHistoryProperty());
            }
        }
    }

    private RealObject mapEntity(Entity e) {
        int id = e.getID().getValue();
        if (e instanceof rescuecore2.standard.entities.Refuge) {
            Refuge r = new Refuge(id);
            mapBuildingProperties((rescuecore2.standard.entities.Building)e, r);
            return r;
        }
        if (e instanceof rescuecore2.standard.entities.FireStation) {
            FireStation fs = new FireStation(id);
            mapBuildingProperties((rescuecore2.standard.entities.Building)e, fs);
            return fs;
        }
        if (e instanceof rescuecore2.standard.entities.PoliceOffice) {
            PoliceOffice po = new PoliceOffice(id);
            mapBuildingProperties((rescuecore2.standard.entities.Building)e, po);
            return po;
        }
        if (e instanceof rescuecore2.standard.entities.AmbulanceCentre) {
            AmbulanceCenter ac = new AmbulanceCenter(id);
            mapBuildingProperties((rescuecore2.standard.entities.Building)e, ac);
            return ac;
        }
        if (e instanceof rescuecore2.standard.entities.Building) {
            Building b = new Building(id);
            mapBuildingProperties((rescuecore2.standard.entities.Building)e, b);
            return b;
        }
        if (e instanceof rescuecore2.standard.entities.Node) {
            Node sn = new Node(id);
            mapNodeProperties((rescuecore2.standard.entities.Node)e, sn);
            return sn;
        }
        if (e instanceof rescuecore2.standard.entities.Road) {
            Road r = new Road(id);
            mapRoadProperties((rescuecore2.standard.entities.Road)e, r);
            return r;
        }
        if (e instanceof rescuecore2.standard.entities.Civilian) {
            Civilian c = new Civilian(id);
            mapHumanProperties((rescuecore2.standard.entities.Civilian)e, c);
            return c;
        }
        if (e instanceof rescuecore2.standard.entities.FireBrigade) {
            FireBrigade fb = new FireBrigade(id);
            mapHumanProperties((rescuecore2.standard.entities.FireBrigade)e, fb);
            return fb;
        }
        if (e instanceof rescuecore2.standard.entities.PoliceForce) {
            PoliceForce pf = new PoliceForce(id);
            mapHumanProperties((rescuecore2.standard.entities.PoliceForce)e, pf);
            return pf;
        }
        if (e instanceof rescuecore2.standard.entities.AmbulanceTeam) {
            AmbulanceTeam at = new AmbulanceTeam(id);
            mapHumanProperties((rescuecore2.standard.entities.AmbulanceTeam)e, at);
            return at;
        }
        LOG.error("Don't know how to map this: " + e);
        return null;
    }

    private void mapBuildingProperties(rescuecore2.standard.entities.Building oldB, Building newB) {
        if (oldB.isEntrancesDefined()) {
            newB.setEntrances(collectionToIDArray(oldB.getEntrances()));
        }
        if (oldB.isXDefined()) {
            newB.setX(oldB.getX());
        }
        if (oldB.isYDefined()) {
            newB.setY(oldB.getY());
        }
    }

    private void mapNodeProperties(rescuecore2.standard.entities.Node oldN, Node newN) {
        if (oldN.isEdgesDefined()) {
            newN.setEdges(collectionToIDArray(oldN.getEdges()));
        }
        if (oldN.isXDefined()) {
            newN.setX(oldN.getX());
        }
        if (oldN.isYDefined()) {
            newN.setY(oldN.getY());
        }
    }

    private void mapRoadProperties(rescuecore2.standard.entities.Road oldR, Road newR) {
        if (oldR.isWidthDefined()) {
            newR.setWidth(oldR.getWidth());
        }
        if (oldR.isBlockDefined()) {
            newR.setBlock(oldR.getBlock());
        }
        if (oldR.isLinesToHeadDefined()) {
            newR.setLinesToHead(oldR.getLinesToHead());
        }
        if (oldR.isLinesToTailDefined()) {
            newR.setLinesToTail(oldR.getLinesToTail());
        }
        if (oldR.isHeadDefined()) {
            newR.setHead(oldR.getHead().getValue());
        }
        if (oldR.isTailDefined()) {
            newR.setTail(oldR.getTail().getValue());
        }
        if (oldR.isLengthDefined()) {
            newR.setLength(oldR.getLength());
        }
    }

    private void mapHumanProperties(rescuecore2.standard.entities.Human oldH, Humanoid newH) {
        if (oldH.isHPDefined()) {
            newH.setHp(oldH.getHP());
        }
        if (oldH.isBuriednessDefined()) {
            newH.setBuriedness(oldH.getBuriedness());
        }
        if (oldH.isPositionDefined()) {
            newH.setPosition(oldH.getPosition().getValue());
        }
        if (oldH.isPositionExtraDefined()) {
            newH.setPositionExtra(oldH.getPositionExtra());
        }
    }

    private int[] collectionToIDArray(Collection<EntityID> list) {
        int[] ids = new int[list.size()];
        int i = 0;
        for (EntityID next : list) {
            ids[i++] = next.getValue();
        }
        return ids;
    }

    private List<EntityID> idArrayToEntityIDList(int[] ids) {
        List<EntityID> result = new ArrayList<EntityID>(ids.length);
        for (int i = 0; i < ids.length; ++i) {
            result.add(new EntityID(ids[i]));
        }
        return result;
    }
}