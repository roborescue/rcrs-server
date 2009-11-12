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

    /**
       Construct an animated world model viewer.
    */
    public AnimatedWorldModelViewer() {
        super();
        timer = new Timer(FRAME_DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    humans.nextFrame();
                    repaint();
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
        addLayer(new NodeLayer());
        addLayer(new RoadBlockageLayer());
        addLayer(new BuildingIconLayer());
        humans = new AnimatedHumanLayer(FRAME_COUNT);
        addLayer(humans);
        CommandLayer commands = new CommandLayer();
        addLayer(commands);
        commands.setRenderMove(false);
    }
}