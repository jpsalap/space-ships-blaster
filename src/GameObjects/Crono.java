package GameObjects;


public class Crono {
    private long delta,lastTime;
    private long time;
    private boolean running;
    
    public Crono(){
        delta=0;
        lastTime=System.currentTimeMillis();
        running=false;
    }
    public void run(long time){
        running=true;
        this.time=time;
        delta=0;
        lastTime=System.currentTimeMillis();
    }
    public void update(){
        long now=System.currentTimeMillis();
        if(running){
            delta += now - lastTime;
            if(delta>=time){
                running=false;
                delta=0;
            }
        }
        lastTime=now;
    }
    public boolean isRunning(){
        return running;
    }
}
