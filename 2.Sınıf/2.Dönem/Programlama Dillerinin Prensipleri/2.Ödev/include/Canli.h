#ifndef CANLI_H
#define CANLI_H

#include "stdio.h"
#include "stdlib.h"
#include "math.h"


typedef enum Bool{false,true}boolean;
typedef enum {BITKI_TURU,BOCEK_TURU,SINEK_TURU,PIRE_TURU,HICBIR_TURU} CanliTuru;

struct CANLI
{
	CanliTuru tur;
	int value;
	
	char* (*gorunum)(); 
	
	void (*sil)(struct CANLI*);
	
};
typedef struct CANLI* Canli;

Canli new_canli(int);

void delete_canli(const Canli);


#endif