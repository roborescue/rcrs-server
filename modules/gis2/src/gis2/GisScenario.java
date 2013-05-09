package gis2;

import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.jfree.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;

import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.Entity;
import rescuecore2.log.Logger;
import rescuecore2.config.Config;


import rescuecore2.scenario.compatibilities.CollapseSimCompatibaleScenarioV1_1;
import rescuecore2.scenario.exceptions.ScenarioException;
import rescuecore2.standard.entities.GasStation;
import rescuecore2.standard.entities.Hydrant;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.FireStation;
import rescuecore2.standard.entities.PoliceOffice;
import rescuecore2.standard.entities.AmbulanceCentre;
import rescuecore2.standard.entities.Human;

/**
 * This class knows how to read scenario files and apply them to
 * StandardWorldModels.
 */
public class GisScenario implements rescuecore2.scenario.Scenario,
		CollapseSimCompatibaleScenarioV1_1 {
	private static final String SCENARIO_NAMESPACE_URI = "urn:roborescue:map:scenario";
	private static final Namespace SCENARIO_NAMESPACE = DocumentHelper
			.createNamespace("scenario", SCENARIO_NAMESPACE_URI);

	private static final int DEFAULT_HP = 10000;
	private static final int DEFAULT_STAMINA = 10000;
	private static final String WATER_QUANTITY_KEY = "fire.tank.maximum";

	private static final QName ROOT_QNAME = DocumentHelper.createQName(
			"scenario", SCENARIO_NAMESPACE);
	private static final QName ID_QNAME = DocumentHelper.createQName("id",
			SCENARIO_NAMESPACE);
	private static final QName LOCATION_QNAME = DocumentHelper.createQName(
			"location", SCENARIO_NAMESPACE);
	private static final QName TIME_QNAME = DocumentHelper.createQName("time",
			SCENARIO_NAMESPACE);
	private static final QName INTENSITY_QNAME = DocumentHelper.createQName(
			"intensity", SCENARIO_NAMESPACE);

	private static final QName SCENARIO_QNAME = DocumentHelper.createQName(
			"scenario", SCENARIO_NAMESPACE);
	private static final QName REFUGE_QNAME = DocumentHelper.createQName(
			"refuge", SCENARIO_NAMESPACE);
	private static final QName GAS_STATION_QNAME = DocumentHelper.createQName(
			"gasstation", SCENARIO_NAMESPACE);
	private static final QName HYDRANT_QNAME = DocumentHelper.createQName(
			"hydrant", SCENARIO_NAMESPACE);
	private static final QName CIV_QNAME = DocumentHelper.createQName(
			"civilian", SCENARIO_NAMESPACE);
	private static final QName FB_QNAME = DocumentHelper.createQName(
			"firebrigade", SCENARIO_NAMESPACE);
	private static final QName AT_QNAME = DocumentHelper.createQName(
			"ambulanceteam", SCENARIO_NAMESPACE);
	private static final QName PF_QNAME = DocumentHelper.createQName(
			"policeforce", SCENARIO_NAMESPACE);
	private static final QName FS_QNAME = DocumentHelper.createQName(
			"firestation", SCENARIO_NAMESPACE);
	private static final QName AC_QNAME = DocumentHelper.createQName(
			"ambulancecentre", SCENARIO_NAMESPACE);
	private static final QName PO_QNAME = DocumentHelper.createQName(
			"policeoffice", SCENARIO_NAMESPACE);
	private static final QName FIRE_QNAME = DocumentHelper.createQName("fire",
			SCENARIO_NAMESPACE);
	private static final QName AFTERSHOCK_QNAME = DocumentHelper.createQName(
			"aftershock", SCENARIO_NAMESPACE);/* Aftershock requirement:2013 */

	private Set<Integer> refuges;
	private Set<Integer> hydrants;
	private Set<Integer> gasStations;
	private Set<Integer> fires;
	private HashMap<Integer, Float> aftershocks;/* Aftershock requirement:2013 */
	private Collection<Integer> civLocations;
	private Collection<Integer> fbLocations;
	private Collection<Integer> atLocations;
	private Collection<Integer> pfLocations;
	private Collection<Integer> fsLocations;
	private Collection<Integer> acLocations;
	private Collection<Integer> poLocations;

	/**
	 * Create an empty scenario.
	 */
	public GisScenario() {
		refuges = new HashSet<Integer>();
		hydrants = new HashSet<Integer>();
		gasStations = new HashSet<Integer>();
		fires = new HashSet<Integer>();
		civLocations = new ArrayList<Integer>();
		fbLocations = new ArrayList<Integer>();
		pfLocations = new ArrayList<Integer>();
		atLocations = new ArrayList<Integer>();
		fsLocations = new ArrayList<Integer>();
		poLocations = new ArrayList<Integer>();
		acLocations = new ArrayList<Integer>();
		/* Aftershock requirement:2013 */
		aftershocks = new HashMap<Integer, Float>();
	}

	/**
	 * Create a scenario from an XML document.
	 * 
	 * @param doc
	 *            The document to read.
	 * @throws ScenarioException
	 *             If the scenario is invalid.
	 */
	public GisScenario(Document doc) throws ScenarioException {
		this();
		read(doc);
	}

	/**
	 * Read scenario data from an XML document.
	 * 
	 * @param doc
	 *            The document to read.
	 * @throws ScenarioException
	 *             If the scenario is invalid.
	 */
	public void read(Document doc) throws ScenarioException {
		hydrants.clear();
		gasStations.clear();
		refuges.clear();
		fires.clear();
		civLocations.clear();
		fbLocations.clear();
		pfLocations.clear();
		atLocations.clear();
		fsLocations.clear();
		poLocations.clear();
		acLocations.clear();
		Element root = doc.getRootElement();
		if (!root.getQName().equals(SCENARIO_QNAME)) {
			throw new ScenarioException(
					"Scenario document has wrong root element: expecting "
							+ SCENARIO_QNAME + "; not " + root.getQName());
		}
		for (Object next : root.elements(REFUGE_QNAME)) {
			Element e = (Element) next;
			refuges.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
		}
		for (Object next : root.elements(HYDRANT_QNAME)) {
			Element e = (Element) next;
			hydrants.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
		}
		for (Object next : root.elements(GAS_STATION_QNAME)) {
			Element e = (Element) next;
			gasStations.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
		}
		for (Object next : root.elements(CIV_QNAME)) {
			Element e = (Element) next;
			civLocations
					.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
		}
		for (Object next : root.elements(FB_QNAME)) {
			Element e = (Element) next;
			fbLocations.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
		}
		for (Object next : root.elements(PF_QNAME)) {
			Element e = (Element) next;
			pfLocations.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
		}
		for (Object next : root.elements(AT_QNAME)) {
			Element e = (Element) next;
			atLocations.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
		}
		for (Object next : root.elements(FS_QNAME)) {
			Element e = (Element) next;
			fsLocations.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
		}
		for (Object next : root.elements(PO_QNAME)) {
			Element e = (Element) next;
			poLocations.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
		}
		for (Object next : root.elements(AC_QNAME)) {
			Element e = (Element) next;
			acLocations.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
		}
		for (Object next : root.elements(FIRE_QNAME)) {
			Element e = (Element) next;
			fires.add(Integer.parseInt(e.attributeValue(LOCATION_QNAME)));
		}
		/*
		 * Aftershock requirement:2013
		 */
		for (Object next : root.elements(AFTERSHOCK_QNAME)) {
			Element e = (Element) next;
			aftershocks.put(Integer.parseInt(e.attributeValue(TIME_QNAME)),
					Float.parseFloat(e.attributeValue(INTENSITY_QNAME)));
		}
	}

	/**
	 * Write scenario data to an XML document.
	 * 
	 * @param doc
	 *            The document to write to.
	 */
	public void write(Document doc) {
		Element root = DocumentHelper.createElement(SCENARIO_QNAME);
		doc.setRootElement(root);
		for (int next : refuges) {
			root.addElement(REFUGE_QNAME).addAttribute(LOCATION_QNAME,
					String.valueOf(next));
		}

		for (int next : fires) {
			root.addElement(FIRE_QNAME).addAttribute(LOCATION_QNAME,
					String.valueOf(next));
		}
		for (int next : hydrants) {
			root.addElement(HYDRANT_QNAME).addAttribute(LOCATION_QNAME,
					String.valueOf(next));
		}
		for (int next : gasStations) {
			root.addElement(GAS_STATION_QNAME).addAttribute(LOCATION_QNAME,
					String.valueOf(next));
		}
		for (int next : civLocations) {
			root.addElement(CIV_QNAME).addAttribute(LOCATION_QNAME,
					String.valueOf(next));
		}
		for (int next : fbLocations) {
			root.addElement(FB_QNAME).addAttribute(LOCATION_QNAME,
					String.valueOf(next));
		}
		for (int next : fsLocations) {
			root.addElement(FS_QNAME).addAttribute(LOCATION_QNAME,
					String.valueOf(next));
		}
		for (int next : pfLocations) {
			root.addElement(PF_QNAME).addAttribute(LOCATION_QNAME,
					String.valueOf(next));
		}
		for (int next : poLocations) {
			root.addElement(PO_QNAME).addAttribute(LOCATION_QNAME,
					String.valueOf(next));
		}
		for (int next : atLocations) {
			root.addElement(AT_QNAME).addAttribute(LOCATION_QNAME,
					String.valueOf(next));
		}
		for (int next : acLocations) {
			root.addElement(AC_QNAME).addAttribute(LOCATION_QNAME,
					String.valueOf(next));
		}
		root.addNamespace("scenario", SCENARIO_NAMESPACE_URI);
	}

	/**
	 * Apply this scenario to a world model.
	 * 
	 * @param model
	 *            The world model to alter.
	 * @param config
	 *            The configuration.
	 * @throws ScenarioException
	 *             if this scenario is invalid.
	 */
	public void apply(StandardWorldModel model, Config config)
			throws ScenarioException {
		Logger.debug("Creating " + refuges.size() + " refuges");
		for (int next : refuges) {
			Logger.debug("Converting building " + next + " to a refuge");
			Building b = (Building) model.getEntity(new EntityID(next));
			if (b == null) {
				throw new ScenarioException("Building " + next
						+ " does not exist");
			}
			Refuge r = new Refuge(b);
			model.removeEntity(b);
			model.addEntity(r);
			Logger.debug("Converted " + b + " into " + r);
		}
		for (int next : gasStations) {
			Logger.debug("Converting building " + next + " to a gas station");
			Building b = (Building) model.getEntity(new EntityID(next));
			if (b == null) {
				throw new ScenarioException("Building " + next
						+ " does not exist");
			}
			GasStation r = new GasStation(b);
			r.setImportance(5);
			model.removeEntity(b);
			model.addEntity(r);
			Logger.debug("Converted " + b + " into " + r);
		}
		for (int next : hydrants) {
			Logger.debug("Converting Road " + next + " to a hydrant");
			Area area = (Area) model.getEntity(new EntityID(next));
			if (area == null || !(area instanceof Road)) {
				throw new ScenarioException("Road " + next + " does not exist");
			}
			Hydrant h = new Hydrant((Road) area);
			model.removeEntity(area);
			model.addEntity(h);
			Logger.debug("Converted " + area + " into " + h);
		}
		Logger.debug("Igniting " + fires.size() + " fires");
		for (int next : fires) {
			Logger.debug("Igniting " + next);
			Building b = (Building) model.getEntity(new EntityID(next));
			if (b == null) {
				throw new ScenarioException("Building " + next
						+ " does not exist");
			}
			b.setIgnition(true);
		}
		int lastID = 0;
		for (StandardEntity next : model) {
			lastID = Math.max(lastID, next.getID().getValue());
		}
		Logger.debug("Creating " + fbLocations.size() + " fire brigades");

		for (int next : fbLocations) {
			EntityID id = new EntityID(next);
			lastID = getNextId(model, config, lastID);
			FireBrigade f = new FireBrigade(new EntityID(lastID));
			setupAgent(f, id, model, config);
		}
		Logger.debug("Creating " + pfLocations.size() + " police forces");
		for (int next : pfLocations) {
			EntityID id = new EntityID(next);
			lastID = getNextId(model, config, lastID);
			PoliceForce p = new PoliceForce(new EntityID(lastID));
			setupAgent(p, id, model, config);
		}
		Logger.debug("Creating " + atLocations.size() + " ambulance teams");
		for (int next : atLocations) {
			EntityID id = new EntityID(next);
			lastID = getNextId(model, config, lastID);
			AmbulanceTeam a = new AmbulanceTeam(new EntityID(lastID));
			setupAgent(a, id, model, config);
		}
		Logger.debug("Creating " + fsLocations.size() + " fire stations");
		for (int next : fsLocations) {
			EntityID id = new EntityID(next);
			Logger.debug("Coverting building " + next + " to a fire station");
			Building b = (Building) model.getEntity(id);
			if (b == null) {
				throw new ScenarioException("Building " + next
						+ " does not exist");
			}
			FireStation f = new FireStation(b);
			model.removeEntity(b);
			model.addEntity(f);
			Logger.debug("Converted " + b + " into " + f);
		}
		Logger.debug("Creating " + poLocations.size() + " police offices");
		for (int next : poLocations) {
			EntityID id = new EntityID(next);
			Logger.debug("Coverting building " + next + " to a police office");
			Building b = (Building) model.getEntity(id);
			if (b == null) {
				throw new ScenarioException("Building " + next
						+ " does not exist");
			}
			PoliceOffice p = new PoliceOffice(b);
			model.removeEntity(b);
			model.addEntity(p);
			Logger.debug("Converted " + b + " into " + p);
		}
		Logger.debug("Creating " + acLocations.size() + " ambulance centres");
		for (int next : acLocations) {
			EntityID id = new EntityID(next);
			Logger.debug("Coverting building " + next
					+ " to an ambulance centre");
			Building b = (Building) model.getEntity(id);
			if (b == null) {
				throw new ScenarioException("Building " + next
						+ " does not exist");
			}
			AmbulanceCentre a = new AmbulanceCentre(b);
			model.removeEntity(b);
			model.addEntity(a);
			Logger.debug("Converted " + b + " into " + a);
		}
		Logger.debug("Creating " + civLocations.size() + " civilians");
		for (int next : civLocations) {
			EntityID id = new EntityID(next);
			lastID = getNextId(model, config, lastID);
			Civilian c = new Civilian(new EntityID(lastID));
			setupAgent(c, id, model, config);
		}
	}

	private int getNextId(StandardWorldModel model, Config config, int lastId) {
		boolean humanRandomId = config.getBooleanValue(
				"senario.human.random-id", true);
		if (humanRandomId) {
			int newId;
			do {
				newId = config.getRandom().nextInt(Integer.MAX_VALUE);
			} while (model.getEntity(new EntityID(newId)) != null);
			return newId;
		} else {
			return lastId + 1;
		}
	}

	/**
	 * Get the set of fire locations.
	 * 
	 * @return The set of fire locations.
	 */
	public Set<Integer> getFires() {
		return Collections.unmodifiableSet(fires);
	}

	/**
	 * Get the set of refuge locations.
	 * 
	 * @return The set of refuge locations.
	 */
	public Set<Integer> getRefuges() {
		return Collections.unmodifiableSet(refuges);
	}

	/**
	 * Get the set of GasStations locations.
	 * 
	 * @return The set of GasStations locations.
	 */
	public Set<Integer> getGasStations() {
		return Collections.unmodifiableSet(gasStations);
	}

	/**
	 * Get the set of hydrant locations.
	 * 
	 * @return The set of hydrant locations.
	 */
	public Set<Integer> getHydrants() {
		return Collections.unmodifiableSet(hydrants);
	}

	/**
	 * Get the list of civilian locations.
	 * 
	 * @return The list of civilian locations.
	 */
	public Collection<Integer> getCivilians() {
		return Collections.unmodifiableCollection(civLocations);
	}

	/**
	 * Get the list of fire brigade locations.
	 * 
	 * @return The list of fire brigade locations.
	 */
	public Collection<Integer> getFireBrigades() {
		return Collections.unmodifiableCollection(fbLocations);
	}

	/**
	 * Get the list of fire station locations.
	 * 
	 * @return The list of fire station locations.
	 */
	public Collection<Integer> getFireStations() {
		return Collections.unmodifiableCollection(fsLocations);
	}

	/**
	 * Get the list of police force locations.
	 * 
	 * @return The list of police force locations.
	 */
	public Collection<Integer> getPoliceForces() {
		return Collections.unmodifiableCollection(pfLocations);
	}

	/**
	 * Get the list of police office locations.
	 * 
	 * @return The list of police office locations.
	 */
	public Collection<Integer> getPoliceOffices() {
		return Collections.unmodifiableCollection(poLocations);
	}

	/**
	 * Get the list of ambulance team locations.
	 * 
	 * @return The list of ambulance team locations.
	 */
	public Collection<Integer> getAmbulanceTeams() {
		return Collections.unmodifiableCollection(atLocations);
	}

	/**
	 * Get the list of ambulance centre locations.
	 * 
	 * @return The list of ambulance centre locations.
	 */
	public Collection<Integer> getAmbulanceCentres() {
		return Collections.unmodifiableCollection(acLocations);
	}

	/**
	 * Set the set of fire locations.
	 * 
	 * @param newLocations
	 *            The new set of locations.
	 */
	public void setFires(Set<Integer> newLocations) {
		fires.clear();
		fires.addAll(newLocations);
	}

	/**
	 * Set the set of refuge locations.
	 * 
	 * @param newLocations
	 *            The new set of locations.
	 */
	public void setRefuges(Set<Integer> newLocations) {
		refuges.clear();
		refuges.addAll(newLocations);
	}

	/**
	 * Set the list of civilian locations.
	 * 
	 * @param newLocations
	 *            The new list of locations.
	 */
	public void setCivilians(Collection<Integer> newLocations) {
		civLocations.clear();
		civLocations.addAll(newLocations);
	}

	/**
	 * Set the list of fire brigade locations.
	 * 
	 * @param newLocations
	 *            The new list of locations.
	 */
	public void setFireBrigades(Collection<Integer> newLocations) {
		fbLocations.clear();
		fbLocations.addAll(newLocations);
	}

	/**
	 * Set the list of fire station locations.
	 * 
	 * @param newLocations
	 *            The new list of locations.
	 */
	public void setFireStations(Collection<Integer> newLocations) {
		fsLocations.clear();
		fsLocations.addAll(newLocations);
	}

	/**
	 * Set the list of police force locations.
	 * 
	 * @param newLocations
	 *            The new list of locations.
	 */
	public void setPoliceForces(Collection<Integer> newLocations) {
		pfLocations.clear();
		pfLocations.addAll(newLocations);
	}

	/**
	 * Set the list of police office locations.
	 * 
	 * @param newLocations
	 *            The new list of locations.
	 */
	public void setPoliceOffices(Collection<Integer> newLocations) {
		poLocations.clear();
		poLocations.addAll(newLocations);
	}

	/**
	 * Set the list of ambulance team locations.
	 * 
	 * @param newLocations
	 *            The new list of locations.
	 */
	public void setAmbulanceTeams(Collection<Integer> newLocations) {
		atLocations.clear();
		atLocations.addAll(newLocations);
	}

	/**
	 * Set the list of ambulance centre locations.
	 * 
	 * @param newLocations
	 *            The new list of locations.
	 */
	public void setAmbulanceCentres(Collection<Integer> newLocations) {
		acLocations.clear();
		acLocations.addAll(newLocations);
	}

	/**
	 * Add a fire.
	 * 
	 * @param location
	 *            The new fire location.
	 */
	public void addFire(int location) {
		fires.add(location);
	}

	/**
	 * Remove a fire.
	 * 
	 * @param location
	 *            The fire location to remove.
	 */
	public void removeFire(int location) {
		fires.remove(location);
	}

	/**
	 * Add a refuge.
	 * 
	 * @param location
	 *            The new refuge location.
	 */
	public void addRefuge(int location) {
		refuges.add(location);
	}

	/**
	 * Remove a refuge.
	 * 
	 * @param location
	 *            The refuge location to remove.
	 */
	public void removeRefuge(int location) {
		refuges.remove(location);
	}

	/**
	 * Add a hydrant.
	 * 
	 * @param location
	 *            The new hydrant location.
	 */
	public void addHydrant(int location) {
		hydrants.add(location);
	}

	/**
	 * Remove a hydrant.
	 * 
	 * @param location
	 *            The hydrant location to remove.
	 */
	public void removeHydrant(int location) {
		hydrants.remove(location);
	}

	/**
	 * Remove a GasStation.
	 * 
	 * @param location
	 *            The GasStation location to remove.
	 */
	public void removeGasStation(int location) {
		gasStations.remove(location);
	}

	/**
	 * Add a GasStation.
	 * 
	 * @param location
	 *            The new GasStation location.
	 */
	public void addGasStation(int location) {
		gasStations.add(location);
	}

	/**
	 * Add a civilian.
	 * 
	 * @param location
	 *            The new civilian location.
	 */
	public void addCivilian(int location) {
		civLocations.add(location);
	}

	/**
	 * Remove a civilian.
	 * 
	 * @param location
	 *            The civilian location to remove.
	 */
	public void removeCivilian(int location) {
		civLocations.remove(location);
	}

	/**
	 * Add a fire brigade.
	 * 
	 * @param location
	 *            The new fire brigade location.
	 */
	public void addFireBrigade(int location) {
		fbLocations.add(location);
	}

	/**
	 * Remove a fire brigade.
	 * 
	 * @param location
	 *            The fire brigade location to remove.
	 */
	public void removeFireBrigade(int location) {
		fbLocations.remove(location);
	}

	/**
	 * Add a fire station.
	 * 
	 * @param location
	 *            The new fire station location.
	 */
	public void addFireStation(int location) {
		fsLocations.add(location);
	}

	/**
	 * Remove a fire station.
	 * 
	 * @param location
	 *            The fire station location to remove.
	 */
	public void removeFireStation(int location) {
		fsLocations.remove(location);
	}

	/**
	 * Add a police force.
	 * 
	 * @param location
	 *            The new police force location.
	 */
	public void addPoliceForce(int location) {
		pfLocations.add(location);
	}

	/**
	 * Remove a police force.
	 * 
	 * @param location
	 *            The police force location to remove.
	 */
	public void removePoliceForce(int location) {
		pfLocations.remove(location);
	}

	/**
	 * Add a police office.
	 * 
	 * @param location
	 *            The new police office location.
	 */
	public void addPoliceOffice(int location) {
		poLocations.add(location);
	}

	/**
	 * Remove a police office.
	 * 
	 * @param location
	 *            The police office location to remove.
	 */
	public void removePoliceOffice(int location) {
		poLocations.remove(location);
	}

	/**
	 * Add an ambulance team.
	 * 
	 * @param location
	 *            The new ambulance team location.
	 */
	public void addAmbulanceTeam(int location) {
		atLocations.add(location);
	}

	/**
	 * Remove an ambulance team.
	 * 
	 * @param location
	 *            The ambulance team location to remove.
	 */
	public void removeAmbulanceTeam(int location) {
		atLocations.remove(location);
	}

	/**
	 * Add an ambulance centre.
	 * 
	 * @param location
	 *            The new ambulance centre location.
	 */
	public void addAmbulanceCentre(int location) {
		acLocations.add(location);
	}

	/**
	 * Remove an ambulance centre.
	 * 
	 * @param location
	 *            The ambulance centre location to remove.
	 */
	public void removeAmbulanceCentre(int location) {
		acLocations.remove(location);
	}

	private void setupAgent(Human h, EntityID position,
			StandardWorldModel model, Config config) throws ScenarioException {
		Entity areaEntity = model.getEntity(position);
		if (areaEntity == null) {
			throw new ScenarioException("Area " + position + " does not exist");
		}
		if (!(areaEntity instanceof Area)) {
			throw new ScenarioException("Entity " + position
					+ " is not an area: " + areaEntity);
		}
		Area area = (Area) areaEntity;
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
		if (h instanceof FireBrigade) {
			((FireBrigade) h).setWater(config.getIntValue(WATER_QUANTITY_KEY));
		}
		model.addEntity(h);
		Logger.debug("Created " + h);
	}

	@Override
	public HashMap<Integer, Float> getAftershocks() {
		return aftershocks;
	}
}
