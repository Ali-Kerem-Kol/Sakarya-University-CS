// Paket tanımı: Kod "pkt" adlı bir paketin içinde bulunur.
package pkt;

// Gerekli kütüphaneleri içe aktarıyoruz.
// BufferedReader ve InputStreamReader, komutun çıktısını okumak için kullanılır.
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

// Program adlı ana sınıfımız.
public class Program {

    // Programın çalışmaya başlayacağı ana metod olan main metodunu tanımlıyoruz.
    public static void main(String[] args) throws IOException {
        
        // Runtime sınıfı ile çalışma zamanı ortamına erişiyoruz.
        // Bu, sistem komutlarını çalıştırmamızı sağlar.
        Runtime rt = Runtime.getRuntime();
        
        // `cmd /C dir` komutunu çalıştırıyoruz.
        // Bu komut, Windows komut isteminde geçerli dizindeki dosya ve klasörleri listeler.
        Process dirProc = rt.exec("cmd /C dir");
        
        // `dirProc` işleminden gelen standart çıktıyı almak için bir InputStream oluşturuyoruz.
        InputStream in = dirProc.getInputStream();
        
        // Kaynakları otomatik olarak kapatmak için `try-with-resources` kullanıyoruz.
        // `BufferedReader`, InputStream'den gelen çıktıyı satır satır okumamızı sağlar.
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            
            // Komutun çıktısını satır satır okuyup ekrana yazdırmak için bir döngü kullanıyoruz.
            while ((line = br.readLine()) != null) {
                System.out.println(line);  // Okunan satır ekrana yazdırılır.
            }
        
        // Hata oluşursa catch bloğu çalışır ve ekrana "Error." mesajı yazdırılır.
        } catch (Exception e) {
            System.out.println("Error.");
        }
    }
    /*Bu Java kodu, Windows işletim sisteminde çalışan dir komutunu çalıştırarak 
     * belirtilen dizindeki dosya ve klasörleri listeleyen bir programdır. 
     * Kodu adım adım inceleyelim:
     * Kodun İşleyişi:
		Runtime Erişimi: Runtime.getRuntime() ile Java'nın çalışma zamanı ortamına erişim sağlanır.
		Komut Çalıştırma: exec("cmd /C dir") komutu ile Windows komut istemcisi (cmd) üzerinden dir komutu çalıştırılır.
		Çıktıyı Okuma: InputStream ve BufferedReader kullanılarak komutun çıktısı satır satır okunur.
		Kaynakları Kapatma: try-with-resources yapısı, BufferedReader'ı iş bitiminde otomatik olarak kapatır.
		Çıktı ve Hata Kontrolü: Çıktı başarıyla ekrana yazdırılırken, bir hata durumunda Error. mesajı gösterilir.
		Özet:
		Bu kod, cmd üzerinden dir komutunu çalıştırır ve mevcut dizindeki dosya ve klasörleri listeler. 
		Çıktıyı satır satır okuyarak ekrana yazdırır ve kaynakları otomatik olarak kapatır.*/
}
