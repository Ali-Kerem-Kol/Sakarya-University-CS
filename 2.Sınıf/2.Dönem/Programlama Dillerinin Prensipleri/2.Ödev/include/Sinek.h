#ifndef SINEK_H
#define SINEK_H

#include "stdio.h"

#include "Bocek.h"

struct SINEK
{
	Canli supersuper;
	Bocek super;

	
	void (*sil)(struct SINEK*);

};
typedef struct SINEK* Sinek;

Sinek new_sinek(int);

char* sinek_gorunum();

void delete_sinek(const Sinek);


#endif