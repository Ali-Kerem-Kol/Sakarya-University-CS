#ifndef BOCEK_H
#define BOCEK_H

#include "stdio.h"

#include "Canli.h"


struct BOCEK
{
	Canli super;

	void (*sil)(struct BOCEK*);
	

};
typedef struct BOCEK* Bocek;

Bocek new_bocek(int);

char* bocek_gorunum(); 

void delete_bocek(const Bocek);


#endif