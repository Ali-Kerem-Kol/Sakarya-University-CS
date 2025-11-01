// "pkt" adında bir paket oluşturulmuş.
// Kod bu paketin içinde yer alır.
package pkt;

// URL bağlantı işlemleri için gerekli olan sınıfları içe aktarıyoruz.
// "URL" sınıfı, belirtilen bir web adresini temsil ederken,
// "URLConnection" sınıfı bu adrese bağlantı yapabilmemizi sağlar.
import java.net.URL;
import java.net.URLConnection;

// Program adlı ana sınıfımızı tanımlıyoruz.
public class Program {
    
    // eris() adlı statik bir metod tanımlıyoruz.
    // Bu metod, belirtilen URL'ye erişim sağlamayı dener.
    public static void eris() {
        
        // Bağlantı durumunu kontrol etmek için bir boolean değişkeni tanımlıyoruz.
        // Başlangıçta bu değişkenin değeri atanmaz.
        boolean connectivity;
        
        // "try" bloğu içinde bağlantıyı deneyeceğimiz için, olası hataları yakalamak amacıyla "try-catch" yapısını kullanıyoruz.
        try {
            // Bağlanmak istediğimiz URL'yi temsil eden bir URL nesnesi oluşturuyoruz.
            URL url = new URL("https://cs.sakarya.edu.tr/");
            
            // URL bağlantısı için bir URLConnection nesnesi oluşturuyoruz.
            // Bu nesne, URL'ye fiziksel olarak bağlanabilmemizi sağlar.
            URLConnection conn = url.openConnection();
            
            // "connect()" metodunu çağırarak bağlantıyı kurmaya çalışıyoruz.
            conn.connect();
            
            // Eğer bağlantı başarılı olursa "connectivity" değişkenini true yapıyoruz.
            connectivity = true;
        
        // Eğer bağlantı sırasında herhangi bir hata oluşursa "catch" bloğu çalışır.
        // Bu durumda, bağlantı başarısız olmuş sayılır.
        } catch (Exception e) {
            // Hata oluşursa, bağlantı başarısız olduğu için "connectivity" değişkenini false yapıyoruz.
            connectivity = false;
        }
        
        // Bağlantı durumunu kontrol etmek için if-else yapısını kullanıyoruz.
        // Eğer "connectivity" true ise bağlantı başarılı demektir.
        if (connectivity == true) {
            System.out.println("Baglanti basarili.");
        
        // Eğer "connectivity" false ise bağlantı başarısız demektir.
        } else {
            System.out.println("Baglanti basarisiz.");
        }
    }

    // Programın çalışmaya başlayacağı ana metod olan main metodunu tanımlıyoruz.
    public static void main(String[] args) {
        
        // "eris" metodunu çağırarak bağlantı durumunu kontrol ediyoruz.
        eris();
    }
    /*Bu Java kodu, belirtilen bir URL'ye bağlantı kurmayı dener ve başarılı olup olmadığını kontrol eder. 
     * Aşağıda, kodun her bir satırının ne işe yaradığını detaylı açıklamalarla birlikte bulabilirsiniz:
     * Özet:
		Kod, https://cs.sakarya.edu.tr/ adresine bağlanmayı dener.
		Bağlantı başarılıysa "Bağlantı başarılı" mesajı, başarısızsa "Bağlantı başarısız" mesajı yazdırılır.
		Bu, internet bağlantınızı veya belirli bir web sitesine erişim durumunu kontrol etmek için kullanılabilir.*/
}
