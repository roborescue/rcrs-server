package firesimulator.gui;

/**
 *
 * Created by Alireza Kandeh on March 2018
 */

public abstract class GUILayer {
    
    abstract public void paint(PaintEvent paintEvent);
    
    public String getString(PaintEvent paintEvent) {
        return null;
    }
    
}
