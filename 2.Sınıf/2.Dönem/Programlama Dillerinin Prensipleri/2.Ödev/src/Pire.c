#include "stdio.h"

#include "Pire.h"

Pire new_pire(int value)
{
	Pire this;
	this = (Pire)malloc(sizeof(struct PIRE));
	this->super = new_bocek(value);
	this->supersuper = this->super->super;
	this->supersuper->tur = PIRE_TURU;

	this->supersuper->gorunum = &pire_gorunum;

	this->sil = &delete_pire;


	return this;
}

char* pire_gorunum()
{
	return "P";
}

void delete_pire(const Pire this)
{
	if(this == NULL)
	{
		return;
	}
	else
	{
		this->super->sil(this->super);
		free(this);
	}
}
