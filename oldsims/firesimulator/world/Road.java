package firesimulator.world;

import org.apache.log4j.Logger;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Road extends Edge {
    private static final Logger LOG = Logger.getLogger(Road.class);

    int width;
    int block;
    int linesToHead;
    int linesToTail;
    StreetNode head;
    StreetNode tail;
	
    public Road(int id) {
        super(id);
        head=null;
        tail=null;
    }
	
    public void initialize(World world){
        head=((StreetNode)world.getObject(getHeadID()));
        tail=((StreetNode)world.getObject(getTailID()));
        if(head==null||tail==null){
            LOG.fatal("Error: head or tail of an streetnode did not exist. exiting");
            System.exit(1);
        }
    }
	
    public void setHead(StreetNode node){
        head=node;
    }
	
    public void setTail(StreetNode node){
        tail=node;
    }
	
    public StreetNode getHead(){
        return head;
    }
	
    public StreetNode getTail(){
        return tail;
    }
	
    public String getType(){
        return "ROAD";
    }
	
    public void setWidth(int width){
        this.width=width;
    }
	
    public void setBlock(int block){
        this.block=block;
    }
	
    public void setLinesToHead(int lines){
        linesToHead=lines;
    }
	
    public void setLinesToTail(int lines){
        linesToTail=lines;
    }
}
