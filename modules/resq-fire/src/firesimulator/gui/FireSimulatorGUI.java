package firesimulator.gui;

import firesimulator.simulator.Simulator;
import firesimulator.world.World;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.text.StyleConstants;
import firesimulator.world.Building;
import java.awt.Polygon;
import java.util.Iterator;
import rescuecore2.misc.gui.PanZoomListener;
import firesimulator.gui.layers.*;
import javax.swing.SwingUtilities;
/**
 *
 * Created by Alireza Kandeh on March 2018
 */

public class FireSimulatorGUI extends JPanel {
	
	private ScreenTransformExt transform = null;
	private ArrayList<JCheckBox> layerCheckBoxes = null;
	private JTextArea stringDataTextArea = null;
	private Simulator simulator = null;
	private World world = null;
	private Building selectedBuilding = null;
	private HashMap<String, JPanel> tabPanels = new HashMap<>();
	private JTabbedPane tabbedPane = new JTabbedPane();
	private DrawingPanel drawingPanel = null;
	private int mouseX = 0;
	private int mouseY = 0;
	
	private final ItemListener layerCheckBoxItemListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			refresh();
		}
	};
	
	public final static String TAB_ALL = "All";
	
	private void addLayers() {
		addLayer(TAB_ALL, Buildings.class, true);
		addLayer(TAB_ALL, BuildingInfo.class, true);
		addLayer(TAB_ALL, Fieryness.class, true);
		addLayer(TAB_ALL, AllConnectionsWeighted.class, false);
		addLayer(TAB_ALL, AllConnections.class, false);
		addLayer(TAB_ALL, AirCells.class, true);
		addLayer(TAB_ALL, BuildingAirCells.class, true);
		addLayer(TAB_ALL, ConnectedBuildings.class, false);
		addLayer(TAB_ALL, ConnectedBuildingsWeighted.class, true);
	}
	
	public void addLayer(String tabName, Class c, boolean de) {
		String packageName = c.getPackage().getName();
		String className = c.getName().replace(packageName + ".", "");
		GUILayerFactory.getInstance().addLayer(className, c);
		JPanel panel = tabPanels.get(tabName);
		if (panel == null) {
			panel = new JPanel();
			BoxLayout bxl = new BoxLayout(panel, BoxLayout.Y_AXIS);
			panel.setLayout(bxl);
			tabbedPane.addTab(tabName, panel);
			tabPanels.put(tabName, panel);
		}
		JCheckBox chb = new JCheckBox(className, de);
		chb.addItemListener(layerCheckBoxItemListener);
		panel.add(chb);
		layerCheckBoxes.add(chb);
	}
	
	public FireSimulatorGUI(Simulator simulator, World world) {
		this.simulator = simulator;
		this.world = world;
		this.drawingPanel = new DrawingPanel();
		this.layerCheckBoxes = new ArrayList<>();
		this.stringDataTextArea = new JTextArea();

		JScrollPane layersScrollPane = new JScrollPane(tabbedPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		layersScrollPane.setPreferredSize(new Dimension(300, 300));
		JScrollPane stringDataScrollPane = new JScrollPane(stringDataTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		stringDataScrollPane.setPreferredSize(new Dimension(300, 300));
		
		this.setLayout(new BorderLayout());
		this.add(layersScrollPane, BorderLayout.WEST);
		this.add(drawingPanel, BorderLayout.CENTER);
		this.add(stringDataScrollPane, BorderLayout.EAST);
		
		addLayers();
		
	}
	
	public void refresh() {
		repaint();
		try {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					updateStringData();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	class DrawingPanel extends JPanel {

		private final int DEFAULT_WIDTH = 480;
		private final int DEFAULT_HEIGHT = 480;

		public DrawingPanel() {
			setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
			transform = new ScreenTransformExt(world.getMinX(), world.getMinY(), world.getMaxX(), world.getMaxY());
			this.setLayout(new FlowLayout(StyleConstants.ALIGN_LEFT));
			new PanZoomListener(DrawingPanel.this).setScreenTransform(transform);
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					mouseX = (int) transform.screenToX((int) (e.getX()));
					mouseY = (int) transform.screenToY((int) (e.getY()));
					selectedBuilding = null;
					for (Iterator i = world.getBuildings().iterator(); i.hasNext();) {
						Building b = (Building) i.next();
						Polygon polygon = new Polygon();
						int apexes[] = b.getApexes();
						for (int n = 0; n < apexes.length; n++) {
							int x_ = apexes[n];
							int y_ = apexes[++n];
							polygon.addPoint(x_, y_);
						}
						if (polygon.contains(mouseX, mouseY)) {
							selectedBuilding = b;
						}
					}
					refresh();
				}
			});
		}

		@Override
		public void paintComponent(Graphics g) {
			transform.rescale(getWidth(), getHeight());
			Graphics2D g2 = (Graphics2D) g;
			g.setColor(new Color(170, 170, 170));
			g.fillRect(0, 0, getWidth(), getHeight());
			PaintEvent paintEvent = new PaintEvent(g2, transform, simulator, world, selectedBuilding, mouseX, mouseY);
			for (JCheckBox chb : layerCheckBoxes) {
				if (chb.isSelected()) {
					GUILayerFactory.getInstance().getLayer(chb.getText()).paint(paintEvent);
				}
			}
		}
	}

	private void updateStringData() {
		String str = "";
		for (JCheckBox chb : layerCheckBoxes) {
			if (chb.isSelected()) {
				PaintEvent paintEvent = new PaintEvent(null, null, simulator, world, selectedBuilding, mouseX, mouseY);
				String s = GUILayerFactory.getInstance().getLayer(chb.getText()).getString(paintEvent);
				if (s != null) {
					str += chb.getText() + ": ";
					str += "\n";
					str += s;
					str += "\n";
					str += "\n";
				}
			}
		}
		stringDataTextArea.setText(str);
	}

}
