package rescuecore2.standard.score;

import rescuecore2.score.AbstractScoreFunction;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.Timestep;

import java.util.Map;
import java.util.EnumMap;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntityConstants;

/**
   Score function that measures the amount of damage done to buildings by fire.
 */
public class BuildingDamageScoreFunction extends AbstractScoreFunction {
    private static final String HEATING_FACTOR = "score.standard.building-fire.heating";
    private static final String BURNING_FACTOR = "score.standard.building-fire.burning";
    private static final String INFERNO_FACTOR = "score.standard.building-fire.inferno";
    private static final String WATER_DAMAGE_FACTOR = "score.standard.building-fire.water-damage";
    private static final String MINOR_DAMAGE_FACTOR = "score.standard.building-fire.minor-damage";
    private static final String MODERATE_DAMAGE_FACTOR = "score.standard.building-fire.moderate-damage";
    private static final String SEVERE_DAMAGE_FACTOR = "score.standard.building-fire.severe-damage";
    private static final String BURNT_OUT_FACTOR = "score.standard.building-fire.burnt-out";
    private static final String ABSOLUTE_KEY = "score.standard.building-fire.absolute";

    private Map<StandardEntityConstants.Fieryness, Double> factors;
    private boolean absolute;

    /**
       Construct a BuildingDamageScoreFunction.
    */
    public BuildingDamageScoreFunction() {
        super("Building damage");
    }

    @Override
    public void initialise(WorldModel<? extends Entity> world, Config config) {
        factors = new EnumMap<StandardEntityConstants.Fieryness, Double>(StandardEntityConstants.Fieryness.class);
        factors.put(StandardEntityConstants.Fieryness.UNBURNT, 1.0);
        factors.put(StandardEntityConstants.Fieryness.HEATING, config.getFloatValue(HEATING_FACTOR));
        factors.put(StandardEntityConstants.Fieryness.BURNING, config.getFloatValue(BURNING_FACTOR));
        factors.put(StandardEntityConstants.Fieryness.INFERNO, config.getFloatValue(INFERNO_FACTOR));
        factors.put(StandardEntityConstants.Fieryness.WATER_DAMAGE, config.getFloatValue(WATER_DAMAGE_FACTOR));
        factors.put(StandardEntityConstants.Fieryness.MINOR_DAMAGE, config.getFloatValue(MINOR_DAMAGE_FACTOR));
        factors.put(StandardEntityConstants.Fieryness.MODERATE_DAMAGE, config.getFloatValue(MODERATE_DAMAGE_FACTOR));
        factors.put(StandardEntityConstants.Fieryness.SEVERE_DAMAGE, config.getFloatValue(SEVERE_DAMAGE_FACTOR));
        factors.put(StandardEntityConstants.Fieryness.BURNT_OUT, config.getFloatValue(BURNT_OUT_FACTOR));
        absolute = config.getBooleanValue(ABSOLUTE_KEY, false);
    }

    @Override
    public double score(WorldModel<? extends Entity> world, Timestep timestep) {
        double sum = 0;
        double max = 0;
        for (Entity next : world) {
            if (next instanceof Building) {
                Building b = (Building)next;
                if (!b.isTotalAreaDefined()) {
                    continue;
                }
                int importance = b.isImportanceDefined() ? b.getImportance() : 1;
                double area = b.getTotalArea() * importance;
                StandardEntityConstants.Fieryness fire = b.getFierynessEnum();
                double factor;
                if (fire == null) {
                    factor = 1;
                }
                else {
                    factor = factors.get(fire);
                }
                sum += area * factor;
                max += area;
            }
        }
        if (absolute) {
            return sum;
        }
        else {
            return sum / max;
        }
    }
}
