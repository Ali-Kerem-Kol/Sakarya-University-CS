package pkt;

class ThreadB extends Thread{
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
		System.out.println("Toplam: "+ b.total);

	}

}
