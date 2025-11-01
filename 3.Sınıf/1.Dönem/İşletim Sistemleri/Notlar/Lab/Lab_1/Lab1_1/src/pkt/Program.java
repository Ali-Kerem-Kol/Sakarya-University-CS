// Bu satır, sınıfın hangi pakette olduğunu belirler.
// Bu durumda, "pkt" adlı bir paket oluşturulmuş.
package pkt;

// Java'nın dosya işlemleri için kullanılan "File" sınıfını içe aktarıyoruz.
// Bu sınıf, dosya ve dizinleri temsil etmek ve onlar üzerinde işlem yapmak için kullanılır.
import java.io.File;

// Program adlı ana sınıfı tanımlıyoruz.
public class Program {

    // Programın çalışmaya başlayacağı ana metod olan main metodunu tanımlıyoruz.
    public static void main(String[] args) {
        
        // "File" sınıfından bir nesne oluşturuyoruz ve bu nesneye "file" adını veriyoruz.
        // "C:" sürücüsünü temsil eden bir nesne oluşturuyoruz.
        File file = new File("c:");
        
        // "getTotalSpace" metodunu çağırarak disk üzerindeki toplam alanı bayt (byte) cinsinden alıyoruz.
        long totalSpace = file.getTotalSpace();
        
        // "getUsableSpace" metodunu çağırarak disk üzerindeki kullanılabilir alanı bayt cinsinden alıyoruz.
        long usableSpace = file.getUsableSpace();
        
        // "getFreeSpace" metodunu çağırarak disk üzerindeki boş alanı bayt cinsinden alıyoruz.
        long freeSpace = file.getFreeSpace();
        
        // Çıktıda bölüme ait bilgileri göstermek için ilk başlık satırını yazdırıyoruz.
        System.out.println(" === Partition Detail === ");

        // Çıktı için bayt cinsinden başlık ekliyoruz.
        System.out.println(" === Byte === ");
        
        // Diskin toplam alanını bayt cinsinden yazdırıyoruz.
        System.out.println("Toplam alan : " + totalSpace + " bytes");
        
        // Diskin kullanılabilir alanını bayt cinsinden yazdırıyoruz.
        System.out.println("Toplam kullanilabilir alan : " + usableSpace + " bytes");
        
        // Diskin boş alanını bayt cinsinden yazdırıyoruz.
        System.out.println("Toplam bos alan : " + freeSpace + " bytes");

        // Çıktı için megabayt (MB) cinsinden başlık ekliyoruz.
        System.out.println(" === Megabyte (MB) === ");
        
        // Bayt cinsindeki toplam alanı MB’ye çevirip yazdırıyoruz.
        // 1 MB = 1024 KB, 1 KB = 1024 bytes olduğu için 1024 * 1024'e bölüyoruz.
        System.out.println("Toplam alan (MB) : " + (totalSpace / 1024 / 1024) + " MB");
        
        // Bayt cinsindeki kullanılabilir alanı MB’ye çevirip yazdırıyoruz.
        System.out.println("Toplam kullanilabilir alan (MB) : " + (usableSpace / 1024 / 1024) + " MB");
        
        // Bayt cinsindeki boş alanı MB’ye çevirip yazdırıyoruz.
        System.out.println("Toplam bos alan (MB) : " + (freeSpace / 1024 / 1024) + " MB");
    }
    /*Bu Java kodu, belirtilen disk bölümü (C: sürücüsü) hakkındaki bilgileri (toplam, kullanılabilir ve boş alan) bayt ve 
     * megabayt cinsinden yazdırır. 
     * Aşağıda bu kodun ayrıntılı açıklaması yer alıyor:
     * Özet:
		Kod, File sınıfı ile bir disk sürücüsü oluşturup, getTotalSpace, getUsableSpace, ve getFreeSpace metodları 
		ile toplam, kullanılabilir ve boş alanı bayt cinsinden elde ediyor.
		Ardından, bu değerleri hem bayt hem de megabayt cinsinden ekrana yazdırıyor.
		Bu yapıyı diğer disk sürücülerine uyarlayarak da farklı alanları öğrenebilirsiniz.*/
}
