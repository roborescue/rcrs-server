package firesimulator.kernel.viewer;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * @author tn
 *
 */
public class SetpFileFilter extends FileFilter {


	public boolean accept(File f) {
		if (f.isDirectory()) return true;
		if(f.getAbsolutePath().endsWith(".stp"))return true;			   
		return false;
	}


	public String getDescription() {
		return "firesimulator setup file";
	}

}
