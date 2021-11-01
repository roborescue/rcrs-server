package rescuecore.debug;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import rescuecore.view.*;
import rescuecore.*;

public class CollectionHandler implements Handler {
	private Collection current;

	private Layer layer;
	private CollectionView component;

	public JComponent getComponent() {
		if (component==null) {
			component = new CollectionView();
			component.setObjects(current);
		}
		return component;
	}

	public Layer getLayer() {
		if (layer==null) {
			layer = new Layer("Collection handler");
			layer.addObjects(current);
		}
		return layer;
	}

	public boolean handle(Object o, int timeStep) {
		if (o instanceof Collection) {
			current = (Collection)o;
			if (layer!=null) layer.setObjects(current);
			if (component != null) component.setObjects(current);
			return true;
		}
		return false;
	}

	public void setMemory(Memory m) {
	}

	private static class CollectionView extends JPanel {
		private CollectionModel model;

		public CollectionView() {
			super(new BorderLayout());
			model = new CollectionModel();
			add(new JScrollPane(new JList(model)),BorderLayout.CENTER);
		}

		public void setObjects(Collection c) {
			model.setObjects(c);
		}

		private static class CollectionModel extends AbstractListModel {
			private String[] data;

			public CollectionModel() {
				data = new String[0];
			}

			public int getSize() {
				return data.length;
			}

			public Object getElementAt(int index) {
				return data[index];
			}

			public void setObjects(Collection c) {
				data = new String[c.size()];
				int i=0;
				for (Iterator it = c.iterator();it.hasNext();++i) {
					Object next = it.next();
					data[i] = next==null?"null":next.toString();
				}
				fireContentsChanged(this,0,data.length);
			}
		}
	}
}
