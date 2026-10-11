package maps.convert;

import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import lombok.Getter;
import lombok.Setter;
import rescuecore2.misc.gui.ShapeDebugFrame;

public abstract class ConvertStep {
    @Setter @Getter private static boolean guiEnabled = false;

    private JProgressBar progress;
    private JLabel status;
    private int progressValue;
    private int progressMax;

    protected ShapeDebugFrame debug;

    protected ConvertStep() {
        debug = new ShapeDebugFrame();
        if (guiEnabled) {
            progress = new JProgressBar();
            progress.setString("");
            progress.setStringPainted(true);
            status = new JLabel();
        } else {
            debug.deactivate();
        }
    }

    protected void setProgress(int amount) {
        progressValue = amount;
        if (!guiEnabled) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            progress.setValue(amount);
            progress.setString(progress.getValue() + " / " + progress.getMaximum());
        });
    }

    protected void bumpProgress() {
        progressValue++;
        if (!guiEnabled) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            progress.setValue(progress.getValue() + 1);
            progress.setString(progress.getValue() + " / " + progress.getMaximum());
        });
    }

    protected void setProgressLimit(int max) {
        progressMax = max;
        if (!guiEnabled) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            progress.setIndeterminate(false);
            progress.setMaximum(max);
            progress.setString(progress.getValue() + " / " + progress.getMaximum());
        });
    }

    protected int getProgressLimit() {
        return progressMax;
    }

    protected void setStatus(String s) {
        if (guiEnabled) {
            SwingUtilities.invokeLater(() -> status.setText(s));
        } else {
            System.out.println(s);
        }
    }

    public JProgressBar getProgressBar() {
        return progress;
    }

    public JComponent getStatusComponent() {
        return status;
    }

    public void doStep() {
        if (guiEnabled) {
            SwingUtilities.invokeLater(() -> progress.setIndeterminate(true));
        } else {
            System.out.println(getDescription());
        }
        step();
        if (guiEnabled) {
            SwingUtilities.invokeLater(() -> {
                progress.setIndeterminate(false);
                progress.setValue(progress.getMaximum());
            });
        }
    }

    public abstract String getDescription();

    protected abstract void step();
}
