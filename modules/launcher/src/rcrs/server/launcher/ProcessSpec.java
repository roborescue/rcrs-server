package rcrs.server.launcher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

enum ProcessSpec {
    KERNEL("kernel", "kernel", "2048m", null, null, List.of(), false, false),
    MISC("misc", "misc", "512m", "misc.MiscSimulator", "misc.cfg",
            List.of("rescuecore2.jar", "standard.jar", "misc.jar"), true, true),
    TRAFFIC("traffic", "traffic", "1024m", "traffic3.simulator.TrafficSimulator", "traffic3.cfg",
            List.of("rescuecore2.jar", "standard.jar", "traffic3.jar"), true, true),
    COLLAPSE("collapse", "collapse", "512m", "collapse.CollapseSimulator", "collapse.cfg",
            List.of("rescuecore2.jar", "standard.jar", "collapse.jar"), true, true),
    CLEAR("clear", "clear", "512m", "clear.ClearSimulator", "clear.cfg",
            List.of("rescuecore2.jar", "standard.jar", "clear.jar"), true, true),
    CIVILIAN("civilian", "civilian", "1512m", "sample.SampleCivilian*n", "civilian.cfg",
            List.of("rescuecore2.jar", "standard.jar", "sample.jar", "kernel.jar"), false, false),
    VIEWER("viewer", "viewer", "512m", "sample.SampleViewer", "viewer.cfg",
            List.of("rescuecore2.jar", "standard.jar", "sample.jar"), false, true),
    JLOG("jlog", "viewer-event-logger", "512m", "sample.SampleViewerEventLogger", "viewer.cfg",
            List.of("rescuecore2.jar", "standard.jar", "sample.jar"), false, false);

    static final List<ProcessSpec> SIMULATORS = List.of(MISC, TRAFFIC, COLLAPSE, CLEAR, CIVILIAN);

    private final String key;
    private final String outputName;
    private final String defaultMemory;
    private final String componentClass;
    private final String configFile;
    private final List<String> jars;
    private final boolean supportsNoGui;
    private final boolean waitForConnection;

    ProcessSpec(String key, String outputName, String defaultMemory, String componentClass,
            String configFile, List<String> jars, boolean supportsNoGui, boolean waitForConnection) {
        this.key = key;
        this.outputName = outputName;
        this.defaultMemory = defaultMemory;
        this.componentClass = componentClass;
        this.configFile = configFile;
        this.jars = jars;
        this.supportsNoGui = supportsNoGui;
        this.waitForConnection = waitForConnection;
    }

    String key() {
        return key;
    }

    String outputName() {
        return outputName;
    }

    String defaultMemory() {
        return defaultMemory;
    }

    String componentClass() {
        return componentClass;
    }

    String configFile() {
        return configFile;
    }

    List<String> jars() {
        return jars;
    }

    boolean supportsNoGui() {
        return supportsNoGui;
    }

    boolean waitForConnection() {
        return waitForConnection;
    }

    static ProcessSpec fromKey(String key) {
        return Arrays.stream(values())
                .filter(process -> process.key.equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown process in memory setting: " + key));
    }

    static String supportedNames() {
        return Arrays.stream(values()).map(ProcessSpec::key).collect(Collectors.joining(", "));
    }
}
