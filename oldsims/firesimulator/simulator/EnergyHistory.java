package firesimulator.simulator;

import java.util.Map;
import java.util.HashMap;

import firesimulator.world.World;
import firesimulator.world.Building;
import org.apache.log4j.Logger;

public class EnergyHistory {
    private static final Logger LOG = Logger.getLogger(EnergyHistory.class);

    private int time;
    private Map<Building, Double> initialEnergy;
    private Map<Building, Double> initialTemperature;
    private Map<Building, Double> burnEnergy;
    private Map<Building, Double> coolEnergy;
    private Map<Building, Double> exchangedWithAir;
    private Map<Building, Double> lostToRadiation;
    private Map<Building, Double> gainedByRadiation;
    private Map<Building, Double> finalEnergy;
    private Map<Building, Double> finalTemperature;

    public EnergyHistory(World world, int time) {
        initialEnergy = new HashMap<Building, Double>();
        initialTemperature = new HashMap<Building, Double>();
        burnEnergy = new HashMap<Building, Double>();
        coolEnergy = new HashMap<Building, Double>();
        exchangedWithAir = new HashMap<Building, Double>();
        lostToRadiation = new HashMap<Building, Double>();
        gainedByRadiation = new HashMap<Building, Double>();
        finalEnergy = new HashMap<Building, Double>();
        finalTemperature = new HashMap<Building, Double>();
        this.time = time;
        for (Building next : world.getBuildings()) {
            initialEnergy.put(next, next.getEnergy());
            initialTemperature.put(next, next.getTemperature());
        }
    }

    public void registerBurn(Building b, double energy) {
        burnEnergy.put(b, energy);
    }

    public void registerCool(Building b, double energy) {
        coolEnergy.put(b, energy);
    }

    public void registerAir(Building b, double energy) {
        exchangedWithAir.put(b, energy);
    }

    public void registerRadiationLoss(Building b, double energy) {
        lostToRadiation.put(b, energy);
    }

    public void registerRadiationGain(Building b, double energy) {
        double old = gainedByRadiation.containsKey(b) ? gainedByRadiation.get(b) : 0;
        gainedByRadiation.put(b, old + energy);
    }

    public void registerFinalEnergy(World world) {
        for (Building next : world.getBuildings()) {
            finalEnergy.put(next, next.getEnergy());
            finalTemperature.put(next, next.getTemperature());
        }
    }

    public void logSummary() {
        LOG.debug("Energy summary at time " + time);
        for (Building next : initialEnergy.keySet()) {
            boolean changed = burnEnergy.containsKey(next) || coolEnergy.containsKey(next) || exchangedWithAir.containsKey(next) || lostToRadiation.containsKey(next) || gainedByRadiation.containsKey(next);
            if (changed && !initialEnergy.get(next).equals(finalEnergy.get(next))) {
                LOG.debug("Building " + next.getID());
                LOG.debug("  Initial energy / temperature: " + initialEnergy.get(next) + " / " + initialTemperature.get(next));
                LOG.debug("  Burn energy                 : " + burnEnergy.get(next));
                LOG.debug("  Cool energy                 : " + coolEnergy.get(next));
                LOG.debug("  Exchanged with air          : " + exchangedWithAir.get(next));
                LOG.debug("  Lost to radiation           : " + lostToRadiation.get(next));
                LOG.debug("  Gained by radiation         : " + gainedByRadiation.get(next));
                LOG.debug("  Final energy / temperature  : " + finalEnergy.get(next) + " / " + finalTemperature.get(next));
            }
        }
    }
}