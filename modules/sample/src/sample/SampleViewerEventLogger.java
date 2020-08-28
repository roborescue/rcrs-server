package sample;

import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import rescuecore2.log.Logger;
import rescuecore2.standard.components.StandardViewer;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Human;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.messages.control.KVTimestep;

/**
 * A simple viewer event recorder.
 */
public class SampleViewerEventLogger extends StandardViewer {

    private String JSON_RECORD_FILE_FORMAT = "%s__%d_viewer_event_log.jlog";
    private static final String TEAM_NAME_KEY = "viewer.team-name";
    private static final String RECORDS_DIR_KEY = "records.dir";
    private String teamName;
    private String recordsDir = "./records";
    private String logFilePath;

    @Override
    protected void postConnect() {
        super.postConnect();

        String mapName = getMapName();
        this.teamName = config.getValue(TEAM_NAME_KEY, "Connecting...");
        recordsDir = config.getValue(RECORDS_DIR_KEY, "./records");
        String totalTime = config.getValue("kernel.timesteps");
        int channelCount = config.getIntValue("comms.channels.count") - 1;//-1 for say

        String JSON_RECORD_FILE_NAME = String.format(JSON_RECORD_FILE_FORMAT, mapName, System.currentTimeMillis());
        logFilePath = recordsDir + "/" + JSON_RECORD_FILE_NAME;


        Logger.info("Sample Viewer Event Recorder start ...");
        Logger.debug("Sample Viewer: mapName: " + mapName + " teamName: " + teamName + " totalTime: " + totalTime + " channelCount:" + channelCount);

        JSONObject jsonInfo = new JSONObject();
        jsonInfo.put("TotalTime", totalTime);
        jsonInfo.put("TeamName", this.teamName);
        jsonInfo.put("channelCount", channelCount);
        jsonInfo.put("MapName", mapName);


        JSONObject jsonRecord = new JSONObject();
        JSONArray jsonAllEntities = new JSONArray();
        for (Entity entity : model.getAllEntities()) {
            jsonAllEntities.put(entity.toJson());
        }

        jsonRecord.put("Entities", jsonAllEntities);
        jsonRecord.put("Info", jsonInfo);
        jsonRecord.put("TimeStep", 0);

        writeJsonFile(jsonRecord, logFilePath, false);
    }

    @Override
    protected void handleTimestep(final KVTimestep kvt) {
        super.handleTimestep(kvt);

        JSONArray jsonEntities = new JSONArray();
        for (EntityID id : kvt.getChangeSet().getChangedEntities()) {
            Entity entity = model.getEntity(id);

            // Filter Entity
            JSONObject jsonEntity = getFilteredEntity(entity, entity.toJson());

            if (jsonEntity == null) {
                continue;
            }

            jsonEntities.put(jsonEntity);
        }

        JSONObject jsonRecord = new JSONObject();

        jsonRecord.put("Entities", jsonEntities);
        jsonRecord.put("TimeStep", kvt.getTime());

        writeJsonFile(jsonRecord, logFilePath, true);
    }

    private JSONObject getFilteredEntity(Entity entity, JSONObject jsonEntity) {
        // SEND Whole entity
        if (entity instanceof Blockade) {
            return jsonEntity;
        }

        jsonEntity.remove("EntityName");

        if (entity instanceof Road) {
            return null;
        }
        if (entity instanceof Building) {
            jsonEntity.remove("Edges");
            jsonEntity.remove("Floors");
        }
        if (entity instanceof Area) {
            jsonEntity.remove("Apexes");
        }
        if (entity instanceof Human) {
            Human human = (Human) entity;
            if (human.getBuriedness() > 0) {
                jsonEntity.remove("Position");
            }
        }

        return jsonEntity;
    }


    private void writeJsonFile(JSONObject output, String filename, boolean append) {

        //Write JSON file
        try (FileWriter file = new FileWriter(filename, append)) {

            file.write(JSONObject.valueToString(output) + "\r\n");
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getMapName() {
        String mapDir = config.getValue("gis.map.dir").trim();
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
        return "SampleViewerRecorder";
    }

}
