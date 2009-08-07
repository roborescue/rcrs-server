package traffic3.manager.gui;

import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 */
public class IO extends JComponent {

    private static JFileChooser fileChooser = new JFileChooser();

    /**
     * get file for open. This method returns existing file, otherwise exception will be thrown.
     * @param component parent component for dialog
     * @param filtername filtername
     * @param filters extentions
     * @return file
     * @throws UserCancelException user canceled
     * @throws FileNotFoundException file not found
     */
    public static File getOpenFile(JComponent component, String filtername, String... filters) throws UserCancelException, FileNotFoundException {
        component.requestFocus();
        javax.swing.filechooser.FileFilter filter = null;
        if (filtername != null) {
            filter = new FileNameExtensionFilter(filtername, filters);
        }
        fileChooser.resetChoosableFileFilters();
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showOpenDialog(component);
        if (result != JFileChooser.APPROVE_OPTION) {
            throw new UserCancelException();
        }
        File file = fileChooser.getSelectedFile();
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        return file;
    }

    /**
     * get file for save. This method will confirm if selected file already exists.
     * @param component parent component for dialog
     * @param filtername filtername
     * @param filters extentions
     * @return file
     */
    public static File getSaveFile(JComponent component, String filtername, String... filters) throws UserCancelException, FileNotFoundException {
        javax.swing.filechooser.FileFilter filter = null;
        if (filtername != null) {
            filter = new FileNameExtensionFilter(filtername, filters);
        }
        fileChooser.resetChoosableFileFilters();
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showSaveDialog(component);
        if (result != JFileChooser.APPROVE_OPTION) {
            throw new UserCancelException();
        }
        File file = fileChooser.getSelectedFile();
        if (file.exists()) {
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(component, "Specified file already exists.\nDo you want to over write?", "Confirm", JOptionPane.OK_CANCEL_OPTION)) {
                throw new UserCancelException();
            }
        }
        return file;
    }

    /**
     * get file for save. This method will confirm if selected file already exists.
     * @param component parent component for dialog
     * @return file
     */
    public static File getSaveFile(JComponent component) throws UserCancelException, FileNotFoundException {
        return getSaveFile(component, null);
    }

    /**
     * get file for open. This method returns existing file, otherwise exception will be thrown.
     * @param component parent component for dialog
     * @return file
     */
    public static File getOpenFile(JComponent component) throws UserCancelException, FileNotFoundException {
        return getOpenFile(component, null);
    }

}
