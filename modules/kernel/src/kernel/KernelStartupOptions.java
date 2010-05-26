package kernel;

import static rescuecore2.misc.java.JavaTools.instantiate;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;

import rescuecore2.misc.Pair;
import rescuecore2.config.Config;
import rescuecore2.components.Component;
import rescuecore2.components.Simulator;
import rescuecore2.components.Viewer;
import rescuecore2.components.Agent;
import rescuecore2.log.Logger;

/**
   Container class for all kernel startup options.
*/
public class KernelStartupOptions {
    private static final String AUTO_SUFFIX = ".auto";

    private List<WorldModelCreator> worldOptions;
    private List<Perception> perceptionOptions;
    private List<CommunicationModel> commsOptions;

    private Map<Simulator, Integer> sims;
    private Map<Viewer, Integer> viewers;
    private Map<Agent, Integer> agents;
    private Map<Component, Integer> other;

    private WorldModelCreator world;
    private Perception perception;
    private CommunicationModel comms;

    /**
       Create a KernelStartupOptions.
       @param config The system configuration.
    */
    public KernelStartupOptions(Config config) {
        Pair<List<WorldModelCreator>, Integer> w = createOptions(config, KernelConstants.GIS_KEY, WorldModelCreator.class);
        worldOptions = w.first();
        world = worldOptions.get(w.second());

        Pair<List<Perception>, Integer> p = createOptions(config, KernelConstants.PERCEPTION_KEY, Perception.class);
        perceptionOptions = p.first();
        perception = perceptionOptions.get(p.second());

        Pair<List<CommunicationModel>, Integer> c = createOptions(config, KernelConstants.COMMUNICATION_MODEL_KEY, CommunicationModel.class);
        commsOptions = c.first();
        comms = commsOptions.get(c.second());

        sims = createComponentOptions(config, KernelConstants.SIMULATORS_KEY, Simulator.class);
        viewers = createComponentOptions(config, KernelConstants.VIEWERS_KEY, Viewer.class);
        agents = createComponentOptions(config, KernelConstants.AGENTS_KEY, Agent.class);
        other = createComponentOptions(config, KernelConstants.COMPONENTS_KEY, Component.class);
    }

    /**
       Get the names of all components that should be started inline.
       @return All inline component class names and the requested number of each.
    */
    public Collection<Pair<String, Integer>> getInlineComponents() {
        List<Pair<String, Integer>> result = new ArrayList<Pair<String, Integer>>();
        for (Map.Entry<Simulator, Integer> next : sims.entrySet()) {
            result.add(new Pair<String, Integer>(next.getKey().getClass().getName(), next.getValue()));
        }
        for (Map.Entry<Viewer, Integer> next : viewers.entrySet()) {
            result.add(new Pair<String, Integer>(next.getKey().getClass().getName(), next.getValue()));
        }
        for (Map.Entry<Agent, Integer> next : agents.entrySet()) {
            result.add(new Pair<String, Integer>(next.getKey().getClass().getName(), next.getValue()));
        }
        for (Map.Entry<Component, Integer> next : other.entrySet()) {
            result.add(new Pair<String, Integer>(next.getKey().getClass().getName(), next.getValue()));
        }
        return result;
    }

    /**
       Get the WorldModelCreator the kernel should use.
       @return The selected WorldModelCreator.
    */
    public WorldModelCreator getWorldModelCreator() {
        return world;
    }

    /**
       Set the WorldModelCreator the kernel should use.
       @param creator The selected WorldModelCreator.
    */
    public void setWorldModelCreator(WorldModelCreator creator) {
        this.world = creator;
    }

    /**
       Get the list of available WorldModelCreator implementations.
       @return All known WorldModelCreators.
    */
    public List<WorldModelCreator> getAvailableWorldModelCreators() {
        return Collections.unmodifiableList(worldOptions);
    }

    /**
       Get the Perception module the kernel should use.
       @return The selected Perception.
    */
    public Perception getPerception() {
        return perception;
    }

    /**
       Set the Perception module the kernel should use.
       @param p The selected Perception.
    */
    public void setPerception(Perception p) {
        perception = p;
    }

    /**
       Get the list of available Perception implementations.
       @return All known Perceptions.
    */
    public List<Perception> getAvailablePerceptions() {
        return Collections.unmodifiableList(perceptionOptions);
    }

    /**
       Get the CommunicationModel the kernel should use.
       @return The selected CommunicationModel.
    */
    public CommunicationModel getCommunicationModel() {
        return comms;
    }

    /**
       Set the CommunicationModel the kernel should use.
       @param c The selected CommunicationModel.
    */
    public void setCommunicationModel(CommunicationModel c) {
        comms = c;
    }

    /**
       Get the list of available CommunicationModel implementations.
       @return All known CommunicationModels.
    */
    public List<CommunicationModel> getAvailableCommunicationModels() {
        return Collections.unmodifiableList(commsOptions);
    }

    /**
       Get the list of available Simulator components.
       @return All known Simulators.
    */
    public Collection<Simulator> getAvailableSimulators() {
        return Collections.unmodifiableSet(sims.keySet());
    }

    /**
       Get the list of available Viewer components.
       @return All known Viewers.
    */
    public Collection<Viewer> getAvailableViewers() {
        return Collections.unmodifiableSet(viewers.keySet());
    }

    /**
       Get the list of available Agent components.
       @return All known Agents.
    */
    public Collection<Agent> getAvailableAgents() {
        return Collections.unmodifiableSet(agents.keySet());
    }

    /**
       Get the list of available components that are not simulators, viewers or agents.
       @return All known Components that are not simulators, viewers or agents.
    */
    public Collection<Component> getAvailableComponents() {
        return Collections.unmodifiableSet(other.keySet());
    }

    /**
       Get the number of instances of a type of component to start.
       @param c The component type.
       @return The number of instances to start.
    */
    public int getInstanceCount(Component c) {
        if (sims.containsKey(c)) {
            return sims.get(c);
        }
        if (viewers.containsKey(c)) {
            return viewers.get(c);
        }
        if (agents.containsKey(c)) {
            return agents.get(c);
        }
        if (other.containsKey(c)) {
            return other.get(c);
        }
        throw new IllegalArgumentException("Component " + c + " not recognised");
    }

    /**
       Set the number of instances of a type of component to start.
       @param c The component type.
       @param count The number of instances to start.
    */
    public void setInstanceCount(Component c, int count) {
        if (c instanceof Simulator) {
            sims.put((Simulator)c, count);
        }
        else if (c instanceof Viewer) {
            viewers.put((Viewer)c, count);
        }
        else if (c instanceof Agent) {
            agents.put((Agent)c, count);
        }
        else {
            other.put(c, count);
        }
    }

    private <T> Pair<List<T>, Integer> createOptions(Config config, String key, Class<T> expectedClass) {
        List<T> instances = new ArrayList<T>();
        int index = 0;
        int selectedIndex = 0;
        Logger.trace("Loading options: " + key);
        List<String> classNames = config.getArrayValue(key);
        String auto = config.getValue(key + AUTO_SUFFIX, null);
        boolean autoFound = false;
        for (String next : classNames) {
            Logger.trace("Option found: '" + next + "'");
            T t = instantiate(next, expectedClass);
            if (t != null) {
                instances.add(t);
                if (next.equals(auto)) {
                    selectedIndex = index;
                    autoFound = true;
                }
                ++index;
            }
        }
        if (auto != null && !autoFound) {
            Logger.warn("Could not find class " + auto + " in config key " + key + ". Values found: " + classNames);
        }
        return new Pair<List<T>, Integer>(instances, selectedIndex);
    }

    private <T> Map<T, Integer> createComponentOptions(Config config, String key, Class<T> expectedClass) {
        Logger.trace("Loading component options: " + key);
        Map<T, Integer> result = new HashMap<T, Integer>();
        List<String> classNames = config.getArrayValue(key, "");
        List<String> autoClassNames = config.getArrayValue(key + AUTO_SUFFIX, "");
        Set<String> allClassNames = new HashSet<String>(classNames);
        allClassNames.addAll(strip(autoClassNames));
        for (String next : allClassNames) {
            Logger.trace("Option found: '" + next + "'");
            T t = instantiate(next, expectedClass);
            if (t != null) {
                int count = getStartCount(next, autoClassNames);
                result.put(t, count);
            }
        }
        return result;
    }

    private int getStartCount(String className, List<String> auto) {
        for (String next : auto) {
            if (next.startsWith(className)) {
                int index = next.indexOf("*");
                if (index == -1) {
                    return 1;
                }
                String arg = next.substring(index + 1);
                if ("n".equals(arg)) {
                    return Integer.MAX_VALUE;
                }
                return Integer.parseInt(arg);
            }
        }
        return 0;
    }

    private List<String> strip(List<String> autoClassNames) {
        List<String> result = new ArrayList<String>(autoClassNames.size());
        // Remove any trailing *n
        for (String s : autoClassNames) {
            int index = s.indexOf("*");
            if (index != -1) {
                result.add(s.substring(0, index));
            }
            else {
                result.add(s);
            }
        }
        return result;
    }
}
