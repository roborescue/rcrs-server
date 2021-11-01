/*
 * Last change: $Date: 2004/05/04 03:09:39 $
 * $Revision: 1.3 $
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

package rescuecore.tools.simulationrunner;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class ProcessViewer extends JPanel {
    private JTabbedPane tabs;
    private JTable summary;
    private ProcessTableModel model;

    private final static int PROCESS_COLUMN_NAME = 0;
    private final static int PROCESS_COLUMN_COMMAND = 1;
    private final static int PROCESS_COLUMN_STATUS = 2;
    private final static int PROCESS_COLUMN_COUNT = 3;
    private final static String[] PROCESS_COLUMN_NAMES = {"Name","Command line","Status"};

    /**
       Generate a ProcessViewer that will view the given processes
	*/
    public ProcessViewer(RescueProcess[] processes) {
		super(new BorderLayout());
		model = new ProcessTableModel(processes);
		summary = new JTable(model);
		tabs = new JTabbedPane();
		add(new JScrollPane(summary),BorderLayout.CENTER);
		add(tabs,BorderLayout.EAST);
		//		for (int i=0;i<processes.length;++i) {
		//			RescueProcess next = processes[i];
			/*
			  Class clazz = filters.get(next.name);
			  if (clazz==null) clazz = DefaultProcessFilter.class;
			  try {
			  ProcessFilter filter = (ProcessFilter)clazz.newInstance();
			  PipedOutputStream out = new PipedOutputStream();
			  PipedInputStream in = new PipedInputStream(out);
			  filter.setInputStream(in);
		    
			  }
			  catch (Exception e) {
			  e.printStackTrace();
			  }
			*/
		//		}
    }

    /**
       Notification that a process is starting
       @param index The index of the process that is starting
	*/
    public void processStarting(int index) {
		model.setStatus(index,"Starting");
    }

    /**
       Notification that a process has started
       @param index The index of the process that has started
	*/
    public void processStarted(int index) {
		model.setStatus(index,"Running");
    }

    /**
       Notification that a process has stopped
       @param index The index of the process that has stopped
	*/
    public void processStopped(int index) {
		model.setStatus(index,"Stopped");
    }

    private class ProcessTableModel extends AbstractTableModel {
		private String[] names, commands, statuses;

		ProcessTableModel(RescueProcess[] processes) {
			names = new String[processes.length];
			commands = new String[processes.length];
			statuses = new String[processes.length];
			for (int i=0;i<processes.length;++i) {
				names[i] = processes[i].getName();
				commands[i] = processes[i].getCommandLine();
				statuses[i] = "Not started";
			}
		}

		void setStatus(int index, String status) {
			statuses[index] = status;
			fireTableDataChanged();
		}

		public int getRowCount() {
			return names.length;
		}

		public int getColumnCount() {
			return PROCESS_COLUMN_COUNT;
		}

		public Object getValueAt(int row, int col) {
			switch (col) {
			case PROCESS_COLUMN_NAME:
				return names[row];
			case PROCESS_COLUMN_COMMAND:
				return commands[row];
			case PROCESS_COLUMN_STATUS:
				return statuses[row];
			default:
				throw new RuntimeException("Unknown column: "+col);
			}
		}

		public String getColumnName(int col) {
			return PROCESS_COLUMN_NAMES[col];
		}

		public Class getColumnClass(int col) {
			return String.class;
		}
    }
}

