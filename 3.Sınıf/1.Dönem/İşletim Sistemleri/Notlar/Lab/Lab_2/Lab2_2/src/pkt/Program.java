// Paket tanımı. Kod, "pkt" adlı bir paketin içinde bulunuyor.
package pkt;

// Gerekli kütüphaneleri içe aktarıyoruz.
// URL bağlantısı ve akış işlemleri için gereken sınıflar içe aktarılır.
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

// Program adlı ana sınıfımız.
public class Program {
	
    // URL'ye erişim sağlayarak bağlantıyı kontrol eden bir metod tanımlıyoruz.
    public static void eris() {
        // Bağlantı durumu için bir boolean değişken tanımlıyoruz.
        boolean connectivity;
        
        // Bağlantıyı test etmek için "try-catch" bloğunu kullanıyoruz.
        try {
            // "sakarya.edu.tr" adresine bağlantı kuracak bir URL nesnesi oluşturuyoruz.
            URL url = new URL("https://www.sakarya.edu.tr/");
            
            // Bağlantıyı açmak için URLConnection nesnesini kullanıyoruz.
            URLConnection conn = url.openConnection();
            
            // "connect" metodu ile bağlantıyı kurmaya çalışıyoruz.
            conn.connect();
            
            // Bağlantı başarılı olursa, "connectivity" true olur.
            connectivity = true;
        
        // Herhangi bir hata oluşursa catch bloğu çalışır ve bağlantı başarısız olur.
        } catch (Exception e) {
            // Bağlantı başarısız olduğu için "connectivity" değişkenini false yapıyoruz.
            connectivity = false;
        }
        
        // Bağlantı durumunu kontrol ediyoruz.
        if (connectivity == true) {
            System.out.println("Baglanti basarili.");
        } else {
            System.out.println("Baglanti basarisiz.");
        }
    }

    // Programın çalışmaya başlayacağı ana metod olan main metodunu tanımlıyoruz.
    public static void main(String[] args) throws IOException {
        
        // Komut satırı argümanlarının sayısını kontrol ediyoruz.
        // Eğer 3 argüman verilmemişse, hata mesajı gösteriyoruz.
        if (args.length != 3) {
            System.err.println("Usage: java OSProcess <command>");
            System.exit(0);  // Program sonlandırılır.
        }
        
        // İlk olarak, komut satırı argümanları ile bir ProcessBuilder nesnesi oluşturuyoruz.
        // Kullanıcıdan gelen ilk 3 argüman ve "www.sakarya.edu.tr" URL'sini kullanarak komut oluşturuluyor.
        ProcessBuilder pb = new ProcessBuilder(args[0], args[1], args[2], "www.sakarya.edu.tr");
        
        // ProcessBuilder ile oluşturduğumuz süreci başlatıyoruz.
        Process process = pb.start();
        
        // Süreçten gelen çıktı akışını okumak için InputStream oluşturuyoruz.
        InputStream is = process.getInputStream();
        
        // InputStream'i karakter akışına çeviriyoruz.
        InputStreamReader isr = new InputStreamReader(is);
        
        // BufferedReader ile sürecin çıktısını satır satır okuyabilmek için bir okuma akışı oluşturuyoruz.
        BufferedReader br = new BufferedReader(isr);
        
        // Çıktı akışını satır satır okuma işlemi başlıyor.
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);  // Okunan satır ekrana yazdırılıyor.
        }
        
        // Kaynakları serbest bırakmak için BufferedReader'ı kapatıyoruz.
        br.close();
        
        // Yeni bir komut satırı dizisi oluşturuyoruz, "dir" komutunu çalıştırmak için kullanılır.
        String[] commandLine = {args[0], args[1], "dir"};
        
        // Runtime sınıfını kullanarak belirtilen komutu çalıştırıyoruz.
        Process process2 = Runtime.getRuntime().exec(commandLine);
        
        // Çalışan sürecin kimliğini (PID) ekrana yazdırıyoruz.
        System.out.println(process2.pid());
        
        // Erişim kontrolü yapan eris() metodunu çağırıyoruz.
        eris();
    }
    /*Bu Java kodu, belirli bir URL'ye erişim sağlayarak bağlantı durumunu kontrol eder ve 
     * ardından ProcessBuilder kullanarak işletim sistemi üzerinde verilen komutları çalıştırır. 
     * Komut satırındaki argümanlar ile başlatılan süreçlerin çıktısını okur ve 
     * ayrıca çalışan sürecin pid (process ID) bilgisini ekrana yazdırır. 
     * Aşağıda, kodun her bir bölümünün açıklaması yer alıyor:
     * Kodun Özeti:
		Bağlantı Testi: eris() metodu, https://www.sakarya.edu.tr/ adresine bağlantı kurar ve bağlantının başarılı olup olmadığını bildirir.
		Komut Çalıştırma: main() metodunda, komut satırı argümanları ile ProcessBuilder nesnesi kullanılarak belirli bir komut çalıştırılır. 
		Komutun çıktısı satır satır okunup ekrana yazdırılır.
		dir Komutu ve PID Yazdırma: Runtime.getRuntime().exec() kullanılarak dir komutu çalıştırılır ve sürecin pid değeri ekrana yazdırılır.
		Bu kod, bağlantı kontrolü ve komutların işletim sistemi üzerinde nasıl çalıştırılabileceği gibi konularda örnek olarak kullanılabilir.
		
		 Programı Çalıştırmak için Argümanları Ayarlayın
		Program sınıfına sağ tıklayın ve Run As > Run Configurations... seçeneğine tıklayın.
		Sol tarafta Java Application altında Program'ı seçin.
		Arguments sekmesine geçin.
		Program arguments alanına üç argüman girin (örneğin):

		"cmd /c dir"
		
		5. Programı Çalıştırın
		Run butonuna tıklayarak programınızı çalıştırın.
		Önemli Notlar
		Programınız çalıştıktan sonra, dir komutunun çıktısını göreceksiniz. eriş metodunun çıktısı da bağlantı durumunu gösterecektir.
		Komut satırı argümanlarını uygun şekilde ayarladığınızdan emin olun. Yanlış argümanlar girerseniz hata mesajı alabilirsiniz.
		Eğer sakarya.edu.tr bağlantısı başarısız olursa, program "Bağlantı başarısız." mesajını gösterecektir.*/
}
