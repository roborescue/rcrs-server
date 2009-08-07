package traffic3.manager.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Font;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.TransferHandler;
import javax.swing.Icon;
import javax.swing.ButtonGroup;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;
import java.awt.datatransfer.UnsupportedFlavorException;

import traffic3.manager.WorldManager;
import traffic3.manager.WorldManagerListener;
import traffic3.manager.WorldManagerEvent;
import traffic3.manager.WorldManagerException;
import traffic3.manager.gui.action.TrafficAction;

import traffic3.objects.TrafficObject;
import traffic3.objects.TrafficAgent;
import traffic3.objects.TrafficBlockade;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaEdge;
import traffic3.objects.area.TrafficAreaNode;
import traffic3.io.Parser;
import traffic3.io.ParserNotFoundException;
import static traffic3.log.Logger.log;
import static traffic3.log.Logger.alert;
import org.util.capture.ImageOutputTool;
import org.util.capture.ImageOutputCountExceededLimitException;
import org.util.property.Value;
import org.util.property.ValueListener;
import org.util.property.ValueEditor;
import org.util.property.BooleanValue;

import org.util.xml.io.XMLConfigManager;
import org.util.xml.io.XMLIO;
import org.util.xml.parse.XMLParseException;
import org.util.xml.element.TagElement;

/**
 *
 */
public class WorldManagerGUI extends JComponent {

    /**
     *
     */
    public static final int IMAGE_UPDATE_RATE_DEFAULT = 30;

    /**
     *
     */
    public static final int IMAGE_UPDATE_RATE_SLIDER_MIN = -100;

    /**
     *
     */
    public static final int IMAGE_UPDATE_RATE_SLIDER_MAX = 100;

    /**
     *
     */
    public static final float STROKE_WIDTH_DEFAULT = 3f;

    /**
     *
     */
    public static final Color SELECTED_OBJECT_COLOR_DEFAULT = new Color(255, 100, 100);

    /**
     *
     */
    public static final Color AREA_CONNECTOR_COLOR_DEFAULT = new Color(255, 150, 150);

    /**
     *
     */
    public static final Color AREA_NODE_COLOR_DEFAULT = new Color(0, 0, 0);

    /**
     *
     */
    public static final Color AREA_EDGE_COLOR_DEFAULT = new Color(50, 50, 50);

    /**
     *
     */
    public static final Color AREA_COLOR_DEFAULT = new Color(255, 200, 200);

    /**
     *
     */
    public static final Color NETWORK_EDGE_COLOR_DEFAULT = new Color(100, 255, 100);

    /**
     *
     */
    public static final Color NETWORK_NODE_COLOR_DEFAULT = new Color(0, 100, 0);


    /**
     *
     */
    public static final double AGENT_RADIUS_DEFAULT = 200;

    /**
     *
     */
    public static final double AGENT_VELOCITY_DEFAULT = 0.7;

    /**
     *
     */
    public static final double BLOCKADE_WIDTH_MEAN_DEFAULT = 0.8;

    /**
     *
     */
    public static final double BLOCKADE_WIDTH_VALIATY_DEFAULT = 0.2;

    /**
     *
     */
    public static final double UNIT_PERCENT = 100;

    /**
     *
     */
    public static final double BLOCKADE_SEPARATE_WIDTH = 5000;

    /**
     *
     */
    public static final double BLOCKADE_SEPARATE_HEIGHT = 5000;


    /**
     *
     */
    private static final int MOUSE_STATE_LABEL_FONT_SIZE_DEFAULT = 12;

    private static final Map<String, Value> RENDERING_SETTINGS = new HashMap<String, Value>();

    /**
     *
     */

    private static final double VIEW_ZOOM_DEFAULT = 40;
    private static final double MOUSE_WHEEL_ZOOM_IN_SPEED = 1.1;
    private static final double MOUSE_WHEEL_ZOOM_OUT_SPEED = 0.9;
    private static final double SIMULATION_TIME_STEP_DEFAULT = 100;
    private static final double MOUSE_CLICK_NODE_DISTANCE = 3.0;
    private static final double MOUSE_CLICK_EDGE_DISTANCE = 2.0;
    private static final int OPEN_ANIMATION_STEP = 100;
    private static final int DRAW_STRING_MARGIN = 2;
    private static final int DRAW_STRING_LINE_HEIGHT = 10;
    private static final int DRAW_SIMULATION_TIME_X = 30;
    private static final int DRAW_SIMULATION_TIME_Y = 20;
    private static final int DRAW_REAL_TIME_X = 230;
    private static final int DRAW_REAL_TIME_Y = 20;
    private static final int DRAW_AGENT_RADIUS = 4;
    private static final double DRAW_AGENT_F_SCALE = 1000000.0;
    private static final double DRAW_AGENT_V_SCALE = 1000.0;

    // for video
    private File videoRecodeDirectory = new File("./TrafficSimulatorLogVideo").getAbsoluteFile();
    private String videoFormat = "png";
    private final int videoFrameLimit = 10000;
    private long videoStart;
    private final int videoSkip = 100;
    private int videoSkipFrameCounter = 0;

    private volatile boolean createImageDrawing = false;
    private volatile boolean createImageCancelDrawing = false;
    private volatile AffineTransform createImageTransform;
    private BufferedImage agentLayerImageBuf;
    private Action getShowTargetsActionBuf;

    /**
     * creating image thread.
     */
    private Thread createImageThread = null;

    /**
     *
     */
    private WorldManagerGUI thisObject = this;

    /**
     *
     */
    private WorldManager worldManager;

    /**
     *
     */
    private BufferedImage offImage;

    /**
     *
     */
    private BufferedImage agentLayerImage;

    /**
     *
     */
    private BufferedImage recodeBufImage;

    /**
     *
     */
    private Map<String, List<TrafficAgent>> agentGroupList = new HashMap<String, List<TrafficAgent>>();


    /**
     *
     */
    private List<String> selectedAgentGroupList = new ArrayList<String>();

    /**
     *
     */
    final private Map<String, TrafficObject> targetList = new HashMap<String, TrafficObject>();
    final private List<TrafficObject> orderedTargetList = new ArrayList<TrafficObject>();

    //private List<TrafficObject> destination_list_ = new ArrayList<TrafficObject>();

    /**
     *
     */
    private double viewZoom;

    /**
     *
     */
    private double viewOffsetX;

    /**
     *
     */
    private double viweOffsetY;

    /**
     *
     */
    private volatile boolean mouseDragging;

    /**
     *
     */
    private BooleanValue showAreaValue;

    /**
     *
     */
    private BooleanValue showAreaEdgeValue;

    /**
     *
     */
    private BooleanValue showAreaNodeValue;

    /**
     *
     */
    private BooleanValue showAreaConnectorValue;

    /**
     *
     */
    private BooleanValue showAreaNodeIDValue;

    /**
     *
     */
    private BooleanValue antialiasingValue;

    /**
     *
     */
    private BooleanValue simulatingValue;


    /**
     *
     */
    private Color selectedObjectColor;

    /**
     *
     */
    private Color areaNodeColor;

    /**
     *
     */
    private Color areaEdgeColor;

    /**
     *
     */
    private Color areaColor;

    /**
     *
     */
    private Color networkEdgeColor;

    /**
     *
     */
    private Color networkNodeColor;

    /**
     *
     */
    private Color areaConnectorColor;



    /**
     *
     */
    private int pressedMousePressedButtonIndex;

    /**
     *
     */
    private Stroke areaEdgeStroke;

    /**
     *
     */
    private Stroke areaConnectorEdgeStroke;

    /**
     *
     */
    private Stroke networkEdgeStroke;

    /**
     *
     */
    private JRadioButton isSelectMode;

    /**
     *
     */
    private JRadioButton isInputAreaMode;

    /**
     *
     */
    private JRadioButton isInputNetworkMode;

    /**
     *
     */
    private JRadioButton isPutAgentMode;

    /**
     *
     */
    //private JCheckBox isSimulating;

    /**
     *
     */
    private JLabel statusLabel;

    /**
     *
     */
    private JLabel mouseStatusLabel;

    /**
     *
     */
    private volatile int imageUpdateTerm;

    /**
     *
     */
    private JSlider imageUpdateRateSlider;

    /**
     *
     */
    private JLabel imageUpdateRateLabel;

    private Point2D mousePoint = null;
    private Point2D mouseDraggingPoint = null;
    private Rectangle2D mouseRectangle = null;
    final private Map<Integer, TrafficObject> preSelectionList = new HashMap<Integer, TrafficObject>();

    /**
     *
     */
    //private JCheckBox agentDirectRenderingMode;

    /**
     *
     */
    private long lastUpdateAgentLayerTime = -1;

    private double simulationTime = -1;
    private long simulationStartWallclockTime = -1;
    private volatile boolean isSimulationRunning;

    private ImageOutputTool imageOutputTool;

    private XMLConfigManager configManager;

    /**
     * Constructor.
     * @param worldManager WorldManager
     * @param config config
     */
    public WorldManagerGUI(WorldManager worldManager, XMLConfigManager config) {
        imageUpdateTerm = IMAGE_UPDATE_RATE_DEFAULT;
        this.worldManager = worldManager;
        configManager = config;
        worldManager.addWorldManagerListener(new WorldManagerListener() {
                @Override
                public void added(WorldManagerEvent e) {
                    createImageInOtherThread();
                }
                @Override
                public void removed(WorldManagerEvent e) {
                    targetList.clear();
                    orderedTargetList.clear();
                    createImageInOtherThread();
                }
                @Override
                public void changed(WorldManagerEvent e) {
                }
                @Override
                public void mapUpdated(WorldManagerEvent e) {
                    createImageInOtherThread();
                }
                @Override
                public void agentUpdated(WorldManagerEvent e) {
                    /*
                    if (e.getSource() instanceof RCRSTrafficSimulator) {
                        simulationTime = ((RCRSTrafficSimulator)e.getSource()).getTime();
                    }
                    */
                    requestToUpdateImage();
                }
                @Override
                public void inputted(WorldManagerEvent e) {
                    fitView();
                    createImageInOtherThread();
                }
            });
        createGUI();
        log("world manager is initialized");
    }

    private void waitFor(int time) {
        try {
            Thread.sleep(time);
        }
        catch (InterruptedException exc) {
            exc.printStackTrace();
        }
    }

    private TagElement getConfigTag(String path) {
        TagElement guiConfigPopup = configManager.getTag(path);
        if (guiConfigPopup.getTagChildren() == null) {
            try {
                //java.io.InputStream in = this.getClass().getResourceAsStream("default-popup.xml");
                java.io.InputStream in = org.util.Handy.getResourceAsStream("data/resources/default-popup.xml");
                TagElement[] defaultTags = XMLIO.read(in).getTagChildren();
                guiConfigPopup.setChildren(defaultTags);
                configManager.outputSetting();
                guiConfigPopup = configManager.getTag("gui/popup");
            }
            catch (NullPointerException e) {
                alert(e, "error");
            }
            catch (XMLParseException e) {
                alert(e, "error");
            }
            catch (IOException e) {
                alert(e, "error");
            }
        }
        return guiConfigPopup;
    }

    /**
     *
     */
    public void createGUI() {
        TagElement guiConfigPopup = getConfigTag("gui/popup");
        Action createImageAction = new AbstractAction("create image action") {
                public void actionPerformed(ActionEvent e) {
                    createImageInOtherThread();
                }
            };
        ValueListener vlistener = new ValueListener() {
                public void valueChanged(Value v) {
                    createImageInOtherThread();
                }
            };
        antialiasingValue = new BooleanValue("antiariasing", false);
        showAreaValue = new BooleanValue("showArea", true);
        showAreaEdgeValue = new BooleanValue("showAreaEdge", false);
        showAreaNodeValue = new BooleanValue("showAreaNode", false);
        showAreaConnectorValue = new BooleanValue("showAreaConnector", false);
        showAreaNodeIDValue = new BooleanValue("showAreaNodeID", false);

        antialiasingValue.addValueListener(vlistener);
        showAreaValue.addValueListener(vlistener);
        showAreaEdgeValue.addValueListener(vlistener);
        showAreaNodeValue.addValueListener(vlistener);
        showAreaConnectorValue.addValueListener(vlistener);
        showAreaNodeIDValue.addValueListener(vlistener);

        RENDERING_SETTINGS.put(antialiasingValue.getKey(), antialiasingValue);
        RENDERING_SETTINGS.put(showAreaValue.getKey(), showAreaValue);
        RENDERING_SETTINGS.put(showAreaEdgeValue.getKey(), showAreaEdgeValue);
        RENDERING_SETTINGS.put(showAreaNodeValue.getKey(), showAreaNodeValue);
        RENDERING_SETTINGS.put(showAreaConnectorValue.getKey(), showAreaConnectorValue);
        RENDERING_SETTINGS.put(showAreaNodeIDValue.getKey(), showAreaNodeIDValue);

        imageUpdateRateSlider = new JSlider(IMAGE_UPDATE_RATE_SLIDER_MIN, IMAGE_UPDATE_RATE_SLIDER_MAX, IMAGE_UPDATE_RATE_DEFAULT);
        imageUpdateRateLabel = new JLabel("30");

        simulatingValue = new BooleanValue("simulating", true);
        RENDERING_SETTINGS.put(simulatingValue.getKey(), simulatingValue);
        simulatingValue.addValueListener(new ValueListener() {
                public void valueChanged(Value v) {
                    new Thread(new Runnable() {
                            public void run() {
                                if (simulatingValue.getValue().booleanValue()) {
                                    startSimulation();
                                }
                                else {
                                    stopSimulation();
                                }
                            }
                        }, "simulation thread").start();
                }
            });

        imageUpdateRateSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    imageUpdateTerm = imageUpdateRateSlider.getValue();
                    imageUpdateRateLabel.setText(String.valueOf(imageUpdateTerm));
                }
            });
        //imageUpdateRateSlider.setPaintLabels(true);
        //imageUpdateRateSlider.setMajorTickSpacing(50);
        //imageUpdateRateSlider.setMinorTickSpacing(10);

        areaConnectorEdgeStroke = new BasicStroke(STROKE_WIDTH_DEFAULT);
        areaEdgeStroke = new BasicStroke(STROKE_WIDTH_DEFAULT);
        networkEdgeStroke = new BasicStroke(STROKE_WIDTH_DEFAULT);

        selectedObjectColor = SELECTED_OBJECT_COLOR_DEFAULT;
        areaConnectorColor = AREA_CONNECTOR_COLOR_DEFAULT;
        areaNodeColor = AREA_NODE_COLOR_DEFAULT;
        areaEdgeColor = AREA_EDGE_COLOR_DEFAULT;
        areaColor = AREA_COLOR_DEFAULT;
        networkEdgeColor = NETWORK_EDGE_COLOR_DEFAULT;
        networkNodeColor = NETWORK_NODE_COLOR_DEFAULT;

        isSelectMode = new JRadioButton("select mode");
        isInputAreaMode = new JRadioButton("input area mode");
        isInputNetworkMode = new JRadioButton("input network mode");
        isPutAgentMode = new JRadioButton("put agent mode");
        ButtonGroup bg = new ButtonGroup();
        bg.add(isSelectMode);
        bg.add(isInputAreaMode);
        bg.add(isInputNetworkMode);
        bg.add(isPutAgentMode);
        isSelectMode.setSelected(true);


        statusLabel = new JLabel("status");
        mouseStatusLabel = new JLabel("X, Y");
        mouseStatusLabel.setFont(new Font("Monospaced", Font.PLAIN, MOUSE_STATE_LABEL_FONT_SIZE_DEFAULT));

        setFocusable(true);
        viewZoom = VIEW_ZOOM_DEFAULT;
        viewOffsetX = 0;
        viweOffsetY = 0;
        if (createImageTransform == null) {
            createImageTransform = new AffineTransform(viewZoom, 0, 0, -viewZoom, viewOffsetX, viweOffsetY);
        }
        createImageTransform.setTransform(viewZoom, 0, 0, -viewZoom, viewOffsetX, viweOffsetY);

        addDragAndDropListener();
        addResizeListener();
        OrgMouseListener oml = new OrgMouseListener(guiConfigPopup);
        addMouseListener(oml);
        addMouseMotionListener(oml);
        addMouseWheelListener(oml);
        addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) { // i, is already used in menubar.
                    switch(e.getKeyChar()) {
                    case 's': isSelectMode.setSelected(true); break;
                    case 'a': isInputAreaMode.setSelected(true); break;
                    case 'n': isInputNetworkMode.setSelected(true); break;
                    case 'p': isPutAgentMode.setSelected(true); break;
                    case 'l': simulatingValue.setValue(!simulatingValue.getValue()); break;
                    case 127: deleteSelection(); break;
                    default: System.out.println("?: " + e.getKeyCode());
                    }
                }
            });
        setPreferredSize(new Dimension(500, 500));
        setFocusable(true);
    }

    /**
     * set value.
     * @param k key
     * @param v value
     */
    public void setValue(String k, Object v) {
        Value value = RENDERING_SETTINGS.get(k);
        if (value instanceof BooleanValue) {
            ((BooleanValue)value).setValue((Boolean)v);
        }
        else {
            System.err.println("set valeue: unknown type: " + value);
        }
    }

    /**
     * get value.
     * @param k key
     * @return value
     */
    public Object getValue(String k) {
        return RENDERING_SETTINGS.get(k).getValue();
    }

    private class OrgMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {
        private int mouseLastX;
        private int mouseLastY;
        private boolean isSometihgSelected;
        private TagElement config = null;
        private JPopupMenu popup = new JPopupMenu();
        private List<TrafficAction> popupList = new ArrayList<TrafficAction>();

        OrgMouseListener(TagElement c) {
            if (c == null) {
                return;
            }
            config = c;
            for (TagElement te : config.getTagChildren("menu-item")) {
                String actionClassName = null;
                try {
                    //TrafficAction ta = new traffic3.manager.gui.action.ShowTargetsAsTextAction();
                    actionClassName = te.getAttributeValue("class");
                    TrafficAction ta = (TrafficAction)(Class.forName(actionClassName).newInstance());
                    ta.setWorldManagerGUI(thisObject);
                    popupList.add(ta);
                    popup.add(ta);
                }
                catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    popup.add(("<html><body><div>error: </div><div style='font-size:7px;color:red;'>" + e.toString() + "</div></body></html>"));
                }
                catch (InstantiationException e) {
                    e.printStackTrace();
                    popup.add(("<html><body><div>error: </div><div style='font-size:7px;color:red;'>" + e.toString() + "</div></body></html>"));
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                    popup.add(("<html><body><div>error: </div><div style='font-size:7px;color:red;'>" + e.toString() + "</div></body></html>"));
                }
            }
        }

        public void mousePressed(MouseEvent e) {
            requestFocus();
            mouseDragging = true;
            pressedMousePressedButtonIndex = e.getButton();
            if (pressedMousePressedButtonIndex == MouseEvent.BUTTON1) {

                //if (isSelectMode.isSelected()) {

                    if (e.isControlDown()) {
                        try {
                            TrafficAreaNode tan = worldManager.createAreaNode(sx2mx(e.getX()), sy2my(e.getY()), 0);
                            if (selectedAgentGroupList.size() == 0) {
                                for (TrafficObject o : targetList.values()) {
                                    if (o instanceof TrafficAgent) {
                                        TrafficAgent agent = (TrafficAgent)o;
                                        agent.setDestination(tan);
                                    }
                                }
                                /*
                                  TrafficAgent[] agent = worldManager.getAgentList();
                                  for (int i = 0; i < agent.length; i++) {
                                  agent[i].setDestination(tan);
                                  }
                                */
                            }
                            else {
                                String[] selectedAgentGroupNameList = selectedAgentGroupList.toArray(new String[0]);
                                for (int j = 0; j < selectedAgentGroupNameList.length; j++) {
                                    List<TrafficAgent> agentList = agentGroupList.get(selectedAgentGroupList.get(j));
                                    for (TrafficAgent agent : agentList) {
                                        agent.setDestination(tan);
                                    }
                                }
                            }
                        }
                        catch (WorldManagerException exc) {
                            exc.printStackTrace();
                            alert(exc, "error");
                        }
                    }
                    else {
                        //isSometihgSelected = listAllHitsToTarget(sx2mx(e.getX()), sy2my(e.getY()), e.isShiftDown());
                        mousePoint = new Point2D.Double(e.getX(), e.getY());
                    }
                    /*
                      }
                      else if (isInputAreaMode.isSelected()) {
                      alert(new UnsupportedOperationException(), "error");
                      }
                      else if (isInputNetworkMode.isSelected()) {
                      //alert(new UnsupportedOperationException(), "error");
                      try {
                      TrafficAreaNode tan = worldManager.createAreaNode(sx2mx(e.getX()), sy2my(e.getY()), 0);
                      }
                      catch (Exception exc) {
                      alert(exc, "error");
                      }
                      }
                    */
            }
            else if (pressedMousePressedButtonIndex == MouseEvent.BUTTON3) {
                final double x = sx2mx(e.getX());
                final double y = sy2my(e.getY());
                //final JPopupMenu popup = new JPopupMenu();
                mouseDragging = false;
                Point2D p = new Point2D.Double(x, y);
                for (TrafficAction ta : popupList) {
                    ta.setPressedPoint(p);
                }
                popup.show(thisObject, e.getX(), e.getY());
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (mouseRectangle != null) {
                double mX1 = sx2mx(mouseRectangle.getMinX());
                double mY1 = sy2my(mouseRectangle.getMinY());
                double mX2 = sx2mx(mouseRectangle.getMaxX());
                double mY2 = sy2my(mouseRectangle.getMaxY());
                double mMinX = Math.min(mX1, mX2);
                double mMinY = Math.min(mY1, mY2);
                double mMaxX = Math.max(mX1, mX2);
                double mMaxY = Math.max(mY1, mY2);
                listAllHitsToTarget(mMinX, mMinY, mMaxX, mMaxY, e.isShiftDown());
            }
            else if (mousePoint != null) {
                if (mousePoint.getX() == e.getX() && mousePoint.getY() == e.getY()) {
                    listAllHitsToTarget(sx2mx(e.getX()), sy2my(e.getY()), e.isShiftDown());
                }
            }
            isSometihgSelected = false;
            mouseDragging = false;
            pressedMousePressedButtonIndex = -1;
            mouseRectangle = null;
            mousePoint = null;
            mouseDraggingPoint = null;
            createImageInOtherThread();
        }
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mouseClicked(MouseEvent e) {}
        public void mouseMoved(MouseEvent e) {
            mouseLastX = e.getX();
            mouseLastY = e.getY();
            double x = sx2mx(mouseLastX);
            double y = sy2my(mouseLastY);
            String xtext = org.util.Handy.toNaturalString((int)x);
            String ytext = org.util.Handy.toNaturalString((int)y);
            setMouseStatus("X:" + xtext + "[mm] Y:" + ytext + "[mm]");
        }
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            if (mousePoint != null) {
                double mlx = sx2mx(mousePoint.getX());
                double mly = sy2my(mousePoint.getY());
                double mx = sx2mx(x);
                double my = sy2my(y);
                double mdx = mlx - mx;
                double mdy = mly - my;
                double distance = Math.sqrt(mdx * mdx + mdy - mdy);
                String xtext = org.util.Handy.toNaturalString((int)mx);
                String ytext = org.util.Handy.toNaturalString((int)my);
                String distanceText = org.util.Handy.toNaturalString((int)distance);
                setMouseStatus("X:" + xtext + "[mm] Y:" + ytext + "[mm]" + " distance:" + distanceText + "[mm]");
            }
            mouseDraggingPoint = new Point2D.Double(x, y);
            if (pressedMousePressedButtonIndex == 2 && !isSometihgSelected && isSelectMode.isSelected()) {
                drag(x - mouseLastX, y - mouseLastY);
            }
            else if(pressedMousePressedButtonIndex == 1) {
                if (targetList.size() != 0) {
                    boolean flag = true;
                    Map<String, TrafficAreaNode> tnlist = new HashMap<String, TrafficAreaNode>();
                    for (TrafficObject o : targetList.values()) {
                        if (o instanceof TrafficAreaNode) {
                            tnlist.put(o.getID(), (TrafficAreaNode)o);
                        }
                        /*
                        //dragg area
                        else if (o instanceof TrafficArea) {
                            TrafficArea area = (TrafficArea)o;
                            for (TrafficAreaNode n : area.getNodes())
                                tnlist.put(n.getID(), n);
                        }
                        //dragg edge
                        else if (o instanceof TrafficAreaEdge) {
                            TrafficAreaEdge edge = (TrafficAreaEdge)o;
                            for (TrafficAreaNode n : edge.getNodes()) {
                                tnlist.put(n.getID(), n);
                            }
                        }
                        */
                    }
                    if (tnlist.size() != 0) {
                        double dx = (x - mouseLastX);
                        double dy = (y - mouseLastY);
                        for (TrafficAreaNode node : tnlist.values()) {
                            double newX = node.getX() + dx / viewZoom;
                            double newY = node.getY() - dy / viewZoom;
                            node.setLocation(newX, newY);
                        }
                    }
                } else {
                    double minX = Math.min(mousePoint.getX(), x);
                    double minY = Math.min(mousePoint.getY(), y);
                    double maxX = Math.max(mousePoint.getX(), x);
                    double maxY = Math.max(mousePoint.getY(), y);
                    mouseRectangle = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
                }
                createImageInOtherThread();
            }
            mouseLastX = x;
            mouseLastY = y;
        }
        public void mouseWheelMoved(MouseWheelEvent e) {
            zoom((e.getWheelRotation() < 0 ? MOUSE_WHEEL_ZOOM_IN_SPEED : MOUSE_WHEEL_ZOOM_OUT_SPEED), e.getX(), e.getY());
        }
    }

    /**
     *
     */
    public void startSimulation() {
        double dt = SIMULATION_TIME_STEP_DEFAULT;
        if (isSimulationRunning) {
            return;
        }
        isSimulationRunning = true;
        Exception exc = null;
        try {
            TrafficAgent[] agentList = worldManager.getAgentList();
            for (int i = 0; i < agentList.length; i++) {
                agentList[i].clearLogDistance();
            }
            traffic3.simulator.Simulator simulator = new traffic3.simulator.Simulator(worldManager, dt);
            simulationStartWallclockTime = System.currentTimeMillis();
            while (isSimulationRunning) {
                simulator.step();
                simulationTime = simulator.getTime();
                requestToUpdateImage();
            }
        }
        catch (NullPointerException e) {
            exc = e;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            exc = e;
        }
        if (exc != null) {
            alert(exc, "error");
            boolean retry = false;
            try {
                retry = org.util.Handy.confirm(this, "Simulation is unexpectedly stopped.\nDo you want to restart simulation?\n"+exc.toString());
            }
            catch (org.util.CannotStopEDTException e) {
                e.printStackTrace();
            }
            isSimulationRunning = false;
            if (retry) {
                startSimulation();
            }
        }
    }

    /**
     * request to update image.
     */
    public void requestToUpdateImage() {

        long updateAgentLayerTime = System.currentTimeMillis();

        if (imageUpdateTerm < 0) {
            waitFor(-imageUpdateTerm);
            updateAgentLayer();
            repaint();
            lastUpdateAgentLayerTime = updateAgentLayerTime;
        }
        else if ((updateAgentLayerTime - lastUpdateAgentLayerTime) > imageUpdateTerm) {
            updateAgentLayer();
            repaint();
            lastUpdateAgentLayerTime = updateAgentLayerTime;
        }

        if (imageOutputTool != null) {
            final int w = getWidth();
            final int h = getHeight();
            /*
            if (recodeBufImage == null || recodeBufImage.getWidth() != w || recodeBufImage.getHeight() != h) {
                recodeBufImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            }
            */
            recodeBufImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D vg = (Graphics2D)recodeBufImage.getGraphics();
            int vw = recodeBufImage.getWidth();
            int vh = recodeBufImage.getHeight();
            //vg.drawImage(offImage, 0, 0, null);
            //vg.drawImage(agentLayerImageBuf, 0, 0, null);
            
            draw(vg, vw, vh);
            drawAgentLayer(vg);
            try {
                imageOutputTool.add(recodeBufImage, simulationTime);
            }
            catch (IOException exc) {
                exc.printStackTrace();
            }
            catch (ImageOutputCountExceededLimitException exc) {
                exc.printStackTrace();
            }
        }
    }

    /**
     *
     */
    public void stopSimulation() {
        isSimulationRunning = false;
    }

    private boolean listAllHitsToTarget(double mx, double my, boolean isShiftDown) {
        boolean isAdded = false;
        if (!isShiftDown) {
            targetList.clear();
            orderedTargetList.clear();
        }
        TrafficAgent[] agentList = worldManager.getAgentList();
        for (int i = 0; i < agentList.length; i++) {
            TrafficAgent agent = agentList[i];
            double dx = agent.getX() - mx;
            double dy = agent.getY() - my;
            double d = Math.max(agent.getRadius(), DRAW_AGENT_RADIUS/viewZoom);
            if (d*d > dx * dx + dy * dy) {
                String id = agent.getID();
                if (isShiftDown && targetList.get(id) != null) {
                    targetList.remove(id);
                }
                else {
                    targetList.put(id, agent);
                    orderedTargetList.add(agent);
                }
                isAdded = true;
            }
        }

        if (!isAdded && showAreaNodeValue.getValue().booleanValue()) {
            TrafficAreaNode node = worldManager.getNearlestAreaNode(mx, my, 0);
            if (node != null) {
                double d = MOUSE_CLICK_NODE_DISTANCE / viewZoom;
                System.out.println(node.getDistance(mx, my, 0));
                if (node.getDistance(mx, my, 0) < d) {
                    String id = node.getID();
                    if (isShiftDown && targetList.get(id) != null) {
                        targetList.remove(id);
                    }
                    else {
                        targetList.put(id, node);
                        orderedTargetList.add(node);
                    }
                    isAdded = true;
                }
            }
        }

        if (!isAdded && showAreaEdgeValue.getValue().booleanValue()) {
            for (TrafficAreaEdge edge : worldManager.getAreaConnectorEdgeList()) {
                if (edge.distance(mx, my) < MOUSE_CLICK_EDGE_DISTANCE / viewZoom) {
                    String id = edge.getID();
                    if (isShiftDown && targetList.get(id) != null) {
                        targetList.remove(id);
                    }
                    else {
                        targetList.put(id, edge);
                        orderedTargetList.add(edge);
                    }
                    isAdded = true;
                }
            }
        }

        if (!isAdded && showAreaValue.getValue().booleanValue()) {
            for (TrafficArea area : worldManager.getAreaList()) {
                if (area.getShape().contains(mx, my)) {
                    String id = area.getID();
                    if (isShiftDown && targetList.get(id) != null) {
                        targetList.remove(id);
                    }
                    else {
                        targetList.put(id, area);
                        orderedTargetList.add(area);
                    }
                    isAdded = true;
                }
            }
        }

        if (targetList.size() == 0) {
            log("clear target");
        }
        else {
            StringBuffer sb = new StringBuffer("setTarget[");
            sb.append(targetList.toString());
            sb.append("]");
            log(sb);
        }
        //getShowTargetsAction().setEnabled(targetList.size()!=0);
        return isAdded;
    }

    private boolean listAllHitsToTarget(double mMinX, double mMinY, double mMaxX, double mMaxY, boolean isShiftDown) {
        boolean isAdded = false;
        if (!isShiftDown) {
            targetList.clear();
            orderedTargetList.clear();
        }
        TrafficAgent[] agentList = worldManager.getAgentList();
        for (int i = 0; i < agentList.length; i++) {
            TrafficAgent agent = agentList[i];
            double x = agent.getX();
            double y = agent.getY();
            if (mMinX < x && x < mMaxX && mMinY < y && y < mMaxY) {
                String id = agent.getID();
                if (isShiftDown && targetList.get(id) != null) {
                    targetList.remove(id);
                }
                else {
                    targetList.put(id, agent);
                    orderedTargetList.add(agent);
                }
                isAdded = true;
            }
        }

        if (targetList.size() == 0) {
            log("clear target");
        }
        else {
            StringBuffer sb = new StringBuffer("setTarget[");
            sb.append(targetList.toString());
            sb.append("]");
            log(sb);
        }
        //getShowTargetsAction().setEnabled(targetList.size()!=0);
        return isAdded;
    }


    private void zoom(double dzoom, double x, double y) {
        viewOffsetX = (viewOffsetX - x) * dzoom + x;
        viweOffsetY = (viweOffsetY - y) * dzoom + y;
        viewZoom *= dzoom;
        createImageTransform.setTransform(viewZoom, 0, 0, -viewZoom, viewOffsetX, viweOffsetY);
        createImageInOtherThread();
    }

    private void drag(double dx, double dy) {
        viewOffsetX += dx;
        viweOffsetY += dy;
        createImageTransform.setTransform(viewZoom, 0, 0, -viewZoom, viewOffsetX, viweOffsetY);
        createImageInOtherThread();
    }

    private void addDragAndDropListener() {
        setTransferHandler(new TransferHandler(null) {
                public boolean canImport(TransferHandler.TransferSupport support) {
                    Transferable trans = support.getTransferable();
                    if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        return true;
                    }
                    if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        return true;
                    }
                    return false;
                }
                public boolean importData(TransferHandler.TransferSupport support) {
                    Transferable trans = support.getTransferable();
                    if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        try {
                            java.util.List<File> data = forceToCast(trans.getTransferData(DataFlavor.javaFileListFlavor));
                            for (Iterator<File> it = data.iterator(); it.hasNext();) {
                                File file = it.next();
                                open(file);
                            }
                        }
                        catch (UnsupportedFlavorException e) {
                            alert(e, "error");
                        }
                        catch (IOException e) {
                            alert(e, "error");
                        }
                        return true;
                    }
                    if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        try {
                            String data = (String)trans.getTransferData(DataFlavor.stringFlavor);
                            for (String text : data.split("\r\n")) {
                                open(new URI(text).toURL());
                            }
                        }
                        catch (UnsupportedFlavorException e) {
                            alert(e, "error");
                        }
                        catch (URISyntaxException e) {
                            alert(e, "error");
                        }
                        catch (IOException e) {
                            alert(e, "error");
                        }
                        return true;
                    }
                    return false;
                }
            });
    }

    /**
     *
     */
    public void addResizeListener() {
        addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    createImageInOtherThread();
                }
            });
    }

    public JMenuBar createMenuBar(TagElement menuElement) {
        JMenuBar menubar = new JMenuBar();
        TagElement[] menuElementTags = menuElement.getTagChildren("menu-item");
        for (TagElement te : menuElementTags) {
            menubar.add(createMenu(te));
        }
        return menubar;
    }

    private JMenu createMenu(TagElement menuElement) {

        String name = menuElement.getAttributeValue("name");
        JMenu menu = new JMenu(name);
        TagElement[] menuElementTags = menuElement.getTagChildren("menu-item");
        for (TagElement te : menuElementTags) {
            //try {
                if ("list".equals(te.getAttributeValue("type"))) {
                    menu.add(createMenu(te));
                }
                else if ("action".equals(te.getAttributeValue("type"))) {
                    String actionName = te.getAttributeValue("name");
                    String actionClassName = te.getAttributeValue("class");
                    Exception exc = null;
                    try {
                        TrafficAction action = (TrafficAction)(Class.forName(actionClassName).newInstance());
                        if (actionName != null && actionName.length() > 0) {
                            action.putValue("Name", actionName);
                        }
                        action.setWorldManagerGUI(this);
                        menu.add(new JMenuItem(action));
                    }
                    catch (InstantiationException e) {
                        exc = e;
                    }
                    catch (ClassNotFoundException e) {
                        exc = e;
                    }
                    catch (IllegalAccessException e) {
                        exc = e;
                    }
                    if (exc != null) {
                        StringBuffer sb = new StringBuffer("<html><body>");
                        sb.append("<div style='color:red;'>Error: creating menu");
                        sb.append("<div style='font-size:9;color:black;margin:0 0 0 20;'>");
                        sb.append("<div>Name: ").append(actionName).append("</div>");
                        sb.append("<div>Class: ").append(actionClassName).append("</div>");
                        sb.append("<div>Error Message: ").append(exc.toString()).append("</div>");
                        sb.append("</div>");
                        sb.append("</div>");
                        sb.append("</body></html>");
                        menu.add(new JMenuItem(sb.toString()));
                        exc.printStackTrace();
                    }
                }
                else if ("check".equals(te.getAttributeValue("type"))) {
                    String valueName = te.getAttributeValue("value");
                    BooleanValue value = (BooleanValue)RENDERING_SETTINGS.get(valueName);
                    try {
                        Boolean initValue = "true".equals(te.getAttributeValue("init"));
                        value.setValue(initValue);
                        menu.add(ValueEditor.createEditor(value));
                    }
                    catch (NullPointerException e) {
                        //StringBuffer sbb = new StringBuffer();
                        //for (String k : RENDERING_SETTINGS.keySet()) {
                        //    sbb.append(k).append(",");
                        //}
                        StringBuffer sb = new StringBuffer("<html><body>");
                        sb.append("<div style='color:red;'>Error: creating check");
                        sb.append("<div style='font-size:9;color:black;margin:0 0 0 20;'>");
                        sb.append("<div>Value Name: ").append(valueName).append("</div>");
                        sb.append("<div>Error Message: ").append(e.toString()).append("</div>");
                        sb.append("<div>Error Message: <br/>");
                        sb.append(RENDERING_SETTINGS.keySet().toString().replaceAll(",", "<br/>")).append(",");
                        sb.append("</div>");
                        sb.append("</div>");
                        sb.append("</div>");
                        sb.append("</body></html>");
                        menu.add(new JMenuItem(sb.toString()));
                        e.printStackTrace();
                    }
                }
                else if ("separator".equals(te.getAttributeValue("type"))) {
                    menu.addSeparator();
                }
                else {
                    menu.add(new JMenuItem("<html><body>Error</body></html>"));
                }
                /*
            }
            catch (RuntimeException exc) {
                exc.printStackTrace();
            }
                */
        }
        return menu;
    }

    /**
     *
     */
    public JMenuBar createMenuBar() {

        TagElement guiConfigMenu = configManager.getTag("gui/menu");

        if (guiConfigMenu.getTagChildren() == null) {
            try {
                java.io.InputStream in = org.util.Handy.getResourceAsStream("data/resources/default-menubar.xml");
                TagElement[] defaultTags = XMLIO.read(in).getTagChildren();
                guiConfigMenu.setChildren(defaultTags);
                configManager.outputSetting();
                guiConfigMenu = configManager.getTag("gui/menu");
            }
            catch (IOException e) {
                alert(e, "error");
            }
            catch (XMLParseException e) {
                alert(e, "error");
            }
        }
        JMenuBar mb = createMenuBar(guiConfigMenu);
        
        /*
        JMenu editMenu = new JMenu("Edit-old");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.add(isSelectMode);
        editMenu.add(isInputAreaMode);
        editMenu.add(isInputNetworkMode);
        editMenu.add(isPutAgentMode);
        mb.add(editMenu);
        */
        return mb;
    }

    public void deleteSelection() {
        new Thread(new Runnable() {
                public void run() {
                    TrafficObject[] targets = createCopyOfTargetList();
                    for (int i = 0; i < targets.length; i++) {
                        try {
                            worldManager.remove(targets[i]);
                        }
                        catch(WorldManagerException e) {
                            log(e, "error");
                        }
                    }
                    createImageInOtherThread();
                }
            }, "delete selection").start();
    }

    /**
     * Open url.
     * @param url url
     * @throws URISyntaxException excpetion
     */
    public void open(final URL url) throws URISyntaxException {
        /*
        if (url.toString().startsWith("file:")) {
            open(new File(url.toURI()));
        }
        else {
            new Thread(new Runnable() { public void run() {
                try {
                    log("importing: " + url);
                    worldManager.open(url);
                    fitView();
                    alert("Successfuly imported.\n\tThere are " + worldManager.getAll().length + " objects.", "information");
                }
                catch (FileNotFoundException e) {
                    alert(e, "error");
                    e.printStackTrace();
                }
                catch (IOException e) {
                    alert(e, "error");
                    e.printStackTrace();
                }
                catch (XMLParseException e) {
                    alert(e, "error");
                    e.printStackTrace();
                }
                catch (ParserNotFoundException e) {
                    alert(e, "error");
                    e.printStackTrace();
                }
                catch (WorldManagerException e) {
                    alert(e, "error");
                    e.printStackTrace();
                }
            } }, "import from url").start();
        }
        */
    }

    /**
     * Open file.
     * @param file file
     */
    public void open(final File file) {
        /*
        final boolean[] finished = new boolean[1];
        new Thread(new Runnable() { public void run() {
            try {
                log("importing: " + file.getAbsolutePath());
                worldManager.open(file);
                fitView();
                alert("Successfuly imported.\n\tThere are " + worldManager.getAll().length + " objects.", "information");
            }
            catch (FileNotFoundException e) {
                alert(e, "error");
                e.printStackTrace();
            }
            catch (IOException e) {
                alert(e, "error");
                e.printStackTrace();
            }
            catch (XMLParseException e) {
                alert(e, "error");
                e.printStackTrace();
            }
            catch (ParserNotFoundException e) {
                alert(e, "error");
                e.printStackTrace();
            }
            catch (WorldManagerException e) {
                alert(e, "error");
                e.printStackTrace();
            }
            finally {
                finished[0] = true;
            }
        } }, "import from file").start();
        new Thread(new Runnable() { public void run() {
            String[] sample = new String[] {"import:<", "import: ^", "import:  >", "import: v"};
            for (int i = 0; !finished[0]; i++) {
                setStatus(String.valueOf(sample[i % sample.length]));
                waitFor(OPEN_ANIMATION_STEP);
            }
        } }, "notify running thread").start();
        */
    }

    /**
     * @return parser
     */
    private Parser selectParser()  {
        /*
        Parser[] parserList = worldManager.getParserList();
        String message = "select export type";
        String title = "select";
        int type = JOptionPane.INFORMATION_MESSAGE;
        Icon icon = null;
        Object selection = JOptionPane.showInputDialog(this, message, title, type, icon, parserList, parserList[0]);
        return (Parser)selection;
        */
        return null;
    }

    /**
     *
     */
    public void save() {
        /*
        final boolean[] finished = new boolean[1];
        new Thread(new Runnable() { public void run() {
            try {
                log(">export");
                Parser parser = selectParser();
                File file = IO.getSaveFile(thisObject, "GML file", "xml", "gml");
                log("selected file: " + file.getAbsolutePath());
                save(file, parser);
            }
            catch (IOException exc) {
                log("cannot find the file.");
            }
            catch (UserCancelException exc) {
                log("cancelled by user.");
            }
            finally {
                finished[0] = true;
            }
        } }, "export to file").start();
        new Thread(new Runnable() { public void run() {
            String[] sample = new String[]{"import:<", "import: ^", "import:  >", "import: v"};
            for (int i = 0; !finished[0]; i++) {
                setStatus(String.valueOf(sample[i % sample.length]));
                waitFor(OPEN_ANIMATION_STEP);
            }
        } }, "notify running thread").start();
        */
    }

    /**
     * Save to a file.
     * @param file file
     * @param parser parser
     * @throws IOException file not found
     */
    public void save(File file, Parser parser) throws IOException {
        //        worldManager.save(file, parser);
        //        alert("Successfuly outputed.");
    }

    /**
     *
     */
    public void createImageInOtherThread() {
        if (createImageThread  ==  null) {
            log("creating image thread.");
            createImageThread = new Thread(new Runnable() { public void run() {
                while (true) {
                    try {
                        synchronized (createImageThread) {
                            createImageThread.wait();
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                        org.util.Handy.show(null, null, e);
                        createImageThread = null;
                    }
                    createImage();
                }
            } }, "create image");
            createImageThread.start();
        }
        else if (createImageDrawing) {
            createImageCancelDrawing = true;
        } else {
            synchronized (createImageThread) {
                createImageThread.notifyAll();
            }
        }
    }

    //    private volatile boolean non_stop_creating_image_thread_is_running_ = false;

    private void createImage() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            return;
        }
        if (offImage == null) {
            offImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            agentLayerImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        else if (offImage.getWidth() < w || offImage.getHeight() < h) {
            offImage = new BufferedImage(Math.max(offImage.getWidth(), w), Math.max(offImage.getHeight(), h), BufferedImage.TYPE_INT_RGB);
            agentLayerImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D g2 = (Graphics2D)offImage.getGraphics();
        g2.setColor(Color.white);
        g2.fillRect(0, 0, w, h);
        draw(g2, w, h);
        repaint();
    }

    /**
     * @return create copy of target list
     */
    public TrafficObject[] createCopyOfTargetList() {
        return targetList.values().toArray(new TrafficObject[0]);
    }

    private void draw(Graphics2D g, int w, int h) {
        /*
        if (createImageDrawing) {
            createImageCancelDrawing = true;
System.out.println("create image cancel");
            return;
        }
System.out.println("create image start");
        */

        createImageDrawing = true;
        createImageCancelDrawing = false;
        long drawStartTime = System.currentTimeMillis();

        TrafficObject[] copyOfTargetList = createCopyOfTargetList();

        if (mouseDragging || !antialiasingValue.getValue().booleanValue()) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        else { // This operation is heavy.
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(areaConnectorEdgeStroke);
        }

        // fill area
        if (!mouseDragging && showAreaValue.getValue().booleanValue()) {
            TrafficArea[] area = worldManager.getAreaList();
            for (int i = 0; i < area.length; i++) {
                g.setColor(areaColor);
                fill(g, area[i]);
            }
            g.setColor(selectedObjectColor);
            for (int i = 0; i < copyOfTargetList.length; i++) {
                if (copyOfTargetList[i] instanceof TrafficArea) {
                    fill(g, (TrafficArea)copyOfTargetList[i]);
                }
            }
        }

        // fill blockade
        if (!mouseDragging && showAreaValue.getValue().booleanValue()) {
            for (TrafficBlockade blockade : worldManager.getBlockadeList()) {
                g.setColor(Color.black);
                fill(g, blockade);
            }
        }

        // drwa area connector edge
        if (showAreaConnectorValue.getValue().booleanValue()) {

            g.setColor(areaConnectorColor);
            /*
            for (TrafficArea area : worldManager.getAreaList()) {
                for (TrafficAreaEdge edge : area.getConnectorEdgeList()) {
                    draw(g, edge);
                }
            }
            */
            for (TrafficAreaEdge edge : worldManager.getAreaConnectorEdgeList()) {
                TrafficArea[] areas = edge.getAreas();
                if (areas != null && areas.length >= 2) {
                    draw(g, edge);
                }
            }
            g.setColor(Color.red);
            for (int i = 0; i < copyOfTargetList.length; i++) {
                if (copyOfTargetList[i] instanceof TrafficAreaEdge) {
                    draw(g, (TrafficAreaEdge)copyOfTargetList[i]);
                }
            }
        }

        // draw edgemp
        if (!createImageCancelDrawing && showAreaEdgeValue.getValue().booleanValue()) {

            g.setColor(areaEdgeColor);
            /*
            for (TrafficArea area : worldManager.getAreaList()) {
                for (Line2D line : area.getUnconnectedEdgeList()) {
                    Point2D p1 = line.getP1();
                    Point2D p2 = line.getP2();
                    g.draw(new Line2D.Double(mx2sx(p1.getX()), my2sy(p1.getY()), mx2sx(p2.getX()), my2sy(p2.getY())));
                }
            }
            */
            for (TrafficAreaEdge edge : worldManager.getAreaConnectorEdgeList()) {
                TrafficArea[] areas = edge.getAreas();
                if (areas == null || areas.length < 2) {
                    draw(g, edge);
                }
            }

            g.setColor(Color.red);
            for (TrafficObject target : copyOfTargetList) {
                if (target instanceof TrafficAreaEdge) {
                    draw(g, (TrafficAreaEdge)target);
                }
            }
        }

        if (!createImageCancelDrawing && showAreaNodeValue.getValue().booleanValue()) {
            g.setColor(areaNodeColor);
            for (TrafficAreaNode node : worldManager.getAreaNodeList()) {
                draw(g, node, showAreaNodeIDValue.getValue().booleanValue());
            }
            g.setColor(Color.red);
            for (TrafficObject target : copyOfTargetList) {
                if (target instanceof TrafficAreaNode) {
                    draw(g, (TrafficAreaNode)target, false);
                }
            }
        }

       

        if (!createImageCancelDrawing) {
            updateAgentLayer();
        }

        g.setColor(Color.black);

        long drawEnd = System.currentTimeMillis();
        g.drawString("[" + (drawEnd - drawStartTime) + "]", DRAW_STRING_MARGIN, DRAW_STRING_LINE_HEIGHT);

        createImageDrawing = false;

        if (createImageCancelDrawing) { // if cancelled then it is required to recreate image
            createImage();
        }
    }


    /**
     * update agent layer.
     * (create agent layer image)
     */
    protected void updateAgentLayer() {
        if (imageOutputTool == null) {
            return;
        }
        int w = getWidth();
        int h = getHeight();
        if (agentLayerImageBuf == null || agentLayerImageBuf.getWidth() != w || agentLayerImageBuf.getHeight() != h) {
            agentLayerImageBuf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D ag = (Graphics2D)agentLayerImageBuf.getGraphics();
        drawAgentLayer(ag);
        agentLayerImage = agentLayerImageBuf;
    }

    private void drawAgentLayer(Graphics2D ag) {
        ag.setColor(Color.green);
        ag.setStroke(new BasicStroke(0f));
        boolean showAreaNodeID = showAreaNodeIDValue.getValue().booleanValue();
        TrafficAgent[] agentList = worldManager.getAgentList();

        for (int i = 0; i < agentList.length; i++) {
            draw(ag, agentList[i], showAreaNodeID, agentList[i].getColor());
        }

        TrafficObject[] copyOfTargetList = createCopyOfTargetList();
        for (int i = 0; i < copyOfTargetList.length; i++) {
            if (copyOfTargetList[i] instanceof TrafficAgent) {
                TrafficAgent agent = (TrafficAgent)copyOfTargetList[i];
                draw(ag, agent, false, Color.red);
                TrafficAreaNode fdestination = agent.getFinalDestination();
                TrafficAreaNode ndestination = agent.getNowDestination();
                double x1 = mx2sx(agent.getX());
                double y1 = my2sy(agent.getY());
                ag.setColor(Color.blue);
                if (fdestination != null) {
                    double x2 = mx2sx(fdestination.getX());
                    double y2 = my2sy(fdestination.getY());
                    ag.draw(new Line2D.Double(x1, y1, x2, y2));
                }
                ag.setColor(Color.green);
                if (ndestination != null) {
                    double x2 = mx2sx(ndestination.getX());
                    double y2 = my2sy(ndestination.getY());
                    ag.draw(new Line2D.Double(x1, y1, x2, y2));
                }
            }
        }

        ag.setColor(Color.black);
        ag.drawString("Simulation Time: " + simulationTime + "[ms]", DRAW_SIMULATION_TIME_X, DRAW_SIMULATION_TIME_Y);

        long time = System.currentTimeMillis();
        ag.drawString("Real Time: " + time + "[ms] (" + (time - videoStart) + ")", DRAW_REAL_TIME_X, DRAW_REAL_TIME_Y);
    }


    /**
     * @param g graphics
     */
    public void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        if (offImage != null) {
            g.drawImage(offImage, 0, 0, this);
            Graphics2D g2 = (Graphics2D)g;
            drawAgentLayer(g2);
            if (mouseRectangle != null) {
                g2.setColor(new Color(0, 0, 255, 50));
                g2.fill(mouseRectangle);
                g2.setColor(Color.black);
                g2.draw(mouseRectangle);
                if (mouseDraggingPoint != null) {
                    g2.draw(new Line2D.Double(mouseDraggingPoint.getX(), mouseDraggingPoint.getY(), mousePoint.getX(), mousePoint.getY()));
                }
            }
            /*
            g.setColor(Color.red);
            testcount = 0;
            synchronized (worldManager.getRTree()) {   
                int id = worldManager.getRTree().getRootNodeId();
                debugNodes(g, worldManager.getRTree(), id);
            }
            g.setColor(Color.blue);
            for (traffic3.manager.RTreeRectangle r : worldManager.rTreeBoundMap.values().toArray(new traffic3.manager.RTreeRectangle[0])) {
                drawMapRect(g, r.min[0], r.min[1], r.max[0], r.max[1]);
            }
            */

            if (imageOutputTool != null) {
                int line = 3;
                g.setColor(Color.black);
                g.drawString("REC[" + imageOutputTool.getLength() + "]", DRAW_STRING_MARGIN, line * DRAW_STRING_LINE_HEIGHT);
            }
        }
        else {
            g.setColor(getBackground());
            g.fillRect(0, 0, w, h);
            g.setColor(getForeground());
            g.drawString("Loading...", 1, DRAW_STRING_LINE_HEIGHT);
            createImageInOtherThread();
        }
    }

    private int testcount = 0;
    public void debugNodes(Graphics g, com.infomatiq.jsi.rtree.RTree rTree, int id) {
        if (testcount++ > 5000) {
            return;
        }
        com.infomatiq.jsi.rtree.Node n = rTree.getNode(id);
        if (n == null || n.getLevel()<0) return;
        com.infomatiq.jsi.Rectangle r = n.getMBR();
        if (r !=null) {
            g.setColor(Color.black);
            g.drawString(id + "[" + n.getLevel() + "]", (int)mx2sx(r.min[0]), (int)my2sy(r.max[1]));
            /*
              g.setColor(new Color((n.getLevel()), 0, 0));
              fillMapRect(g, r.min[0], r.min[1], r.max[0], r.max[1]);
            */
            g.setColor(Color.red);
            drawMapRect(g, r.min[0], r.min[1], r.max[0], r.max[1]);
        }
        for (int i = 0; i < n.getEntryCount(); i++) {
            int idd = n.getId(i);
            int a = n.getLevel();
            debugNodes(g, rTree, idd);
        }
    }

    public void drawMapRect(Graphics g, double mx0, double my0, double mx1, double my1) {
        drawScreenRect(g, mx2sx(mx0), my2sy(my0), mx2sx(mx1), my2sy(my1));
    }

    public void drawScreenRect(Graphics g, double sx0, double sy0, double sx1, double sy1) {
        int x = (int)Math.min(sx0, sx1);
        int y = (int)Math.min(sy0, sy1);
        int www = (int)Math.abs(sx1 - sx0) + 1;
        int hhh = (int)Math.abs(sy1 - sy0) + 1;
        g.drawRect(x, y, www, hhh);
    }

    public void fillMapRect(Graphics g, double mx0, double my0, double mx1, double my1) {
        fillScreenRect(g, mx2sx(mx0), my2sy(my0), mx2sx(mx1), my2sy(my1));
    }

    public void fillScreenRect(Graphics g, double sx0, double sy0, double sx1, double sy1) {
        int x = (int)Math.min(sx0, sx1);
        int y = (int)Math.min(sy0, sy1);
        int www = (int)Math.abs(sx1 - sx0) + 1;
        int hhh = (int)Math.abs(sy1 - sy0) + 1;
        g.fillRect(x, y, www, hhh);
    }
    

    /**
     * draw edge to graphics.
     * @param g graphics
     * @param edge edge
     */
    public void draw(Graphics2D g, TrafficAreaEdge edge) {
        GeneralPath gp = (GeneralPath)(edge.getPath().clone());
        gp.transform(createImageTransform);
        g.draw(gp);
    }

    /**
     * draw node to graphics.
     * @param g graphics
     * @param node edge
     * @param showID whether show id or not
     */
    public void draw(Graphics2D g, TrafficAreaNode node, boolean showID) {
        double d = MOUSE_CLICK_NODE_DISTANCE;
        //double d = 4; //<?>
        Shape arc = new Ellipse2D.Double(mx2sx(node.getX()) - d, my2sy(node.getY()) - d, d + d, d + d);
        g.fill(arc);
        if (showID) {
            g.drawString(String.valueOf(node.getID()), (int)mx2sx(node.getX()) + DRAW_STRING_MARGIN, (int)my2sy(node.getY()));
        }
    }

    /**
     * fill area.
     * @param g graphics
     * @param area area
     */
    public void fill(Graphics2D g, TrafficArea area) {
        if (area == null || area.getShape() == null) {
            return;
        }
        GeneralPath gp = (GeneralPath)(area.getShape().clone());
        gp.transform(createImageTransform);
        g.fill(gp);
    }

    /**
     * draw border of the area.
     * @param g graphics
     * @param area area
     */
    public void draw(Graphics2D g, TrafficArea area) {
        if (area == null || area.getShape() == null) {
            return;
        }
        GeneralPath gp = (GeneralPath)(area.getShape().clone());
        gp.transform(createImageTransform);
        g.draw(gp);
    }

    /**
     * fill blockade.
     * @param g graphics
     * @param blockade blockade
     */
    public void fill(Graphics2D g, TrafficBlockade blockade) {
        GeneralPath gp = (GeneralPath)(blockade.getShape().clone());
        gp.transform(createImageTransform);
        g.fill(gp);
    }

    /**
     * draw agent to graphics.
     * @param g graphics
     * @param agent agent
     * @param showID show id or not
     * @param color color
     */
    public void drawFast(Graphics2D g, TrafficAgent agent, boolean showID, Color color) {
        double x = mx2sx(agent.getX()) - DRAW_AGENT_RADIUS;
        double y = my2sy(agent.getY()) - DRAW_AGENT_RADIUS;
        Ellipse2D shape = new Ellipse2D.Double(x, y, DRAW_AGENT_RADIUS * 2, DRAW_AGENT_RADIUS * 2);
        if (g.getColor() != color) {
            g.setColor(color);
        }
        g.fill(shape);
    }

    /**
     * draw agent to graphics.
     * @param g graphics
     * @param agent agent
     * @param showID show id or not
     * @param color color
     */
    public void draw(Graphics2D g, TrafficAgent agent, boolean showID, Color color) {
        double d = Math.max(viewZoom * agent.getRadius(), DRAW_AGENT_RADIUS);
        double x = mx2sx(agent.getX());
        double y = my2sy(agent.getY());
        //Arc2D.Double arc = new Arc2D.Double(x-d, y-d, d*2, d*2, 0, 360,Arc2D.OPEN);
        Shape arc = new Ellipse2D.Double(x - d, y - d, d + d, d + d);
        g.setColor(color);
        g.fill(arc);
        g.setColor(Color.black);
        g.draw(arc);

        double x2 = mx2sx(agent.getX() + agent.getFX() * DRAW_AGENT_F_SCALE);
        double y2 = my2sy(agent.getY() + agent.getFY() * DRAW_AGENT_F_SCALE);
        double x3 = mx2sx(agent.getX() + agent.getVX() * DRAW_AGENT_V_SCALE);
        double y3 = my2sy(agent.getY() + agent.getVY() * DRAW_AGENT_V_SCALE);
        g.setColor(Color.black);
        g.draw(new Line2D.Double(x, y, x3, y3));
        g.setColor(Color.red);
        g.draw(new Line2D.Double(x, y, x2, y2));
        if (showID) {
            g.drawString(agent.getID(), (int)x, (int)y);
        }
    }

    private double sx2mx(double mx) { return (mx - viewOffsetX) / viewZoom; }
    private double sy2my(double my) { return -(my - viweOffsetY) / viewZoom; }
    private double mx2sx(double sx) { return sx * viewZoom + viewOffsetX; }
    private double my2sy(double sy) { return -sy * viewZoom + viweOffsetY; }
    private Point2D.Double s2m(Point2D.Double s) { s.setLocation(sx2mx(s.getX()), sy2my(s.getY())); return s; }
    private Point2D.Double m2s(Point2D.Double m) { m.setLocation(mx2sx(m.getX()), my2sy(m.getY())); return m; }

    /**
     * show target information.
     */
    public void showTargetsInformation() {
        showInformation(targetList.values().toArray(new TrafficObject[0]));
    }

    /**
     * show information of ids.
     * @param ids ids
     */
    public void showInformationByIDs(String... ids) {
        TrafficObject[] objectList = new TrafficObject[ids.length];
        for (int i = 0; i < ids.length; i++) {
            objectList[i] = worldManager.getTrafficObject(ids[i]);
        }
        showInformation(objectList);
    }

    /**
     * show information of object list.
     * @param objectList list of traffic object
     */
    public void showInformation(final TrafficObject... objectList) {

        new Thread(new Runnable() { public void run() {
            try {
            
                //final Dimension preferredSize = new Dimension(500, 300);
                //thisObject.requestFocus();
                StringBuffer sb = new StringBuffer();
                sb.append("<html>");
                for (int i = 0; i < objectList.length; i++) {
                    sb.append("<div>").append(objectList[i].toLongString()).append("</div>");
                }
                sb.append("</html>");
                /*
                  TextPane tp = new JTextPane();
                  tp.setContentType("text/html");
                  tp.setText(sb.toString());
                  tp.setBackground(getBackground());
                  JScrollPane sp = new JScrollPane(tp);
                  sp.setBorder(null);
                  sp.setPreferredSize(preferredSize);
                  JOptionPane.showMessageDialog(thisObject, sp);
                */
                org.util.Handy.show(thisObject, null, sb.toString());
            }
            catch (Exception e) {
                alert(e);
                //org.util.Handy.show(thisObject, null, e);
            }
        } }, "show information").start();
    }

    /**
     * set image update rate.
     * @param rate rate
     */
    public void setImageUpdateRate(int rate) {
        imageUpdateTerm = rate;
    }

    /**
     * @return update rate
     */
    public int setImageUpdateRate() {
        return imageUpdateTerm;
    }

    /**
     * get world manager.
     * @return world manager
     */
    public WorldManager getWorldManager() {
        return worldManager;
    }

    /**
     * fit view.
     */
    public void fitView() {
        Rectangle2D.Double view = worldManager.calcRange();
        if (view != null) {
            setView(view);
        }
    }

    /**
     * set view.
     * @param view view
     */
    public void setView(Rectangle2D view) {
        log("set view:" + view);
        double dwidth = (double)getWidth() / view.getWidth();
        double dheight = (double)getHeight() / view.getHeight();
        final double dd = 0.9;
        final double half = 0.5;
        if (dwidth < dheight) {
            viewZoom = dwidth * dd;
            viewOffsetX = -view.getX() * viewZoom + view.getWidth() * (dwidth - viewZoom) * half;
            viweOffsetY = -view.getY() * viewZoom + view.getHeight() * (dheight - viewZoom) * half;
            // viweOffsetY = -view.getY()*viewZoom + view.getHeight()*(dheight-viewZoom)*0.5+getHeight();
        }
        else {
            viewZoom = dheight * dd;
            viewOffsetX = -view.getX() * viewZoom + view.getWidth() * (dwidth - viewZoom) * half;
            viweOffsetY = -view.getY() * viewZoom + view.getHeight() * (dheight - viewZoom) * half;
            // viweOffsetY = -view.getY()*viewZoom + view.getHeight()*(dheight-viewZoom)*0.5+getHeight();
        }
        viweOffsetY = -viweOffsetY + getHeight();
        createImageTransform.setTransform(viewZoom, 0, 0, -viewZoom, viewOffsetX, viweOffsetY);
        createImageInOtherThread();
    }

    /**
     * get target list.
     * @return target list
     */
    public Map<String, TrafficObject> getTargetList() {
        return targetList;
    }

    public TrafficObject[] getOrderedTargets() {
        return orderedTargetList.toArray(new TrafficObject[0]);
    }

    /**
     * get agent group list.
     * @return agent group
     */
    public Map<String, List<TrafficAgent>> getAgentGroupList() {
        return agentGroupList;
    }

    /**
     * get selected agent group list.
     * @return selected agent group list.
     */
    public List<String> getSelectedAgentGroupList() {
        return selectedAgentGroupList;
    }

    /**
     * switch rec.
     */
    public void switchRec() {
        new Thread(new Runnable() { public void run() {
            try {
                if (imageOutputTool == null) {
                    imageOutputTool = new ImageOutputTool(videoRecodeDirectory);
                    imageOutputTool.setMinimumInterval(1000);
                }
                else {
                    imageOutputTool.outputHTMLFiles();
                    imageOutputTool = null;
                }
            }
            catch (IOException exc) {
                alert(exc, "error");
            }
        } }, "video recoder").start();
    }

    /**
     * set mouse status.
     * @param message message
     */
    private void setMouseStatus(String message) {
        mouseStatusLabel.setText(message);
    }

    /**
     * set status.
     * @param message message
     */
    public void setStatus(final String message) {
        statusLabel.setText(message);
        new Thread(new Runnable() {
                public void run() {
                    waitFor(1000);
                    if (statusLabel.getText().equals(message)) {
                        statusLabel.setText("");
                    }
                }
            }).start();
    }

    /**
     * menubar.
     * @return menubar
     */
    /*
    public JMenuBar getMenuBar() {
        return menuBar;
    }
    */

    /**
     * statusbar.
     * @return status bar
     */
    public JComponent getStatusBar() {
        final Dimension maximumSize = new Dimension(300, 30);

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.add(imageUpdateRateLabel, BorderLayout.WEST);
        sliderPanel.add(imageUpdateRateSlider, BorderLayout.CENTER);
        sliderPanel.setMaximumSize(maximumSize);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
        southPanel.add(mouseStatusLabel);
        southPanel.add(Box.createHorizontalGlue());
        southPanel.add(sliderPanel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(statusLabel, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);
        return panel;
    }

    @SuppressWarnings("unchecked")
    private static <E> java.util.List<E> forceToCast(Object list) {
        return (java.util.List<E>)list;
    }
}
