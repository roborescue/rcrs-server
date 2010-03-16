package rescuecore2.view;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
   Some common icons.
*/
public final class Icons {
    /** A tick icon. */
    public static final Icon TICK = new ImageIcon(Icons.class.getClassLoader().getResource("rescuecore2/view/tick.png"));

    /** A cross icon. */
    public static final Icon CROSS = new ImageIcon(Icons.class.getClassLoader().getResource("rescuecore2/view/cross.png"));

    private Icons() {}
}
