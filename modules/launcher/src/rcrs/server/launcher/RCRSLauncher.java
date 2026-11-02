package rcrs.server.launcher;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Cross-platform launcher for precomputation and competition runs.
 *
 * <p>This replaces the orchestration performed by start-precompute.sh and
 * start-comprun.sh without requiring a POSIX shell.</p>
 */
public final class RCRSLauncher {
    private static final long POLL_INTERVAL_MILLIS = 1_000;

    private final List<Process> processes = new ArrayList<>();

    private RCRSLauncher() {
    }

    public static void main(String[] args) {
        int exitCode;
        try {
            exitCode = new RCRSLauncher().run(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            printUsage();
            exitCode = 2;
        } catch (Exception e) {
            System.err.println("Launcher failed: " + e.getMessage());
            e.printStackTrace(System.err);
            exitCode = 1;
        }
        System.exit(exitCode);
    }

    private int run(String[] args) throws Exception {
        LauncherOptions options = LauncherOptions.parse(args);
        if (options.help) {
            printUsage();
            return 0;
        }

        options.resolvePaths();
        validate(options);
        prepareDirectories(options);
        deleteOldLogs(options.logDir, options.mode == RunMode.COMPRUN ? "*.log*" : "*.log");

        Thread shutdownHook = new Thread(this::destroyAll, "rcrs-launcher-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        try {
            startKernel(options);
            if (options.mode == RunMode.COMPRUN) {
                startSimulators(options);
                startViewer(options);
                startViewerEventLogger(options);
            }

            System.out.println("Start your agents");
            waitFor(options.logDir.resolve("kernel.log"), "Kernel has shut down", processes.get(0));
            return 0;
        } finally {
            destroyAll();
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException ignored) {
                // The JVM is already shutting down and the hook is running.
            }
        }
    }

    private void startKernel(LauncherOptions options) throws IOException, InterruptedException {
        List<String> arguments = new ArrayList<>();
        arguments.add("-Xmx" + options.memoryFor(ProcessSpec.KERNEL));
        arguments.add("-cp");
        arguments.add(classpath(options.rootDir, allJars(options.rootDir), allLibs(options.rootDir)));
        arguments.add("-Dlog4j.log.dir=" + options.logDir);
        arguments.add("kernel.StartKernel");
        arguments.add("-c");
        arguments.add(options.configDir.resolve("kernel.cfg").toString());
        arguments.add("--gis.map.dir=" + options.mapDir);
        arguments.add("--kernel.logname=" + options.logDir.resolve("rescue.log.7z"));
        if (options.noGui) {
            arguments.add("--nogui");
        }
        arguments.add("--nomenu");
        if (options.mode == RunMode.COMPRUN) {
            arguments.add("--autorun");
        }

        Process kernel = startProcess(options, "kernel", arguments);
        waitFor(options.logDir.resolve("kernel.log"), "Listening for connections", kernel);
    }

    private void startSimulators(LauncherOptions options) throws IOException, InterruptedException {
        for (ProcessSpec simulator : ProcessSpec.SIMULATORS) {
            Process process = startComponent(options, simulator, List.of());
            if (simulator.waitForConnection()) {
                waitFor(options.logDir.resolve(simulator.outputName() + "-out.log"), "success", process);
            }
        }
    }

    private Process startComponent(LauncherOptions options, ProcessSpec process,
            List<String> extraArguments) throws IOException {
        List<Path> classpathEntries = new ArrayList<>(allLibs(options.rootDir));
        for (String jar : process.jars()) {
            classpathEntries.add(options.rootDir.resolve("jars").resolve(jar));
        }

        List<String> arguments = new ArrayList<>();
        arguments.add("-Xmx" + options.memoryFor(process));
        arguments.add("-cp");
        arguments.add(classpath(options.rootDir, classpathEntries));
        arguments.add("-Dlog4j.log.dir=" + options.logDir);
        arguments.add("rescuecore2.LaunchComponents");
        arguments.add(process.componentClass());
        arguments.add("-c");
        arguments.add(options.configDir.resolve(process.configFile()).toString());
        if (options.noGui && process.supportsNoGui()) {
            arguments.add("--nogui");
        }
        arguments.addAll(extraArguments);
        return startProcess(options, process.outputName(), arguments);
    }

    private void startViewer(LauncherOptions options) throws IOException, InterruptedException {
        if (options.noGui) {
            return;
        }

        List<String> extraArguments = viewerTeamArguments(options);
        extraArguments.add("--viewer.maximise=true");
        Process viewer = startComponent(options, ProcessSpec.VIEWER, extraArguments);
        waitFor(options.logDir.resolve("viewer-out.log"), "success", viewer);
    }

    private void startViewerEventLogger(LauncherOptions options) throws IOException {
        if (!options.jlog) {
            return;
        }

        List<String> extraArguments = viewerTeamArguments(options);
        extraArguments.add("--records.dir=" + options.recordsDir);
        startComponent(options, ProcessSpec.JLOG, extraArguments);
    }

    private static List<String> viewerTeamArguments(LauncherOptions options) {
        List<String> arguments = new ArrayList<>();
        if (!options.team.isEmpty()) {
            arguments.add("--viewer.team-name=" + options.team);
        }
        return arguments;
    }

    private Process startProcess(LauncherOptions options, String name, List<String> arguments) throws IOException {
        List<String> command = new ArrayList<>();
        command.add(javaExecutable());
        command.addAll(arguments);

        System.out.println("Starting " + name + "...");
        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(options.rootDir.resolve("scripts").toFile())
                .redirectErrorStream(true);
        Process process = builder.start();
        processes.add(process);
        pumpOutput(name, process.getInputStream(), options.logDir.resolve(name + "-out.log"));
        return process;
    }

    private static void pumpOutput(String name, InputStream input, Path logFile) throws IOException {
        OutputStream log = Files.newOutputStream(logFile);
        Thread thread = new Thread(() -> {
            try (InputStream source = new BufferedInputStream(input); OutputStream destination = log) {
                byte[] buffer = new byte[8_192];
                int count;
                while ((count = source.read(buffer)) != -1) {
                    synchronized (System.out) {
                        System.out.write(buffer, 0, count);
                        System.out.flush();
                    }
                    destination.write(buffer, 0, count);
                    destination.flush();
                }
            } catch (IOException e) {
                System.err.println("Could not capture output from " + name + ": " + e.getMessage());
            }
        }, "rcrs-output-" + name);
        thread.setDaemon(true);
        thread.start();
    }

    private static void waitFor(Path logFile, String text, Process process) throws InterruptedException, IOException {
        System.out.println("Waiting for '" + logFile + "' to contain '" + text + "'...");
        long position = 0;
        String carry = "";
        int messageCounter = 30;
        while (true) {
            if (Files.exists(logFile)) {
                try (RandomAccessFile file = new RandomAccessFile(logFile.toFile(), "r")) {
                    if (file.length() < position) {
                        position = 0;
                        carry = "";
                    }
                    file.seek(position);
                    byte[] bytes = new byte[(int) Math.min(file.length() - position, 64 * 1024)];
                    while (bytes.length > 0) {
                        int count = file.read(bytes);
                        if (count <= 0) {
                            break;
                        }
                        String chunk = carry + new String(bytes, 0, count, StandardCharsets.UTF_8);
                        if (chunk.contains(text)) {
                            return;
                        }
                        int keep = Math.min(text.length() - 1, chunk.length());
                        carry = chunk.substring(chunk.length() - keep);
                        position += count;
                        bytes = new byte[(int) Math.min(file.length() - position, 64 * 1024)];
                    }
                }
            }

            if (!process.isAlive()) {
                throw new IOException("Process exited with code " + process.exitValue()
                        + " before writing '" + text + "' to " + logFile);
            }
            if (--messageCounter == 0) {
                System.out.println("Still waiting for '" + text + "'...");
                messageCounter = 30;
            }
            Thread.sleep(POLL_INTERVAL_MILLIS);
        }
    }

    private synchronized void destroyAll() {
        for (int i = processes.size() - 1; i >= 0; i--) {
            Process process = processes.get(i);
            if (process.isAlive()) {
                process.destroy();
            }
        }
        for (int i = processes.size() - 1; i >= 0; i--) {
            Process process = processes.get(i);
            if (!process.isAlive()) {
                continue;
            }
            try {
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void validate(LauncherOptions options) {
        requireDirectory(options.rootDir, "RCRS server root");
        requireDirectory(options.rootDir.resolve("scripts"), "scripts");
        requireDirectory(options.rootDir.resolve("jars"), "jars (run ./gradlew completeBuild first)");
        requireDirectory(options.rootDir.resolve("lib"), "lib (run ./gradlew completeBuild first)");
        requireDirectory(options.mapDir, "map");
        requireDirectory(options.configDir, "config");
        if (!Files.isRegularFile(options.configDir.resolve("kernel.cfg"))) {
            throw new IllegalArgumentException("Kernel config does not exist: "
                    + options.configDir.resolve("kernel.cfg"));
        }
    }

    private static void requireDirectory(Path directory, String description) {
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException(description + " directory does not exist: " + directory);
        }
    }

    private static void prepareDirectories(LauncherOptions options) throws IOException {
        Files.createDirectories(options.logDir);
        Files.createDirectories(options.recordsDir);
    }

    private static void deleteOldLogs(Path directory, String glob) throws IOException {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, glob)) {
            for (Path file : files) {
                if (Files.isRegularFile(file)) {
                    Files.delete(file);
                }
            }
        }
    }

    private static List<Path> allJars(Path root) throws IOException {
        return jarFiles(root.resolve("jars"));
    }

    private static List<Path> allLibs(Path root) throws IOException {
        return jarFiles(root.resolve("lib"));
    }

    private static List<Path> jarFiles(Path directory) throws IOException {
        try (var paths = Files.list(directory)) {
            return paths.filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
    }

    @SafeVarargs
    private static String classpath(Path root, List<Path>... groups) {
        List<Path> entries = new ArrayList<>();
        entries.add(root);
        for (List<Path> group : groups) {
            entries.addAll(group);
        }
        return classpath(entries);
    }

    private static String classpath(Path root, List<Path> entries) {
        List<Path> allEntries = new ArrayList<>();
        allEntries.add(root);
        allEntries.addAll(entries);
        return classpath(allEntries);
    }

    private static String classpath(List<Path> entries) {
        return String.join(System.getProperty("path.separator"),
                entries.stream().map(path -> path.toAbsolutePath().normalize().toString()).toList());
    }

    private static String javaExecutable() {
        String executable = isWindows() ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", executable).toString();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar rcrs-server-launcher.jar <precompute|comprun> [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --root <dir>              RCRS server root (normally detected automatically)");
        System.out.println("  -m, --map <dir>           Map directory (default: maps/test/map)");
        System.out.println("  -c, --config <dir>        Config directory (default: maps/test/config)");
        System.out.println("  -l, --log <dir>           Log directory (default: logs/log)");
        System.out.println("  -t, --team <name>         Team name shown by the viewer");
        System.out.println("  -s, --timestamp           Add timestamp, team and map to the log directory");
        System.out.println("  -g, --nogui               Disable GUI components");
        System.out.println("  -j, --jlog                Enable viewer event recording");
        System.out.println("  -r, --jlog-dir <dir>      Event record directory (default: logs/jlog)");
        System.out.println("  --memory <name>=<size>    Set process max heap; repeatable (example: kernel=4g)");
        System.out.println("                            Names: " + ProcessSpec.supportedNames());
        System.out.println("  -h, --help                Show this help");
    }
}
