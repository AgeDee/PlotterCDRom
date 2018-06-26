package hellopi;

public class AxisThreadController implements Runnable {

    String tName;
    public Thread t;
    Axis axis;
    int temp = 0;
    
    public AxisThreadController(Axis axis, String name){
        this.axis = axis;
        tName = name;
    }
    
    @Override
    public void run() {
        System.out.println("Watek o nazwie: " + tName);
       
            axis.setPos(40, 10); //pozycja 0 predkosc 1mm/s
            axis.setPos(0, 4); //pozycja 0 predkosc 1mm/s
            axis.setPos(40, 8); //pozycja 0 predkosc 1mm/s
            axis.setPos(0, 20); //pozycja 0 predkosc 1mm/s
            axis.setPos(40, 2); //pozycja 0 predkosc 1mm/s
            axis.setPos(0, 1); //pozycja 0 predkosc 1mm/s
        
    }
    
    public void start(int x){
        if (t == null) {
            t = new Thread (this, tName);
            temp = x;
            t.start ();
        }
    }
    
}
