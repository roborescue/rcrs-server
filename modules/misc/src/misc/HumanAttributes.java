package misc;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.EntityID;

import rescuecore2.standard.entities.Human;

import java.util.Random;

/**
 Class for holding information about humans.
 */
/*
 * Implementation of Refuge Bed Capacity
 * @author Farshid Faraji
 * May 2020 During Covid-19 :-)))
 * */
public class HumanAttributes {
    private Human human;
    private EntityID id;
    private DamageType damageFire;
    private DamageType damageCollapse;
    private DamageType damageBury;
    private Random random;

    /**
     Construct a HumanAttributes object that wraps a Human.
     @param h The Human to wrap.
     @param config The system configuration.
     */
    public HumanAttributes(Human h, Config config) {
        this.human = h;
        this.id = h.getID();
        // Generate Random for each Human
        this.random = new Random(config.getRandom().nextLong());
        damageFire = new DamageType("fire", config, random);
        damageCollapse = new DamageType("collapse", config, random);
        damageBury = new DamageType("bury", config, random);
    }

    /**
     Get the ID of the wrapped human.
     @return The human ID.
     */
    public EntityID getID() {
        return id;
    }

    /**
     Get the wrapped human.
     @return The wrapped human.
     */
    public Human getHuman() {
        return human;
    }

    /**
     Get the random sequence of the wrapped human.
     @return The random sequence.
     */
    public Random getRandom(){
        return random;
    }
    /**
     Add some collapse damage.
     @param d The amount of damage to add.
     */
    public void addCollapseDamage(double d) {
        damageCollapse.addDamage(d);
    }

    /**
     Get the amount of collapse damage this human has.
     @return The amount of collapse damage.
     */
    public double getCollapseDamage() {
        return damageCollapse.getDamage();
    }

    /**
     Set the amount of collapse damage this human has.
     @param d The new collapse damage.
     */
    public void setCollapseDamage(double d) {
        damageCollapse.setDamage(d);
    }

    /**
     Add some buriedness damage.
     @param d The amount of damage to add.
     */
    public void addBuriednessDamage(double d) {
        damageBury.addDamage(d);
    }

    /**
     Get the amount of buriedness damage this human has.
     @return The amount of buriedness damage.
     */
    public double getBuriednessDamage() {
        return damageBury.getDamage();
    }

    /**
     Set the amount of buriedness damage this human has.
     @param d The new buriedness damage.
     */
    public void setBuriednessDamage(double d) {
        damageBury.setDamage(d);
    }

    /**
     Add some fire damage.
     @param d The amount of damage to add.
     */
    public void addFireDamage(double d) {
        damageFire.addDamage(d);
    }

    /**
     Get the amount of fire damage this human has.
     @return The amount of fire damage.
     */
    public double getFireDamage() {
        return damageFire.getDamage();
    }

    /**
     Set the amount of fire damage this human has.
     @param d The new fire damage.
     */
    public void setFireDamage(double d) {
        damageFire.setDamage(d);
    }

    /**
     Get the total damage of this human, rounded to the nearest integer.
     @return The total damage.
     */
    public int getTotalDamage() {
        return (int)Math.round(damageCollapse.getDamage() + damageFire.getDamage() + damageBury.getDamage());
    }

    /**
     Progress all damage types.
     */
    public void progressDamage() {
        damageCollapse.progress();
        damageFire.progress();
        damageBury.progress();
    }

    public void progressDamageInRefuge()
    {
        //int damage = getTotalDamage();
        damageCollapse.progressInRefuge();
        damageFire.progressInRefuge();
        damageBury.progressInRefuge();
    }

    /**
     Clear all damage.
     */
    public void clearDamage() {
        damageCollapse.setDamage(0);
        damageBury.setDamage(0);
        damageFire.setDamage(0);
    }
}

