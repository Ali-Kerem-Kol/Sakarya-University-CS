#ifndef BITKI_H
#define BITKI_H

#include "stdio.h"

#include "Canli.h"


struct BITKI
{
	Canli super;
	
	void (*sil)(struct BITKI*);
	

};
typedef struct BITKI* Bitki;

Bitki new_bitki(int);

char* bitki_gorunum();

void delete_bitki(const Bitki);


#endif