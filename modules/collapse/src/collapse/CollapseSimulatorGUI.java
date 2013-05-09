package collapse;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;

/**
   GUI for the collapse simulator.
*/
public class CollapseSimulatorGUI extends JPanel {
    private JLabel timeLabel;
    private JLabel statusLabel;
    private JProgressBar collapseProgress;
    private JProgressBar fireProgress;
    private JProgressBar blockadeProgress;

    private int collapse;
    private int fire;
    private int block;

    /**
       Construct a collapse simulator GUI.
    */
    public CollapseSimulatorGUI() {
        super(new GridLayout(0, 2));

        timeLabel = new JLabel("Not started");
        statusLabel = new JLabel("Not started");
        collapseProgress = new JProgressBar(0, 1);
        fireProgress = new JProgressBar(0, 1);
        blockadeProgress = new JProgressBar(0, 1);

        collapseProgress.setStringPainted(true);
        fireProgress.setStringPainted(true);
        blockadeProgress.setStringPainted(true);

        add(new JLabel("Timestep"));
        add(timeLabel);
        add(new JLabel("Status"));
        add(statusLabel);
        add(new JLabel("Collapsing buildings"));
        add(collapseProgress);
        add(new JLabel("Fire damage"));
        add(fireProgress);
        add(new JLabel("Creating blockades"));
        add(blockadeProgress);
    }

    /**
       Notify the gui that a new timestep has started.
       @param time The timestep.
    */
    void timestep(final int time) {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    timeLabel.setText(String.valueOf(time));
                    collapseProgress.setValue(0);
                    fireProgress.setValue(0);
                    blockadeProgress.setValue(0);
                    collapse = 0;
                    fire = 0;
                    block = 0;
                }
            });
    }

    /**
       Notify the gui that collapse computation has begun.
       @param buildingCount The number of buildings to process.
    */
    void startCollapse(final int buildingCount) {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    statusLabel.setText("Collapsing buildings");
                    collapseProgress.setMaximum(buildingCount);
                    collapseProgress.setValue(0);
                    collapse = 0;
                }
            });
    }

    /**
       Notify the gui that a building collapse has been processed.
    */
    void bumpCollapse() {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    collapseProgress.setValue(++collapse);
                }
            });
    }

    /**
       Notify the gui that building collapse computation is complete.
    */
    void endCollapse() {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    collapseProgress.setValue(collapseProgress.getMaximum());
                }
            });
    }

    /**
       Notify the gui that fire collapse computation has begun.
       @param buildingCount The number of buildings to process.
    */
    void startFire(final int buildingCount) {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    statusLabel.setText("Fire damage");
                    fireProgress.setMaximum(buildingCount);
                    fireProgress.setValue(0);
                    fire = 0;
                }
            });
    }

    /**
       Notify the gui that a fire collapse has been processed.
    */
    void bumpFire() {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireProgress.setValue(++fire);
                }
            });
    }

    /**
       Notify the gui that fire collapse computation is complete.
    */
    void endFire() {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireProgress.setValue(fireProgress.getMaximum());
                }
            });
    }

    /**
       Notify the gui that blockade generation has begun.
       @param buildingCount The number of buildings to process.
    */
    void startBlock(final int buildingCount) {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    statusLabel.setText("Computing blockades");
                    if (buildingCount == 0) {
                        blockadeProgress.setMaximum(1);
                        blockadeProgress.setValue(1);
                    }
                    else {
                        blockadeProgress.setMaximum(buildingCount);
                        blockadeProgress.setValue(0);
                        block = 0;
                    }
                }
            });
    }

    /**
       Notify the gui that blockade generation for a building has been processed.
    */
    void bumpBlock() {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    blockadeProgress.setValue(++block);
                }
            });
    }

    /**
       Notify the gui that blockade generation is complete.
    */
    void endBlock() {
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    blockadeProgress.setValue(blockadeProgress.getMaximum());
                    statusLabel.setText("Done");
                }
            });
    }
    
}