/*
 * Last change: $Date: 2004/07/11 22:26:28 $
 * $Revision: 1.4 $
 *
 * Copyright (c) 2004, The Black Sheep, Department of Computer Science, The University of Auckland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of The Black Sheep, The Department of Computer Science or The University of Auckland nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package rescuecore.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import rescuecore.*;
import rescuecore.event.*;

public class ObjectInspector extends JPanel implements ObjectSelectionListener, PropertyListener {
    private PropertiesTableModel model;
    private RescueObject view;
    private int[] properties;
	private boolean showUpdateData;

    private final static int COLUMN_PROPERTY = 0;
    private final static int COLUMN_VALUE = 1;
	private final static int COLUMN_LAST_UPDATE = 2;
	private final static int COLUMN_UPDATE_SOURCE = 3;
    private final static int NUM_COLUMNS = 4;
	private final static int NUM_BASIC_COLUMNS = 2;

	public ObjectInspector() {
		this(true);
	}

    public ObjectInspector(boolean showUpdates) {
		super(new BorderLayout());
		model = new PropertiesTableModel();
		add(new JScrollPane(new JTable(model)),BorderLayout.CENTER);
		showUpdateData = showUpdates;
    }

    public void objectSelected(ObjectSelectionEvent e) {
		if (e.getSelectedObject()==null) return;
		if (e.getSelectedObject() instanceof RescueObject) showObject((RescueObject)e.getSelectedObject());
    }

    public void propertyChanged(PropertyChangedEvent event) {
		model.refresh();
    }

    public void showObject(RescueObject o) {
		if (view!=null) view.removePropertyListener(this);
		view = o;
		properties = view==null?null:view.getKnownPropertyTypes();
		model.refresh();
		if (view!=null) view.addPropertyListener(this);
    }

    private class PropertiesTableModel extends AbstractTableModel {
		public void refresh() {
			fireTableDataChanged();
		}

		public int getRowCount() {
			return view==null?0:properties.length+2;
		}

		public int getColumnCount() {
			return showUpdateData?NUM_COLUMNS:NUM_BASIC_COLUMNS;
		}

		public String getColumnName(int col) {
			switch(col) {
			case COLUMN_PROPERTY:
				return "Property";
			case COLUMN_VALUE:
				return "Value";
			case COLUMN_LAST_UPDATE:
				return "Last update";
			case COLUMN_UPDATE_SOURCE:
				return "Update source";
			default:
				throw new IllegalArgumentException("Unknown column: "+col);
			}
		}

		public Class getColumnClass(int col) {
			return String.class;
		}

		public Object getValueAt(int row, int col) {
			Property p;
			switch (col) {
			case COLUMN_PROPERTY:
				if (row==0) return "ID";
				if (row==1) return "Type";
				return Handy.getPropertyName(properties[row-2]);
			case COLUMN_VALUE:
				if (row==0) return ""+view.getID();
				if (row==1) return Handy.getTypeName(view.getType());
				p = view.getProperty(properties[row-2]);
				if (p==null) return "<unknown>";
				return p.getStringValue();
			case COLUMN_LAST_UPDATE:
				if (row==0) return "";
				if (row==1) return "";
				p = view.getProperty(properties[row-2]);
				if (p==null) return "<unknown>";
				return ""+p.getLastUpdate();
			case COLUMN_UPDATE_SOURCE:
				if (row==0) return "";
				if (row==1) return "";
				p = view.getProperty(properties[row-2]);
				if (p==null) return "<unknown>";
				Object result = p.getLastUpdateSource();
				return result==null?"null":result.toString();
			default:
				throw new IllegalArgumentException("Unknown column: "+col);
			}
		}	
    }
}
