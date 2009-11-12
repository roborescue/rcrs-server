package rescuecore2.standard.view;

import rescuecore2.view.LayerViewComponent;

import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
   A viewer for StandardWorldModels.
 */
public class AnimatedWorldModelViewer extends LayerViewComponent {
    private static final int FRAME_COUNT = 25;
    private static final int FRAME_DELAY = 1000 / FRAME_COUNT;

    private final Object frameLock = new Object();

    private AnimatedHumanLayer humans;
    private Timer timer;
    private int frame;


    /**
       Construct an animated world model viewer.
    */
    public AnimatedWorldModelViewer() {
        addDefaultLayers();
        timer = new Timer(FRAME_DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    humans.nextFrame();
                    repaint();
                    synchronized (frameLock) {
                        ++frame;
                        if (frame == FRAME_COUNT) {
                            timer.stop();
                        }
                    }
                }
            });
        timer.setRepeats(true);
    }

    @Override
    public String getViewerName() {
        return "Animated world model viewer";
    }

    /**
       Add the default layer set, i.e. nodes, roads, buildings, humans and commands.
    */
    public void addDefaultLayers() {
        addLayer(new NodeLayer());
        addLayer(new RoadLayer());
        addLayer(new BuildingLayer());
        addLayer(new BuildingIconLayer());
        humans = new AnimatedHumanLayer(FRAME_COUNT);
        addLayer(humans);
        addLayer(new CommandLayer());
    }

    @Override
    public void view(Object... objects) {
        super.view(objects);
        synchronized (frameLock) {
            frame = 0;
            timer.start();
        }
    }
}