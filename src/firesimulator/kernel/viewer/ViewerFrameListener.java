package firesimulator.kernel.viewer;

public interface ViewerFrameListener {

    int EV_INIT_DONE = 0;
    
    public void inform(int type, Object param);
    
}
