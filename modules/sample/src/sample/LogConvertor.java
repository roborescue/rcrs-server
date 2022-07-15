package sample;

import static rescuecore2.misc.java.JavaTools.instantiate;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import rescuecore2.Constants;
import rescuecore2.Timestep;
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
import rescuecore2.log.UpdatesRecord;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.KVTimestep;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.misc.java.LoadableTypeProcessor;
import rescuecore2.registry.Registry;
import rescuecore2.score.ScoreFunction;
import rescuecore2.standard.messages.StandardMessageURN;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;
import rescuecore2.worldmodel.WorldModel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import rescuecore2.Constants;
import rescuecore2.Timestep;
import rescuecore2.messages.Command;
import rescuecore2.messages.control.KVTimestep;
import rescuecore2.score.ScoreFunction;
import rescuecore2.standard.components.StandardViewer;
import rescuecore2.standard.messages.StandardMessageURN;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;


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
			if(target.endsWith(".jlog")) {
				JLogGenerator jlog = new JLogGenerator();
				jlog.convert2jlog(reader,target);
			}else {
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
			}
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
class JLogGenerator{
	
	private static final Logger LOG = Logger.getLogger(LogConvertor.class);
	private static final int    PRECISION               = 3;

	  private String              JSON_RECORD_FILE_FORMAT = "%s__%d_viewer_event_log.jlog";
	  private static final String TEAM_NAME_KEY           = "viewer.team-name";
	  private static final String RECORDS_DIR_KEY         = "records.dir";
	  private ScoreFunction       scoreFunction;
	  private NumberFormat        format;

	  private String              teamName;
	  private String              logFilePath;

	private Config config;

	private WorldModel<? extends Entity> model;

	public void convert2jlog(LogReader reader, String target) throws LogException {
		logFilePath=target;
		config=reader.getConfig();
		scoreFunction = makeScoreFunction();
		format = NumberFormat.getInstance();
		format.setMaximumFractionDigits( PRECISION );
		
		// write summery
		JSONObject jsonSummery = generateSummery();
		writeJsonFile( jsonSummery, logFilePath, false );
		
		
		// write map
		JSONObject jsonRecord = new JSONObject();
		model=reader.getWorldModel(0);
		JSONArray jsonAllEntities = generateMap();
		
		jsonRecord.put( "Entities", jsonAllEntities );
		jsonRecord.put( "TimeStep", 0 );
		
		writeJsonFile( jsonRecord, logFilePath, true );
		for(int i=0;i<=reader.getMaxTimestep();i++) {
			LOG.warn("converting ... "+ i+" / "+reader.getMaxTimestep());
//			handleTimestep(reader.getCommands(i),reader.getEntitiesWithUpdates(i),reader.getUpdates(i))
			KVTimestep kvt=new KVTimestep(0, i, reader.getCommands(i).getCommands(), reader.getUpdates(i).getChangeSet());
			this.model=reader.getWorldModel(i);
			handleTimestep(kvt);
		}
	}
	private void handleTimestep( final KVTimestep kvt) {

	    JSONObject jsonInfo = generateInfo( kvt );
	    JSONObject jsonRecord = new JSONObject();

	    JSONArray jsonEntities = new JSONArray();
	    JSONArray jsonDeletedEntities = new JSONArray();
	    JSONArray jsonCommands = new JSONArray();

	    try {
	        jsonEntities = generateChanges( kvt );
	        jsonDeletedEntities = getDeletedEntities( kvt );
	        jsonCommands = getCommandActionLog( kvt );
	    } catch(Exception e) {
	       e.printStackTrace();
	    }

	    jsonRecord.put( "Info", jsonInfo );
	    jsonRecord.put( "TimeStep", kvt.getTime() );
	    jsonRecord.put( "Commands", jsonCommands );
	    jsonRecord.put( "Entities", jsonEntities );
	    jsonRecord.put( "DeletedEntities", jsonDeletedEntities );

	    writeJsonFile( jsonRecord, logFilePath, true );
	  }


	  private JSONArray generateMap() {
	    JSONArray jsonAllEntities = new JSONArray();
	    for ( Entity entity : model.getAllEntities() ) {
	      try {
	        jsonAllEntities.put( entity.toJson() );
	      } catch(Exception e) {
	        e.printStackTrace();
	      }
	    }
	    return jsonAllEntities;
	  }


	  private JSONObject generateSummery() {
	    String mapName = getMapName();

	    this.teamName = config.getValue( TEAM_NAME_KEY, "unknown" );
//	    recordsDir = config.getValue( RECORDS_DIR_KEY, "./records" );
	    String totalTime = config.getValue( "kernel.timesteps" );
	    int channelCount = config.getIntValue( "comms.channels.count" ) - 1;// -1
	                                                                        // for
	                                                                        // say

	    String JSON_RECORD_FILE_NAME = String.format( JSON_RECORD_FILE_FORMAT,
	        mapName, System.currentTimeMillis() );
//	    logFilePath = recordsDir + "/" + JSON_RECORD_FILE_NAME;

//	    Logger
//	        .debug( "Sample Viewer: mapName: " + mapName + " teamName: " + teamName
//	            + " totalTime: " + totalTime + " channelCount:" + channelCount );

	    JSONObject jsonSummery = new JSONObject();
	    jsonSummery.put( "TotalTime", totalTime );
	    jsonSummery.put( "TeamName", this.teamName );
	    jsonSummery.put( "channelCount", channelCount );
	    jsonSummery.put( "MapName", mapName );
	    return jsonSummery;
	  }


	  private JSONObject generateInfo( final KVTimestep t ) {
	    JSONObject jsonInfo = new JSONObject();
	    double score = scoreFunction.score( model, new Timestep( t.getTime() ) );

	    jsonInfo.put( "Score", format.format( score ) );
	    return jsonInfo;
	  }


	  private JSONArray generateChanges( final KVTimestep kvt ) {
	    JSONArray jsonEntities = new JSONArray();
	    for ( EntityID id : kvt.getChangeSet().getChangedEntities() ) {
	      Entity entity = model.getEntity( id );
	      Set<Property> changedProperties = kvt.getChangeSet()
	          .getChangedProperties( entity.getID() );
	      try {
	        // Filter Entity
	        JSONObject jsonEntity = entity.toJson();
	        JSONObject filteredJsonEntity = new JSONObject();
	        for ( Property property : changedProperties ) {
	          String propertyName = property.getURN()+"";
	          if ( jsonEntity.has( propertyName )
	              && !jsonEntity.isNull( propertyName ) ) {
	            String jsonEntityProperty = jsonEntity.get( propertyName ).toString();
	            filteredJsonEntity.put( propertyName, jsonEntityProperty );
	          }
	        }

	        if ( !filteredJsonEntity.isEmpty() ) {
	          filteredJsonEntity.put( "Id", jsonEntity.get( "Id" ) );
	          jsonEntities.put( jsonEntity );
	        }
	      } catch(Exception e) {
	        e.printStackTrace();
	      }
	    }

	    return jsonEntities;
	  }


	  private JSONArray getDeletedEntities( final KVTimestep kvt ) {
	    JSONArray jsonDeletedEntities = new JSONArray();
	    for ( EntityID id : kvt.getChangeSet().getDeletedEntities() ) {
	      try {
	        jsonDeletedEntities.put( id.getValue() );
	      } catch(Exception e) {
	        e.printStackTrace();
	      }
	    }

	    return jsonDeletedEntities;
	  }


	  List<StandardMessageURN> allowed_command_child = Arrays.asList(
	      StandardMessageURN.AK_CLEAR, StandardMessageURN.AK_CLEAR_AREA,
	      StandardMessageURN.AK_EXTINGUISH, StandardMessageURN.AK_LOAD,
	      StandardMessageURN.AK_MOVE, StandardMessageURN.AK_RESCUE,
	      StandardMessageURN.AK_REST, StandardMessageURN.AK_UNLOAD );


	  private JSONArray getCommandActionLog( final KVTimestep t ) {
	    JSONArray jsonAllEntities = new JSONArray();

	    for ( Command command : t.getCommands() ) {
	      StandardMessageURN commandStandardMessageURN = StandardMessageURN
	          .fromInt( command.getURN() );
	      try {
	        if ( allowed_command_child.contains( commandStandardMessageURN ) ) {
	          jsonAllEntities.put( command.toJson() );
	        }
	      } catch(Exception e) {
	        e.printStackTrace();
	      }
	    }
	    return jsonAllEntities;
	  }


	  private void writeJsonFile( JSONObject output, String filename,
	      boolean append ) {

	    // Write JSON file
	    try ( FileWriter file = new FileWriter( filename, append ) ) {

	      file.write( JSONObject.valueToString( output ) + "\r\n" );
	      file.flush();

	    } catch ( IOException e ) {
	      e.printStackTrace();
	    }
	  }


	  private ScoreFunction makeScoreFunction() {
	    String className = config.getValue( Constants.SCORE_FUNCTION_KEY );
	    ScoreFunction result = instantiate( className, ScoreFunction.class );
	    result.initialise( model, config );
	    return result;
	  }


	  private String getMapName() {
	    String mapDir = config.getValue( "gis.map.dir" ).trim();
	    String[] map_spl = mapDir.split( "/" );
	    int index = map_spl.length - 1;
	    String mapName = map_spl[index].trim();
	    if ( mapName.equals( "" ) ) mapName = map_spl[--index].trim();
	    if ( mapName.equals( "map" ) ) mapName = map_spl[--index].trim();

	    return mapName;
	  }
	}
