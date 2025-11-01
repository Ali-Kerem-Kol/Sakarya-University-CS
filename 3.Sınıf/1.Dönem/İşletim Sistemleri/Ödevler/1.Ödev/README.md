# IsletimSistemleri54

Bu proje, basit bir shell uygulaması geliştirilmesini içermektedir. Proje, işletim sistemleri dersi kapsamında hazırlanmıştır ve temel shell komutlarının işlevselliğini sunmayı hedeflemektedir.

## Proje Yapısı

### Ana Dosyalar
- **README.md**: Projeyle ilgili temel bilgileri içeren dosya.
- **Rapor.pdf**: Projenin detaylı açıklamasını içeren rapor.
- **makefile**: Projeyi derlemek için kullanılan makefile.
- **bin/shell**: Derlenmiş shell uygulaması.

### Kaynak Kodlar
- **src/shell.c**: Shell uygulamasının ana dosyası.
- **src/shell_utils.c**: Yardımcı işlevleri içeren dosya.
- **include/shell.h**: Projeye ait başlık dosyası.

### Derleme Çıktıları
- **build/shell.o**: `shell.c` dosyasından üretilen obje dosyası.
- **build/shell_utils.o**: `shell_utils.c` dosyasından üretilen obje dosyası.

## Kurulum

Projeyi derlemek için aşağıdaki adımları izleyin:

1. Proje dizinine gidin:
   ```bash
   cd IsletimSistemleri54-main
   ```

2. Makefile kullanarak projeyi derleyin:
   ```bash
   make
   ```

3. Derlenen uygulamayı çalıştırın:
   ```bash
   ./bin/shell
   ```

## Kullanım

Derlenen shell uygulaması, temel komutları çalıştırmanızı sağlar. Desteklenen bazı komutlar:
- `cd`
- `ls`
- `exit`

Kullanım örneği:
```bash
$ ./bin/shell
> ls
> cd ..
> exit
```

## Katkıda Bulunma

Katkıda bulunmak için şu adımları izleyin:
1. Bu projeyi forklayın.
2. Yeni bir özellik dalı oluşturun:
   ```bash
   git checkout -b yeni-ozellik
   ```
3. Değişikliklerinizi yapın ve commit edin:
   ```bash
   git commit -m "Yeni bir özellik eklendi."
   ```
4. Değişikliklerinizi ana dala gönderin:
   ```bash
   git push origin yeni-ozellik
   ```
5. Bir Pull Request oluşturun.

## Lisans

Bu proje MIT Lisansı ile lisanslanmıştır. Daha fazla bilgi için `LICENSE` dosyasına bakın.

## İletişim

Herhangi bir sorunuz veya öneriniz varsa, lütfen projenin geliştiricileri ile iletişime geçin.
