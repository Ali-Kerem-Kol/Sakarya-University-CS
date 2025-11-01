package pkt;

public class kubDelik {
	private int contents;
	private boolean available = false;
	
	public synchronized int get() {
		while (available == false) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO: handle exception
			}
		}
		available = false;
		notifyAll();
		return contents; //!!!!!
	}
	
	public synchronized void put(int value) {
		while(available == true) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO: handle exception
			}
		}
		contents = value;
		available = true;
		notifyAll();
	}
	
}

