package misc;

import java.util.Random;
import java.util.Map;
import java.util.EnumMap;

import rescuecore2.config.Config;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Refuge;

/**
 * Container for all misc simulator parameters.
 * @author Maitreyi Nanjanath
 * @author Cameron Skinner
 */
public class MiscParameters {
    private Config config;
    private Random rng;

    // All building classes indexed by building code and degree of collapse. The BuildingClass class knows about buriedness rates, injury rates etc.
    private Map<BuildingCode, Map<BrokennessDegree, BuildingClass>> buildingClasses;

    private DamageType collapseDamage;
    private DamageType buryDamage;
    private DamageType fireDamage;

    /**
       Create a new MiscParameters object based on a Config.
       @param config The Config to read.
    */
    public MiscParameters(Config config) {
        this.config = config;
        rng = config.getRandom();
        initBuildingData();
        initInjuryData();
    }

    /**
       Find out if an agent inside a building should be buried due to collapse.
       @param b The building to check.
       @return True if an agent inside the building should be buried, false otherwise.
    */
    public boolean shouldBuryAgent(Building b) {
        if (!b.isBuildingCodeDefined() || !b.isBrokennessDefined() || b.getBrokenness() == 0||b instanceof Refuge) {
            return false;
        }
        BuildingClass clazz = getBuildingClass(b);
        return clazz.shouldBury();
    }

    /**
       Get the buriedness of an agent inside a building.
       @param b The building to check.
       @return The buriedness of an agent inside the building.
    */
    public int getBuriedness(Building b) {
        if (!b.isBuildingCodeDefined() || !b.isBrokennessDefined() || b.getBrokenness() == 0) {
            return 0;
        }
        BuildingClass clazz = getBuildingClass(b);
        return clazz.getAgentBuriedness();
    }

    /**
       Get the amount of damage an agent should take as a result of being in a collapsing building.
       @param b The building.
       @param agent The agent being damaged.
       @return The amount of damage to add to the agent.
    */
    public int getCollapseDamage(Building b, Human agent) {
        if (!b.isBuildingCodeDefined() || !b.isBrokennessDefined() || b.getBrokenness() == 0) {
            return 0;
        }
        BuildingClass clazz = getBuildingClass(b);
        Injury injury = clazz.getCollapseInjury();
        return collapseDamage.getDamage(injury, agent);
    }

    /**
       Get the amount of damage an agent should take as a result of being buried in a collapsed building.
       @param b The building.
       @param agent The agent being damaged.
       @return The amount of damage to add to the agent.
    */
    public int getBuryDamage(Building b, Human agent) {
        if (!b.isBuildingCodeDefined() || !b.isBrokennessDefined() || b.getBrokenness() == 0) {
            return 0;
        }
        BuildingClass clazz = getBuildingClass(b);
        Injury injury = clazz.getBuryInjury();
        return buryDamage.getDamage(injury, agent);
    }

    /**
       Get the amount of damage an agent should take as a result of being in a burning building.
       @param b The building.
       @param agent The agent being damaged.
       @return The amount of damage to add to the agent.
    */
    public int getFireDamage(Building b, Human agent) {
        if (!b.isBuildingCodeDefined()) {
            return 0;
        }
        BuildingClass clazz = getBuildingClass(b);
        Injury injury = clazz.getFireInjury();
        return fireDamage.getDamage(injury, agent);
    }

    private void initBuildingData() {
        buildingClasses = new EnumMap<BuildingCode, Map<BrokennessDegree, BuildingClass>>(BuildingCode.class);
        for (BuildingCode code : BuildingCode.values()) {
            Map<BrokennessDegree, BuildingClass> codeMap = new EnumMap<BrokennessDegree, BuildingClass>(BrokennessDegree.class);
            for (BrokennessDegree degree : BrokennessDegree.values()) {
                codeMap.put(degree, new BuildingClass(config, code, degree));
            }
            buildingClasses.put(code, codeMap);
        }
    }

    private void initInjuryData() {
        collapseDamage = new DamageType(config, "collapse");
        buryDamage = new DamageType(config, "bury");
        fireDamage = new DamageType(config, "fire");
    }

    private BuildingClass getBuildingClass(Building b) {
        BuildingCode code = BuildingCode.values()[b.getBuildingCode()];
        BrokennessDegree degree = BrokennessDegree.getBrokennessDegree(b);
        return buildingClasses.get(code).get(degree);
    }

    private class BuildingClass {
        private double buriedProbability;
        private int initialBuriedness;
        private Map<Injury, Double> collapseInjuryProbability;
        private Map<Injury, Double> buryInjuryProbability;
        private Map<Injury, Double> fireInjuryProbability;

        public BuildingClass(Config config, BuildingCode code, BrokennessDegree degree) {
            buriedProbability = config.getFloatValue("misc.buriedness." + code + "." + degree + ".rate");
            initialBuriedness = config.getIntValue("misc.buriedness." + code + "." + degree + ".value");
            collapseInjuryProbability = new EnumMap<Injury, Double>(Injury.class);
            buryInjuryProbability = new EnumMap<Injury, Double>(Injury.class);
            fireInjuryProbability = new EnumMap<Injury, Double>(Injury.class);
            collapseInjuryProbability.put(Injury.SLIGHT, config.getFloatValue("misc.injury.collapse." + code + "." + degree + ".slight"));
            collapseInjuryProbability.put(Injury.SERIOUS, config.getFloatValue("misc.injury.collapse." + code + "." + degree + ".serious"));
            collapseInjuryProbability.put(Injury.CRITICAL, config.getFloatValue("misc.injury.collapse." + code + "." + degree + ".critical"));
            buryInjuryProbability.put(Injury.SLIGHT, config.getFloatValue("misc.injury.bury." + code + "." + degree + ".slight"));
            buryInjuryProbability.put(Injury.SERIOUS, config.getFloatValue("misc.injury.bury." + code + "." + degree + ".serious"));
            buryInjuryProbability.put(Injury.CRITICAL, config.getFloatValue("misc.injury.bury." + code + "." + degree + ".critical"));
            fireInjuryProbability.put(Injury.SLIGHT, config.getFloatValue("misc.injury.fire." + code + "." + degree + ".slight"));
            fireInjuryProbability.put(Injury.SERIOUS, config.getFloatValue("misc.injury.fire." + code + "." + degree + ".serious"));
            fireInjuryProbability.put(Injury.CRITICAL, config.getFloatValue("misc.injury.fire." + code + "." + degree + ".critical"));
        }

        public int getAgentBuriedness() {
            return initialBuriedness;
        }

        public boolean shouldBury() {
            return rng.nextDouble() < buriedProbability;
        }

        public Injury getCollapseInjury() {
            return getInjury(collapseInjuryProbability);
        }

        public Injury getBuryInjury() {
            return getInjury(buryInjuryProbability);
        }

        public Injury getFireInjury() {
            return getInjury(fireInjuryProbability);
        }

        private Injury getInjury(Map<Injury, Double> table) {
            double d = rng.nextDouble();
            double d1 = table.get(Injury.SLIGHT);
            double d2 = table.get(Injury.SERIOUS) + d1;
            double d3 = table.get(Injury.CRITICAL) + d2;
            if (d < d1) {
                return Injury.SLIGHT;
            }
            if (d < d2) {
                return Injury.SERIOUS;
            }
            if (d < d3) {
                return Injury.CRITICAL;
            }
            return Injury.NONE;
        }
    }

    private enum BuildingCode {
        WOOD,
            STEEL,
            CONCRETE;

        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    private enum BrokennessDegree {
        NONE(0, 0),
            PARTIAL(1, 25),
            HALF(26, 50),
            ALL(51, 100);

        private int min;
        private int max;

        private BrokennessDegree(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public static BrokennessDegree getBrokennessDegree(Building b) {
            int brokenness = b.isBrokennessDefined() ? b.getBrokenness() : 0;
            for (BrokennessDegree next : BrokennessDegree.values()) {
                if (brokenness >= next.min && brokenness <= next.max) {
                    return next;
                }
            }
            return BrokennessDegree.NONE;
        }
    }

    private enum Injury {
        NONE,
        SLIGHT,
        SERIOUS,
        CRITICAL;
    }

    private static class DamageType {
        private Map<Injury, Integer> damage;
        private double ambulanceMultiplier;
        private double policeMultiplier;
        private double fireMultiplier;

        public DamageType(Config config, String type) {
            damage = new EnumMap<Injury, Integer>(Injury.class);
            damage.put(Injury.NONE, 0);
            damage.put(Injury.SLIGHT, config.getIntValue("misc.injury." + type + ".slight"));
            damage.put(Injury.SERIOUS, config.getIntValue("misc.injury." + type + ".serious"));
            damage.put(Injury.CRITICAL, config.getIntValue("misc.injury." + type + ".critical"));
            ambulanceMultiplier = config.getFloatValue("misc.injury." + type + ".multiplier.ambulance");
            policeMultiplier = config.getFloatValue("misc.injury." + type + ".multiplier.police");
            fireMultiplier = config.getFloatValue("misc.injury." + type + ".multiplier.fire");
        }

        public int getDamage(Injury injury, Human agent) {
            int result = damage.get(injury);
            if (agent instanceof AmbulanceTeam) {
                return (int)(result * ambulanceMultiplier);
            }
            if (agent instanceof PoliceForce) {
                return (int)(result * policeMultiplier);
            }
            if (agent instanceof FireBrigade) {
                return (int)(result * fireMultiplier);
            }
            return result;
        }
    }
}
