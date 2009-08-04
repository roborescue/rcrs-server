package firesimulator.kernel.viewer.extinguishManager;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import firesimulator.kernel.viewer.SelectorListener;
import firesimulator.kernel.viewer.ViewerFrame;
import firesimulator.world.Building;
import firesimulator.world.RescueObject;

/**
 * @author tn
 *
 */
public class ExtinguishManager extends JPanel implements SelectorListener{
	
	/**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    private class TimeFrame extends JDialog{   	
		/**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 1L;
		private final JTextField quantityField=new JTextField("1000");			
		private final JTextField startField=new JTextField("000");			
		private final JTextField endField=new JTextField("000");
		
		static final int MODE_NEW = 0;
		static final int MODE_EDIT = 1;
		
		int mode = MODE_NEW;			
		
        public TimeFrame(){
			super();
			setTitle("extinguish job");
			setModal(true);
			//super("extinguish job");			
			setSize(250,150);
			setLocation(300,300);
			getContentPane().setLayout(new GridBagLayout());
			GridBagConstraints constraints=new GridBagConstraints();
			constraints.gridwidth=1;
			//constraints.gridwidth=GridBagConstraints.REMAINDER;

			quantityField.setColumns(4);
			JPanel dummyPanel=new JPanel();
			dummyPanel.add(quantityField);	
			dummyPanel.setBorder(BorderFactory.createTitledBorder(dummyPanel.getBorder(),"water"));					
			getContentPane().add(dummyPanel,constraints);

			startField.setColumns(3);
			dummyPanel=new JPanel();
			dummyPanel.add(startField);
			dummyPanel.setBorder(BorderFactory.createTitledBorder(dummyPanel.getBorder(),"start"));						
			getContentPane().add(dummyPanel,constraints);

			endField.setColumns(3);
			dummyPanel=new JPanel();
			dummyPanel.add(endField);
			dummyPanel.setBorder(BorderFactory.createTitledBorder(dummyPanel.getBorder(),"end"));
			constraints.gridwidth=GridBagConstraints.REMAINDER;						
			getContentPane().add(dummyPanel,constraints);			
			JButton ok=new JButton("ok");
			ok.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if (mode == MODE_NEW)
						continueNewExtinguish(quantityField.getText(),startField.getText(),endField.getText());
					else if (mode == MODE_EDIT)
						continueEditExtinguish(quantityField.getText(),startField.getText(),endField.getText());
					setVisible(false);
				}});
			getContentPane().add(ok,constraints);
		}
		
		public void setValues(int quantity, int start, int end)
		{
			quantityField.setText(Integer.toString(quantity));
			startField.setText(Integer.toString(start));
			endField.setText(Integer.toString(end));
		}
	}
	
	ViewerFrame parent;
	Building selectedBuilding;
	ExtinguishJob editJob;
	Collection jobs;
	JPanel jobsPanel;
	TimeFrame tf;
	
	public ExtinguishManager(ViewerFrame parent){
		this.parent=parent;
		selectedBuilding=null;
		jobs=new LinkedList();
		tf=new TimeFrame();
		setLayout(new BorderLayout());
		JToolBar buttonBar=new JToolBar();
		buttonBar.setFloatable(false);
		add(buttonBar,BorderLayout.NORTH);
		JButton extinguishButton=new JButton("new extinguish job");
		extinguishButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				makeExtinguishJob();
			}});
		buttonBar.add(extinguishButton);
		jobsPanel=new JPanel();
		//jobsPanel.setBackground(Color.BLUE);
		jobsPanel.setLayout(new BoxLayout(jobsPanel,BoxLayout.Y_AXIS));
		//jobsPanel.setLayout(new GridBagLayout());
		add(jobsPanel,BorderLayout.CENTER);
	}

	public void makeExtinguishJob(){
		if(selectedBuilding==null)return;
		tf.mode = TimeFrame.MODE_NEW;
		tf.setValues(1000,parent.world.getTime(), parent.world.getTime());
		tf.setVisible(true);
	}
	
	public void continueNewExtinguish(String q,String s,String e){
		try {
			int quantity=new Integer(q).intValue();
			int start=new Integer(s).intValue();
			int end=new Integer(e).intValue();
			ExtinguishJob exJob=new ExtinguishJob(quantity,start,end,selectedBuilding,parent.world,this);			
			GridBagConstraints gbc=new GridBagConstraints();
			gbc.gridwidth=GridBagConstraints.REMAINDER;
			jobsPanel.add(exJob);
			jobs.add(exJob);
			jobsPanel.validate();
			jobsPanel.update(jobsPanel.getGraphics());			
		} catch (Exception ex) {			
			ex.printStackTrace();
		}
	}

	public void removeJob(ExtinguishJob ej){
		jobs.remove(ej);
		jobsPanel.remove(ej);
		jobsPanel.validate();
		jobsPanel.update(jobsPanel.getGraphics());
	}
	
	public void modifyJob(ExtinguishJob ej){
		tf.setValues(ej.quantity, ej.start, ej.end);
		tf.mode = TimeFrame.MODE_EDIT;
		editJob = ej;	
		tf.setVisible(true);
	}

	public void continueEditExtinguish(String q,String s,String e){
		try {
			int quantity=new Integer(q).intValue();
			int start=new Integer(s).intValue();
			int end=new Integer(e).intValue();
			editJob.quantity = quantity;
			editJob.start = start;
			editJob.end = end;

			jobsPanel.validate();
			jobsPanel.update(jobsPanel.getGraphics());
		} catch (Exception ex) {			
			ex.printStackTrace();
		}
	}


	public void select(RescueObject o, int modifier) {
		selectedBuilding=(Building)o;
	}
	
	public void nextCycle(){
		for(Iterator i=jobs.iterator();i.hasNext();((ExtinguishJob)i.next()).execute());
	}

}
