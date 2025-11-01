#include "Dugum.hpp"

Dugum::Dugum(Sayi* sayi, Dugum* next /*= nullptr*/)
{
    this->sayi = sayi;
    this->next = next;
}
