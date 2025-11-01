// "pkt" adında bir paket tanımlanır. Kod bu paketin içinde bulunur.
package pkt;

// Program adlı ana sınıfımız.
public class Program {

    // Programın çalışmaya başlayacağı ana metod olan main metodunu tanımlıyoruz.
    public static void main(String[] args) {
        
        // "try-catch" bloğu ile olası hataları yakalıyoruz.
        try {
            // Kullanıcıya hem MS Paint hem de Notepad programlarının çalışacağını belirten bir mesaj yazdırıyoruz.
            System.out.println("Ms paint ve Notepad birlike calistir. \n");
            
            // MS Paint programının çalışmaya başlayacağını belirten bir mesaj yazdırıyoruz.
            System.out.println("1.Ms paint calisacak");
            
            // "mspaint.exe" komutunu çalıştırarak MS Paint programını başlatıyoruz.
            Process p1 = Runtime.getRuntime().exec("mspaint.exe");
            
            // Notepad programının çalışmaya başlayacağını belirten bir mesaj yazdırıyoruz.
            System.out.println("2.Notepad calisacak");
            
            // "notepad.exe" komutunu çalıştırarak Notepad programını başlatıyoruz.
            Process p2 = Runtime.getRuntime().exec("notepad.exe");
            
            // MS Paint programının kapanmasını beklemek için "waitFor()" metodunu çağırıyoruz.
            p1.waitFor();
            
            // MS Paint programı kapatıldıktan sonra bu mesaj ekrana yazdırılır.
            System.out.println("Ms-paint programindan ciktiniz.");
            
            // Notepad programının kapanmasını beklemek için "waitFor()" metodunu çağırıyoruz.
            p2.waitFor();
            
            // Notepad programı kapatıldıktan sonra bu mesaj ekrana yazdırılır.
            System.out.println("Notepad programindan ciktiniz.");
        
        // Herhangi bir hata oluşursa catch bloğu çalışır ve hata mesajı ekrana yazdırılır.
        } catch (Exception e) {
            System.out.println("Oops! bir hata olustu.");
        }
    }
    /*Bu Java kodu, MS Paint ve Notepad programlarını aynı anda çalıştırır ve her iki programın 
     * kapanmasını bekleyerek kapanış mesajlarını ekrana yazar. Kodu detaylı bir şekilde açıklayalım:
     * Kodun İşleyişi:
		MS Paint ve Notepad Programlarının Başlatılması:

		exec("mspaint.exe") komutuyla MS Paint programı başlatılır.
		exec("notepad.exe") komutuyla Notepad programı başlatılır.
		Bu iki komut, MS Paint ve Notepad’i aynı anda başlatır.
		Kapanmalarını Beklemek:

		p1.waitFor() ile program, MS Paint kapatılana kadar bekler.
		MS Paint kapatıldıktan sonra ekrana "Ms-paint programindan ciktiniz." mesajı yazdırılır.
		p2.waitFor() ifadesi, Notepad kapatılana kadar bekler.
		Notepad kapatıldıktan sonra "Notepad programindan ciktiniz." mesajı yazdırılır.
		Hata Durumu:

		Bir hata oluşması durumunda catch bloğu çalışır ve ekrana "Oops! bir hata olustu." mesajı yazdırılır.
		Özet:
		Bu program, MS Paint ve Notepad uygulamalarını aynı anda başlatır. 
		Her iki program kapatılana kadar bekler ve kapatıldıklarında ekrana bilgi mesajları yazdırılır. 
		waitFor() metodları, MS Paint ve Notepad’in kapanmasını ayrı ayrı bekler, böylece her programın kapanışında bir mesaj gösterilir.*/
}