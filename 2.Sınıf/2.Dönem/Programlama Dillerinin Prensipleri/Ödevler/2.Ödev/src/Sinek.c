#include "stdio.h"

#include "Sinek.h"


Sinek new_sinek(int value)
{
	Sinek this;
	this = (Sinek)malloc(sizeof(struct SINEK));
	this->super = new_bocek(value);
	this->supersuper = this->super->super;
	this->supersuper->tur = SINEK_TURU;

	this->supersuper->gorunum = &sinek_gorunum;

	this->sil = &delete_sinek;


	return this;
}

char* sinek_gorunum()
{
	return "S";
}

void delete_sinek(const Sinek this)
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
