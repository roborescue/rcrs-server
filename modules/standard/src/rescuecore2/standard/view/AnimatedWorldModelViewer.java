package rescuecore2.standard.view;

import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
   A viewer for StandardWorldModels.
 */
public class AnimatedWorldModelViewer extends StandardWorldModelViewer {
    private static final int FRAME_COUNT = 10;
    private static final int ANIMATION_TIME = 750;
    private static final int FRAME_DELAY = ANIMATION_TIME / FRAME_COUNT;

    private AnimatedHumanLayer humans;
    private Timer timer;
    private final Object lock = new Object();
    private boolean done;

    /**
       Construct an animated world model viewer.
    */
    public AnimatedWorldModelViewer() {
        super();
        timer = new Timer(FRAME_DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    synchronized (lock) {
                        if (done) {
                            return;
                        }
                        done = true;
                        if (humans.nextFrame()) {
                            done = false;
                            repaint();
                        }
                    }
                }
            });
        timer.setRepeats(true);
        timer.start();
    }

    @Override
    public String getViewerName() {
        return "Animated world model viewer";
    }

    @Override
    public void addDefaultLayers() {
        addLayer(new BuildingLayer());
        addLayer(new RoadLayer());
        addLayer(new AreaNeighboursLayer());
        addLayer(new RoadBlockageLayer());
        addLayer(new AreaIconLayer());
        humans = new AnimatedHumanLayer();
        addLayer(humans);
        CommandLayer commands = new CommandLayer();
        addLayer(commands);
        commands.setRenderMove(false);
        addLayer(new PositionHistoryLayer());
    }

    @Override
    public void view(Object... objects) {
        super.view(objects);
        synchronized (lock) {
            done = false;
            humans.computeAnimation(FRAME_COUNT);
        }
    }
}
