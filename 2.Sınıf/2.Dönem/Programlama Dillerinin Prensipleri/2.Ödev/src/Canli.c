#include "stdio.h"

#include "Canli.h"

Canli new_canli(int value)
{
	Canli this;
	this = (Canli)malloc(sizeof(struct CANLI));
	this->value = value;
	
	this->sil = &delete_canli;
	
	return this;
}

void delete_canli(const Canli this)
{
	if(this == NULL) 
	{
		return;
	}
	else
	{
		free(this);
	}
}
