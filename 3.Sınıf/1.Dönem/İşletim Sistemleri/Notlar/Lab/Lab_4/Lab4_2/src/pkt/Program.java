package pkt;

class NewThread implements Runnable {
	String name;
	Thread thread;
	
	public NewThread(String name) {
		this.name = name;
		thread = new Thread(this,name);
        System.out.println("Yeni thread: " + thread + " oluşturuldu\n");
        thread.start();
	}

	@Override
	public void run() {
		try {
			for(int j = 5; j > 0;j--) {
				System.out.println(name + ": " + j);
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			System.out.println(name + " thread kesintiye ugradi");
		}
		System.out.println(name + " thread cikiliyor");
	}
}

public class Program {
	
	
	public static void main(String[] args) {
		
		new NewThread("Thread 1.");
		new NewThread("Thread 2.");
		new NewThread("Thread 3.");
		
		try {
			Thread.sleep(8000);
		} catch (InterruptedException e) {
			System.out.println("Main Thread Kesintiye Ugradi");
		}
		System.out.println("Main Thread'den Cikiliyor");
	}
	
	

}
