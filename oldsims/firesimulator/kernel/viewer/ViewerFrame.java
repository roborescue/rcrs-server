package firesimulator.kernel.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import firesimulator.kernel.VirtualKernel;
import firesimulator.kernel.viewer.extinguishManager.ExtinguishManager;
import firesimulator.kernel.viewer.monitor.BuildingMonitor;
import firesimulator.simulator.Simulator;
import firesimulator.util.ComponentContainer;
import firesimulator.util.ComponentFactory;
import firesimulator.util.Configuration;
import firesimulator.world.Building;
import firesimulator.world.RescueObject;
import firesimulator.world.World;

/**
 * @author tn
 *
 */
public class ViewerFrame extends JFrame implements SelectorListener{
	
    private static final long serialVersionUID = 1L;
    public World world;
	Simulator sim;
	Map map;
	VirtualKernel kernel;
	JProgressBar progress;
	FuelColorizer fc;
	JTable inspectionTable;
	RescueObject selObject=null;
	String fname;
	Collection controllSetters;
	public ExtinguishManager em;
	Timer timer;
	boolean reset;
    JTabbedPane eastBar;
    private Set viewerFrameListener;
    final JComboBox buildingColorBox = new JComboBox();
    final JComboBox buildingBox=new JComboBox();
    final JComboBox roadBox=new JComboBox();
    final JComboBox gridBox=new JComboBox();
    final JComboBox gridColorBox=new JComboBox();
    
	public ViewerFrame(World world, VirtualKernel kernel,String fileName){
		super();
		reset=true;
        viewerFrameListener = new HashSet();
		controllSetters=new LinkedList();
		fname="default.stp";
		setTitle("Firesimulator Setup Environment: "+fileName);
		this.world=world;
		this.kernel=kernel;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1024,768);		
		initLayout();		
		setControlls();
		setVisible(true);		
	}
	
	public void register(Simulator simulator){
		sim=simulator;
	}
	
	public void saveSetup(String fileName){		
		try {			
			Configuration.storeHiddenProps(fileName);			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
				"unable to write\n"+fileName+"\n"+e.getLocalizedMessage(),
				"setup was not written",
				JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void loadSetup(String fileName){
		if(!Configuration.loadSetup(fileName)){		
			JOptionPane.showMessageDialog(this,
				"unable to load\n"+fileName,
				"setup was not loaded",
				JOptionPane.ERROR_MESSAGE);
				return;
		}	
		sim.reset();
		setControlls();
	}
	
	private void setControlls() {
		for(Iterator i =controllSetters.iterator();i.hasNext();){
			Setter s=(Setter) (i.next());
			s.setControll();
		}
	}
	
	protected void initLayout(){
		//################################################
		//#             creating menu                    #
		//################################################
		JMenuBar menuBar=new JMenuBar();
		setJMenuBar(menuBar);
		JMenu menu=new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		JMenuItem mItem=new JMenuItem("save setup");
		mItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				saveSetup(fname);								
			}
		});
		menu.add(mItem);
		mItem=new JMenuItem("save setup as..");
		mItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc=new JFileChooser();
				fc.setFileFilter(new SetpFileFilter());
				int retVal=fc.showSaveDialog(ViewerFrame.this);
				if (retVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					saveSetup(fname=file.getAbsolutePath());					
				}																
			}
		});
		menu.add(mItem);
		mItem=new JMenuItem("load setup..");
		mItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc=new JFileChooser();
				fc.setFileFilter(new SetpFileFilter());
				int retVal=fc.showOpenDialog(ViewerFrame.this);
				if (retVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					loadSetup(fname=file.getAbsolutePath());					
				}																												
			}
		});
		menu.add(mItem);
		menuBar.add(menu);
		//################################################
		//# 		creating southern bar                #
		//################################################
		Container cp=getContentPane();
		cp.setLayout(new BorderLayout());		
		JPanel southBar= new JPanel();
		southBar.setLayout(new BorderLayout());
		cp.add(southBar,BorderLayout.SOUTH);
		final JSlider goalCycle = new JSlider(JSlider.HORIZONTAL,0,300,15);
		progress=new JProgressBar();
		progress.setMinimum(0);
		progress.setStringPainted(true);
		final JLabel currentCycle=new JLabel(new Integer(world.getTime()).toString());
		JPanel timePanel=new JPanel(new BorderLayout());
		timePanel.add(currentCycle,BorderLayout.NORTH);
		timePanel.add(progress,BorderLayout.SOUTH);
		southBar.add(timePanel,BorderLayout.EAST);
		goalCycle.setMajorTickSpacing(50);
		goalCycle.setMinorTickSpacing(5);
		goalCycle.setPaintTicks(true);
		goalCycle.setPaintLabels(true);
		goalCycle.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				JSlider s=(JSlider)e.getSource();
				currentCycle.setText(new Integer(s.getValue()).toString());		
				if(!s.getValueIsAdjusting())jumpToGoal(s.getValue(),reset);	
		}});
		southBar.add(goalCycle,BorderLayout.CENTER);		
		//################################################
		//# 		creating eastern bar                 #
		//################################################
		eastBar =new JTabbedPane();		
		cp.add(eastBar,BorderLayout.EAST);
		//################################################
		//#		creating visual bar                      #
		//################################################
		JPanel visualSetting=new JPanel();
		visualSetting.setLayout(new GridBagLayout());		
		GridBagConstraints gbc=new GridBagConstraints();
		gbc.gridwidth=GridBagConstraints.REMAINDER;
		gbc.fill=GridBagConstraints.BOTH;
		gbc.gridheight=1;	
		map=new Map(world,new BuildingCodeColorizer());
		cp.add(map,BorderLayout.CENTER);
		eastBar.addTab("visual",visualSetting);
		buildingColorBox.setEditable(false);
		BuildingColorizer initColor;		
		buildingColorBox.addItem(initColor=new SelectedBuildingColorizer());
		map.registerSelectorListener((SelectorListener)initColor);
		//CONNECTION VIEW
		//colorizerBox.addItem(initColor=new BuildingConnectionColorizer());
		//map.registerSelectorListener((SelectorListener)initColor);
		buildingColorBox.addItem(initColor=new FierynessColorizer());
		buildingColorBox.addItem(fc=new FuelColorizer());
		buildingColorBox.addItem(new TempColorizer());
		buildingColorBox.addItem(new BuildingCodeColorizer());		
		buildingColorBox.addItem(new TypeColorizer());
		buildingColorBox.setSelectedItem(initColor);		
		map.setBuildingColorizer(initColor);
		JPanel bcpanel=new JPanel(new GridLayout());
		bcpanel.setBorder(BorderFactory.createTitledBorder(bcpanel.getBorder(),"building color"));
		bcpanel.add(buildingColorBox);
		buildingColorBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				map.setBuildingColorizer((BuildingColorizer)buildingColorBox.getSelectedItem());
				if(fc==buildingColorBox.getSelectedItem())fc.init(world.getBuildings());
				updateMap();
			}			
		});				
		
		buildingBox.setEditable(false);
		Painter tmp;
		buildingBox.addItem(tmp=new SolidPainter());
		buildingBox.addItem(new OutlinePainter());
		WallPainter wp;
		buildingBox.addItem(wp=new WallPainter());	
		map.registerSelectorListener(wp);
		buildingBox.addItem(new NullPainter());
		buildingBox.setSelectedItem(tmp);
		JPanel bppanel=new JPanel(new GridLayout());		
		bppanel.setBorder(BorderFactory.createTitledBorder(bppanel.getBorder(),"building style"));
		bppanel.add(buildingBox);
		map.setBuildingPainter(tmp);
		buildingBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				map.setBuildingPainter((Painter)buildingBox.getSelectedItem());
				map.update(map.getGraphics());
			}
		});		
		
		roadBox.setEditable(false);
		roadBox.addItem(new RoadPainter());		
		roadBox.addItem(tmp=new NullPainter());
		map.setRoadPainter(tmp);
		roadBox.setSelectedItem(tmp);
		JPanel rppanel=new JPanel(new GridLayout());
		rppanel=new JPanel(new GridLayout());
		rppanel.setBorder(BorderFactory.createTitledBorder(rppanel.getBorder(),"road style"));
		rppanel.add(roadBox);
		roadBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				map.setRoadPainter((Painter)roadBox.getSelectedItem());
				map.update(map.getGraphics());
			}
		});			
		final JCheckBox antialiasingCheck=new JCheckBox("antialias");
		antialiasingCheck.setSelected(map.getAntialias());
		antialiasingCheck.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				map.setAntialias(antialiasingCheck.isSelected());
				map.update(map.getGraphics());
			}
		});
		
		gridBox.setEditable(false);
		gridBox.addItem(new OutlinePainter());
		gridBox.addItem(new SolidPainter());		
		gridBox.addItem(new ValueSolidPainter());		
		gridBox.addItem(tmp=new NullPainter());
		gridBox.setSelectedItem(tmp);
		map.setAirPainter(tmp);
		JPanel gppanel=new JPanel(new GridLayout());
		gppanel=new JPanel(new GridLayout());
		gppanel.setBorder(BorderFactory.createTitledBorder(gppanel.getBorder(),"grid style"));
		gppanel.add(gridBox);
		gridBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				map.setAirPainter((Painter)gridBox.getSelectedItem());
				map.update(map.getGraphics());
			}
		});		
		gridColorBox.setEditable(false);		
		AirColorizer tair;
		gridColorBox.addItem(tair=new TempColorizer());		
		gridColorBox.setSelectedItem(tair);		
		map.setAirColorizer(tair);
		gridColorBox.addItem(tair=new AssignedColorizer(world));
		map.registerSelectorListener((SelectorListener)tair);		
		JPanel gcpanel=new JPanel(new GridLayout());
		gcpanel.setBorder(BorderFactory.createTitledBorder(gcpanel.getBorder(),"grid color"));
		gcpanel.add(gridColorBox);		
		gridColorBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				map.setAirColorizer((AirColorizer)gridColorBox.getSelectedItem());
				map.update(map.getGraphics());
			}
		});
		JPanel aniPanel=new JPanel(new GridLayout(2,1));
		aniPanel.setBorder(BorderFactory.createTitledBorder(aniPanel.getBorder(),"animate simulation"));
		final JFormattedTextField periodeF=new JFormattedTextField(ComponentFactory.getMaskFormater("##.# seconds"));
		periodeF.setText("01.0 seconds");
		
		JToggleButton autoButton=new JToggleButton("animate");
		autoButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JToggleButton tb=(JToggleButton)arg0.getSource();
				if(tb.isSelected()){
					String p=periodeF.getText();
					int w=p.indexOf(" seconds");
					p=p.substring(0,w);
					float f;
					try{
						f=Float.parseFloat(p);
					}catch(Exception e){
						f=1;
					}
					
					if(f<0.3)
						f=0.3f;
					int per=(int)(f*1000);				
					reset=false;
					timer=new Timer();
					timer.scheduleAtFixedRate(new TimerTask(){

						public void run() {
							int nt=goalCycle.getValue()+1;
							if(nt>=goalCycle.getMaximum())
								nt=0;
							goalCycle.setValue(nt);							
						}},1000,per);
				}else{
					timer.cancel();
					reset=true;
				}
			}});
		aniPanel.add(periodeF);
		aniPanel.add(autoButton);
		JButton monitorBuilding = new JButton("Monitor Building");
		monitorBuilding.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                if(selObject == null || !(selObject instanceof Building))
                    return;
                BuildingMonitor m = new BuildingMonitor((Building)selObject,sim);
                
            }});
		visualSetting.add(bppanel,gbc);
		visualSetting.add(bcpanel,gbc);
		visualSetting.add(gppanel,gbc);
		visualSetting.add(gcpanel,gbc);
		visualSetting.add(rppanel,gbc);
		visualSetting.add(aniPanel,gbc);
		visualSetting.add(antialiasingCheck,gbc);
		visualSetting.add(monitorBuilding);
		//################################################
		//# 	  	creating general bar                #
		//################################################						
		JPanel simPanel=new JPanel(new GridLayout(4,1));		
		eastBar.addTab("gerneral",simPanel);				
		ComponentContainer compcon;
		JPanel flowpanel=new JPanel(new GridLayout(4,1));		
		flowpanel.setBorder(BorderFactory.createTitledBorder(flowpanel.getBorder(),"energy flow"));
		compcon=ComponentFactory.createField(0,1,ComponentFactory.FLOAT,4,"radiation coefficient","","radiation_coefficient");
		controllSetters.add(compcon.getSetter());		
		flowpanel.add(compcon.getComponent(),compcon.getConstrains());
		compcon=ComponentFactory.createField(0,1,ComponentFactory.FLOAT,2,"atmospheric energy loss","%","energy_loss");
		controllSetters.add(compcon.getSetter());
		flowpanel.add(compcon.getComponent(),compcon.getConstrains());
		compcon=ComponentFactory.createField(0,1,ComponentFactory.FLOAT,2,"air to air flow","%","air_to_air_flow");		
		controllSetters.add(compcon.getSetter());
		flowpanel.add(compcon.getComponent(),compcon.getConstrains());
		compcon=ComponentFactory.createField(0,999,ComponentFactory.FLOAT,0,"air to building flow","","air_to_building_flow");		        
		controllSetters.add(compcon.getSetter());
		flowpanel.add(compcon.getComponent(),compcon.getConstrains());
		JPanel windpanel=new JPanel(new GridLayout(2,1));		
		windpanel.setBorder(BorderFactory.createTitledBorder(windpanel.getBorder(),"wind"));
		compcon=ComponentFactory.createField(0,360,ComponentFactory.FLOAT,1,"direction","�","wind_direction");		
		controllSetters.add(compcon.getSetter());
		windpanel.add(compcon.getComponent(),compcon.getConstrains());
		compcon=ComponentFactory.createField(0,5000,ComponentFactory.INT,0,"speed","mm","wind_speed");		
		controllSetters.add(compcon.getSetter());
		windpanel.add(compcon.getComponent(),compcon.getConstrains());
		JPanel extinguishpanel=new JPanel(new GridLayout(3,1));		
		extinguishpanel.setBorder(BorderFactory.createTitledBorder(extinguishpanel.getBorder(),"extinguish"));
		compcon=ComponentFactory.createField(0,9999,ComponentFactory.FLOAT,2,"thermal capacity","","water_thermal_capacity");		
		controllSetters.add(compcon.getSetter());
		extinguishpanel.add(compcon.getComponent(),compcon.getConstrains());
		compcon=ComponentFactory.createField(0,9999,ComponentFactory.INT,0,"max. quantity","","max_extinguish_power_sum");		
		controllSetters.add(compcon.getSetter());
		extinguishpanel.add(compcon.getComponent(),compcon.getConstrains());
		compcon=ComponentFactory.createField(0,99999,ComponentFactory.INT,0,"max. distance","","water_distance");		
		controllSetters.add(compcon.getSetter());
		extinguishpanel.add(compcon.getComponent(),compcon.getConstrains());
		simPanel.add(flowpanel);
		simPanel.add(windpanel);
		simPanel.add(extinguishpanel);		
		//################################################
		//# 			creating buidling bar            #
		//################################################						
		JPanel bPanel=new JPanel(new GridBagLayout());		
		eastBar.addTab("buildings",bPanel);	
		JPanel buildingSettings=new JPanel(new GridLayout(3,1));
		gbc=new GridBagConstraints();
		gbc.gridwidth=GridBagConstraints.REMAINDER;
		gbc.gridheight=5;		
		bPanel.add(buildingSettings,gbc);
		JPanel woodPanel=new JPanel(new GridLayout(5,1));
		woodPanel.setBorder(BorderFactory.createTitledBorder(woodPanel.getBorder(),"wooden buildings"));
		buildingSettings.add(woodPanel);
		compcon=ComponentFactory.createField(0,100,ComponentFactory.FLOAT,2,"heat capacity","","wooden_capacity");		
		controllSetters.add(compcon.getSetter());
		woodPanel.add(compcon.getComponent(),compcon.getConstrains());
		compcon=ComponentFactory.createField(0,99999,ComponentFactory.FLOAT,2,"energy density","","wooden_energy");				
		controllSetters.add(compcon.getSetter());
		woodPanel.add(compcon.getComponent(),compcon.getConstrains());
		compcon=ComponentFactory.createField(0,1000,ComponentFactory.FLOAT,2,"ignition point","","wooden_ignition");		
		controllSetters.add(compcon.getSetter());
		woodPanel.add(compcon.getComponent(),compcon.getConstrains());
		JPanel steelPanel=new JPanel(new GridLayout(5,1));
		steelPanel.setBorder(BorderFactory.createTitledBorder(steelPanel.getBorder(),"steel buildings"));
		buildingSettings.add(steelPanel);
		compcon=ComponentFactory.createField(0,100,ComponentFactory.FLOAT,2,"heat capacity","","steel_capacity");		
		controllSetters.add(compcon.getSetter());
		steelPanel.add(compcon.getComponent(),compcon.getConstrains());
		compcon=ComponentFactory.createField(0,99999,ComponentFactory.FLOAT,2,"energy density","","steel_energy");				
		controllSetters.add(compcon.getSetter());
		steelPanel.add(compcon.getComponent(),compcon.getConstrains());
		compcon=ComponentFactory.createField(0,1000,ComponentFactory.FLOAT,2,"ignition point","","steel_ignition");		
		controllSetters.add(compcon.getSetter());
		steelPanel.add(compcon.getComponent(),compcon.getConstrains());
		JPanel concretePanel=new JPanel(new GridLayout(5,1));
		concretePanel.setBorder(BorderFactory.createTitledBorder(concretePanel.getBorder(),"concrete buildings"));
		buildingSettings.add(concretePanel);
		compcon=ComponentFactory.createField(0,100,ComponentFactory.FLOAT,2,"heat capcity","","concrete_capacity");		
		controllSetters.add(compcon.getSetter());
		concretePanel.add(compcon.getComponent(),compcon.getConstrains());
		compcon=ComponentFactory.createField(0,99999,ComponentFactory.FLOAT,2,"energy density","","concrete_energy");				
		controllSetters.add(compcon.getSetter());
		concretePanel.add(compcon.getComponent(),compcon.getConstrains());
		compcon=ComponentFactory.createField(0,1000,ComponentFactory.FLOAT,2,"ignition point","","concrete_ignition");		
		controllSetters.add(compcon.getSetter());
		concretePanel.add(compcon.getComponent(),compcon.getConstrains());
		
		//################################################
		//#     	creating inspection bar              #
		//################################################
		JPanel inspectPanel=new JPanel(new GridLayout());
		inspectionTable=new JTable(new Object[9][2],new String[]{"name","value"});
		JScrollPane scrollPane = new JScrollPane(inspectionTable);		
		inspectionTable.setPreferredScrollableViewportSize(new Dimension(100, 300));
		inspectPanel.add(scrollPane);
		eastBar.addTab("inspect",inspectPanel);
		map.registerSelectorListener(this);
	 	em=new ExtinguishManager(this);
	 	map.registerSelectorListener(em);
	 	eastBar.addTab("extinguish simulator",em);
	}	
    
    public void addDialogField(String title, Component component){
        eastBar.addTab(title,component);
    }
    
    public void addViewerFrameListener(ViewerFrameListener l){
        viewerFrameListener.add(l);
    }
    
    public void removeViewerFrameListener(ViewerFrameListener l){
        viewerFrameListener.remove(l);
    }
    
    protected void fireViewerFrameEvent(int code, Object param){
        for(Iterator i = viewerFrameListener.iterator(); i.hasNext(); ((ViewerFrameListener)i.next()).inform(code,param));
    }
    
    public void initDone() {
        fireViewerFrameEvent(ViewerFrameListener.EV_INIT_DONE,world);
    }
    
    public void addBuildingColor(BuildingColorizer c){
        buildingColorBox.addItem(c);
    }
    
    public void addBuildingPainter(Painter p){
        buildingBox.addItem(p);
    }
    
    public void addRoadPainter(RoadPainter p){
        roadBox.addItem(p);
    }
    
    public void addGridPainter(Painter p){
        gridBox.addItem(p);
    }

    public void addGridColor(AirColorizer c){
        gridColorBox.addItem(c);
    }
    
    
	protected void jumpToGoal(int i,boolean reset) {
		progress.setMaximum(i);
		progress.setValue(0);
		progress.setString("initiating");
		progress.update(progress.getGraphics());
//		kernel.jumpTo(i,reset);
		kernel.jumpTo(i,true);	
		progress.setValue(i);
		progress.setString("complete");
		fc.init(world.getBuildings());
		setInspection();
	}
	
	public void informProgress(int cycle,int goal){
		if(cycle%10==0){
			progress.setValue(cycle);
			progress.setString(cycle+" of "+goal);
			progress.update(progress.getGraphics());
		}
	}

	public void updateMap(){
		map.update(map.getGraphics());
	}


	public void select(RescueObject o,int modifier) {
		if(o==null)return;
		selObject=o;
		setInspection();		
	}

    public Map getMap(){
        return map;
    }
	
	private void setInspection() {
		if(selObject instanceof Building){
			Building b=(Building)selObject;
			inspectionTable.setValueAt("id",0,0);
			inspectionTable.setValueAt(new Integer(b.getID()),0,1);
			inspectionTable.setValueAt("floors",1,0);
			inspectionTable.setValueAt(new Integer(b.getFloors()),1,1);
			inspectionTable.setValueAt("code",2,0);
			inspectionTable.setValueAt(b.codeToString(),2,1);
			inspectionTable.setValueAt("fieryness",3,0);
			inspectionTable.setValueAt(new Integer(b.getFieryness()),3,1);
			inspectionTable.setValueAt("ignition point",4,0);
			inspectionTable.setValueAt(new Integer(b.getIgnition()),4,1);
			inspectionTable.setValueAt("fuel",5,0);
			inspectionTable.setValueAt(new Float(b.fuel)+"("+b.getInitialFuel()+")",5,1);
			inspectionTable.setValueAt("temperature",6,0);
			inspectionTable.setValueAt(new Float(b.getTemperature()),6,1);			
			inspectionTable.setValueAt("water",7,0);
			inspectionTable.setValueAt(new Integer(b.getWaterQuantity()),7,1);
			inspectionTable.setValueAt("",8,0);
			inspectionTable.setValueAt("",8,1);
			return;
		}
	}

}
