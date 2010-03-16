package gis2;

import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.Namespace;
import org.dom4j.QName;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Human;

/**
   This class knows how to read scenario files and apply them to StandardWorldModels.
*/
public class Scenario {
    private static final int DEFAULT_HP = 10000;
    private static final int DEFAULT_STAMINA = 10000;

    private static final String RCR_NAMESPACE_URI = "urn:roborescue:map:scenario";
    private static final Namespace RCR_NAMESPACE = DocumentHelper.createNamespace("rcr", RCR_NAMESPACE_URI);

    private static final QName RCR_ROOT_QNAME = DocumentHelper.createQName("scenario", RCR_NAMESPACE);
    private static final QName ID_QNAME = DocumentHelper.createQName("id", RCR_NAMESPACE);
    private static final QName LOCATION_QNAME = DocumentHelper.createQName("location", RCR_NAMESPACE);

    private static final XPath REFUGE_XPATH = DocumentHelper.createXPath("//rcr:scenario/rcr:refuge");
    private static final XPath CIV_XPATH = DocumentHelper.createXPath("//rcr:scenario/rcr:civilian");
    private static final XPath FB_XPATH = DocumentHelper.createXPath("//rcr:scenario/rcr:firebrigade");
    private static final XPath AT_XPATH = DocumentHelper.createXPath("//rcr:scenario/rcr:ambulanceteam");
    private static final XPath PF_XPATH = DocumentHelper.createXPath("//rcr:scenario/rcr:policeforce");
    private static final XPath FIRE_XPATH = DocumentHelper.createXPath("//rcr:scenario/rcr:fire");

    // Map from uri prefix to uri for XPaths
    private static final Map<String, String> URIS = new HashMap<String, String>();

    private Collection<Integer> refugeIDs;
    private Collection<Integer> civLocations;
    private Collection<Integer> fbLocations;
    private Collection<Integer> atLocations;
    private Collection<Integer> pfLocations;
    private Collection<Integer> fires;

    static {
        URIS.put("rcr", RCR_NAMESPACE_URI);

        REFUGE_XPATH.setNamespaceURIs(URIS);
        CIV_XPATH.setNamespaceURIs(URIS);
        FB_XPATH.setNamespaceURIs(URIS);
        AT_XPATH.setNamespaceURIs(URIS);
        PF_XPATH.setNamespaceURIs(URIS);
        FIRE_XPATH.setNamespaceURIs(URIS);
    }

    /**
       Create a scenario from an XML document.
       @param doc The document to read.
    */
    public Scenario(Document doc) {
        refugeIDs = new HashSet<Integer>();
        fires = new HashSet<Integer>();
        civLocations = new ArrayList<Integer>();
        fbLocations = new ArrayList<Integer>();
        pfLocations = new ArrayList<Integer>();
        atLocations = new ArrayList<Integer>();
        for (Object next : REFUGE_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            refugeIDs.add(Integer.parseInt(e.attributeValue(ID_QNAME)));
        }
        for (Object next : CIV_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            civLocations.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
        }
        for (Object next : FB_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            fbLocations.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
        }
        for (Object next : PF_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            pfLocations.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
        }
        for (Object next : AT_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            atLocations.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
        }
        for (Object next : FIRE_XPATH.selectNodes(doc)) {
            Element e = (Element)next;
            fires.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
        }
    }

    /**
       Apply this scenario to a world model.
       @param model The world model to alter.
    */
    public void apply(StandardWorldModel model) throws ScenarioException {
        for (int next : refugeIDs) {
            Logger.debug("Coverting building " + next + " to a refuge");
            Building b = (Building)model.getEntity(new EntityID(next));
            if (b == null) {
                throw new ScenarioException("Building " + next + " does not exist");
            }
            Refuge r = new Refuge(b);
            model.removeEntity(b);
            model.addEntity(r);
            Logger.debug("Converted " + b + " into " + r);
        }
        for (int next : fires) {
            Logger.debug("Igniting " + next);
            Building b = (Building)model.getEntity(new EntityID(next));
            if (b == null) {
                throw new ScenarioException("Building " + next + " does not exist");
            }
            b.setIgnition(true);
        }
        int nextID = 0;
        for (StandardEntity next : model) {
            nextID = Math.max(nextID, next.getID().getValue());
        }
        for (int next : civLocations) {
            EntityID id = new EntityID(next);
            Civilian c = new Civilian(new EntityID(++nextID));
            setupAgent(c, id, model);
        }
        for (int next : fbLocations) {
            EntityID id = new EntityID(next);
            FireBrigade f = new FireBrigade(new EntityID(++nextID));
            setupAgent(f, id, model);
       }
        for (int next : pfLocations) {
            EntityID id = new EntityID(next);
            PoliceForce p = new PoliceForce(new EntityID(++nextID));
            setupAgent(p, id, model);
        }
        for (int next : atLocations) {
            EntityID id = new EntityID(next);
            AmbulanceTeam a = new AmbulanceTeam(new EntityID(++nextID));
            setupAgent(a, id, model);
        }
    }

    private void setupAgent(Human h, EntityID position, StandardWorldModel model) {
        Area area = (Area)model.getEntity(position);
        h.setX(area.getX());
        h.setY(area.getY());
        h.setPosition(position);
        h.setStamina(DEFAULT_STAMINA);
        h.setHP(DEFAULT_HP);
        h.setDamage(0);
        h.setBuriedness(0);
        h.setDirection(0);
        h.setTravelDistance(0);
        h.setPositionHistory(new int[0]);
        model.addEntity(h);
        Logger.debug("Created " + h);
    }
}
