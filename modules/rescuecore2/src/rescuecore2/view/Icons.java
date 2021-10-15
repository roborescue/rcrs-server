package rescuecore2.view;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import rescuecore2.log.Logger;

/**
   Some common icons.
*/
public final class Icons {
    /** A tick icon. */
    public static final Icon TICK = Icons.get(Icons.class,"rescuecore2/view/tick.png");

    /** A cross icon. */
    public static final Icon CROSS = Icons.get(Icons.class,"rescuecore2/view/cross.png");

    private Icons() {}
    
    public static ImageIcon get(Class<?> clas, String path) {
    	try {
    		URL iconPath = clas.getClassLoader().getResource(path);
        	return new ImageIcon(iconPath);
        	}catch(Exception e) {
        		Logger.error(clas+" : " + path +" not find");
        		return new ImageIcon();
        	}
	}

}
