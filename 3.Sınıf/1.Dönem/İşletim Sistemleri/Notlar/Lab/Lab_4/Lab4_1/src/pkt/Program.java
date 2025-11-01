package pkt;

public class Program {
    public static void main(String[] args){
        PrintChar printA = new PrintChar('a', 100);
        PrintChar printB = new PrintChar('b', 100);
        PrintNum print100 = new PrintNum(100);

        print100.start();
        printA.start();
        printB.start();
    }
}

class PrintChar extends Thread {
    private char charToPrint;
    private int times;

    public PrintChar(char c, int t) {
        charToPrint = c;
        times = t;
    }
    public void run() {
        for(int i = 0; i< times; i++) {
            System.out.print(charToPrint);
        }
    }
}

class PrintNum extends Thread {
    private int lastNum;

    public PrintNum(int n) {
        lastNum = n;
    }
    public void run() {
        for (int i = 1;i<= lastNum; i++) {
            System.out.print(" " + i);
        }
    }
}