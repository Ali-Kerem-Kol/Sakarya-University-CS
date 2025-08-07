# SimpleFS – Temel Dosya Sistemi Simülatörü

C dilinde sistem programlama kavramları kullanılarak hazırlanmış basit bir dosya sistemi simülatörü.

## Özellikler
- Dosya oluşturma, silme, yazma, okuma  
- Dosya yeniden adlandırma, kopyalama, taşıma, ekleme (append), kesme (truncate)  
- Dosya listesini görüntüleme  
- Disk biçimlendirme, defragmentasyon, bütünlük kontrolü  
- Tüm diskin yedeğini alma ve geri yükleme  
- Dosya içeriklerini karşılaştırma  
- Dosya işlemlerinin log kaydını tutma  

## Derleme Talimatları
1. `make` komutunu çalıştırın.  
2. `SimpleFS` adlı çalıştırılabilir dosya oluşturulacaktır.  

## Çalıştırma Talimatları
- Simülatörü başlatmak için:
  ./SimpleFS
Açılan menüden yapmak istediğiniz işlemi seçin ve ekrandaki yönlendirmeleri izleyin.

## Notlar
- Sanal disk olarak disk.sim dosyası kullanılır.

- Tüm işlemler düşük seviyeli sistem çağrıları (open, read, write, lseek, close) ile gerçekleştirilir.

## Yazar
- ALI KEREK KOL - B221210042 , OMER ELMAS - B221210582
