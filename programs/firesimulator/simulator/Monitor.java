package firesimulator.simulator;

import firesimulator.world.World;


public interface Monitor {

    public void step(World world);
    
    public void reset(World world);
    
    public void done(World world);
    
}
