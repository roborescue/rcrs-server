package firesimulator.world;

/**
 * @author tn
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Road extends Edge {

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
            System.out.println("Error: head or tail of an streetnode did not exist. exiting");
            System.exit(1);
        }
    }
	
    private void setHead(StreetNode node){
        head=node;
    }
	
    private void setTail(StreetNode node){
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
	
    private void setWidth(int width){
        this.width=width;
    }
	
    private void setBlock(int block){
        this.block=block;
    }
	
    private void setLinesToHead(int lines){
        linesToHead=lines;
    }
	
    private void setLinesToTail(int lines){
        linesToTail=lines;
    }
	
    public void input(String property, int[] value) {
        if ("WIDTH".equals(property)) {
            setWidth(value[0]);
        }
        else if ("BLOCK".equals(property)) {
            setBlock(value[0]);
        }
        else if ("LINES_TO_HEAD".equals(property)) {
            setLinesToHead(value[0]);
        }
        else if ("LINES_TO_TAIL".equals(property)) {
            setLinesToTail(value[0]);
        }
        else {
            super.input(property, value); 
        }
    }
}
