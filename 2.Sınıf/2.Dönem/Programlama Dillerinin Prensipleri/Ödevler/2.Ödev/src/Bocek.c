#include "stdio.h"

#include "Bocek.h"

Bocek new_bocek(int value)
{
	Bocek this;
	this = (Bocek)malloc(sizeof(struct BOCEK));
	this->super = new_canli(value);
	this->super->tur = BOCEK_TURU;
	
	this->super->gorunum = &bocek_gorunum;

	this->sil = &delete_bocek;


	return this;
}

char* bocek_gorunum()
{
	return "C";
}

void delete_bocek(const Bocek this)
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
