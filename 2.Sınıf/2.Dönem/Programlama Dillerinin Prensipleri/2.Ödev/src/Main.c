#include "stdio.h"
#include "stdlib.h"
#include "string.h" // strtok işlevini kullanabilmek için string.h başlık dosyasını dahil ettik.

////////////////////////
#include "Bitki.h"
#include "Bocek.h"
#include "Canli.h"
#include "Habitat.h"
#include "Pire.h"
#include "Sinek.h"
////////////////////////


Habitat veri_oku_ve_habitat_olustur(char* dosya_adi) 
{
    FILE *dosya = fopen(dosya_adi, "r");
    if (dosya == NULL) 
    {
        printf("Dosya acilamadi.\n");
        exit(EXIT_FAILURE);
    }
    
    int satir_sayisi = 0;
    int en_buyuk_sutun_sayisi = 0;
    int sayi;

    // Satır ve en büyük sütun sayısını hesapla
    char satir[256]; // Geçici bir dizi kullanarak satırı oku
    while (fgets(satir, sizeof(satir), dosya) != NULL) 
    {
        int sutun_sayisi = 0;
        char *token = strtok(satir, " "); // Boşluklara göre ayır
        while (token != NULL) 
        {
            sutun_sayisi++;
            token = strtok(NULL, " ");
        }
        if (sutun_sayisi > en_buyuk_sutun_sayisi) 
        {
            en_buyuk_sutun_sayisi = sutun_sayisi;
        }
        satir_sayisi++;
    }

    // Dosyayı başa al
    rewind(dosya);

    // Habitat oluştur
    Habitat habitat = new_habitat(satir_sayisi, en_buyuk_sutun_sayisi);
    
    // Verileri oku ve habitat matrisine ekle
    for (int i = 0; i < satir_sayisi; i++) 
    {
        int j = 0;
        char *token = strtok(fgets(satir, sizeof(satir), dosya), " ");
        while (token != NULL) 
        {
            sayi = atoi(token); // Stringi tamsayıya dönüştür
            if (sayi >= 1 && sayi <= 9) 
            {
                habitat->canliMatris[i][j] = (Canli)new_bitki(sayi);
                habitat->turMatris[i][j] = BITKI;
            }
            else if (sayi >= 10 && sayi <= 20) 
            {
                habitat->canliMatris[i][j] = (Canli)new_bocek(sayi);
                habitat->turMatris[i][j] = BOCEK;
            } 
            else if (sayi >= 21 && sayi <= 50) 
            {
                habitat->canliMatris[i][j] = (Canli)new_sinek(sayi);
                habitat->turMatris[i][j] = SINEK;
            } 
            else if (sayi >= 51 && sayi <= 99) 
            {
                habitat->canliMatris[i][j] = (Canli)new_pire(sayi);
                habitat->turMatris[i][j] = PIRE;
            } 
            else 
            {
                habitat->canliMatris[i][j] = NULL;
                habitat->turMatris[i][j] = HICBIR;
            }
            j++;
            token = strtok(NULL, " ");
        }
        // Eksik sütunları NULL ile doldur
        for (; j < en_buyuk_sutun_sayisi; j++) 
        {
            habitat->canliMatris[i][j] = NULL;
            habitat->turMatris[i][j] = HICBIR;
        }
    }

    fclose(dosya);

    return habitat;
}


int main()
{   
    // Veri dosyasını oku
    Habitat habitat = veri_oku_ve_habitat_olustur("Veri.txt");

    habitat->yazdir(habitat);

    printf("Press any key to continue...\n");
    getchar(); // Kullanıcının herhangi bir tuşa basmasını bekler
    
    printf("\n");

    habitat->habitat_simulasyon_baslat(habitat);

    habitat->habitat_kazanan(habitat);

    // Belleği temizle
    
    delete_habitat(habitat);

    return 0;
}