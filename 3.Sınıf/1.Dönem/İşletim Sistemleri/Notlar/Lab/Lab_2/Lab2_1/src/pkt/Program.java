// "pkt" adında bir paket tanımlıyoruz. Kod bu paketin içinde yer alır.
package pkt;

// Gerekli kütüphaneleri içe aktarıyoruz.
// "BufferedReader" ve "InputStreamReader" sınıfları ile çıktı akışını okuyacağız.
// "File" sınıfı, ProcessBuilder'da çalışma dizinini ayarlamak için kullanılıyor.
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

// Program adlı ana sınıfımızı tanımlıyoruz.
public class Program {

    // Programın çalışmaya başlayacağı ana metod olan main metodunu tanımlıyoruz.
    public static void main(String[] args) {
        
        // Programın olası hataları yakalaması için "try-catch" yapısı kullanıyoruz.
        try {
            
            // Çalıştırmak istediğimiz komutları bir dizi ("array") içinde tanımlıyoruz.
            // "threeCommands" dizisi, cmd.exe içinde çalışacak komutları içeriyor.
            // Bu dizi şu komutları çalıştıracak: 
            // 1. "dir" - C sürücüsündeki dosya ve klasörleri listeler
            // 2. "ping google.com" - Google'a ping gönderir
            // 3. "tasklist" - Çalışan işlemleri listeler
            String threeCommands[] = {
                "cmd", "/c",         // cmd.exe komutunu çalıştırmak için gerekli parametreler
                "dir",               // C sürücüsündeki dosya ve klasörleri listeleyen komut
                "&&",                // Komutlar arasında "AND" operatörü, önceki komut başarılı olursa sonraki çalışır
                "ping", "google.com", // Google'a ping atma komutu
                "&&",
                "tasklist"           // Çalışan işlemleri listeleyen komut
            };
            
            // ProcessBuilder nesnesi oluşturuyoruz ve komut dizisini bu nesneye veriyoruz.
            // Bu, belirtilen komutların bir süreç olarak çalıştırılmasını sağlar.
            ProcessBuilder builder = new ProcessBuilder(threeCommands);
            
            // Çalışma dizinini "C://" olarak ayarlıyoruz.
            // "dir" komutunun "C://" dizininde işlem yapmasını sağlıyoruz.
            builder.directory(new File("C://"));
            
            // Hata akışını, standart çıktı akışına yönlendiriyoruz.
            // Böylece oluşabilecek hataları da okuyabiliriz.
            builder.redirectErrorStream(true);
            
            // Belirtilen komutları çalıştıran bir süreç başlatıyoruz.
            Process subProcess = builder.start();
            
            // "BufferedReader" ve "InputStreamReader" kullanarak alt sürecin çıktısını okuyoruz.
            BufferedReader subProcessInputReader = new BufferedReader(new InputStreamReader(subProcess.getInputStream()));
            
            // Alt süreçten gelen çıktıyı satır satır okumak için bir döngü kullanıyoruz.
            String line = null;
            while ((line = subProcessInputReader.readLine()) != null) {
                // Okunan her satırı ekrana yazdırıyoruz.
                System.out.println(line);
            }
            
            // Okuma işlemi bittikten sonra BufferedReader nesnesini kapatıyoruz.
            subProcessInputReader.close();
        
        // Eğer herhangi bir hata oluşursa "catch" bloğu çalışır ve ekrana "Error." mesajı yazdırır.
        } catch (Exception e) {
            System.out.println("Error.");
        }

    }
    /*Bu Java kodu, Windows komut isteminde (cmd.exe) birden fazla komutu sıralı bir şekilde çalıştırır. 
     * Kod, bir ProcessBuilder kullanarak dir, ping google.com ve 
     * tasklist komutlarını tek bir süreçte arka arkaya çalıştırır ve her komutun çıktısını ekrana yazdırır. 
     * Aşağıda her bir satırın ne yaptığını detaylı olarak açıklıyorum:
     * Özet:
		Kod, dir, ping google.com ve tasklist komutlarını cmd.exe içinde sırasıyla çalıştırır.
		ProcessBuilder, bu komutları tek bir süreç olarak yürütmek için kullanılır ve çıktıyı okur.
		Çıktı, her satır okundukça ekrana yazdırılır; herhangi bir hata oluşursa "Error." mesajı gösterilir.
		Bu yapı, birden fazla komutun arka arkaya çalıştırılması gerektiğinde faydalı bir yaklaşımdır.*/
}
