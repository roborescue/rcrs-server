package maps.gml.editor;

import maps.gml.GMLNode;
import maps.gml.GMLCoordinates;

import java.util.Random;

import org.uncommons.maths.random.DiscreteUniformGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

/**
   A function for adding noise to node coordinates.
*/
public class AddNoiseFunction extends ProgressFunction {
    private static final int RANGE = 5;
    private static final double FACTOR = 0.001;

    private Random random;

    /**
       Construct an AddNoiseFunction.
       @param editor The editor instance.
    */
    public AddNoiseFunction(GMLEditor editor) {
        super(editor);
        random = new MersenneTwisterRNG();
    }

    @Override
    public String getName() {
        return "Add noise";
    }

    @Override
    protected String getTitle() {
        return "Adding noise";
    }

    @Override
    protected void executeImpl() {
        DiscreteUniformGenerator generator = new DiscreteUniformGenerator(-RANGE, RANGE, random);
        setProgressLimit(editor.getMap().getNodes().size());
        for (GMLNode next : editor.getMap().getNodes()) {
            GMLCoordinates c = next.getCoordinates();
            c.setX(c.getX() + (generator.nextValue() * FACTOR));
            c.setY(c.getY() + (generator.nextValue() * FACTOR));
            bumpProgress();
        }
        editor.setChanged();
        editor.getViewer().repaint();
    }
}