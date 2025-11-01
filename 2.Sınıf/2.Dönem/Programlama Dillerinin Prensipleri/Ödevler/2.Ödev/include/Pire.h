#ifndef PIRE_H
#define PIRE_H

#include "stdio.h"

#include "Bocek.h"

struct PIRE
{
	Canli supersuper;
	Bocek super;
	
	void (*sil)(struct PIRE*);

};
typedef struct PIRE* Pire;

Pire new_pire(int);

char* pire_gorunum();

void delete_pire(const Pire);


#endif