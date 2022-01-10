package gis2.scenario;

import gis2.GisScenario;
import gis2.ScenarioException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import maps.MapException;
import maps.MapReader;
import maps.gml.GMLMap;
import maps.gml.GMLRefuge;
import maps.gml.view.DecoratorOverlay;
import maps.gml.view.FilledShapeDecorator;
import maps.gml.view.GMLMapViewer;
import maps.gml.view.GMLObjectInspector;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import rescuecore2.config.Config;

/**
 * A component for editing scenarios.
 */
public class ScenarioEditor extends JPanel {

  private static final int VIEWER_PREFERRED_SIZE = 500;
  private static final int INSPECTOR_PREFERRED_WIDTH = 300;
  private static final int INSPECTOR_PREFERRED_HEIGHT = 500;

  private static final Color FIRE_COLOUR = new Color(255, 0, 0, 128);
  private static final Color FIRE_STATION_COLOUR = new Color(255, 255, 0);
  private static final Color POLICE_OFFICE_COLOUR = new Color(0, 0, 255);
  private static final Color AMBULANCE_CENTRE_COLOUR = new Color(255, 255, 255);
  private static final Color REFUGE_COLOUR = new Color(0, 128, 0);
  private static final Color HYDRANT_COLOUR = new Color(128, 128, 0);
  private static final Color GAS_STATION_COLOUR = new Color(255, 128, 0);

  private GMLMap map;
  private GMLMapViewer viewer;
  private GMLObjectInspector inspector;
  private DecoratorOverlay fireOverlay;
  private DecoratorOverlay centreOverlay;
  private transient AgentOverlay agentOverlay;
  private GisScenario scenario;
  private Tool currentTool;
  private JLabel statusLabel;

  private boolean changed;

  private UndoManager undoManager;
  private transient Action undoAction;
  private transient Action redoAction;

  private File baseDir;
  private File saveFile;

  private FilledShapeDecorator fireDecorator = new FilledShapeDecorator(
      FIRE_COLOUR, null, null);
  private FilledShapeDecorator fireStationDecorator = new FilledShapeDecorator(
      FIRE_STATION_COLOUR, null, null);
  private FilledShapeDecorator policeOfficeDecorator = new FilledShapeDecorator(
      POLICE_OFFICE_COLOUR, null, null);
  private FilledShapeDecorator ambulanceCentreDecorator = new FilledShapeDecorator(
      AMBULANCE_CENTRE_COLOUR, null, null);
  private FilledShapeDecorator refugeDecorator = new FilledShapeDecorator(
      REFUGE_COLOUR, null, null);
  private FilledShapeDecorator gasStationDecorator = new FilledShapeDecorator(
      GAS_STATION_COLOUR, null, null);
  private FilledShapeDecorator hydrantDecorator = new FilledShapeDecorator(null,
      HYDRANT_COLOUR, null);

  /**
   * Construct a new ScenarioEditor.
   *
   * @param menuBar
   *   The menu bar to add menus to.
   */
  public ScenarioEditor(JMenuBar menuBar) {
    this(menuBar, null, null);
  }


  /**
   * Construct a new ScenarioEditor.
   *
   * @param menuBar
   *   The menu bar to add menus to.
   * @param map
   *   The GMLMap to view.
   * @param scenario
   *   The scenario to edit.
   */
  public ScenarioEditor(JMenuBar menuBar, GMLMap map, GisScenario scenario) {
    super(new BorderLayout());
    this.map = map;
    this.scenario = scenario;
    viewer = new GMLMapViewer(map);
    viewer.setPaintNodes(false);
    statusLabel = new JLabel("Status");
    fireOverlay = new DecoratorOverlay();
    centreOverlay = new DecoratorOverlay();
    agentOverlay = new AgentOverlay(this);
    viewer.addOverlay(fireOverlay);
    viewer.addOverlay(centreOverlay);
    viewer.addOverlay(agentOverlay);
    inspector = new GMLObjectInspector(map);
    undoManager = new UndoManager();
    viewer.setPreferredSize(
        new Dimension(VIEWER_PREFERRED_SIZE, VIEWER_PREFERRED_SIZE));
    inspector.setPreferredSize(
        new Dimension(INSPECTOR_PREFERRED_WIDTH, INSPECTOR_PREFERRED_HEIGHT));
    viewer.setBackground(Color.GRAY);
    viewer.getPanZoomListener().setPanOnRightMouse();
    changed = false;
    JToolBar fileToolbar = new JToolBar("File");
    JToolBar editToolbar = new JToolBar("Edit");
    JToolBar toolsToolbar = new JToolBar("Tools");
    toolsToolbar.setLayout(new ModifiedFlowLayout());
    JToolBar functionsToolbar = new JToolBar("Functions");
    JMenu fileMenu = new JMenu("File", false);
    JMenu editMenu = new JMenu("Edit", false);
    JMenu toolsMenu = new JMenu("Tools", false);
    JMenu functionsMenu = new JMenu("Functions", false);

    createFileActions(fileMenu, fileToolbar);
    createEditActions(editMenu, editToolbar);
    createToolActions(toolsMenu, toolsToolbar);
    createFunctionActions(functionsMenu, functionsToolbar);

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewer,
        inspector);
    add(split, BorderLayout.CENTER);
    JPanel toolbars = new JPanel(new ModifiedFlowLayout());
    toolbars.add(fileToolbar);
    toolbars.add(editToolbar);
    toolbars.add(functionsToolbar);
    toolbars.add(toolsToolbar);
    add(toolbars, BorderLayout.NORTH);
    add(statusLabel, BorderLayout.SOUTH);
    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(toolsMenu);
    menuBar.add(functionsMenu);

    baseDir = new File(System.getProperty("user.dir"));
    saveFile = null;
  }


  /**
   * Entry point.
   *
   * @param args
   *   Command line arguments.
   */
  public static void main(String[] args) {
    final JFrame frame = new JFrame("Scenario Editor");
    JMenuBar menuBar = new JMenuBar();
    final ScenarioEditor editor = new ScenarioEditor(menuBar);
    if (args.length > 0 && args[0].length() > 0) {
      try {
        editor.load(args[0]);
      } catch (CancelledByUserException e) {
        return;
      } catch (MapException e) {
        e.printStackTrace();
      } catch (ScenarioException e) {
        e.printStackTrace();
      } catch (rescuecore2.scenario.exceptions.ScenarioException e) {
        e.printStackTrace();
      }
    }

    frame.setJMenuBar(menuBar);
    frame.setContentPane(editor);
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    frame.pack();
    frame.addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e) {
        try {
          editor.close();
          frame.setVisible(false);
          frame.dispose();
          System.exit(0);
        } catch (CancelledByUserException ex) {
          frame.setVisible(true);
        }
      }
    });
    frame.setVisible(true);
  }


  /**
   * Load a map and scenario by showing a file chooser dialog.
   *
   * @throws CancelledByUserException
   *   If the user cancels
   *   the change due to
   *   unsaved changes.
   * @throws MapException
   *   If there is a
   *   problem reading the
   *   map.
   * @throws ScenarioException
   *   If there is a
   *   problem reading the
   *   scenario.
   * @throws rescuecore2.scenario.exceptions.ScenarioException
   */
  public void load() throws CancelledByUserException, MapException,
      ScenarioException, rescuecore2.scenario.exceptions.ScenarioException {
    JFileChooser chooser = new JFileChooser(baseDir);
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setFileFilter(new FileFilter() {

      @Override
      public boolean accept(File f) {
        return f.isDirectory();
      }


      @Override
      public String getDescription() {
        return "Directories";
      }
    });
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      load(chooser.getSelectedFile());
    }
  }


  /**
   * Load a map and scenario from a directory.
   *
   * @param filename
   *   The name of the file to read.
   *
   * @throws CancelledByUserException
   *   If the user cancels
   *   the change due to
   *   unsaved changes.
   * @throws MapException
   *   If there is a
   *   problem reading the
   *   map.
   * @throws ScenarioException
   *   If there is a
   *   problem reading the
   *   scenario.
   * @throws rescuecore2.scenario.exceptions.ScenarioException
   */
  public void load(String filename)
      throws CancelledByUserException, MapException, ScenarioException,
      rescuecore2.scenario.exceptions.ScenarioException {
    load(new File(filename));
  }


  /**
   * Load a map and scenario from a directory.
   *
   * @param dir
   *   The directory to read.
   *
   * @throws CancelledByUserException
   *   If the user cancels
   *   the change due to
   *   unsaved changes.
   * @throws MapException
   *   If there is a
   *   problem reading the
   *   map.
   * @throws ScenarioException
   *   If there is a
   *   problem reading the
   *   scenario.
   * @throws rescuecore2.scenario.exceptions.ScenarioException
   */
  public void load(File dir) throws CancelledByUserException, MapException,
      ScenarioException, rescuecore2.scenario.exceptions.ScenarioException {

    try (FileReader r = new FileReader(new File(dir, "scenario.xml"))) {
      GMLMap newMap = (GMLMap) MapReader.readMap(new File(dir, "map.gml"));

      SAXReader saxReader = new SAXReader();
      Document doc = saxReader.read(r);
      GisScenario newScenario = new GisScenario(doc, new Config());
      setScenario(newMap, newScenario);
      baseDir = dir;
      saveFile = new File(dir, "scenario.xml");
    } catch (IOException | DocumentException e) {
      throw new ScenarioException(e);
    }
  }


  /**
   * Set the map and scenario.
   *
   * @param newMap
   *   The new map.
   * @param newScenario
   *   The new scenario.
   *
   * @throws CancelledByUserException
   *   If the user cancels the change due to
   *   unsaved changes.
   */
  public void setScenario(GMLMap newMap, GisScenario newScenario)
      throws CancelledByUserException {
    checkForChanges();
    if (!checkScenario(newMap, newScenario)) {
      JOptionPane.showMessageDialog(null,
          "The scenario file contained errors.");
      return;
    }
    map = newMap;
    scenario = newScenario;
    changed = false;
    viewer.setMap(map);
    inspector.setMap(map);
    updateOverlays();
  }


  public void updateGMLRefuges() {
    for (int next : scenario.getRefuges()) {
      GMLRefuge refuge = new GMLRefuge(next, map.getBuilding(next).getEdges());
      refuge.setBedCapacity(scenario.getRefugeBedCapacity().get(next));
      refuge.setRefillCapacity(scenario.getRefugeRefillCapacity().get(next));
      scenario.addGMLRefuge(refuge);
    }
  }


  /**
   * Get the map.
   *
   * @return The map.
   */
  public GMLMap getMap() {
    return map;
  }


  /**
   * Get the scenario.
   *
   * @return The scenario.
   */
  public GisScenario getScenario() {
    return scenario;
  }


  /**
   * Save the scenario.
   *
   * @throws ScenarioException
   *   If there is a problem saving the scenario.
   */
  public void save() throws ScenarioException {
    if (saveFile == null) {
      saveAs();
    }
    if (saveFile != null) {
      Document doc = DocumentHelper.createDocument();
      scenario.write(doc);
      try {
        if (!saveFile.exists()) {
          File parent = saveFile.getParentFile();
          if ((!parent.exists()) && (!saveFile.getParentFile().mkdirs())) {
            throw new ScenarioException(
                "Couldn't create file " + saveFile.getPath());
          }
          if (!saveFile.createNewFile()) {
            throw new ScenarioException(
                "Couldn't create file " + saveFile.getPath());
          }
        }
        XMLWriter writer = new XMLWriter(new FileOutputStream(saveFile),
            OutputFormat.createPrettyPrint());
        writer.write(doc);
        writer.flush();
        writer.close();
      } catch (IOException e) {
        throw new ScenarioException(e);
      }
      baseDir = saveFile.getParentFile();
      changed = false;
    }
  }


  /**
   * Save the scenario.
   *
   * @throws ScenarioException
   *   If there is a problem saving the scenario.
   */
  public void saveAs() throws ScenarioException {
    JFileChooser chooser = new JFileChooser(baseDir);
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      saveFile = chooser.getSelectedFile();
      save();
    }
  }


  /**
   * Close the editor.
   *
   * @throws CancelledByUserException
   *   If the user cancels the close due to unsaved
   *   changes."
   */
  public void close() throws CancelledByUserException {
    checkForChanges();
  }


  /**
   * Get the map viewer.
   *
   * @return The map viewer.
   */
  public GMLMapViewer getViewer() {
    return viewer;
  }


  /**
   * Get the object inspector.
   *
   * @return The object inspector.
   */
  public GMLObjectInspector getInspector() {
    return inspector;
  }


  /**
   * Register a change to the map.
   */
  public void setChanged() {
    changed = true;
  }


  /**
   * Register an undoable edit.
   *
   * @param edit
   *   The edit to add.
   */
  public void addEdit(UndoableEdit edit) {
    undoManager.addEdit(edit);
    undoAction.setEnabled(undoManager.canUndo());
    redoAction.setEnabled(undoManager.canRedo());
  }


  /**
   * Update the overlay views.
   */
  public void updateOverlays() {
    updateGMLRefuges();
    updateFireOverlay();
    updateCentreOverlay();
    updateAgentOverlay();
    updateStatusLabel();
    viewer.repaint();
  }


  private void checkForChanges() throws CancelledByUserException {
    if (changed) {
      switch (JOptionPane.showConfirmDialog(null,
          "The current scenario has changes. Do you want to save them?")) {
        case JOptionPane.YES_OPTION:
          try {
            save();
          } catch (ScenarioException e) {
            JOptionPane.showMessageDialog(null, e);
            throw new CancelledByUserException();
          }
          break;

        case JOptionPane.NO_OPTION:
          changed = false;
          return;

        case JOptionPane.CANCEL_OPTION:
          throw new CancelledByUserException();

        default:
          throw new RuntimeException(
              "JOptionPane.showConfirmDialog returned something weird");
      }
    }
  }


  private void createFileActions(JMenu menu, JToolBar toolbar) {
    Action newAction = new AbstractAction("New") {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          checkForChanges();
          setScenario(map, new GisScenario());
        } catch (CancelledByUserException ex) {
        }
      }
    };
    Action loadAction = new AbstractAction("Load") {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          checkForChanges();
          load();
        } catch (CancelledByUserException ex) {
        } catch (MapException | ScenarioException ex) {
          JOptionPane.showMessageDialog(null, ex);
        } catch (rescuecore2.scenario.exceptions.ScenarioException ex) {
          ex.printStackTrace();
        }
      }
    };
    Action saveAction = new AbstractAction("Save") {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          save();
        } catch (ScenarioException ex) {
          JOptionPane.showMessageDialog(null, ex);
        }
      }
    };
    Action saveAsAction = new AbstractAction("Save as") {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          saveAs();
        } catch (ScenarioException ex) {
          JOptionPane.showMessageDialog(null, ex);
        }
      }
    };
    toolbar.add(newAction);
    toolbar.add(loadAction);
    toolbar.add(saveAction);
    toolbar.add(saveAsAction);
    menu.add(newAction);
    menu.add(loadAction);
    menu.add(saveAction);
    menu.add(saveAsAction);
  }


  private void createEditActions(JMenu menu, JToolBar toolbar) {
    undoAction = new AbstractAction("Undo") {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          undoManager.undo();
        } catch (CannotUndoException ex) {
          JOptionPane.showMessageDialog(null, ex);
        }
        setEnabled(undoManager.canUndo());
        redoAction.setEnabled(undoManager.canRedo());
      }
    };
    redoAction = new AbstractAction("Redo") {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          undoManager.redo();
        } catch (CannotUndoException ex) {
          JOptionPane.showMessageDialog(null, ex);
        }
        setEnabled(undoManager.canRedo());
        undoAction.setEnabled(undoManager.canUndo());
      }
    };
    undoAction.setEnabled(false);
    redoAction.setEnabled(false);
    toolbar.add(undoAction);
    toolbar.add(redoAction);
    menu.add(undoAction);
    menu.add(redoAction);
  }


  private void createToolActions(JMenu menu, JToolBar toolbar) {
    ButtonGroup toolbarGroup = new ButtonGroup();
    ButtonGroup menuGroup = new ButtonGroup();
    menu.addSeparator();
    toolbar.addSeparator();
    addTool(new PlaceFireTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new RemoveFireTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new PlaceRefugeTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new RemoveRefugeTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new PlaceGasStationTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new RemoveGasStationTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new PlaceHydrantTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new RemoveHydrantTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new PlaceCivilianTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new RemoveCivilianTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    menu.addSeparator();
    toolbar.addSeparator();
    addTool(new PlaceFireBrigadeTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new RemoveFireBrigadeTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new PlacePoliceForceTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new RemovePoliceForceTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new PlaceAmbulanceTeamTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new RemoveAmbulanceTeamTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    menu.addSeparator();
    toolbar.addSeparator();
    addTool(new PlaceFireStationTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new RemoveFireStationTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new PlacePoliceOfficeTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new RemovePoliceOfficeTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new PlaceAmbulanceCentreTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
    addTool(new RemoveAmbulanceCentreTool(this), menu, toolbar, menuGroup,
        toolbarGroup);
  }


  private void createFunctionActions(JMenu menu, JToolBar toolbar) {
    addFunction(new RandomiseFunction(this), menu, toolbar);
    addFunction(new ClearFiresFunction(this), menu, toolbar);
    addFunction(new ClearAgentsFunction(this), menu, toolbar);
    addFunction(new ClearAllFunction(this), menu, toolbar);
    addFunction(new PlaceAgentsFunction(this), menu, toolbar);
    addFunction(new RandomHydrantPlacementFunction(this), menu, toolbar);
  }


  private void addTool(final Tool t, JMenu menu, JToolBar toolbar,
      ButtonGroup menuGroup, ButtonGroup toolbarGroup) {
    final JToggleButton toggle = new JToggleButton();
    final JCheckBoxMenuItem check = new JCheckBoxMenuItem();
    Action action = new AbstractAction(t.getName()) {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (currentTool != null) {
          currentTool.deactivate();
        }
        currentTool = t;
        toggle.setSelected(true);
        check.setSelected(true);
        currentTool.activate();
      }
    };
    toggle.setAction(action);
    check.setAction(action);
    menu.add(check);
    toolbar.add(toggle);
    menuGroup.add(check);
    toolbarGroup.add(toggle);
  }


  private void addFunction(final Function f, JMenu menu, JToolBar toolbar) {
    Action action = new AbstractAction(f.getName()) {

      @Override
      public void actionPerformed(ActionEvent e) {
        f.execute();
      }
    };
    toolbar.add(action);
    menu.add(action);
  }


  private boolean checkScenario(GMLMap newMap, GisScenario newScenario) {
    boolean valid = true;
    for (int id : newScenario.getFires()) {
      if (newMap.getBuilding(id) == null) {
        valid = false;
      }
    }
    for (int id : newScenario.getRefuges()) {
      if (newMap.getBuilding(id) == null) {
        valid = false;
      }
    }
    for (int id : newScenario.getHydrants()) {
      if (newMap.getRoad(id) == null) {
        valid = false;
      }
    }
    for (int id : newScenario.getFireStations()) {
      if (newMap.getBuilding(id) == null) {
        valid = false;
      }
    }
    for (int id : newScenario.getAmbulanceCentres()) {
      if (newMap.getBuilding(id) == null) {
        valid = false;
      }
    }
    for (int id : newScenario.getPoliceOffices()) {
      if (newMap.getBuilding(id) == null) {
        valid = false;
      }
    }

    for (int id : newScenario.getCivilians()) {
      if (newMap.getShape(id) == null) {
        valid = false;
      }
    }
    for (int id : newScenario.getFireBrigades()) {
      if (newMap.getShape(id) == null) {
        valid = false;
      }
    }
    for (int id : newScenario.getAmbulanceTeams()) {
      if (newMap.getShape(id) == null) {
        valid = false;
      }
    }
    for (int id : newScenario.getPoliceForces()) {
      if (newMap.getShape(id) == null) {
        valid = false;
      }
    }
    return valid;
  }


  private void updateStatusLabel() {
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        statusLabel.setText(scenario.getFires().size() + " fires, "
            + scenario.getRefuges().size() + " refuges, "
            + scenario.getHydrants().size() + " hydrants, "
            + scenario.getGasStations().size() + " gas stations, "
            + scenario.getCivilians().size() + " civilians, "
            + scenario.getFireBrigades().size() + " fb, "
            + scenario.getFireStations().size() + " fs, "
            + scenario.getPoliceForces().size() + " pf, "
            + scenario.getPoliceOffices().size() + " po, "
            + scenario.getAmbulanceTeams().size() + " at, "
            + scenario.getAmbulanceCentres().size() + " ac");
      }
    });
  }


  private void updateFireOverlay() {
    fireOverlay.clearAllBuildingDecorators();
    for (int next : scenario.getFires()) {
      fireOverlay.setBuildingDecorator(fireDecorator, map.getBuilding(next));
    }
  }


  private void updateCentreOverlay() {
    centreOverlay.clearAllBuildingDecorators();
    centreOverlay.clearAllRoadDecorators();
    for (int next : scenario.getFireStations()) {
      centreOverlay.setBuildingDecorator(fireStationDecorator,
          map.getBuilding(next));
    }
    for (int next : scenario.getPoliceOffices()) {
      centreOverlay.setBuildingDecorator(policeOfficeDecorator,
          map.getBuilding(next));
    }
    for (int next : scenario.getAmbulanceCentres()) {
      centreOverlay.setBuildingDecorator(ambulanceCentreDecorator,
          map.getBuilding(next));
    }
    for (int next : scenario.getRefuges()) {

      centreOverlay.setBuildingDecorator(refugeDecorator,
          scenario.getRefuge(next));
    }
    for (int next : scenario.getGasStations()) {
      centreOverlay.setBuildingDecorator(gasStationDecorator,
          map.getBuilding(next));
    }
    for (int next : scenario.getHydrants()) {
      centreOverlay.setRoadDecorator(hydrantDecorator, map.getRoad(next));
    }
  }


  private void updateAgentOverlay() {
  }
}