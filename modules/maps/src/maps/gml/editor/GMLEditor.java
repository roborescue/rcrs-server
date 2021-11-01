package maps.gml.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import maps.MapException;
import maps.MapReader;
import maps.MapWriter;
import maps.gml.GMLCoordinates;
import maps.gml.GMLMap;
import maps.gml.GMLObject;
import maps.gml.formats.RobocupFormat;
import maps.gml.view.GMLMapViewer;
import maps.gml.view.GMLObjectInspector;

import rescuecore2.log.Logger;

/**
 * A component for editing GML maps.
 */
public class GMLEditor extends JPanel {
  private static final int VIEWER_PREFERRED_SIZE = 500;
  private static final int INSPECTOR_PREFERRED_WIDTH = 300;
  private static final int INSPECTOR_PREFERRED_HEIGHT = 500;

  private static final double SNAP_MIN_RESOLUTION = 0.001;
  private static final double SNAP_MAX_RESOLUTION = 1000;

  private static final NumberFormat FORMAT = new DecimalFormat("#0.000");

  private GMLMap map;
  private GMLMapViewer viewer;
  private GMLObjectInspector inspector;
  private JLabel x;
  private JLabel y;
  private boolean changed;
  private ViewerMouseListener viewerMouseListener;
  private Tool currentTool;

  private UndoManager undoManager;
  private Action undoAction;
  private Action redoAction;

  private File saveFile;
  private File baseDir;

  private Snap snap;

  /**
   * Construct a new GMLEditor.
   *
   * @param menuBar The menu bar to add menus to.
   */
  public GMLEditor(JMenuBar menuBar) {
    super(new BorderLayout());
    map = new GMLMap();
    viewer = new GMLMapViewer(map);
    inspector = new GMLObjectInspector(map);
    undoManager = new UndoManager();
    viewer.setPreferredSize(new Dimension(VIEWER_PREFERRED_SIZE, VIEWER_PREFERRED_SIZE));
    inspector.setPreferredSize(new Dimension(INSPECTOR_PREFERRED_WIDTH, INSPECTOR_PREFERRED_HEIGHT));
    viewer.setBackground(Color.GRAY);
    viewer.getPanZoomListener().setPanOnRightMouse();
    snap = new Snap();
    changed = false;
    x = new JLabel("X: ");
    y = new JLabel("Y: ");
    JToolBar fileToolbar = new JToolBar("File");
    JToolBar viewToolbar = new JToolBar("View");
    JToolBar editToolbar = new JToolBar("Edit");
    JToolBar toolsToolbar = new JToolBar("Tools");
    JToolBar functionsToolbar = new JToolBar("Functions");
    JMenu fileMenu = new JMenu("File", false);
    JMenu viewMenu = new JMenu("View", false);
    JMenu editMenu = new JMenu("Edit", false);
    JMenu toolsMenu = new JMenu("Tools", false);
    JMenu functionsMenu = new JMenu("Functions", false);

    createFileActions(fileMenu, fileToolbar);
    createViewActions(viewMenu, viewToolbar);
    createEditActions(editMenu, editToolbar);
    createToolActions(toolsMenu, toolsToolbar);
    createFunctionActions(functionsMenu, functionsToolbar);

    JPanel main = new JPanel(new BorderLayout());
    JPanel labels = new JPanel(new GridLayout(1, 2));
    labels.add(x);
    labels.add(y);
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewer, inspector);
    main.add(split, BorderLayout.CENTER);
    main.add(labels, BorderLayout.SOUTH);
    add(main, BorderLayout.CENTER);
    JPanel toolbars = new JPanel(new GridLayout(0, 1));
    toolbars.add(fileToolbar);
    toolbars.add(viewToolbar);
    toolbars.add(editToolbar);
    toolbars.add(toolsToolbar);
    toolbars.add(functionsToolbar);
    add(toolbars, BorderLayout.NORTH);
    menuBar.add(fileMenu);
    menuBar.add(viewMenu);
    menuBar.add(editMenu);
    menuBar.add(toolsMenu);
    menuBar.add(functionsMenu);

    viewerMouseListener = new ViewerMouseListener();
    viewer.addMouseListener(viewerMouseListener);
    viewer.addMouseMotionListener(viewerMouseListener);

    baseDir = new File(System.getProperty("user.dir"));
  }

  /**
   * Entry point.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    final JFrame frame = new JFrame("GMLEditor");
    JMenuBar menuBar = new JMenuBar();
    final GMLEditor editor = new GMLEditor(menuBar);
    if (args.length > 0 && args[0].length() > 0) {
      try {
        editor.load(args[0]);
      } catch (CancelledByUserException e) {
        return;
      } catch (MapException e) {
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
   * Load a map by showing a file chooser dialog.
   *
   * @throws CancelledByUserException If the user cancels the change due to
   *                                  unsaved changes.
   * @throws MapException             If there is a problem reading the map.
   */
  public void load() throws CancelledByUserException, MapException {
    JFileChooser chooser = new JFileChooser(baseDir);
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      load(chooser.getSelectedFile());
    }
  }

  /**
   * Load a map from a file.
   *
   * @param filename The name of the file to read.
   * @throws CancelledByUserException If the user cancels the change due to
   *                                  unsaved changes.
   * @throws MapException             If there is a problem reading the map.
   */
  public void load(String filename) throws CancelledByUserException, MapException {
    load(new File(filename));
  }

  /**
   * Load a map from a file.
   *
   * @param file The file to read.
   * @throws CancelledByUserException If the user cancels the change due to
   *                                  unsaved changes.
   * @throws MapException             If there is a problem reading the map.
   */
  public void load(File file) throws CancelledByUserException, MapException {
    setMap((GMLMap) MapReader.readMap(file));
    saveFile = file;
    baseDir = saveFile.getParentFile();
  }

  /**
   * Set the map.
   *
   * @param newMap The new map.
   * @throws CancelledByUserException If the user cancels the change due to
   *                                  unsaved changes.
   */
  public void setMap(GMLMap newMap) throws CancelledByUserException {
    checkForChanges();
    map = newMap;
    changed = false;
    viewer.setMap(map);
    inspector.setMap(map);
    viewer.repaint();
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
   * Save the map.
   *
   * @throws MapException If there is a problem saving the map.
   */
  public void save() throws MapException {
    if (saveFile == null) {
      JFileChooser chooser = new JFileChooser(baseDir);
      if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        saveFile = chooser.getSelectedFile();
      }
    }
    if (saveFile != null) {
      Logger.debug("Saving to " + saveFile.getAbsolutePath());
      MapWriter.writeMap(map, saveFile, RobocupFormat.INSTANCE);
      baseDir = saveFile.getParentFile();
      changed = false;
    }
  }

  /**
   * Close the editor.
   *
   * @throws CancelledByUserException If the user cancels the close due to unsaved
   *                                  changes."
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
   * @param edit The edit to add.
   */
  public void addEdit(UndoableEdit edit) {
    undoManager.addEdit(edit);
    undoAction.setEnabled(undoManager.canUndo());
    redoAction.setEnabled(undoManager.canRedo());
  }

  /**
   * Snap coordinates to the grid.
   *
   * @param c The coordinates to snap.
   * @return The passed-in coordinates object.
   */
  public GMLCoordinates snap(GMLCoordinates c) {
    snap.snap(c);
    return c;
  }

  private void updatePositionLabels(final Point p) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        GMLCoordinates c = viewer.getCoordinatesAtPoint(p.x, p.y);
        x.setText("X: " + FORMAT.format(c.getX()));
        y.setText("Y: " + FORMAT.format(c.getY()));
      }
    });
  }

  private void checkForChanges() throws CancelledByUserException {
    if (changed) {
      switch (JOptionPane.showConfirmDialog(null, "The current map has changes. Do you want to save them?")) {
        case JOptionPane.YES_OPTION:
          try {
            save();
          } catch (MapException e) {
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
          throw new RuntimeException("JOptionPane.showConfirmDialog returned something weird");
      }
    }
  }

  private void createFileActions(JMenu menu, JToolBar toolbar) {
    Action newAction = new AbstractAction("New") {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          checkForChanges();
          setMap(new GMLMap());
        } catch (CancelledByUserException ex) {
          return;
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
          return;
        } catch (MapException ex) {
          JOptionPane.showMessageDialog(null, ex);
        }
      }
    };
    Action saveAction = new AbstractAction("Save") {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          save();
        } catch (MapException ex) {
          JOptionPane.showMessageDialog(null, ex);
        }
      }
    };
    Action saveAsAction = new AbstractAction("Save as") {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          saveFile = null;
          save();
        } catch (MapException ex) {
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

  private void createViewActions(JMenu menu, JToolBar toolbar) {
    final JCheckBox snapBox = new JCheckBox("Snap to grid", snap.isEnabled());
    final JSpinner snapSpinner = new JSpinner(
        new SpinnerNumberModel(snap.getResolution(), SNAP_MIN_RESOLUTION, SNAP_MAX_RESOLUTION, SNAP_MIN_RESOLUTION));
    snapSpinner.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        snap.setResolution((Double) snapSpinner.getValue());
      }
    });
    snapBox.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        snap.setEnabled(snapBox.isSelected());
      }
    });
    Action gridAction = new AbstractAction("Show grid") {
      @Override
      public void actionPerformed(ActionEvent e) {
        viewer.setGridEnabled((Boolean) getValue(Action.SELECTED_KEY));
        viewer.repaint();
      }
    };
    gridAction.putValue(Action.SELECTED_KEY, false);
    toolbar.add(snapSpinner);
    toolbar.add(snapBox);
    toolbar.add(new JToggleButton(gridAction));
    menu.add(new JCheckBoxMenuItem(gridAction));

    // Create the "show objects" button and textfield
    final JTextField showField = new JTextField();
    JButton showButton = new JButton("Show");
    showButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String s = showField.getText();
        List<GMLObject> objects = new ArrayList<GMLObject>();
        for (String next : s.split(",")) {
          int id = Integer.parseInt(next.trim());
          GMLObject o = map.getObject(id);
          if (o != null) {
            objects.add(o);
          }
        }
        viewer.view(objects);
        viewer.repaint();
      }
    });
    toolbar.addSeparator();
    toolbar.add(showField);
    toolbar.add(showButton);

    // Add the reset zoom action
    Action resetZoom = new AbstractAction("Reset zoom") {
      @Override
      public void actionPerformed(ActionEvent e) {
        viewer.viewAll();
        viewer.repaint();
      }
    };
    toolbar.addSeparator();
    menu.addSeparator();
    toolbar.add(resetZoom);
    menu.add(resetZoom);
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
    addTool(new InspectTool(this), menu, toolbar, menuGroup, toolbarGroup);
    menu.addSeparator();
    toolbar.addSeparator();
    addTool(new CreateNodeTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new CreateEdgeTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new CreateRoadTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new CreateBuildingTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new CreateSpaceTool(this), menu, null, menuGroup, null);
    menu.addSeparator();
    toolbar.addSeparator();
    addTool(new DeleteNodeTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new DeleteEdgeTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new DeleteShapeTool(this), menu, toolbar, menuGroup, toolbarGroup);
    menu.addSeparator();
    toolbar.addSeparator();
    addTool(new MoveNodeTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new MergeNodesTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new MergeLinesTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new SplitEdgeTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new SplitShapeTool(this), menu, toolbar, menuGroup, toolbarGroup);
    addTool(new TogglePassableTool(this), menu, toolbar, menuGroup, toolbarGroup);
  }

  private void createFunctionActions(JMenu menu, JToolBar toolbar) {
    addFunction(new ScaleFunction(this), menu, toolbar);
    addFunction(new FixNearbyNodesFunction(this), menu, toolbar);
    addFunction(new FixDuplicateEdgesFunction(this), menu, toolbar);
    addFunction(new SplitEdgesFunction(this), menu, toolbar);
    addFunction(new ComputePassableEdgesFunction(this), menu, toolbar);
    addFunction(new PruneOrphanNodesFunction(this), menu, toolbar);
    addFunction(new PruneOrphanEdgesFunction(this), menu, toolbar);
    addFunction(new FixDegenerateShapesFunction(this), menu, toolbar);
    addFunction(new FixAttachedObjectsFunction(this), menu, toolbar);
    addFunction(new ValidateFunction(this), menu, toolbar);
    addFunction(new AddNoiseFunction(this), menu, toolbar);
  }

  private void addTool(final Tool t, JMenu menu, JToolBar toolbar, ButtonGroup menuGroup, ButtonGroup toolbarGroup) {
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
    if (toolbar != null) {
      toolbar.add(toggle);
      toolbarGroup.add(toggle);
    }
    menuGroup.add(check);
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

  private class ViewerMouseListener implements MouseListener, MouseMotionListener {
    @Override
    public void mouseMoved(MouseEvent e) {
      updatePositionLabels(fixEventPoint(e.getPoint()));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      updatePositionLabels(fixEventPoint(e.getPoint()));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      updatePositionLabels(fixEventPoint(e.getPoint()));
    }

    private Point fixEventPoint(Point p) {
      Insets insets = viewer.getInsets();
      return new Point(p.x - insets.left, p.y - insets.top);
    }
  }
}