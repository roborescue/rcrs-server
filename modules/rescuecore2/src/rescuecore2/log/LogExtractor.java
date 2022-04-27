package rescuecore2.log;

import static rescuecore2.misc.java.JavaTools.instantiate;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import rescuecore2.Constants;
import rescuecore2.Timestep;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.misc.java.LoadableTypeProcessor;
import rescuecore2.registry.Registry;
import rescuecore2.score.ScoreFunction;
import rescuecore2.view.ViewComponent;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

/**
 * A class for viewing log files.
 */
public class LogExtractor {
	private static final String VIEWERS_KEY = "log.viewers";

	private ScoreFunction scoreFunction;
	private LogReader log;
	private List<ViewComponent> viewers;
	private int maxTime;

	private int current_time;
	WorldModel<? extends Entity> current_model = null;

	/**
	 * Construct a LogViewer.
	 *
	 * @param reader The LogReader to read.
	 * @param config The system configuration.
	 * @throws LogException If there is a problem reading the log.
	 */
	public LogExtractor(LogReader reader, Config config) throws LogException {
		this.log = reader;
		registerViewers(config);
		maxTime = log.getMaxTimestep();
		scoreFunction = makeScoreFunction(config);
		showTimestep(0);
	}

	/**
	 * Show a particular timestep in the viewer.
	 *
	 * @param time The timestep to show. If this value is out of range then this
	 *             method will silently return.
	 */
	public void showTimestep(int time) {
		try {
			if (time < 0 || time > maxTime) {
				return;
			}
			// CommandsRecord commandsRecord = null;//log.getCommands(time);
			// UpdatesRecord updatesRecord = log.getUpdates(time);
			/*
			 * if (updatesRecord != null) {
			 * updates.addAll(updatesRecord.getChangeSet()); }
			 */
			current_model = log.getWorldModel(time);
			/*
			 * for (ViewComponent next : viewers) { next.view(model,
			 * commandsRecord == null ? null : commandsRecord.getCommands(),
			 * updatesRecord == null ? null : updatesRecord.getChangeSet());
			 * next.repaint(); }
			 */
			current_time = time;
		} catch (LogException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	public int getTime() {
		return current_time;
	}

	public boolean step() {
		if (current_time == maxTime) {
			return false;
		}
		showTimestep(current_time + 1);
		return true;
	}

	public void setDimension(int width, int height) {
		for (ViewComponent next : viewers) {
			next.setBounds(0, 0, width, height);
		}
	}

	public double getScore() {
		return scoreFunction.score(current_model, new Timestep(current_time));
	}

	public BufferedImage paintImage() {
		if (viewers.isEmpty()) {
			return null;
		}
		ViewComponent view = viewers.get(0);
		view.view(current_model, null, null);
		// Create the image
		GraphicsConfiguration configuration = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		BufferedImage image = configuration.createCompatibleImage(
				view.getWidth(), view.getHeight(), Transparency.TRANSLUCENT);

		// Render the component onto the image
		Graphics graphics = image.createGraphics();
		view.paint(graphics);
		graphics.dispose();
		return image;
	}

	public void writeImage(String filename) {
		BufferedImage bi = paintImage();
		File outfile = new File(filename);
		try {
			ImageIO.write(bi, "png", outfile);
		} catch (IOException e) {
			System.out.println("Error writing image: " + e.getMessage());
		}
	}

	private void registerViewers(Config config) {
		viewers = new ArrayList<ViewComponent>();
		for (String next : config.getArrayValue(VIEWERS_KEY, "")) {
			ViewComponent viewer = instantiate(next, ViewComponent.class);
			if (viewer != null) {
				viewer.initialise(config);
				viewers.add(viewer);
			}
		}
	}

	private ScoreFunction makeScoreFunction(Config config) {
		String className = config.getValue(Constants.SCORE_FUNCTION_KEY);
		ScoreFunction result = instantiate(className, ScoreFunction.class);
		try {
			WorldModel<? extends Entity> model = log.getWorldModel(0);
			result.initialise(model, config);
		} catch (LogException e) {
			System.out.println("Error: " + e.getMessage());
		}
		return result;
	}

	/**
	 * Launch a new LogViewer.
	 *
	 * @param args Command line arguments. Accepts only one argument: the name
	 *             of a log file.
	 */
	public static void main(String[] args) {
		Config config = new Config();
		try {
			args = CommandLineOptions.processArgs(args, config);
			if (args.length != 2) {
				printUsage();
				return;
			}
			String name = args[0];
			String outdir = args[1];
			processJarFiles(config);
			LogReader reader = RCRSLogFactory.getLogReader(name,
					Registry.SYSTEM_REGISTRY);
			LogExtractor log = new LogExtractor(reader, config);
			log.setDimension(1024, 786);
			// viewer.setPreferredSize(new Dimension(VIEWER_SIZE, VIEWER_SIZE));
			List<Double> scores = new ArrayList<Double>();
			scores.add(log.getScore());
			writeFile(outdir + "/init-score.txt", "" + log.getScore());
			while (log.step()) {
				if (log.getTime() == 1) {
					log.writeImage(outdir + "/snapshot-init.png");
				}
				if (log.getTime() % 50 == 0) {
					log.writeImage(
							outdir + "/snapshot-" + log.getTime() + ".png");
				}
				scores.add(log.getScore());
			}
			log.writeImage(outdir + "/snapshot-final.png");
			writeFile(outdir + "/final-score.txt", "" + log.getScore());

			StringBuffer scoreString = new StringBuffer();
			for (Double score : scores) {
				if (scoreString.length() != 0) {
					scoreString.append(" ");
				}
				scoreString.append(score);
			}
			writeFile(outdir + "/scores.txt", scoreString.toString());
		} catch (IOException e) {
			Logger.error("Error reading log", e);
		} catch (ConfigException e) {
			Logger.error("Configuration error", e);
		} catch (LogException e) {
			Logger.error("Error reading log", e);
		}

		System.exit(0);
	}

	private static void writeFile(String filename, String content) {
		try {
			PrintWriter out = new PrintWriter(
					new BufferedWriter(new FileWriter(filename)));
			out.print(content);
			out.close();
		} catch (IOException e) {
			System.out.println("Error writing file: " + e.getMessage());
		}

	}

	private static void printUsage() {
		System.out.println("Usage: LogExtractor <filename> <output directory>");
	}

	private static void processJarFiles(Config config) throws IOException {
		LoadableTypeProcessor processor = new LoadableTypeProcessor(config);
		processor.addFactoryRegisterCallbacks(Registry.SYSTEM_REGISTRY);
		processor.process();
	}
}
