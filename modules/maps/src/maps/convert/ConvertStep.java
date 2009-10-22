package maps.convert;

import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import rescuecore2.misc.gui.ShapeDebugFrame;

/**
   A step in the map conversion process.
*/
public abstract class ConvertStep {
    /** A ShapeDebugFrame for use by subclasses. */
    protected ShapeDebugFrame debug;

    private JProgressBar progress;
    private JLabel status;

    /**
       Construct a ConvertStep.
    */
    protected ConvertStep() {
        this.progress = new JProgressBar();
        this.status = new JLabel();
        progress.setString("");
        progress.setStringPainted(true);
        debug = new ShapeDebugFrame();
    }

    /**
       Set the progress level.
       @param amount The new progress.
    */
    protected void setProgress(final int amount) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setValue(amount);
                    progress.setString(progress.getValue() + " / " + progress.getMaximum());
                }
            });
    }

    /**
       Increase the progress level by one.
    */
    protected void bumpProgress() {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setValue(progress.getValue() + 1);
                    progress.setString(progress.getValue() + " / " + progress.getMaximum());
                }
            });
    }

    /**
       Increase the maximum progress level by one.
    */
    protected void bumpMaxProgress() {
        bumpMaxProgress(1);
    }

    /**
       Increase the maximum progress level by some amount.
       @param amount The amount to increase the maximum progress level.
    */
    protected void bumpMaxProgress(final int amount) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setMaximum(progress.getMaximum() + amount);
                    progress.setString(progress.getValue() + " / " + progress.getMaximum());
                }
            });
    }

    /**
       Set the progress maximum.
       @param max The new progress maximum.
    */
    protected void setProgressLimit(final int max) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setIndeterminate(false);
                    progress.setMaximum(max);
                    progress.setString(progress.getValue() + " / " + progress.getMaximum());
                }
            });
    }

    /**
       Set the status label.
       @param s The new status label.
    */
    protected void setStatus(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    status.setText(s);
                }
            });
    }

    /**
       Get the JProgressBar component for this step.
       @return The progress bar component.
    */
    public JProgressBar getProgressBar() {
        return progress;
    }

    /**
       Get the status component for this step.
       @return The status component.
    */
    public JComponent getStatusComponent() {
        return status;
    }

    /**
       Perform the conversion step.
    */
    public final void doStep() {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setIndeterminate(true);
                }
            });
        step();
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setIndeterminate(false);
                    progress.setValue(progress.getMaximum());
                }
            });
        debug.deactivate();
    }

    /**
       Get a user-friendly description of this step.
       @return A description string.
     */
    public abstract String getDescription();

    /**
       Perform the step.
    */
    protected abstract void step();
}