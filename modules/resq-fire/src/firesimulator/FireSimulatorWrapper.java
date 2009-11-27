package firesimulator;

import rescuecore2.config.NoSuchConfigOptionException;
import rescuecore2.components.AbstractSimulator;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.DefaultWorldModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.Update;
import rescuecore2.messages.control.Commands;
import rescuecore2.messages.control.SKUpdate;
import rescuecore2.standard.messages.AKExtinguish;

import firesimulator.kernel.Kernel;
import firesimulator.world.World;
import firesimulator.world.WorldInfo;
import firesimulator.world.Refuge;
import firesimulator.world.FireStation;
import firesimulator.world.PoliceOffice;
import firesimulator.world.AmbulanceCenter;
import firesimulator.world.Building;
import firesimulator.world.Civilian;
import firesimulator.world.FireBrigade;
import firesimulator.world.PoliceForce;
import firesimulator.world.AmbulanceTeam;
import firesimulator.world.StreetNode;
import firesimulator.world.Road;
import firesimulator.world.RescueObject;
import firesimulator.world.MovingObject;
import firesimulator.simulator.Simulator;
import firesimulator.simulator.ExtinguishRequest;
import firesimulator.util.Configuration;

import java.util.Collection;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

/**
   A rescuecore2 Simulator that wraps the ResQ Freiburg fire simulator.
 */
public class FireSimulatorWrapper extends AbstractSimulator<Entity> {
    private Simulator sim;
    private World world;
    private WrapperKernel kernel;

    @Override
    protected WorldModel<Entity> createWorldModel() {
        return new DefaultWorldModel<Entity>(Entity.class);
    }

    @Override
    protected void postConnect() {
        Configuration c = new Configuration();
        c.initialize();
        for (String next : c.getPropertyNames()) {
            try {
                String value = config.getValue(next);
                Configuration.setProperty(next, value, true);
                System.out.println("Setting '" + next + "' to '" + value + "'");
            }
            catch (NoSuchConfigOptionException e) {
                // Ignore
                System.out.println("Ignoring property " + next);
            }
        }
        world = new World();
        kernel = new WrapperKernel();
        sim = new Simulator(kernel, world);
        // Map each entity to a fire simulator object
        for (Entity next : model) {
            RescueObject r = mapEntity(next);
            if (r != null) {
                world.putObject(r);
            }
        }
        Thread t = new Thread() {
                public void run() {
                    sim.run();
                }
            };
        t.start();
    }

    @Override
    protected void handleUpdate(Update u) {
        super.handleUpdate(u);
        // Merge objects
        for (EntityID id : u.getChangeSet().getChangedEntities()) {
            Entity e = model.getEntity(id);
            RescueObject r = world.getObject(id.getValue());
            if (r == null) {
                world.putObject(mapEntity(e));
            }
            else {
                if (r instanceof Building && e instanceof rescuecore2.standard.entities.Building) {
                    Building b = (Building)r;
                    mapBuildingProperties((rescuecore2.standard.entities.Building)e, b);
                    // Check for new ignitions
                    if (b.getIgnition() == 1 && b.isInflameable()) {
                        int fieryness = b.getFieryness();
                        // CHECKSTYLE:OFF:MagicNumber
                        if (fieryness == 0 || fieryness == 4) {
                            // CHECKSTYLE:ON:MagicNumber
                            b.ignite();
                        }
                    }
                }
                else if (r instanceof MovingObject && e instanceof rescuecore2.standard.entities.Human) {
                    mapHumanProperties((rescuecore2.standard.entities.Human)e, (MovingObject)r);
                }
                else if (r instanceof StreetNode && e instanceof rescuecore2.standard.entities.Node) {
                    mapNodeProperties((rescuecore2.standard.entities.Node)e, (StreetNode)r);
                }
                else if (r instanceof Road && e instanceof rescuecore2.standard.entities.Road) {
                    mapRoadProperties((rescuecore2.standard.entities.Road)e, (Road)r);
                }
                else {
                    System.out.println("Don't know how to map " + r + " from " + e);
                }
            }
        }
    }

    @Override
    protected void handleCommands(Commands c) {
        for (Command next : c.getCommands()) {
            if (next instanceof AKExtinguish) {
                AKExtinguish ex = (AKExtinguish)next;
                EntityID agentID = ex.getAgentID();
                EntityID targetID = ex.getTarget();
                int water = ex.getWater();
                FireBrigade source = (FireBrigade)world.getObject(agentID.getValue());
                Building target = (Building)world.getObject(targetID.getValue());
                ExtinguishRequest req = new ExtinguishRequest(source, target, water);
                world.addExtinguishRequest(req);
            }
        }
        try {
            kernel.commandsReceived(c.getTime());
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private RescueObject mapEntity(Entity e) {
        int id = e.getID().getValue();
        if (e instanceof rescuecore2.standard.entities.World) {
            return new WorldInfo(id);
        }
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
            StreetNode sn = new StreetNode(id);
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
        System.err.println("Don't know how to map this: " + e);
        return null;
    }

    private void mapBuildingProperties(rescuecore2.standard.entities.Building oldB, Building newB) {
        if (oldB.isFloorsDefined()) {
            newB.setFloors(oldB.getFloors());
        }
        if (oldB.isBuildingAttributesDefined()) {
            newB.setAttributes(oldB.getBuildingAttributes());
        }
        if (oldB.isIgnitionDefined()) {
            newB.setIgnition(oldB.getIgnition() ? 1 : 0);
        }
        if (oldB.isFierynessDefined()) {
            newB.setFieryness(oldB.getFieryness());
        }
        if (oldB.isBrokennessDefined()) {
            newB.setBrokenness(oldB.getBrokenness());
        }
        if (oldB.isEntrancesDefined()) {
            newB.setEntrances(collectionToIDArray(oldB.getEntrances()));
        }
        if (oldB.isBuildingCodeDefined()) {
            newB.setCode(oldB.getBuildingCode());
        }
        if (oldB.isGroundAreaDefined()) {
            newB.setBuildingAreaGround(oldB.getGroundArea());
        }
        if (oldB.isTotalAreaDefined()) {
            newB.setBuildingAreaTotal(oldB.getTotalArea());
        }
        if (oldB.isApexesDefined()) {
            newB.setApexes(oldB.getApexes());
        }
        if (oldB.isXDefined()) {
            newB.setX(oldB.getX());
        }
        if (oldB.isYDefined()) {
            newB.setY(oldB.getY());
        }
    }

    private void mapNodeProperties(rescuecore2.standard.entities.Node oldN, StreetNode newN) {
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

    private void mapHumanProperties(rescuecore2.standard.entities.Human oldH, MovingObject newH) {
        if (oldH.isStaminaDefined()) {
            newH.setStamina(oldH.getStamina());
        }
        if (oldH.isHPDefined()) {
            newH.setHp(oldH.getHP());
        }
        if (oldH.isDamageDefined()) {
            newH.setDamage(oldH.getDamage());
        }
        if (oldH.isBuriednessDefined()) {
            newH.setBuriedness(oldH.getBuriedness());
        }
        if (oldH.isPositionDefined()) {
            newH.setPositionId(oldH.getPosition().getValue());
        }
        if (oldH.isPositionExtraDefined()) {
            newH.setPositionExtra(oldH.getPositionExtra());
        }
        if (oldH instanceof rescuecore2.standard.entities.FireBrigade && newH instanceof FireBrigade) {
            rescuecore2.standard.entities.FireBrigade oldFB = (rescuecore2.standard.entities.FireBrigade)oldH;
            FireBrigade newFB = (FireBrigade)newH;
            if (oldFB.isWaterDefined()) {
                newFB.setInitialWaterQuantity(oldFB.getWater());
            }
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

    private class WrapperKernel implements Kernel {
        private CyclicBarrier commandsBarrier;
        private int time;

        public WrapperKernel() {
            commandsBarrier = new CyclicBarrier(2);
            time = 0;
        }

        @Override
        public void register(Simulator s) {
        }

        @Override
        public void establishConnection() {
        }

        @Override
        public void signalReadyness() {
        }

        @Override
        public boolean waitForNextCycle() {
            try {
                commandsBarrier.await();
                return true;
            }
            catch (InterruptedException e) {
                return false;
            }
            catch (BrokenBarrierException e) {
                return false;
            }
        }

        @Override
        public void sendUpdate() {
            ChangeSet changes = new ChangeSet();
            for (Object next : world.getBuildings()) {
                Building b = (Building)next;
                rescuecore2.standard.entities.Building oldB = (rescuecore2.standard.entities.Building)model.getEntity(new EntityID(b.getID()));
                if ((!oldB.isFierynessDefined()) || (oldB.getFieryness() != b.getFieryness())) {
                    oldB.setFieryness(b.getFieryness());
                    changes.addChange(oldB, oldB.getFierynessProperty());
                }
            }
            for (Object next : world.getFirebrigades()) {
                FireBrigade fb = (FireBrigade)next;
                if (fb.hasChanged()) {
                    rescuecore2.standard.entities.FireBrigade oldFB = (rescuecore2.standard.entities.FireBrigade)model.getEntity(new EntityID(fb.getID()));
                    changes.addChange(oldFB, oldFB.getWaterProperty());
                }
            }
            send(new SKUpdate(simulatorID, time, changes));
        }

        @Override
        public void receiveUpdate() {
        }

        void commandsReceived(int newTime) throws InterruptedException, BrokenBarrierException {
            this.time = newTime;
            commandsBarrier.await();
        }
    }
}