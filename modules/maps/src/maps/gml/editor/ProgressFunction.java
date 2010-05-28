package maps.gml.editor;

import java.awt.Window;
import java.awt.Dialog;
import java.awt.BorderLayout;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import rescuecore2.log.Logger;

/**
   Abstract base class for Function implementations that require a progress dialog.
*/
public abstract class ProgressFunction extends AbstractFunction {
    private JProgressBar progress;

    /**
       Construct a ProgressFunction.
       @param editor The editor instance.
    */
    protected ProgressFunction(GMLEditor editor) {
        super(editor);
        progress = new JProgressBar();
        progress.setStringPainted(true);
    }

    @Override
    public void execute() {
        final JDialog dialog = new JDialog((Window)editor.getViewer().getTopLevelAncestor(), getTitle(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getContentPane().add(progress, BorderLayout.CENTER);
        dialog.pack();
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setValue(0);
                    progress.setIndeterminate(true);
                }
            });
        Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        executeImpl();
                    }
                    // CHECKSTYLE:OFF:IllegalCatch
                    catch (RuntimeException e) {
                        // CHECKSTYLE:ON:IllegalCatch
                        Logger.error("Error running " + this, e);
                    }
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            };
        t.start();
        dialog.setVisible(true);
    }

    /**
       Execute the function.
    */
    protected abstract void executeImpl();

    /**
       Get the title for the progress dialog.
       @return The dialog title.
    */
    protected abstract String getTitle();

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
       Set the progress string.
       @param s The new progress string.
    */
    protected void setProgressString(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progress.setString(s);
                }
            });
    }
}