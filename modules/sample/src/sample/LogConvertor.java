package sample;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.log.CommandsRecord;
import rescuecore2.log.ConfigRecord;
import rescuecore2.log.EndLogRecord;
import rescuecore2.log.InitialConditionsRecord;
import rescuecore2.log.LogException;
import rescuecore2.log.LogReader;
import rescuecore2.log.LogWriter;
import rescuecore2.log.RCRSLogFactory;
import rescuecore2.log.StartLogRecord;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.misc.java.LoadableTypeProcessor;
import rescuecore2.registry.Registry;
import rescuecore2.worldmodel.EntityID;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class LogConvertor {
	private static final Logger LOG = Logger.getLogger(LogConvertor.class);
	public static void main(String[] args) {
		BasicConfigurator.configure();

		Config config = new Config();
		try {
			args = CommandLineOptions.processArgs(args, config);
			if (args.length != 2) {
				printUsage();
				return;
			}
			String first = args[0];
			String target = args[1];
			processJarFiles(config);
			LogReader reader = RCRSLogFactory.getLogReader(first,
					Registry.SYSTEM_REGISTRY);
			LogWriter writer= RCRSLogFactory.getLogWriter(new File(target));
			writer.writeRecord(new StartLogRecord());
			writer.writeRecord(new InitialConditionsRecord(reader.getWorldModel(0)));
			writer.writeRecord(new ConfigRecord(config));
			
			int maxTime=reader.getMaxTimestep();
			for (int i =1;i<=maxTime;i++) {
				LOG.warn("converting ... "+ i+" / "+maxTime);
				writer.writeRecord(reader.getCommands(i));
				writer.writeRecord(reader.getUpdates(i));
				for (EntityID id: reader.getEntitiesWithUpdates(i)) {
					writer.writeRecord(reader.getPerception(i, id));
				}			
			}
			writer.writeRecord(new EndLogRecord());
		} catch (IOException e) {
			LOG.error("Error reading log", e);
		} catch (ConfigException e) {
			LOG.error("Configuration error", e);
		} catch (LogException e) {
			LOG.error("Error reading log", e);
		}
	}
	private static void printUsage() {
		System.out.println("Usage: LogConvertor <filename> <target>");
	}
	private static void processJarFiles(Config config) throws IOException {
		LoadableTypeProcessor processor = new LoadableTypeProcessor(config);
		processor.addFactoryRegisterCallbacks(Registry.SYSTEM_REGISTRY);
		processor.process();
	}
}
