// "pkt" adında bir paket tanımlanır. Kod bu paketin içinde bulunur.
package pkt;

// Program adlı ana sınıfımız.
public class Program {

    // Programın çalışmaya başlayacağı ana metod olan main metodunu tanımlıyoruz.
    public static void main(String[] args) {
        
        // "try-catch" bloğu ile olası hataları yakalıyoruz.
        try {
            // "MS Paint programi calistir" mesajını ekrana yazdırıyoruz.
            System.out.println("Ms Paint programi calistir");
            
            // "Runtime.getRuntime().exec" ile işletim sistemi üzerinde MS Paint programını çalıştırıyoruz.
            // Bu satır, Windows işletim sisteminde bulunan "mspaint.exe" programını başlatır.
            Process p = Runtime.getRuntime().exec("mspaint.exe");
            
            // waitFor() metodu, MS Paint programı kapanana kadar bekler.
            // MS Paint kapandığında kod çalışmaya devam eder.
            p.waitFor();
            
            // MS Paint kapatıldıktan sonra bu mesaj ekrana yazdırılır.
            System.out.println("Ms paint programindan ciktiniz.");
        
        // Herhangi bir hata oluşursa catch bloğu çalışır ve hata mesajını ekrana yazdırır.
        } catch (Exception e) {
            // Hatanın türü ve detayı "exception is:" mesajıyla birlikte ekrana yazdırılır.
            System.out.println("exception is:" + e);
        }
    }
    /*Bu Java kodu, işletim sisteminde bulunan mspaint.exe (MS Paint) programını başlatan ve program 
     * kapatıldığında ekrana bir mesaj yazdıran bir programdır. 
     * Kodun her adımını açıklayalım:
     * Kodun İşleyişi:
		MS Paint Başlatma: Runtime.getRuntime().exec("mspaint.exe") komutu ile MS Paint başlatılır.
		Programın Beklemesi: waitFor() metodu, MS Paint çalışırken Java programını beklemeye alır; 
		MS Paint kapatıldığında Java programı devam eder.
		Programdan Çıkış Mesajı: MS Paint kapatıldıktan sonra ekrana "Ms paint programindan ciktiniz." mesajı yazdırılır.
		Hata Durumu: Herhangi bir hata oluşursa catch bloğunda bu hata exception is: mesajıyla birlikte gösterilir.
		Özet:
		Bu program, MS Paint’i çalıştırır ve kullanıcı MS Paint’i kapatınca program kapanmadan önce bir mesaj verir. 
		waitFor() ile MS Paint kapanana kadar programın çalışmayı beklemesi sağlanır.*/
}
