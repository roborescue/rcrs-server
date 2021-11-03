/*
 * Last change: $Date: 2004/05/31 01:56:13 $ $Revision: 1.6 $ Copyright (c)
 * 2004, The Black Sheep, Department of Computer Science, The University of
 * Auckland All rights reserved. Redistribution and use in source and binary
 * forms, with or without modification, are permitted provided that the
 * following conditions are met: Redistributions of source code must retain the
 * above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. Neither the name of
 * The Black Sheep, The Department of Computer Science or The University of
 * Auckland nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package rescuecore.debug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Debugger extends JPanel {

  private Collection<DebugPane> panes;
  // private WorldLog log;
  private DebugLog log;
  private JTabbedPane tabs;
  private JSlider time;

  public Debugger() {
    super(new BorderLayout());
    tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    // lower panel
    JPanel bottom = new JPanel();
    JPanel timePanel = new JPanel(new BorderLayout());
    Border b = BorderFactory.createLineBorder(Color.BLACK);
    b = BorderFactory.createTitledBorder(b, "Time Step");
    timePanel.setBorder(b);
    time = new JSlider(0, 300, 0);
    time.setPaintLabels(true);
    time.setPaintTicks(true);
    time.setSnapToTicks(true);
    time.setMinorTickSpacing(1);
    time.setMajorTickSpacing(50);
    JButton left = new JButton(" < ");
    JButton right = new JButton(" > ");
    left.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        moveTime(-1);
      }
    });
    right.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        moveTime(1);
      }
    });
    time.addChangeListener(new ChangeListener() {

      public void stateChanged(ChangeEvent e) {
        setTimeStep(time.getValue());
      }
    });
    timePanel.add(time, BorderLayout.CENTER);
    timePanel.add(left, BorderLayout.WEST);
    timePanel.add(right, BorderLayout.EAST);

    add(tabs, BorderLayout.CENTER);
    add(timePanel, BorderLayout.SOUTH);
  }

  public void init(File file) throws IOException, ClassNotFoundException {
    // log = new WorldLog(file);
    log = new DebugLog(file);
    // Create one tab per rescue component
    tabs.removeAll();
    // AgentLog[] aLogs = log.getAgentLogs();
    Collection<String> names = log.getAllNames();
    panes = new ArrayList<DebugPane>(names.size());
    for (String next : names) {
      DebugPane pane = makeDebugPane(next, log);
      panes.add(pane);
      tabs.add(pane);
    }
    try {
      registerHandler(CommandHandler.class);
      registerHandler(StringHandler.class);
      registerHandler(UpdateHandler.class);
    } catch (Exception e) {
      // Should never happen
      e.printStackTrace();
    }
    time.setMaximum(log.getMaxTimestep());
    setTimeStep(0);
  }

  public int getTimeStep() {
    return time.getValue();
  }

  public void registerHandler(String className) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    registerHandler(Class.forName(className));
  }

  public void registerHandler(Class clazz)
      throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    for (DebugPane next : panes) {
      Handler hand = (Handler) clazz.getDeclaredConstructor().newInstance();
      next.registerHandler(hand);
    }
  }

  @Deprecated
  public void registerHandler(Handler h) {
    try {
      registerHandler(h.getClass());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void moveTime(int change) {
    time.setValue(time.getValue() + change);
  }

  public DebugPane makeDebugPane(String name, DebugLog log) {
    return new DebugPane(log, name);
  }

  private void setTimeStep(int time) {
    for (DebugPane next : panes) {
      next.moveToTimeStep(time);
    }
  }

  public static void main(String[] args) {
    Debugger d = null;
    String fileName = null;
    Collection handlers = new ArrayList();
    for (int i = 0; i < args.length; ++i) {
      if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--help")) {
        printUsage();
        return;
      } else if (fileName == null)
        fileName = args[i];
      else {
        // Is this a file?
        File f = new File(args[i]);
        if (f.exists()) {
          String nextClass = null;
          try {
            BufferedReader in = new BufferedReader(new FileReader(f));
            nextClass = in.readLine();
            while (nextClass != null) {
              if (!nextClass.equals("") && !nextClass.startsWith("#")) {
                handlers.add(nextClass);
              }
              nextClass = in.readLine();
            }
          } catch (IOException e) {
            System.err.println("Error reading file " + args[i] + ": " + e);
          }
        } else {
          handlers.add(args[i]);
        }
      }
    }

    d = new Debugger();

    File file = null;
    try {
      if (fileName == null) {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          file = chooser.getSelectedFile();
        } else
          System.exit(0);
      } else {
        file = new File(fileName);
      }
      d.init(file);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
    for (Iterator it = handlers.iterator(); it.hasNext();) {
      String next = (String) it.next();
      try {
        d.registerHandler(next);
      } catch (Exception e) {
        System.err.println("Couldn't register handler " + next + ": " + e);
      }
    }
    JFrame frame = new JFrame("Debugger");
    frame.setContentPane(d);
    frame.addWindowListener(new WindowAdapter() {

      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    Toolkit t = Toolkit.getDefaultToolkit();
    Dimension size = t.getScreenSize();
    frame.setSize((int) size.getWidth(), (int) size.getHeight());
    frame.setVisible(true);
  }

  private static void printUsage() {
    System.out.println("Usage: Debugger [options] [filename [handlers]]");
    System.out.println(
        "Any handlers will be loaded and registered automatically. You can specify handlers as either fully-qualified class names or file names. Each line in each file name should contain the fully-qualified class name of a handler, lines beginning with a # will be ignored");
    System.out.println("Options");
    System.out.println("-h\t--help\tPrint this message");
  }
}