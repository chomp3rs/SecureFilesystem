//Dustin Behnke, Bryan Chin-Foon, Andrei Ta
public class BinarySemaphore {
    boolean value;
    BinarySemaphore(boolean initValue) {
        value = initValue;
    }
    public synchronized void P() {
        while (value == false)
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        value = false;
    }
    public synchronized void V() {
        value = true;
        notify();
    }
}