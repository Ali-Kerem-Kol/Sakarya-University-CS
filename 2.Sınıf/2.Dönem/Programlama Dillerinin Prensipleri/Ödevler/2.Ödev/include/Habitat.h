#ifndef HABITAT_H
#define HABITAT_H

#include "stdio.h"

#include "Canli.h"

#define MAX_ROW 256 // Maksimum satır sayısı
#define MAX_COL 256 // Maksimum sütun sayısı

// Habitat matrisindeki her bir hücre için tür tanımlaması
typedef enum 
{
    HICBIR,
    BITKI,
    BOCEK,
    SINEK,
    PIRE
} HUCRE_TURU;


// Habitat sınıfı tanımı
struct HABITAT {
    int row; // Satır sayısı
    int col; // Sütun sayısı

    Canli canliMatris[MAX_ROW][MAX_COL]; // Canlı matrisi
    HUCRE_TURU turMatris[MAX_ROW][MAX_COL]; // Hücre türleri matrisi

	void (*delete_habitat)(struct HABITAT*);
	void (*clear_habitat)(struct HABITAT*);
	void (*habitat_simulasyon_baslat)(struct HABITAT*);
    void (*habitat_kazanan)(struct HABITAT *);
    void (*yazdir)(struct HABITAT *);
};
typedef struct HABITAT* Habitat;

Habitat new_habitat(int,int);

void delete_habitat(const Habitat);

void habitat_simulasyon_baslat(Habitat); 

void habitat_kazanan(struct HABITAT *);

void yazdir(const Habitat);


#endif