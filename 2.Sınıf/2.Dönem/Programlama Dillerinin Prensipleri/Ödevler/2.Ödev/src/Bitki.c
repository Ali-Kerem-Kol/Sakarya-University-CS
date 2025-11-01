#include "stdio.h"

#include "Bitki.h"
#include "Habitat.h"

Bitki new_bitki(int value)
{
	Bitki this;
	this = (Bitki)malloc(sizeof(struct BITKI));
	this->super = new_canli(value);
	this->super->tur = BITKI_TURU;
	this->super->gorunum = &bitki_gorunum;

	this->sil = &delete_bitki;

	return this;
}

char* bitki_gorunum()
{
	return "B";
}

void delete_bitki(const Bitki this)
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