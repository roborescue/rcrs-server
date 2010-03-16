package traffic3;

import java.io.File;
import java.util.Properties;
import java.util.Arrays;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import traffic3.manager.WorldManager;
import traffic3.manager.gui.WorldManagerGUI;

import org.util.xml.io.XMLConfigManager;

public class PlainLaunch {

    public static void start(Properties properties) {
        try {
            Object filepath = (Object)properties.get("plain.setting");
            File file = new File((String)filepath);
            XMLConfigManager config = new XMLConfigManager(file);

            WorldManager manager = new WorldManager();
            WorldManagerGUI gui = new WorldManagerGUI(manager, config);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(gui, BorderLayout.CENTER);
            panel.add(gui.getStatusBar(), BorderLayout.SOUTH);
            javax.swing.JFrame frame = org.util.Handy.showFrame(panel);
            frame.setTitle("Traffic Simulator");
            frame.setJMenuBar(gui.createMenuBar());
            frame.pack();
        }
        catch (Exception e) { 
            e.printStackTrace();
        }
    }

    public static void listDefaultProperties(Properties properties) {
        properties.put("mode", "plain");
        properties.put("plain.setting", "./traffic3-config.xml");
    }
}
