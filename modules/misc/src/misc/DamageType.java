package misc;

import rescuecore2.config.Config;

import java.util.Random;

import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.number.NumberGenerator;

/**
 Container for information about different damage types.
 */
/*
 * Implementation of Refuge Bed Capacity
 * @author Farshid Faraji
 * May 2020 During Covid-19 :-)))
 * */
public class DamageType {
    private String type;
    private double k;
    private double l;
    private NumberGenerator<Double> noise;

    private double damage;

    /**
     Construct a DamageType.
     @param type The name of this type.
     @param config The system configuration.
     @param Random sequence proprietary.
     */
    public DamageType(String type, Config config, Random random) {
        this.type = type;
        k = config.getFloatValue("misc.injury." + type + ".k");
        l = config.getFloatValue("misc.injury." + type + ".l");
        double mean = config.getFloatValue("misc.injury." + type + ".noise.mean");
        double sd = config.getFloatValue("misc.injury." + type + ".noise.sd");
        noise = new GaussianGenerator(mean, sd, random);
        damage = 0;
    }

    /**
     Get the type name.
     @return The type name.
     */
    public String getType() {
        return type;
    }

    /**
     Compute damage progression for this type.
     @return The new damage.
     */
    public double progress() {
        if (damage <= 0) {
            return damage;
        }
        double n = noise.nextValue();
        damage = damage + (k * damage * damage) + l + n;
        return damage;
    }

    public double progressInRefuge() {
        if (damage <= 0) {
            return damage;
        }
        double n = noise.nextValue();
        damage = damage - (k * damage * damage) - l - (2*n);
        return damage;
    }
    /**
     Get the current damage.
     @return The current damage.
     */
    public double getDamage() {
        return damage;
    }

    /**
     Set the current damage.
     @param d The current damage.
     */
    public void setDamage(double d) {
        damage = d;
    }

    /**
     Add some damage.
     @param d The amount to add.
     */
    public void addDamage(double d) {
        damage += d;
    }
}
