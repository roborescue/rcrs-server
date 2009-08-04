package firesimulator.kernel.viewer.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import firesimulator.simulator.Monitor;
import firesimulator.simulator.Simulator;
import firesimulator.world.Building;
import firesimulator.world.World;

public class BuildingMonitor extends JPanel implements Monitor, WindowListener {
    

    private static final long serialVersionUID = 1L;
    public static final Color FIERYNESS_COL = Color.RED;
    public static final Color BURNED_COL = Color.GREEN.darker().darker();
    public static final Color FUEL_COL = Color.ORANGE.darker();
    public static final Color TEMP_COL = Color.BLUE;
    public static final Color WATER_COL = Color.CYAN;
    public static final Color GRID_COL = new Color(0.9f,0.9f,0.9f);
    
    public static final int ROUNDS = 301;
    public static  int Y_RES = 300;
    public static  int X_RES = 300;
    private static final double CORNER_OFFS=15;
    
    private static final double maxExpTemp=1000d;
    private static final double maxExpWater=10000d;
    private static final double UNDEFINED = Double.MIN_VALUE;
        
    private Building building;
    private Simulator simulator;
    private JFrame frame;
    
    private double[] fieryness;
    private double[] fuel;
    private double[] temp;
    private double[] burned;
    private double[] water;
    
    private boolean showFier = true;
    private boolean showTemp = true;
    private boolean showFuel = true;
    private boolean showBurn = true;
    private boolean showWatr = true;
    
    public BuildingMonitor(Building building, Simulator simulator){
        super();
        this.building = building;
        this.simulator = simulator;
        fieryness=new double[ROUNDS];
        fuel = new double[ROUNDS];
        temp = new double[ROUNDS];
        burned = new double[ROUNDS];
        water = new double[ROUNDS];        
        simulator.addMonitor(this);
        frame = new JFrame("Monitor Building "+building.getID());
        frame.setSize(400,400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        JPanel legend = new JPanel();
        JCheckBox box = new JCheckBox("Fieryness");
        box.setForeground(FIERYNESS_COL);            
        box.setSelected(showFier);
        box.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                showFier = !showFier;
                repaint();                
            }});
        legend.add(box);        
        box = new JCheckBox("Temperature");
        box.setForeground(TEMP_COL);        
        box.setSelected(showTemp);
        box.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                showTemp = !showTemp;
                repaint();                
            }});
        legend.add(box);
        box = new JCheckBox("Fuel");
        box.setForeground(FUEL_COL);
        box.setSelected(showFuel);
        box.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                showFuel= !showFuel;
                repaint();                
            }});
        legend.add(box);
        box = new JCheckBox("Rate");
        box.setForeground(BURNED_COL);
        box.setSelected(showBurn);
        box.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                showBurn= !showBurn;
                repaint();                
            }});
        legend.add(box);
        box = new JCheckBox("Water");
        box.setForeground(WATER_COL);
        box.setSelected(showWatr);
        box.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                showWatr= !showWatr;
                repaint();                
            }});
        legend.add(box);
        frame.getContentPane().add(legend,BorderLayout.SOUTH);
//        frame.getContentPane().add(toolBar,BorderLayout.SOUTH);
        frame.getContentPane().add(this,BorderLayout.CENTER);
        frame.addWindowListener(this);
        frame.setVisible(true);
    }
    
    public void step(World world) {
        int current = world.getTime();
        fieryness[current] = building.getFieryness();
        fuel[current] = building.getFuel();
        temp[current] = building.getTemperature();
        burned[current] = building.getPrevBurned();
        water[current] = building.getWaterQuantity();
    }

    public void reset(World world) {   
        reset(fieryness);
        reset(temp);
        reset(fuel);
        reset(burned);
        reset(water);
    }
    
    private void reset(double[] data){
        for(int i=0;i<data.length;i++)
            data[i] = UNDEFINED;
    }

    public void done(World world) {
        repaint();
    }
    
    public void paint(Graphics g1){
        X_RES=(int) (getWidth()-(2*CORNER_OFFS));
        Y_RES=(int) (getHeight()-(2*CORNER_OFFS));
        Graphics2D g = (Graphics2D) g1;
        g.setColor(Color.WHITE);
        g.fillRect(0,0,getWidth(),getHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform translate = new AffineTransform();
        translate.setToTranslation(CORNER_OFFS,getHeight()-CORNER_OFFS);
        AffineTransform scale = new AffineTransform();
        double sx = ((double)(getWidth()-(2*CORNER_OFFS)))/((double)X_RES);
        double sy = -((double)(getHeight()-(2*CORNER_OFFS)))/((double)Y_RES);
        scale.setToScale(sx,sy);
        translate.concatenate(scale);
        g.setTransform(translate);
        drawSystem(g);
        if(showTemp)drawArray(temp,g,TEMP_COL,0,maxExpTemp);
        if(showFier)drawArray(fieryness,g,FIERYNESS_COL,0,7);
        if(showWatr)drawArray(water,g,WATER_COL,0,maxExpWater);
        if(showFuel)drawArray(fuel,g,FUEL_COL);
        if(showBurn)drawArray(burned,g,BURNED_COL);
    }

    
    private void drawArray(double[] temp2,Graphics2D g,Color color){
        double max = Double.MIN_VALUE+10;
        double min = Double.MAX_VALUE;
        for(int i=0;i<temp2.length;i++){
            if(max<temp2[i])
                max=temp2[i];
            if(max>temp2[i])
                min=temp2[i];
        }
        drawArray(temp2,g,color,min,max);
    }
    
    private void drawArray(double[] temp2,Graphics2D g,Color color,double min, double max) {        
        g.setColor(color);
        double dx = ((double)X_RES)/((double)ROUNDS);
        double facY = Y_RES/max;
        for(int i = 1; i < ROUNDS-1 && temp2[i] != UNDEFINED; i++){            
            g.drawLine((int)((i-1)*dx),(int)(temp2[i-1]*facY),(int)(i*dx),(int)(temp2[i]*facY));                        
        }
    }

    private void drawSystem(Graphics2D g) {        
        double dx = ((double)X_RES)/((double)ROUNDS);
        double dy = ((double)Y_RES)/((double)20);
        g.setColor(GRID_COL);
        for(int i = 0; i < ROUNDS; i+=10){            
            g.drawLine((int)(i*dx),1,(int)(i*dx),Y_RES);                           
        }
        for(int i = 0; i < 20; i+=1){         
            int y = (int)(i*dy);
            g.drawLine(1,y,X_RES,y);                           
        }
        g.setColor(Color.BLACK);
        g.drawLine(0,0,X_RES,0);
        g.drawLine(0,0,0,Y_RES);        
        for(int i = 0; i < ROUNDS; i+=10){            
            g.drawLine((int)(i*dx),0,(int)(i*dx),-2);                           
//            g.drawString(new Integer(i).toString(),(float) (i*dx),-3f);
        }
    }

    public void windowOpened(WindowEvent e) {
      
    }

    public void windowClosing(WindowEvent e) {
        
    }

    public void windowClosed(WindowEvent e) {
        simulator.removeMonitor(this);
    }

    public void windowIconified(WindowEvent e) {
        
    }

    public void windowDeiconified(WindowEvent e) {
        
    }

    public void windowActivated(WindowEvent e) {
        
    }

    public void windowDeactivated(WindowEvent e) {
        
    }

}
