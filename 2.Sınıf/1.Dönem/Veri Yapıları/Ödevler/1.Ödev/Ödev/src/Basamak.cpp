#include "Basamak.hpp"

Basamak::Basamak(const int& rakam, Basamak* next /*= nullptr*/)
{
    this->rakam = rakam;
    this->next = next;
}
