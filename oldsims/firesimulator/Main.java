package firesimulator;

import firesimulator.kernel.Kernel;
import firesimulator.kernel.RealKernel;
import firesimulator.kernel.VirtualKernel;
import firesimulator.simulator.Simulator;
import firesimulator.util.Configuration;
import firesimulator.world.World;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author tn
 *
 */
public class Main {
    private static final Log LOG = LogFactory.getLog(Main.class);
	
	public static void main(String[] args) {		
		System.setProperty("apple.laf.useScreenMenuBar","true");
		System.setProperty("apple.awt.brushMetalLook","true");
		Configuration conf=new Configuration();
		conf.initialize();
		conf.parse(args); 
		printwelcome();
		loadSetup();		
		World world=new World();
		Kernel kernel;				
		if(Configuration.isActive("virtual")){				
			kernel = new VirtualKernel(world);			
		}else{
			kernel = new RealKernel(world,Configuration.getValue("host"),new Integer(Configuration.getValue("port")).intValue());
		}			
		Simulator sim=new Simulator(kernel,world);
		sim.run();
	}


	private static void loadSetup() {
		String filename;
		if(Configuration.isActive("csetup"))
			filename=Configuration.getValue("csetup");
		else
			filename=null;
		Configuration.loadConfigTXT(filename);
		if(Configuration.isActive("setup"))
			filename=Configuration.getValue("setup");
		else
			filename="default.stp";
		LOG.info("loading values from \""+filename+"\"");
		if(!Configuration.loadSetup(filename)){			
			LOG.fatal("unable to load setup file. exiting,");
			System.exit(1);
		}
	}

	private static void printwelcome() {
		System.out.println("ResQ Firesimulator");
		System.out.println(Configuration.VERSION);
		System.out.println("");
	}
}
