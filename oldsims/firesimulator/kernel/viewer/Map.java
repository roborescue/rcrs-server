package firesimulator.kernel.viewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.VolatileImage;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JPanel;

import firesimulator.world.Building;
import firesimulator.world.World;


public class Map extends JPanel implements MouseListener{
	
    private static final long serialVersionUID = 1L;
    public static boolean ENABLE = true; 
    private BuildingColorizer buildingColorizer;
	private Painter buildingPainter;
	private Painter roadPainter;
	private Painter airPainter;
	private AirColorizer airColorizer;
	private World world;
  	private VolatileImage doubleBuffer;
  	private Collection selectorListener;
  	private AffineTransform lastAffineTransform;
  	private boolean antialias;
  	
	public Map(World world,BuildingColorizer bc){
		this.world=world;
		selectorListener=new LinkedList();
		addMouseListener(this);
		antialias=true;
		setBuildingColorizer(bc);
		setBuildingPainter(new OutlinePainter());
		setRoadPainter(new RoadPainter());
		setAirPainter(new SolidPainter());
		setAirColorizer(new TempColorizer());
		doubleBuffer=createVolatileImage(getWidth(),getWidth());
	}
    
    public Map(){
        super();
    }
	
	void setAirColorizer(AirColorizer col){
		airColorizer=col;
	}

	void setAirPainter(Painter painter) {
		airPainter=painter;
	}

	public void setRoadPainter(Painter painter) {
		roadPainter=painter;
	}

	public void setBuildingPainter(Painter painter) {
		buildingPainter=painter;
	}

	public void registerSelectorListener(SelectorListener sl){
		if(!selectorListener.contains(sl))selectorListener.add(sl);
	}
	
	public void unregisterSelectorListener(SelectorListener sl){
		selectorListener.remove(sl);
	}
		
	
	public void paint(Graphics g){
	    if(!ENABLE)
	        return;
		if(doubleBuffer==null||doubleBuffer.getWidth()!=getWidth()||doubleBuffer.getHeight()!=getHeight()){
			doubleBuffer=createVolatileImage(getWidth(),getHeight());
		}
		renderOffscreen();
		g.drawImage(doubleBuffer,0,0,this);
	}
	
	private void renderOffscreen(){
		do {
			if (doubleBuffer.validate(getGraphicsConfiguration()) ==VolatileImage.IMAGE_INCOMPATIBLE){
				doubleBuffer = createVolatileImage(getWidth(),getHeight());				
			}
			Graphics2D g = doubleBuffer.createGraphics();
			if(world.isIntialized()){
				prepareBackground(g);
				setAffineTransformation(g);
				if(antialias)g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				else g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
				long t1=System.currentTimeMillis();
				drawAir(g);
				drawBuildings(g);									
				drawRoads(g);
			}else drawNothing(g);
			g.dispose();
		} while (doubleBuffer.contentsLost());
	}

	private void drawAir(Graphics2D g){
		airPainter.paint(g,world,airColorizer);
	}

	private void drawRoads(Graphics2D g) {
		roadPainter.paint(g,world,(BuildingColorizer)null);
	}

	public void setBuildingColorizer(BuildingColorizer bc){
		buildingColorizer=bc; 
	}
	private BuildingColorizer getColorizer(){
		return buildingColorizer;
	}
	
	private void drawNothing(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0,0,doubleBuffer.getWidth(),doubleBuffer.getHeight());
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);		
		Font f = new Font( "SansSerif", Font.PLAIN, 40 );		
		g.setFont(f);		
		g.setColor(Color.DARK_GRAY);
		String hello=" ResQ Firesimulator";
		Rectangle2D bound=g.getFont().getStringBounds(hello,g.getFontRenderContext());
		g.drawString(hello,((float)(doubleBuffer.getWidth()-bound.getWidth())/2),((float)(doubleBuffer.getHeight()-bound.getHeight())/2));
		String hello2="is getting ready...";
		f = new Font( "SansSerif", Font.ITALIC, 20 );		
		g.setFont(f);		
		g.setColor(Color.DARK_GRAY);
		Rectangle2D bound2=g.getFont().getStringBounds(hello2,g.getFontRenderContext());
		g.drawString(hello2,((float)(doubleBuffer.getWidth()-bound2.getWidth())/2),((float)(doubleBuffer.getHeight()+bound.getHeight())/2));
	}

	private void drawBuildings(Graphics2D g) {		
		buildingPainter.paint(g,world,getColorizer());
	}

	private void setAffineTransformation(Graphics2D g) {
		AffineTransform at=new AffineTransform();								
		double sx=(double)doubleBuffer.getWidth()/(double)(world.getMaxX()-world.getMinX());
		double sy=(double)doubleBuffer.getHeight()/(double)(world.getMaxY()-world.getMinY());
		double s=Math.min(sx,sy);								
		at.setToScale(s,s);
		at.translate((double)-world.getMinX(),(double)-world.getMinY());
		g.transform(at);		
		lastAffineTransform=g.getTransform();		
	}

	private void prepareBackground(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0,0,doubleBuffer.getWidth(),doubleBuffer.getHeight());
	}

	public void mouseClicked(MouseEvent e) {
		if(lastAffineTransform!=null&&selectorListener!=null){
			double x=(1/lastAffineTransform.getScaleX()*(double)e.getX())+((double)world.getMinX());
			double y=(1/lastAffineTransform.getScaleY()*(double)e.getY())+((double)world.getMinY());											
			for(Iterator i=world.getBuildings().iterator();i.hasNext();){
				Building b=(Building)i.next();
				if(b.getPolygon().contains(x,y)){
					for(Iterator it =selectorListener.iterator();it.hasNext();((SelectorListener)it.next()).select(b,e.getButton()));
					update(getGraphics());													
					return;	
				}
			}
			for(Iterator it =selectorListener.iterator();it.hasNext();((SelectorListener)it.next()).select(null,e.getButton()));			
		}
		update(getGraphics());		
	}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	protected void setAntialias(boolean b) {
		antialias=b;
	}
	
	public boolean getAntialias(){
		return antialias;
	}

}
