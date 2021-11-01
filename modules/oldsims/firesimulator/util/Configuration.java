package firesimulator.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import firesimulator.world.Wall;

import org.apache.log4j.Logger;

public class Configuration {
    private static final Logger LOG = Logger.getLogger(Configuration.class);

    private static final String PREFIX = "resq-fire.";


	public class Prop{

		private String name;
		private String command;
		private String description;
		private String paramName;
		private boolean paramReq;
		private String value;
		boolean active;

		public Prop(String name, String command, String description , String paramName, boolean paramReq,String def){
			this.name=name;
			this.command=command;
			this.description=description;
			this.paramName=paramName;
			this.paramReq=paramReq;
			if(def!=null){
				value=def;
				active=true;
			}else{
				value=null;
				active=false;
			}
		}

		public String getValue(){
			return value;
		}

		public boolean isActive(){
			return active;
		}

		public boolean validate(){
			if(paramReq)
				if(value==null||value.length()==0)return false;
			return true;
		}

		public String getDescription(){
			if(description==null)return "";
			return "\n"+name+":  "+command+" "+(paramName!=null?(!paramReq?"[":"")+"<"+paramName+">"+(!paramReq?"]":""):"")+"\n"+description+"\n";
		}

	}

	private static LinkedList Props	=new LinkedList();
	private static final String CONFIG_TXT_PATH=".";
	public static String VERSION="06.08.2005";

	public void initialize(){
		Props.add(new Prop(PREFIX + "store","s","Stores the intial data from the kernel in the given file.","filename",true,null));
		Props.add(new Prop(PREFIX + "virtual","v","Use the virtual kernel instead of the rescue kernel.\nRequires a .scn file.","filename",true,null));
		Props.add(new Prop(PREFIX + "host","h","The host to connect to. Default host is localhost.","host",true,"localhost"));
		Props.add(new Prop(PREFIX + "port","p","The port to connect to. Default port is 6000","port",true,"6000"));
		Props.add(new Prop(PREFIX + "setup","stp","Uses the given setup file","filename",true,null));
		Props.add(new Prop(PREFIX + "csetup","cstp","Uses the given config.txt file","filename",true,null));
		Props.add(new Prop(PREFIX + "ray_rate","ray_rate","Number of emitted rays per mm while sampling. Default rate is "+Wall.RAY_RATE,"rate",true,Wall.RAY_RATE+""));
		Props.add(new Prop(PREFIX + "help","help","Prints this text and exits",null,false,null));
		//hidden parameters
		Props.add(new Prop(PREFIX + "cell_size","cell_size",null,null,true,null));
                Props.add(new Prop(PREFIX + "max_ray_distance","max_ray_distance",null,null,true,null));
		Props.add(new Prop(PREFIX + "energy_loss","energy_loss",null,null,true,null));
		Props.add(new Prop(PREFIX + "air_to_air_flow","air_to_air_flow",null,null,true,null));
		Props.add(new Prop(PREFIX + "air_to_building_flow","air_to_building_flow",null,null,true,null));
		Props.add(new Prop(PREFIX + "air_cell_heat_capacity","air_cell_heat_capacity",null,null,true,null));
		Props.add(new Prop(PREFIX + "wooden_capacity","wooden_capacity",null,null,true,null));
		Props.add(new Prop(PREFIX + "wooden_energy","wooden_energy",null,null,true,null));
		Props.add(new Prop(PREFIX + "wooden_ignition","wooden_ignition",null,null,true,null));
		Props.add(new Prop(PREFIX + "wooden_burning","wooden_burning",null,null,true,null));
		Props.add(new Prop(PREFIX + "wooden_speed","wooden_speed",null,null,true,null));
		Props.add(new Prop(PREFIX + "steel_capacity","steel_capacity",null,null,true,null));
		Props.add(new Prop(PREFIX + "steel_energy","steel_energy",null,null,true,null));
		Props.add(new Prop(PREFIX + "steel_ignition","steel_ignition",null,null,true,null));
		Props.add(new Prop(PREFIX + "steel_burning","steel_burning",null,null,true,null));
		Props.add(new Prop(PREFIX + "steel_speed","steel_speed",null,null,true,null));
		Props.add(new Prop(PREFIX + "concrete_capacity","concrete_capacity",null,null,true,null));
		Props.add(new Prop(PREFIX + "concrete_energy","concrete_energy",null,null,true,null));
		Props.add(new Prop(PREFIX + "concrete_ignition","concrete_ignition",null,null,true,null));
		Props.add(new Prop(PREFIX + "concrete_burning","concrete_burning",null,null,true,null));
		Props.add(new Prop(PREFIX + "concrete_speed","concrete_speed",null,null,true,null));
		Props.add(new Prop(PREFIX + "max_extinguish_power_sum","max_extinguish_power_sum",null,null,true,null));
		Props.add(new Prop(PREFIX + "water_refill_rate","water_refill_rate",null,null,true,null));
		Props.add(new Prop(PREFIX + "water_hydrant_refill_rate","water_hydrant_refill_rate",null,null,true,null));
		Props.add(new Prop(PREFIX + "water_capacity","water_capacity",null,null,true,null));
		Props.add(new Prop(PREFIX + "water_thermal_capacity","water_thermal_capacity",null,null,true,null));
		Props.add(new Prop(PREFIX + "water_distance","water_distance",null,null,true,null));
		Props.add(new Prop(PREFIX + "radiation_coefficient","radiation_coefficient",null,null,true,null));
		Props.add(new Prop(PREFIX + "wind_speed","wind_speed",null,null,true,null));
		Props.add(new Prop(PREFIX + "wind_direction","wind_direction",null,null,true,null));
		Props.add(new Prop(PREFIX + "wind_random","wind_random",null,null,true,null));
		Props.add(new Prop(PREFIX + "randomseed","randomseed",null,null,true,null));
		Props.add(new Prop("random.seed","random.seed",null,null,true,null));
		Props.add(new Prop(PREFIX + "refuge_inflammable","refuge_inflammable",null,null,true,null));
		Props.add(new Prop(PREFIX + "fire_station_inflammable","firestation_inflammable",null,null,true,null));
		Props.add(new Prop(PREFIX + "police_office_inflammable","policeoffice_inflammable",null,null,true,null));
		Props.add(new Prop(PREFIX + "ambulance_center_inflammable","ambulancecenter_inflammable",null,null,true,null));
		Props.add(new Prop(PREFIX + "gamma","gamma",null,null,true,null));
		Props.add(new Prop(PREFIX + "rays.dir","rays",null,null,true,"rays"));
		Props.add(new Prop(PREFIX + "burn-rate-average","burn-rate-average",null,null,true,"0.2"));
		Props.add(new Prop(PREFIX + "burn-rate-variance","burn-rate-variance",null,null,true,"0"));
	}

	public void parse(String cmdLine){
		StringTokenizer st=new StringTokenizer(cmdLine,"-");
		try{
			while(st.hasMoreTokens()){
				 String tok=st.nextToken();
				 int index=tok.indexOf(" ");
				 String cmd;
				 if(index==-1){
				 	cmd=tok.trim();
					if(cmd.length()==0)continue;
				 	Prop p =propForCmd(cmd);
				 	p.active=true;
				 }else{
				 	cmd=tok.substring(0,index).trim();
				 	if(cmd.length()==0)continue;
					Prop p =propForCmd(cmd);
					p.active=true;
					p.value=tok.substring(index).trim();
				 }
			}
		}catch(Exception e){printHelpAndExit();}
		if(isActive("help")){
			printHelpAndExit();
		}
	}

	private void printHelpAndExit(){
		System.out.println("ResQ Firesimulator");
		System.out.println(VERSION);
		System.out.println("author: Timo N�ssle\nemail: nuessle@informatik.uni-freiburg.de\n");
		System.out.println("java Main [-<option> <value>]*");
		for(Iterator i=Props.iterator();i.hasNext();)
			System.out.print(((Prop)i.next()).getDescription());
		System.exit(0);
	}

	private static Configuration.Prop propForCmd(String cmd){
		for(Iterator i=Props.iterator();i.hasNext();){
			Prop p=(Prop)i.next();
			if(p.command.compareTo(cmd)==0)return p;
		}
		return null;
	}

	public static boolean isActive(String name){
		for(Iterator i=Props.iterator();i.hasNext();){
			Prop p=(Prop)i.next();
			if(p.name.compareTo(name)==0) return p.isActive();
		}
		return false;
	}

	public static String getValue(String name){
		for(Iterator i=Props.iterator();i.hasNext();){
			Prop p=(Prop)i.next();
			if(p.name.compareTo(name)==0) return p.getValue();
		}
		return null;
	}


	public void parse(String[] args) {
		if(args.length<1)return;
		String s="";
		for(int i=0;i<args.length;s+=" "+args[i],i++);
		parse(s);
	}


	public static boolean loadSetup(String fileName) {
		try {
			FileInputStream fis=new FileInputStream(new File(fileName));
			Properties prop=new Properties();
			prop.load(fis);
			fis.close();
			for(Iterator i=Props.iterator();i.hasNext();){
				Prop p=(Prop)i.next();
				String val=prop.getProperty(p.command);
				if(val!=null){
					p.value=val;
					p.active=true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static void loadConfigTXT(String filename) {
		String fname=CONFIG_TXT_PATH+File.separator+"config.txt";
		if(filename!=null)
			fname=filename;
		LOG.info("loading values from \""+fname+"\"");
		try{
			Pattern comment=Pattern.compile("([^#]*)(#(.*))*",Pattern.DOTALL);
			Pattern keyValue=Pattern.compile("([^:]*):(.*)",Pattern.DOTALL);
			BufferedReader br=new BufferedReader(new FileReader(fname));
			Hashtable lines=new Hashtable();
			String line;
			String key;
			String value;
			while((line=br.readLine())!=null){
				line=line.trim();
				Matcher m=comment.matcher(line);
				if(m.matches()){
					Matcher gm=keyValue.matcher(m.group(1));
					if(gm.matches()&&gm.groupCount()==2){
						key=gm.group(1).trim();
						value=gm.group(2).trim();
						lines.put(key,value);
					}
				}
			}
			for(Iterator i=Props.iterator();i.hasNext();){
				Prop p=(Prop)i.next();
				String name=p.name;
				value=(String) lines.get(name);
				if(value!=null){
					p.active=true;
					p.value=value;
				}
			}

		}catch (Exception e) {
                    LOG.error("unable to load \""+fname+"\"", e);
		}


	}

	public static void setProperty(String name, String value, boolean state){
		for(Iterator i=Props.iterator();i.hasNext();){
			Prop p=(Prop)i.next();
			if(p.name.compareTo(name)==0){
				p.value=value;
				p.active=state;
				break;
			}
		}
	}

    public static List<String> getPropertyNames() {
        List<String> result = new ArrayList<String>();
        for(Iterator i = Props.iterator(); i.hasNext();){
            Prop next = (Prop)i.next();
            result.add(next.name);
        }
        return result;
    }

	public static void dump(){
		for(Iterator i=Props.iterator();i.hasNext();){
			Prop p=(Prop)i.next();
			LOG.debug(p.command+"="+p.value+ "["+p.active+"]");
		}
	}


	public static void storeHiddenProps(String fileName) throws IOException {
		Properties prop= new Properties();
		for(Iterator i=Props.iterator();i.hasNext();){
			Prop p=(Prop)i.next();
			if(p.description==null){
                            if(p.value==null) {
                                LOG.debug(p.command);
                            }
                            prop.put(p.command,p.value);
			}
		}
		if(!fileName.endsWith(".stp"))fileName+=".stp";
		File f=new File(fileName);
		if(f.exists())f.delete();
		f.createNewFile();
		FileOutputStream fos=new FileOutputStream(f);
		prop.store(fos,"fire simulator setup file");
		fos.close();
	}


}
