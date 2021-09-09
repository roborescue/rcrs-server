package sample;

import static rescuecore2.misc.java.JavaTools.instantiate;

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
import rescuecore2.commands.Command;
import rescuecore2.log.Logger;
import rescuecore2.messages.control.KVTimestep;
import rescuecore2.score.ScoreFunction;
import rescuecore2.standard.commands.StandardCommandURN;
import rescuecore2.standard.components.StandardViewer;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Property;

/**
 * A simple viewer event recorder.
 */
public class SampleViewerEventLogger extends StandardViewer {

  private static final int PRECISION = 3;

  private static final String JSON_RECORD_FILE_FORMAT = "%s__%d_viewer_event_log.jlog";
  private static final String TEAM_NAME_KEY = "viewer.team-name";
  private static final String RECORDS_DIR_KEY = "records.dir";

  private static List<StandardCommandURN> ALLOWED_COMMANDS = Arrays.asList(StandardCommandURN.AK_CLEAR,
      StandardCommandURN.AK_CLEAR_AREA, StandardCommandURN.AK_EXTINGUISH, StandardCommandURN.AK_LOAD,
      StandardCommandURN.AK_MOVE, StandardCommandURN.AK_RESCUE, StandardCommandURN.AK_REST,
      StandardCommandURN.AK_UNLOAD);

  private ScoreFunction scoreFunction;
  private NumberFormat format;
  private String logFilePath;

  @Override
  protected void postConnect() {
    super.postConnect();

    scoreFunction = makeScoreFunction();
    format = NumberFormat.getInstance();
    format.setMaximumFractionDigits(PRECISION);

    Logger.info("Sample Viewer Event Recorder start ...");

    // Write simulation summary
    JSONObject jsonSummary = generateSummary();

    writeJSONFile(jsonSummary, this.logFilePath, false);

    // Write all map information
    JSONArray jsonAllEntities = generateMap();

    JSONObject jsonRecord = new JSONObject();
    jsonRecord.put("Entities", jsonAllEntities);
    jsonRecord.put("TimeStep", 0);

    writeJSONFile(jsonRecord, this.logFilePath, true);
  }

  @Override
  protected void handleTimestep(final KVTimestep kvt) {
    super.handleTimestep(kvt);

    JSONObject jsonInfo = generateInfo(kvt);

    JSONArray jsonEntities = generateChanges(kvt);
    JSONArray jsonDeletedEntities = getDeletedEntities(kvt);
    JSONArray jsonCommands = getCommandActionLog(kvt);

    JSONObject jsonRecord = new JSONObject();
    jsonRecord.put("Info", jsonInfo);
    jsonRecord.put("TimeStep", kvt.getTime());
    jsonRecord.put("Commands", jsonCommands);
    jsonRecord.put("Entities", jsonEntities);
    jsonRecord.put("DeletedEntities", jsonDeletedEntities);

    writeJSONFile(jsonRecord, this.logFilePath, true);
  }

  private JSONObject generateSummary() {
    String mapName = getMapName();

    String teamName = this.config.getValue(TEAM_NAME_KEY, "unknown");
    String recordsDir = this.config.getValue(RECORDS_DIR_KEY, "./records");
    String totalTime = this.config.getValue("kernel.timesteps");
    int channelCount = this.config.getIntValue("comms.channels.count") - 1;

    String JSON_RECORD_FILE_NAME = String.format(JSON_RECORD_FILE_FORMAT, mapName, System.currentTimeMillis());
    this.logFilePath = recordsDir + "/" + JSON_RECORD_FILE_NAME;

    Logger.debug("Sample Viewer: mapName: " + mapName + " teamName: " + teamName + " totalTime: " + totalTime
        + " channelCount: " + channelCount);

    JSONObject jsonSummary = new JSONObject();
    jsonSummary.put("TotalTime", totalTime);
    jsonSummary.put("TeamName", teamName);
    jsonSummary.put("ChannelCount", channelCount);
    jsonSummary.put("MapName", mapName);

    return jsonSummary;
  }

  private JSONArray generateMap() {
    JSONArray jsonAllEntities = new JSONArray();
    for (Entity entity : model.getAllEntities()) {
      try {
        jsonAllEntities.put(entity.toJson());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return jsonAllEntities;
  }

  private JSONObject generateInfo(final KVTimestep kvt) {
    double score = scoreFunction.score(model, new Timestep(kvt.getTime()));

    JSONObject jsonInfo = new JSONObject();
    jsonInfo.put("Score", format.format(score));

    return jsonInfo;
  }

  private JSONArray generateChanges(final KVTimestep kvt) {
    JSONArray jsonEntities = new JSONArray();

    for (EntityID id : kvt.getChangeSet().getChangedEntities()) {

      Entity entity = model.getEntity(id);
      JSONObject jsonEntity = entity.toJson();

      Set<Property<?>> changedProperties = kvt.getChangeSet().getChangedProperties(entity.getID());

      JSONObject filteredJSONEntity = new JSONObject();
      for (Property<?> property : changedProperties) {
        String propertyName = property.getURN();
        if (jsonEntity.has(propertyName) && !jsonEntity.isNull(propertyName)) {
          String jsonEntityProperty = jsonEntity.get(propertyName).toString();
          filteredJSONEntity.put(propertyName, jsonEntityProperty);
        }
      }

      if (!filteredJSONEntity.isEmpty()) {
        filteredJSONEntity.put("Id", jsonEntity.get("Id"));
        jsonEntities.put(jsonEntity);
      }
    }

    return jsonEntities;
  }

  private JSONArray getDeletedEntities(final KVTimestep kvt) {
    JSONArray jsonDeletedEntities = new JSONArray();

    for (EntityID id : kvt.getChangeSet().getDeletedEntities()) {
      jsonDeletedEntities.put(id.getValue());
    }

    return jsonDeletedEntities;
  }

  private JSONArray getCommandActionLog(final KVTimestep kvt) {
    JSONArray jsonAllEntities = new JSONArray();

    for (Command command : kvt.getCommands()) {
      StandardCommandURN commandStandardMessageURN = StandardCommandURN.fromString(command.getURN());

      if (ALLOWED_COMMANDS.contains(commandStandardMessageURN)) {
        jsonAllEntities.put(command.toJson());
      }
    }

    return jsonAllEntities;
  }

  private void writeJSONFile(JSONObject output, String filename, boolean append) {

    // Write JSON file
    try (FileWriter file = new FileWriter(filename, append)) {
      file.write(JSONObject.valueToString(output) + "\r\n");
      file.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private ScoreFunction makeScoreFunction() {
    String className = this.config.getValue(Constants.SCORE_FUNCTION_KEY);
    ScoreFunction result = instantiate(className, ScoreFunction.class);
    result.initialise(model, config);

    return result;
  }

  private String getMapName() {
    String mapDir = this.config.getValue("gis.map.dir").trim();
    String[] map_spl = mapDir.split("/");
    int index = map_spl.length - 1;
    String mapName = map_spl[index].trim();
    if (mapName.equals(""))
      mapName = map_spl[--index].trim();
    if (mapName.equals("map"))
      mapName = map_spl[--index].trim();

    return mapName;
  }

  @Override
  public String toString() {
    return "SampleViewerEventLogger";
  }
}