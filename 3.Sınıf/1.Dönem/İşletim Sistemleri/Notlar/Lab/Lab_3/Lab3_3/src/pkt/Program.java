// "pkt" adında bir paket tanımlanır. Kod bu paketin içinde bulunur.
package pkt;

// Program adlı ana sınıfımız.
public class Program {

    // Programın çalışmaya başlayacağı ana metod olan main metodunu tanımlıyoruz.
    public static void main(String[] args) {
        
        // "try-catch" bloğu ile olası hataları yakalıyoruz.
        try {
            // İlk olarak, "Ms paint calisacak" mesajını ekrana yazdırıyoruz.
            System.out.println("Ms paint calisacak");
            
            // "mspaint.exe" (MS Paint) programını başlatmak için Runtime sınıfını kullanıyoruz.
            Process p1 = Runtime.getRuntime().exec("mspaint.exe");
            
            // "waitFor()" metodu, MS Paint programı kapanana kadar bekler.
            p1.waitFor();
            
            // MS Paint kapatıldıktan sonra ekrana "Ms-paint uygulamasindan ciktiniz." mesajı yazdırılır.
            System.out.println("Ms-paint uygulamasindan ciktiniz.");
            
            // Notepad programının çalışacağını belirten bir mesaj yazdırılır.
            System.out.println("Notepad calisacak");
            
            // "notepad.exe" (Notepad) programını başlatıyoruz.
            Process p2 = Runtime.getRuntime().exec("notepad.exe");
            
            // "waitFor()" metodu, Notepad programı kapanana kadar bekler.
            p2.waitFor();
            
            // Notepad kapatıldıktan sonra ekrana "Notepadd uygulamasindan ciktiniz." mesajı yazdırılır.
            System.out.println("Notepadd uygulamasindan ciktiniz.");
        
        // Herhangi bir hata oluşursa catch bloğu çalışır ve bir hata mesajı ekrana yazdırılır.
        } catch (Exception e) {
            // Hata mesajı ekrana yazdırılır.
            System.out.println("Oops! bir hata olustu.");
        }
    }
    /*Bu Java kodu, sırasıyla MS Paint ve Notepad programlarını başlatıp, her iki programın kapanmasını bekleyen bir programdır. 
     * Kullanıcı MS Paint ve Notepad uygulamalarını kapattığında, ekrana kapanış mesajları yazdırılır. Kodun adım adım açıklamasını yapalım:
     * Kodun İşleyişi:
		MS Paint Başlatma: exec("mspaint.exe") komutu ile Windows işletim sisteminde MS Paint çalıştırılır.
		MS Paint’in Kapanmasını Bekleme: p1.waitFor() ifadesi, MS Paint kapatılana kadar programın beklemesini sağlar.
		Notepad Başlatma: MS Paint kapatıldıktan sonra, exec("notepad.exe") ile Notepad programı başlatılır.
		Notepad’in Kapanmasını Bekleme: p2.waitFor() ifadesi Notepad kapatılana kadar programın beklemesini sağlar.
		Çıkış Mesajları ve Hata Durumu: Her iki program kapandıktan sonra çıkış mesajları ekrana yazdırılır. 
		Hata oluşursa, catch bloğu "Oops! bir hata olustu." mesajını gösterir.
		Özet:
		Bu program, MS Paint ve Notepad’i sırayla çalıştırır. 
		Kullanıcı MS Paint’i kapattığında Notepad başlar ve Notepad kapandıktan sonra program sonlanır. 
		waitFor() metodu ile her program kapatılana kadar beklenir ve kapanış mesajları ekrana yazdırılır.*/
}
