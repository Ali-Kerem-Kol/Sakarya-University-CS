// "pkt" adında bir paket tanımlıyoruz. Kodumuz bu paketin içinde yer alır.
package pkt;

// Gerekli kütüphaneleri içe aktarıyoruz.
// "BufferedReader" sınıfı, giriş akışından verileri tamponlu bir şekilde okumamızı sağlar.
// "InputStreamReader" sınıfı, byte akışını karakter akışına çevirir, yani akışı okunabilir hale getirir.
import java.io.BufferedReader;
import java.io.InputStreamReader;

// Program adlı ana sınıfımızı tanımlıyoruz.
public class Program {

    // Programın çalışmaya başlayacağı ana metod olan main metodunu tanımlıyoruz.
    public static void main(String[] args) {
        
        // Programın olası hataları yakalaması için "try-catch" yapısı kullanıyoruz.
        try {
            // "line" adında bir "String" değişken tanımlıyoruz.
            // Bu değişken, her satır okunduğunda satır verisini tutacak.
            String line;
            
            // Windows işletim sisteminde çalışan işlemleri listelemek için "tasklist.exe" komutunu çalıştırıyoruz.
            // "System.getenv("windir")" ifadesi, Windows klasörünün yolunu döndürür (örneğin: C:\Windows).
            // Böylece "tasklist.exe" komutunu "C:\Windows\system32\tasklist.exe" olarak çalıştırmış oluruz.
            Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
            
            // Çalıştırılan komuttan gelen çıktıyı okumak için bir "BufferedReader" nesnesi oluşturuyoruz.
            // "InputStreamReader" ile sürecin ("p" nesnesinin) giriş akışını okuyup, bunu "BufferedReader" nesnesine iletiyoruz.
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            
            // Komuttan gelen çıktıyı satır satır okumak için bir "while" döngüsü kullanıyoruz.
            // "readLine()" metodu ile bir satır okuyoruz ve "line" değişkenine atıyoruz.
            // Eğer satır boş değilse ("null" değilse), döngü devam eder.
            while ((line = input.readLine()) != null) {
                
                // Okunan satırı ekrana yazdırıyoruz.
                System.out.println(line);
            }
            
            // İşlem bittikten sonra "BufferedReader" nesnesini kapatarak kaynakları serbest bırakıyoruz.
            input.close();
        
        // Eğer komut çalıştırma veya okuma işlemleri sırasında herhangi bir hata oluşursa, "catch" bloğu çalışır.
        } catch (Exception e) {
            // Oluşan hatayı ekrana yazdırır. "e.printStackTrace()" hata detaylarını gösterir.
            e.printStackTrace();
        }
    }
    /*Bu Java kodu, Windows işletim sisteminde çalışan işlemlerin bir listesini almak için tasklist.exe komutunu kullanır ve 
     * çıktısını ekrana yazdırır. 
     * Aşağıda, kodun her bir satırının açıklaması yer almaktadır:
     * Özet:
		Bu kod, Windows'ta çalışan işlemleri listelemek için tasklist.exe komutunu kullanır.
		Komutun çıktısı satır satır okunur ve her satır ekrana yazdırılır.
		Eğer bir hata oluşursa, hata detayları ekrana yazdırılır.
		Bu kod yalnızca Windows ortamında çalışır, çünkü tasklist.exe komutu Windows’a özgüdür.*/
}
