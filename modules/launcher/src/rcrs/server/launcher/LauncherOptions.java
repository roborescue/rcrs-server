package rcrs.server.launcher;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

final class LauncherOptions {
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    RunMode mode;
    Path rootDir;
    Path mapDir;
    Path configDir;
    Path logDir;
    Path recordsDir;
    String team = "";
    boolean timestamp;
    boolean noGui;
    boolean jlog;
    boolean help;

    private final Map<ProcessSpec, String> memory = defaultMemory();

    private LauncherOptions() {
    }

    static LauncherOptions parse(String[] args) {
        LauncherOptions options = new LauncherOptions();
        if (args.length == 0) {
            throw new IllegalArgumentException("Missing mode: precompute or comprun");
        }
        if (isHelp(args[0])) {
            options.help = true;
            return options;
        }
        options.mode = RunMode.parse(args[0]);

        for (int i = 1; i < args.length; i++) {
            String option = args[i];
            switch (option) {
                case "--root" -> options.rootDir = Path.of(value(args, ++i, option));
                case "-m", "--map" -> options.mapDir = Path.of(value(args, ++i, option));
                case "-c", "--config" -> options.configDir = Path.of(value(args, ++i, option));
                case "-l", "--log" -> options.logDir = Path.of(value(args, ++i, option));
                case "-t", "--team" -> options.team = value(args, ++i, option);
                case "-r", "--jlog-dir" -> options.recordsDir = Path.of(value(args, ++i, option));
                case "--memory" -> options.parseMemory(value(args, ++i, option));
                case "-s", "--timestamp" -> options.timestamp = true;
                case "-g", "--nogui" -> options.noGui = true;
                case "-j", "--jlog" -> options.jlog = true;
                case "-x", "+x" -> {
                    // Accepted for shell-script compatibility; Java does not require xterm.
                }
                case "-h", "--help" -> options.help = true;
                default -> {
                    if (option.startsWith("--memory=")) {
                        options.parseMemory(option.substring("--memory=".length()));
                    } else {
                        throw new IllegalArgumentException("Unknown option: " + option);
                    }
                }
            }
        }
        return options;
    }

    String memoryFor(ProcessSpec process) {
        return memory.get(process);
    }

    void resolvePaths() {
        rootDir = rootDir == null ? discoverRoot() : rootDir.toAbsolutePath().normalize();
        mapDir = resolve(rootDir, mapDir, "maps/test/map");
        configDir = resolve(rootDir, configDir, "maps/test/config");
        logDir = resolve(rootDir, logDir, "logs/log");
        recordsDir = resolve(rootDir, recordsDir, "logs/jlog");

        if (timestamp) {
            String mapName = mapDir.getFileName().toString();
            if ("map".equals(mapName) && mapDir.getParent() != null) {
                mapName = mapDir.getParent().getFileName().toString();
            }
            String suffix = TIMESTAMP.format(LocalDateTime.now()) + "-"
                    + (team.isEmpty() ? "" : team + "-") + mapName;
            logDir = logDir.resolve(suffix);
        }
    }

    private void parseMemory(String specification) {
        int separator = specification.indexOf('=');
        if (separator <= 0 || separator == specification.length() - 1) {
            throw new IllegalArgumentException(
                    "Invalid memory setting '" + specification + "'; expected <name>=<size>");
        }
        String processName = specification.substring(0, separator).toLowerCase(Locale.ROOT);
        String size = specification.substring(separator + 1).toLowerCase(Locale.ROOT);
        ProcessSpec process = ProcessSpec.fromKey(processName);
        if (!size.matches("[1-9][0-9]*[kmg]")) {
            throw new IllegalArgumentException(
                    "Invalid heap size '" + size + "'; use a positive value such as 512m or 4g");
        }
        memory.put(process, size);
    }

    private static Map<ProcessSpec, String> defaultMemory() {
        Map<ProcessSpec, String> result = new EnumMap<>(ProcessSpec.class);
        for (ProcessSpec process : ProcessSpec.values()) {
            result.put(process, process.defaultMemory());
        }
        return result;
    }

    private static boolean isHelp(String argument) {
        return "-h".equals(argument) || "--help".equals(argument);
    }

    private static Path resolve(Path root, Path configured, String defaultPath) {
        Path path = configured == null ? root.resolve(defaultPath) : configured;
        if (!path.isAbsolute()) {
            path = root.resolve(path);
        }
        return path.toAbsolutePath().normalize();
    }

    private static String value(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing value for " + option);
        }
        return args[index];
    }

    private static Path discoverRoot() {
        String configuredRoot = System.getProperty("rcrs.root");
        if (configuredRoot != null && !configuredRoot.isBlank()) {
            return Path.of(configuredRoot).toAbsolutePath().normalize();
        }

        try {
            Path location = Paths.get(RCRSLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path candidate = Files.isRegularFile(location) ? location.getParent() : location;
            if (candidate != null && "jars".equals(candidate.getFileName().toString())) {
                candidate = candidate.getParent();
            }
            if (isRoot(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
        } catch (URISyntaxException | NullPointerException ignored) {
            // Fall back to searching from the current working directory.
        }

        Path candidate = Path.of("").toAbsolutePath().normalize();
        while (candidate != null) {
            if (isRoot(candidate)) {
                return candidate;
            }
            candidate = candidate.getParent();
        }
        throw new IllegalArgumentException("Could not locate the RCRS server root; use --root <directory>");
    }

    private static boolean isRoot(Path candidate) {
        return candidate != null && Files.isDirectory(candidate.resolve("maps"))
                && Files.isDirectory(candidate.resolve("scripts"));
    }
}
