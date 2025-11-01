package pkt;


class ThreadB extends Thread {
	int total;
	
	@Override
	public void run() {
		for(int i=0;i<100;i++) {
			total+=i;
		}
	}
}
public class Program {

	public static void main(String[] args) {
		ThreadB b =new ThreadB();
		b.start();
		synchronized (b) {
			try {
			System.out.println("B thread inin islemini bitirmesini bekle");
			b.wait();
			} catch (InterruptedException e) {
			e.printStackTrace();
			}
		}
		System.out.println("Toplam: "+ b.total);

	}

}