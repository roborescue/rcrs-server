package sample;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import org.json.JSONObject;

import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.messages.control.ControlMessageFactory;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardPropertyFactory;
import rescuecore2.standard.messages.StandardMessageFactory;

public class URNMapPrinter {

	public static void main(String[] args) throws IOException, ConfigException {
		System.out.println("URNMapPrinter --python_out=file --js_out=file --json_out=file");
		Config config=new Config();
		CommandLineOptions.processArgs(args,config);
		
		JSONObject json = toJSON();
		write2file(config.getValue("json_out", "rcrs_urn.json"), json.toString());
		write2file(config.getValue("python_out", "rcrs_urn.py"), toPython(json));
		write2file(config.getValue("js_out", "rcrs_urn.js"), toJS(json));
//		System.out.println();
//		
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		System.out.println("Python");
//		System.out.println(Registry.SYSTEM_REGISTRY.toPython());
//		
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		System.out.println("Javascript");
//		System.out.println(Registry.SYSTEM_REGISTRY.toJS());
	}

	private static void write2file(String path,String content) throws FileNotFoundException {
		try (PrintStream out = new PrintStream(new FileOutputStream(path))) {
		    out.print(content);
		    out.close();
		}
	}
	
//	public static String toPython() {
//		String out="from enum import IntEnum";
//		out+="\n#### Messages ####\n";
//		
////		ArrayList<Integer> msgkeys = new ArrayList<Integer>(messageFactories.keySet());
////		Collections.sort(msgkeys);
//		int[] msgkeys = ControlMessageFactory.INSTANCE.getKnownMessageURNs();
//		Arrays.sort(msgkeys);
//		int[] commandskeys = StandardMessageFactory.INSTANCE.getKnownMessageURNs();
//		Arrays.sort(commandskeys);
//		int[] entitykeys  = StandardEntityFactory.INSTANCE.getKnownEntityURNs();
//		Arrays.sort(entitykeys);
//		int[] propskeys  = StandardPropertyFactory.INSTANCE.getKnownPropertyURNs();
//		Arrays.sort(propskeys);
//		
//		out+="class Control(IntEnum):\n";
//		for (Integer urn : msgkeys) {
//			String prettyName = ControlMessageFactory.INSTANCE.getPrettyName(urn);
//			out+="\t"+prettyName+"="+urn+"\n";
//		}
//		out+="class Command(IntEnum):\n";
//		for (Integer urn : msgkeys) {
//			String prettyName = StandardMessageFactory.INSTANCE.getPrettyName(urn);
//			out+="\t"+prettyName+"="+urn+"\n";
//		}
//		out+="\n#### Entities ####\n";
//		out+="class Entity(IntEnum):\n";
//		for (Integer urn : entitykeys) {
//			String prettyName = StandardEntityFactory.INSTANCE.getPrettyName(urn);
//			out+="\t"+prettyName+"="+urn+"\n";
//		}
//		out+="\n#### Properties ####\n";
//		out+="class Property(IntEnum):\n";
//		for (Integer urn : propskeys) {
//			String prettyName = StandardPropertyFactory.INSTANCE.getPrettyName(urn);
//			out+="\t"+prettyName+"="+urn+"\n";
//		}
//		
////		out+="\n#### PrettyName ####\n";
////		out+="MAP={\n";
////		out+="\n#### Messages ####\n";
////		for (Integer urn : msgkeys) {
////			String prettyName = urn_prettyName.get(urn);
////			out+="\t"+urn+":'"+prettyName+"',\n";
////		}
////		out+="\n#### Entities ####\n";
////		for (Integer urn : entitykeys) {
////			String prettyName = urn_prettyName.get(urn);
////			out+="\t"+urn+":'"+prettyName+"',\n";
////		}
////		out+="\n#### Properties ####\n";
////		for (Integer urn : propkeys ) {
////			String prettyName = urn_prettyName.get(urn);
////			out+="\t"+urn+":'"+prettyName+"',\n";
////		}
////		out+="}";
//		return out;
//	}
	
//	public static String toJS() {
//		String out="";
//		out+="\n//#### Messages ####\n";
//		int[] msgkeys = ControlMessageFactory.INSTANCE.getKnownMessageURNs();
//		Arrays.sort(msgkeys);
//		int[] commandskeys = StandardMessageFactory.INSTANCE.getKnownMessageURNs();
//		Arrays.sort(commandskeys);
//		int[] entitykeys  = StandardEntityFactory.INSTANCE.getKnownEntityURNs();
//		Arrays.sort(entitykeys);
//		int[] propskeys  = StandardPropertyFactory.INSTANCE.getKnownPropertyURNs();
//		Arrays.sort(propskeys);
//		
//		for (Integer urn : msgkeys) {
//			String prettyName = ControlMessageFactory.INSTANCE.getPrettyName(urn);
//			out+="const "+prettyName+"="+urn+";\n";
//		}
//		for (Integer urn : commandskeys) {
//			String prettyName = StandardMessageFactory.INSTANCE.getPrettyName(urn);
//			out+="const "+prettyName+"="+urn+";\n";
//		}
//		out+="\n//#### Entities ####\n";
//		
//		for (Integer urn : entitykeys) {
//			String prettyName = StandardMessageFactory.INSTANCE.getPrettyName(urn);
//			out+="const "+prettyName+"="+urn+";\n";
//		}
//		out+="\n//#### Properties ####\n";
//		
//		for (Integer urn : propskeys ) {
//			String prettyName = StandardPropertyFactory.INSTANCE.getPrettyName(urn);
//			out+="const "+prettyName+"="+urn+";\n";
//		}
//		
//		return out;
//	}
	public static String toPython(JSONObject json) {
		String out="from enum import IntEnum\n";
		String map="{int(u):u for u in ";
		for (String key : json.keySet()) {
			out+="\n\nclass "+key+"(IntEnum):\n";
			JSONObject sub = ((JSONObject)json.get(key));
			for (String prettyName : sub.keySet()) {
				int urn=(int) sub.get(prettyName);
				out+="\t"+prettyName+"="+urn+"\n";
			}
			map+= "list("+key+") +";
		}
		map=map.substring(0,map.length()-1)+"}";
		out+="\n\nMAP="+map;
		return out;
	}
	public static String toJS(JSONObject json) {
		String out="";
		String map="Object.assign({},";
		
		for (String key : json.keySet()) {
			out+="\n\nconst "+key+"={\n";
			JSONObject sub = ((JSONObject)json.get(key));
			for (String prettyName : sub.keySet()) {
				int urn=(int) sub.get(prettyName);
				out+="\t"+prettyName+":"+urn+",\n";
			}
			out+="}";
			map+="...Object.keys("+key+").map(p=>({["+key+"[p]]:p})),";
		}
		map=map.substring(0,map.length()-1)+")";
		out+="\n\nMAP="+map;
		return out;
	}
	public static JSONObject toJSON() {
		JSONObject json=new JSONObject();
		JSONObject messages=new JSONObject();
		JSONObject entities=new JSONObject();
		JSONObject properties=new JSONObject();
		JSONObject commands=new JSONObject();
		int[] msgkeys = ControlMessageFactory.INSTANCE.getKnownMessageURNs();
		Arrays.sort(msgkeys);
		int[] commandskeys = StandardMessageFactory.INSTANCE.getKnownMessageURNs();
		Arrays.sort(commandskeys);
		int[] entitykeys  = StandardEntityFactory.INSTANCE.getKnownEntityURNs();
		Arrays.sort(entitykeys);
		int[] propskeys  = StandardPropertyFactory.INSTANCE.getKnownPropertyURNs();
		Arrays.sort(propskeys);
		
		for (Integer urn : msgkeys) {
			String prettyName = ControlMessageFactory.INSTANCE.getPrettyName(urn);
			messages.put(prettyName,urn);
		}
		for (Integer urn : commandskeys) {
			String prettyName = StandardMessageFactory.INSTANCE.getPrettyName(urn);
			commands.put(prettyName,urn);
		}
		
		for (Integer urn : entitykeys) {
			String prettyName = StandardEntityFactory.INSTANCE.getPrettyName(urn);
			entities.put(prettyName,urn);
		}
		
		for (Integer urn : propskeys ) {
			String prettyName = StandardPropertyFactory.INSTANCE.getPrettyName(urn);
			properties.put(prettyName,urn);
		}
		
	
		json.put("Message", messages);
		json.put("Entitiy", entities);
		json.put("Property", properties);
		json.put("Command", commands);
		
		return json;
	}
}
