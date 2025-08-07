//ALI KEREK KOL - B221210042 
//OMER ELMAS - B221210582
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include "fs.h"

void menu() {
    printf("\nSimpleFS - Dosya Sistemi Simülatörü\n");
    printf("1. Dosya Oluştur\n");
    printf("2. Dosya Sil\n");
    printf("3. Dosyaya Yaz\n");
    printf("4. Dosyadan Oku\n");
    printf("5. Dosyaları Listele\n");
    printf("6. Diski Biçimlendir\n");
    printf("7. Dosya Adını Değiştir\n");
    printf("8. Dosya Var mı Kontrol Et\n");
    printf("9. Dosya Boyutunu Öğren\n");
    printf("10. Dosyaya Ekle\n");
    printf("11. Dosyayı Kısalt (Truncate)\n");
    printf("12. Dosya Kopyala\n");
    printf("13. Dosya Taşı\n");
    printf("14. Diski Birleştir (Defragment)\n");
    printf("15. Bütünlüğü Kontrol Et\n");
    printf("16. Disk Yedeği Al\n");
    printf("17. Disk Yedeğini Geri Yükle\n");
    printf("18. Dosya İçeriğini Göster (cat)\n");
    printf("19. İki Dosyayı Karşılaştır (diff)\n");
    printf("20. Çıkış\n");
    printf("Seçiminiz: ");
}

int main() {
    struct stat st;
    if (stat(DISK_NAME, &st) != 0) {
        printf("Disk bulunamadı. Yeni disk oluşturuluyor...\n");
        fs_format();
    }

    int choice;
    char filename[64], filename2[64];
    char buffer[1024];
    int offset, size;

    while (1) {
        menu();
        scanf("%d", &choice);

        switch (choice) {
            case 1:
                printf("Dosya adını girin: ");
                scanf("%s", filename);
                fs_create(filename);
                break;
            case 2:
                printf("Dosya adını girin: ");
                scanf("%s", filename);
                fs_delete(filename);
                break;
            case 3:
                printf("Dosya adını girin: ");
                scanf("%s", filename);
                printf("Veriyi girin: ");
                scanf(" %[^\n]", buffer);
                fs_write(filename, buffer, strlen(buffer));
                break;
            case 4:
                printf("Dosya adını girin: ");
                scanf("%s", filename);
                printf("Offset ve boyut girin: ");
                scanf("%d %d", &offset, &size);
                if (fs_read(filename, offset, size, buffer) == 0) {
                    buffer[size] = '\0';
                    printf("Okunan Veri: %s\n", buffer);
                }
                break;
            case 5:
                fs_ls();
                break;
            case 6:
                fs_format();
                break;
            case 7:
                printf("Eski dosya adını girin: ");
                scanf("%s", filename);
                printf("Yeni dosya adını girin: ");
                scanf("%s", filename2);
                fs_rename(filename, filename2);
                break;
            case 8:
                printf("Dosya adını girin: ");
                scanf("%s", filename);
                printf("Var mı: %d\n", fs_exists(filename));
                break;
            case 9:
                printf("Dosya adını girin: ");
                scanf("%s", filename);
                printf("Boyut: %d bayt\n", fs_size(filename));
                break;
            case 10:
                printf("Dosya adını girin: ");
                scanf("%s", filename);
                printf("Eklenecek veriyi girin: ");
                scanf(" %[^\n]", buffer);
                fs_append(filename, buffer, strlen(buffer));
                break;
            case 11:
                printf("Dosya adını girin: ");
                scanf("%s", filename);
                printf("Yeni boyutu girin: ");
                scanf("%d", &size);
                fs_truncate(filename, size);
                break;
            case 12:
                printf("Kaynak dosya adını girin: ");
                scanf("%s", filename);
                printf("Hedef dosya adını girin: ");
                scanf("%s", filename2);
                fs_copy(filename, filename2);
                break;
            case 13:
                printf("Eski dosya adını girin: ");
                scanf("%s", filename);
                printf("Yeni dosya adını girin: ");
                scanf("%s", filename2);
                fs_mv(filename, filename2);
                break;
            case 14:
                fs_defragment();
                break;
            case 15:
                fs_check_integrity();
                break;
            case 16:
                printf("Yedek dosya adını girin: ");
                scanf("%s", filename);
                fs_backup(filename);
                break;
            case 17:
                printf("Yedek dosya adını girin: ");
                scanf("%s", filename);
                fs_restore(filename);
                break;
            case 18:
                printf("Dosya adını girin: ");
                scanf("%s", filename);
                fs_cat(filename);
                break;
            case 19:
                printf("İlk dosya adını girin: ");
                scanf("%s", filename);
                printf("İkinci dosya adını girin: ");
                scanf("%s", filename2);
                if (fs_diff(filename, filename2)) {
                    printf("Dosyalar farklı.\n");
                } else {
                    printf("Dosyalar aynı.\n");
                }
                break;
            case 20:
                printf("Çıkılıyor...\n");
                exit(0);
            default:
                printf("Geçersiz seçim.\n");
        }
    }

    return 0;
}

